package com.school.paymentservice.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.HexFormat;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.school.common.enums.PaymentStatus;
import com.school.common.exception.ResourceNotFoundException;
import com.school.common.multitenancy.TenantContext;
import com.school.paymentservice.dto.CreateGatewayPaymentRequest;
import com.school.paymentservice.dto.GatewayPaymentOrderDTO;
import com.school.paymentservice.dto.PaymentDTO;
import com.school.paymentservice.entity.GatewayPaymentOrder;
import com.school.paymentservice.entity.MonthlyFee;
import com.school.paymentservice.repository.GatewayPaymentOrderRepository;
import com.school.paymentservice.repository.MonthlyFeeRepository;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class GatewayPaymentService {
    private static final String RAZORPAY = "RAZORPAY";
    private static final String PAYU = "PAYU";
    private static final String PENDING = "PENDING";
    private static final String PAID = "PAID";

    private final GatewayPaymentOrderRepository orderRepository;
    private final MonthlyFeeRepository monthlyFeeRepository;
    private final PaymentService paymentService;
    private final RestClient.Builder restClientBuilder;

    @Value("${payment.gateways.razorpay.key-id:}")
    private String razorpayKeyId;
    @Value("${payment.gateways.razorpay.key-secret:}")
    private String razorpayKeySecret;
    @Value("${payment.gateways.payu.key:}")
    private String payuKey;
    @Value("${payment.gateways.payu.salt:}")
    private String payuSalt;
    @Value("${payment.gateways.razorpay.webhook-secret:}")
    private String razorpayWebhookSecret;
    @Value("${payment.gateways.public-base-url:}")
    private String publicBaseUrl;
    @Value("${payment.gateways.mode:test}")
    private String mode;

    @Transactional
    public GatewayPaymentOrderDTO createOrder(CreateGatewayPaymentRequest request, HttpServletRequest servletRequest) {
        String provider = request.getProvider().trim().toUpperCase(Locale.ROOT);
        if (!RAZORPAY.equals(provider) && !PAYU.equals(provider)) {
            throw new IllegalArgumentException("Payment provider must be RAZORPAY or PAYU");
        }
        validateProviderConfiguration(provider);

        List<Long> feeIds = request.getMonthlyFeeIds().stream().distinct().toList();
        if (feeIds.size() != request.getMonthlyFeeIds().size()) {
            throw new IllegalArgumentException("Monthly fee ids must not contain duplicates");
        }

        List<MonthlyFee> fees = monthlyFeeRepository.findAllById(feeIds);
        if (fees.size() != feeIds.size()) {
            throw new IllegalArgumentException("One or more monthly fee records do not exist");
        }
        Map<Long, MonthlyFee> feeById = new HashMap<>();
        fees.forEach(fee -> feeById.put(fee.getId(), fee));
        fees = feeIds.stream().map(feeById::get)
                .sorted((left, right) -> left.getMonthYear().compareTo(right.getMonthYear()))
                .toList();

        BigDecimal total = BigDecimal.ZERO;
        for (MonthlyFee fee : fees) {
            if (!fee.getStudentId().equals(request.getStudentId())) {
                throw new IllegalArgumentException("All monthly fees must belong to the requested student");
            }
            if (fee.getStatus() == PaymentStatus.PAID || fee.getStatus() == PaymentStatus.EXEMPT
                    || fee.getTotalPayable() == null) {
                throw new IllegalArgumentException("Paid, exempt, or unconfigured fees cannot be included");
            }
            BigDecimal outstanding = BigDecimal.valueOf(fee.getTotalPayable())
                    .subtract(BigDecimal.valueOf(getRecordedPaidAmount(fee.getId())));
            if (outstanding.signum() <= 0) {
                throw new IllegalArgumentException("Selected monthly fee has no outstanding balance");
            }
            total = total.add(outstanding);
        }

        long amountPaise;
        try {
            amountPaise = total.movePointRight(2).setScale(0, RoundingMode.UNNECESSARY).longValueExact();
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException("Fee total must be an exact INR paise amount", exception);
        }
        String reference = UUID.randomUUID().toString();
        String providerReference;
        String qrImageUrl = null;
        String qrPayload = null;
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(30);

        if (RAZORPAY.equals(provider)) {
            JsonNode response = createRazorpayQr(request, reference, amountPaise);
            providerReference = requiredText(response, "id");
            qrImageUrl = requiredText(response, "image_url");
            expiresAt = LocalDateTime.now().plusSeconds(response.path("close_by").asLong(1800));
        } else {
            providerReference = payuTransactionId(reference);
            qrPayload = createPayuQr(request, reference, total, servletRequest);
        }

        GatewayPaymentOrder order = GatewayPaymentOrder.builder()
                .reference(reference)
                .provider(provider)
                .providerReference(providerReference)
                .studentId(request.getStudentId())
                .monthlyFeeIds(String.join(",", feeIds.stream().map(String::valueOf).toList()))
                .amountPaise(amountPaise)
                .status(PENDING)
                .qrImageUrl(qrImageUrl)
                .qrPayload(qrPayload)
                .createdAt(LocalDateTime.now())
                .expiresAt(expiresAt)
                .build();
        return toDto(orderRepository.save(order));
    }

    @Transactional
    public GatewayPaymentOrderDTO refreshOrder(String reference) {
        GatewayPaymentOrder order = orderRepository.findByReferenceForUpdate(reference)
                .orElseThrow(() -> new ResourceNotFoundException("GatewayPaymentOrder", "reference", reference));
        if (PAID.equals(order.getStatus()) || LocalDateTime.now().isAfter(order.getExpiresAt())) {
            return toDto(order);
        }
        VerificationResult result = RAZORPAY.equals(order.getProvider())
                ? verifyRazorpay(order)
                : verifyPayu(order);
        if (result.paid()) {
            completeOrder(order, result.providerPaymentId());
        }
        return toDto(order);
    }

    @Transactional
    public void processRazorpayWebhook(String reference, byte[] rawBody, String signature) {
        verifyRazorpayWebhookSignature(rawBody, signature);
        JsonNode body;
        try {
            body = new com.fasterxml.jackson.databind.ObjectMapper().readTree(rawBody);
        } catch (java.io.IOException exception) {
            throw new IllegalArgumentException("Invalid Razorpay webhook JSON", exception);
        }
        if (!"qr_code.credited".equals(body.path("event").asText())) {
            return;
        }

        JsonNode qr = body.path("payload").path("qr_code").path("entity");
        JsonNode payment = body.path("payload").path("payment").path("entity");
        String qrId = requiredText(qr, "id");
        GatewayPaymentOrder order = orderRepository.findByProviderReferenceForUpdate(qrId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "GatewayPaymentOrder", "providerReference", qrId));
        if (!order.getReference().equals(reference)) {
            throw new IllegalArgumentException("Razorpay webhook reference does not match the fee order");
        }
        if (!payment.path("captured").asBoolean()
                || !"captured".equalsIgnoreCase(payment.path("status").asText())
                || !"INR".equalsIgnoreCase(payment.path("currency").asText())
                || payment.path("amount").asLong(-1) != order.getAmountPaise()) {
            throw new IllegalArgumentException("Razorpay webhook payment does not match the fee order");
        }
        completeOrder(order, requiredText(payment, "id"));
    }

    @Transactional
    public void processPayuCallback(String reference, Map<String, String> callback) {
        verifyPayuCallback(reference, callback);
        if (!"success".equalsIgnoreCase(callback.get("status"))) {
            return;
        }
        GatewayPaymentOrder order = orderRepository.findByReferenceForUpdate(reference)
                .orElseThrow(() -> new ResourceNotFoundException("GatewayPaymentOrder", "reference", reference));
        if (!PAYU.equals(order.getProvider())
                || !order.getProviderReference().equals(callback.get("txnid"))) {
            throw new IllegalArgumentException("PayU callback does not match the fee order");
        }
        VerificationResult result = verifyPayu(order);
        if (result.paid()) {
            completeOrder(order, result.providerPaymentId());
        }
    }

    @Transactional(readOnly = true)
    public Long getOrderStudentId(String reference) {
        return orderRepository.findByReference(reference)
                .orElseThrow(() -> new ResourceNotFoundException("GatewayPaymentOrder", "reference", reference))
                .getStudentId();
    }

    private JsonNode createRazorpayQr(CreateGatewayPaymentRequest request, String reference, long amountPaise) {
        Map<String, Object> body = new HashMap<>();
        body.put("type", "upi_qr");
        body.put("usage", "single_use");
        body.put("fixed_amount", true);
        body.put("payment_amount", amountPaise);
        body.put("name", request.getCustomerName());
        body.put("description", "School fee payment " + reference);
        body.put("close_by", java.time.Instant.now().plusSeconds(1800).getEpochSecond());
        body.put("notes", Map.of("school_fee_reference", reference, "student_id", request.getStudentId()));

        JsonNode response = restClientBuilder.build().post()
                .uri("https://api.razorpay.com/v1/payments/qr_codes")
                .headers(headers -> headers.setBasicAuth(razorpayKeyId, razorpayKeySecret))
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(JsonNode.class);
        if (response == null) {
            throw new IllegalStateException("Razorpay returned an empty QR response");
        }
        return response;
    }

    private String createPayuQr(CreateGatewayPaymentRequest request, String reference, BigDecimal amount,
            HttpServletRequest servletRequest) {
        String amountValue = amount.setScale(2, RoundingMode.UNNECESSARY).toPlainString();
        String transactionId = payuTransactionId(reference);
        String productInfo = "School fee " + reference;
        String firstName = request.getCustomerName().trim().split("\\s+", 2)[0];
        String email = request.getCustomerEmail().trim();
        String hashInput = String.join("|", payuKey, transactionId, amountValue, productInfo,
                firstName, email, "", "", "", "", "", "", "", "", "", "", payuSalt);
        String callbackUrl = paymentCallbackUrl("payu", reference);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("key", payuKey);
        form.add("txnid", transactionId);
        form.add("amount", amountValue);
        form.add("productinfo", productInfo);
        form.add("firstname", firstName);
        form.add("email", email);
        form.add("phone", request.getCustomerPhone().replaceAll("\\D", ""));
        form.add("surl", callbackUrl);
        form.add("furl", callbackUrl);
        form.add("pg", "DBQR");
        form.add("bankcode", "UPIDBQR");
        form.add("txn_s2s_flow", "4");
        form.add("s2s_client_ip", clientIp(servletRequest));
        form.add("s2s_device_info", servletRequest.getHeader(HttpHeaders.USER_AGENT) == null
                ? "School fee portal" : servletRequest.getHeader(HttpHeaders.USER_AGENT));
        form.add("expiry_time", "1800");
        form.add("hash", sha512(hashInput));

        JsonNode response = restClientBuilder.build().post()
                .uri(payuPaymentUrl())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(JsonNode.class);
        if (response == null) {
            throw new IllegalStateException("PayU returned an empty QR response");
        }
        String qrString = response.path("result").path("qrString").asText();
        if (qrString.isBlank()) {
            throw new IllegalStateException("PayU did not return a dynamic UPI QR payload");
        }
        return qrString;
    }

    private VerificationResult verifyRazorpay(GatewayPaymentOrder order) {
        JsonNode response = restClientBuilder.build().get()
                .uri("https://api.razorpay.com/v1/payments/qr_codes/{id}/payments", order.getProviderReference())
                .headers(headers -> headers.setBasicAuth(razorpayKeyId, razorpayKeySecret))
                .retrieve()
                .body(JsonNode.class);
        if (response == null) {
            throw new IllegalStateException("Razorpay returned an empty payment status");
        }
        for (JsonNode payment : response.path("items")) {
            if (payment.path("captured").asBoolean()
                    && "captured".equalsIgnoreCase(payment.path("status").asText())
                    && "INR".equalsIgnoreCase(payment.path("currency").asText())
                    && payment.path("amount").asLong(-1) == order.getAmountPaise()) {
                return new VerificationResult(true, requiredText(payment, "id"));
            }
        }
        return VerificationResult.pending();
    }

    private VerificationResult verifyPayu(GatewayPaymentOrder order) {
        String transactionId = order.getProviderReference();
        String command = "verify_payment";
        String hash = sha512(String.join("|", payuKey, command, transactionId, payuSalt));
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("key", payuKey);
        form.add("command", command);
        form.add("var1", transactionId);
        form.add("hash", hash);

        JsonNode response = restClientBuilder.build().post()
                .uri(payuVerifyUrl())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(JsonNode.class);
        if (response == null) {
            throw new IllegalStateException("PayU returned an empty payment status");
        }
        JsonNode detail = response.path("transaction_details").path(transactionId);
        boolean success = "success".equalsIgnoreCase(detail.path("status").asText())
                && "captured".equalsIgnoreCase(detail.path("unmappedstatus").asText());
        BigDecimal verifiedAmount;
        try {
            verifiedAmount = new BigDecimal(detail.path("amt").asText());
        } catch (NumberFormatException exception) {
            return VerificationResult.pending();
        }
        try {
            if (verifiedAmount.setScale(2, RoundingMode.UNNECESSARY)
                    .movePointRight(2).longValueExact() != order.getAmountPaise()) {
                return VerificationResult.pending();
            }
        } catch (ArithmeticException exception) {
            return VerificationResult.pending();
        }
        if (!success) {
            return VerificationResult.pending();
        }
        return new VerificationResult(true, detail.path("mihpayid").asText(transactionId));
    }

    private void completeOrder(GatewayPaymentOrder order, String providerPaymentId) {
        if (PAID.equals(order.getStatus())) {
            return;
        }
        recordVerifiedPayments(order, providerPaymentId);
        order.setStatus(PAID);
        order.setProviderPaymentId(providerPaymentId);
        orderRepository.save(order);
    }

    private void verifyRazorpayWebhookSignature(byte[] rawBody, String signature) {
        if (razorpayWebhookSecret.isBlank() || signature == null || signature.isBlank()) {
            throw new IllegalArgumentException("Razorpay webhook signature is missing or not configured");
        }
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(razorpayWebhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] expected = mac.doFinal(rawBody);
            byte[] received = HexFormat.of().parseHex(signature);
            if (!MessageDigest.isEqual(expected, received)) {
                throw new IllegalArgumentException("Invalid Razorpay webhook signature");
            }
        } catch (java.security.GeneralSecurityException | IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid Razorpay webhook signature", exception);
        }
    }

    private void verifyPayuCallback(String reference, Map<String, String> callback) {
        String key = callback.getOrDefault("key", "");
        String transactionId = callback.getOrDefault("txnid", "");
        String status = callback.getOrDefault("status", "");
        if (!payuKey.equals(key) || !transactionId.equals(payuTransactionId(reference))) {
            throw new IllegalArgumentException("PayU callback does not match the payment transaction");
        }

        String reverseHash = String.join("|", payuSalt, status, "", "", "", "", "",
                callback.getOrDefault("udf5", ""), callback.getOrDefault("udf4", ""),
                callback.getOrDefault("udf3", ""), callback.getOrDefault("udf2", ""),
                callback.getOrDefault("udf1", ""), callback.getOrDefault("email", ""),
                callback.getOrDefault("firstname", ""), callback.getOrDefault("productinfo", ""),
                callback.getOrDefault("amount", ""), transactionId, key);
        byte[] expectedHash = sha512(reverseHash).getBytes(StandardCharsets.US_ASCII);
        byte[] callbackHash = callback.getOrDefault("hash", "").toLowerCase(Locale.ROOT)
                .getBytes(StandardCharsets.US_ASCII);
        if (!MessageDigest.isEqual(expectedHash, callbackHash)) {
            throw new IllegalArgumentException("Invalid PayU callback hash");
        }
    }

    private void recordVerifiedPayments(GatewayPaymentOrder order, String providerPaymentId) {
        List<Long> feeIds = java.util.Arrays.stream(order.getMonthlyFeeIds().split(","))
                .map(Long::valueOf)
                .toList();
        List<MonthlyFee> fees = monthlyFeeRepository.findAllById(feeIds);
        if (fees.size() != feeIds.size()) {
            throw new IllegalStateException("A monthly fee for this gateway payment no longer exists");
        }

        BigDecimal remaining = BigDecimal.valueOf(order.getAmountPaise(), 2);
        List<MonthlyFee> sortedFees = new ArrayList<>(fees);
        sortedFees.sort((left, right) -> left.getMonthYear().compareTo(right.getMonthYear()));
        for (MonthlyFee fee : sortedFees) {
            BigDecimal outstanding = BigDecimal.valueOf(fee.getTotalPayable())
                    .subtract(BigDecimal.valueOf(getRecordedPaidAmount(fee.getId())));
            if (outstanding.signum() <= 0) {
                continue;
            }
            BigDecimal allocated = outstanding.min(remaining);
            if (allocated.signum() <= 0) {
                break;
            }
            PaymentDTO payment = PaymentDTO.builder()
                    .studentId(order.getStudentId())
                    .monthlyFeeId(fee.getId())
                    .monthYear(fee.getMonthYear())
                    .transactionId(providerPaymentId + "-" + fee.getId())
                    .paymentMethod(order.getProvider() + "_UPI")
                    .amountPaid(allocated.doubleValue())
                    .status(PaymentStatus.PAID)
                    .build();
            paymentService.createPayment(payment);
            remaining = remaining.subtract(allocated);
        }
        if (remaining.signum() != 0) {
            throw new IllegalStateException("Verified gateway amount no longer matches the outstanding fee balance");
        }
    }

    private double getRecordedPaidAmount(Long monthlyFeeId) {
        double paidAmount = 0;
        for (PaymentDTO payment : paymentService.getPaymentsByMonthlyFee(monthlyFeeId)) {
            if (payment.getStatus() == PaymentStatus.PAID && payment.getAmountPaid() != null) {
                paidAmount += payment.getAmountPaid();
            }
        }
        return paidAmount;
    }

    private void validateProviderConfiguration(String provider) {
        if (RAZORPAY.equals(provider) && (razorpayKeyId.isBlank() || razorpayKeySecret.isBlank())) {
            throw new IllegalStateException("Configure RAZORPAY_KEY_ID and RAZORPAY_KEY_SECRET before enabling Razorpay");
        }
        if (RAZORPAY.equals(provider) && (razorpayWebhookSecret.isBlank() || publicBaseUrl.isBlank())) {
            throw new IllegalStateException(
                    "Configure RAZORPAY_WEBHOOK_SECRET and PAYMENT_PUBLIC_BASE_URL before enabling Razorpay");
        }
        if (PAYU.equals(provider) && (payuKey.isBlank() || payuSalt.isBlank())) {
            throw new IllegalStateException("Configure PAYU_KEY and PAYU_SALT before enabling PayU");
        }
        if (PAYU.equals(provider) && publicBaseUrl.isBlank()) {
            throw new IllegalStateException("Configure PAYMENT_PUBLIC_BASE_URL before enabling PayU");
        }
    }

    private String payuPaymentUrl() {
        return "prod".equalsIgnoreCase(mode) ? "https://secure.payu.in/_payment" : "https://test.payu.in/_payment";
    }

    private String payuTransactionId(String reference) {
        return "SCH" + reference.replace("-", "").substring(0, 22);
    }

    private String paymentCallbackUrl(String provider, String reference) {
        String tenant = TenantContext.getTenant();
        if (publicBaseUrl.isBlank() || tenant == null || !tenant.matches("[A-Za-z0-9_]+")) {
            throw new IllegalStateException("Configure PAYMENT_PUBLIC_BASE_URL and a valid tenant before creating a QR");
        }
        return publicBaseUrl.replaceAll("/+$", "")
                + "/payment-service/api/v1/payments/gateway-orders/webhooks/"
                + provider + "/" + tenant + "/" + reference;
    }

    private String payuVerifyUrl() {
        return "prod".equalsIgnoreCase(mode)
                ? "https://info.payu.in/merchant/postservice.php?form=2"
                : "https://test.payu.in/merchant/postservice.php?form=2";
    }

    private String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        return forwardedFor == null || forwardedFor.isBlank()
                ? request.getRemoteAddr()
                : forwardedFor.split(",")[0].trim();
    }

    private String sha512(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-512").digest(value.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-512 is unavailable", exception);
        }
    }

    private String requiredText(JsonNode node, String field) {
        String value = node.path(field).asText();
        if (value.isBlank()) {
            throw new IllegalStateException("Payment provider response is missing " + field);
        }
        return value;
    }

    private GatewayPaymentOrderDTO toDto(GatewayPaymentOrder order) {
        return GatewayPaymentOrderDTO.builder()
                .reference(order.getReference())
                .provider(order.getProvider())
                .status(order.getStatus())
                .amountPaise(order.getAmountPaise())
                .currency("INR")
                .qrImageUrl(order.getQrImageUrl())
                .qrPayload(order.getQrPayload())
                .providerReference(order.getProviderReference())
                .expiresAt(order.getExpiresAt())
                .build();
    }

    private record VerificationResult(boolean paid, String providerPaymentId) {
        static VerificationResult pending() {
            return new VerificationResult(false, null);
        }
    }
}
