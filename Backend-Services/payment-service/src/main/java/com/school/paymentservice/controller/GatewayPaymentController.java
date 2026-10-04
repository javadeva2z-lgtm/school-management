package com.school.paymentservice.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.client.RestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.school.common.response.ApiResponse;
import com.school.common.multitenancy.TenantContext;
import com.school.paymentservice.dto.CreateGatewayPaymentRequest;
import com.school.paymentservice.dto.GatewayPaymentOrderDTO;
import com.school.paymentservice.service.GatewayPaymentService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments/gateway-orders")
public class GatewayPaymentController {
    private final GatewayPaymentService gatewayPaymentService;
    private final RestClient.Builder restClientBuilder;
    private final String userServiceBaseUrl;

    public GatewayPaymentController(
            GatewayPaymentService gatewayPaymentService,
            RestClient.Builder restClientBuilder,
            @Value("${payment.gateways.user-service-base-url:http://localhost:8000/user-service}")
            String userServiceBaseUrl) {
        this.gatewayPaymentService = gatewayPaymentService;
        this.restClientBuilder = restClientBuilder;
        this.userServiceBaseUrl = userServiceBaseUrl;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    public ResponseEntity<ApiResponse<GatewayPaymentOrderDTO>> createOrder(
            @Valid @RequestBody CreateGatewayPaymentRequest request,
            HttpServletRequest servletRequest) {
        Long studentId = resolveStudentId(request.getAdmissionNumber(), servletRequest);
        GatewayPaymentOrderDTO response = gatewayPaymentService.createOrder(request, studentId, servletRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Payment QR created. Complete the payment in your UPI app."));
    }

    @GetMapping("/{reference}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    public ResponseEntity<ApiResponse<GatewayPaymentOrderDTO>> refreshOrder(
            @PathVariable String reference,
            HttpServletRequest servletRequest) {
        assertStudentOwns(gatewayPaymentService.getOrderAdmissionNumber(reference));
        return ResponseEntity.ok(ApiResponse.success(gatewayPaymentService.refreshOrder(reference)));
    }

    @PostMapping("/webhooks/razorpay/{schoolCode}/{reference}")
    public ResponseEntity<ApiResponse<Void>> razorpayWebhook(
            @PathVariable String schoolCode,
            @PathVariable String reference,
            @RequestBody byte[] rawBody,
            @RequestHeader(name = "X-Razorpay-Signature", required = false) String signature) {
        assertWebhookTenant(schoolCode);
        gatewayPaymentService.processRazorpayWebhook(reference, rawBody, signature);
        return ResponseEntity.ok(ApiResponse.success(null, "Webhook processed"));
    }

    @PostMapping("/webhooks/payu/{schoolCode}/{reference}")
    public ResponseEntity<ApiResponse<Void>> payuCallback(
            @PathVariable String schoolCode,
            @PathVariable String reference,
            @RequestParam Map<String, String> callback) {
        assertWebhookTenant(schoolCode);
        gatewayPaymentService.processPayuCallback(reference, callback);
        return ResponseEntity.ok(ApiResponse.success(null, "Callback processed"));
    }

    private void assertWebhookTenant(String schoolCode) {
        if (!schoolCode.equals(TenantContext.getTenant())) {
            throw new IllegalArgumentException("Payment callback tenant does not match the request tenant");
        }
    }

    private Long resolveStudentId(Long admissionNumber, HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || admissionNumber == null || admissionNumber <= 0) {
            throw new IllegalArgumentException("A valid admission number and authenticated user are required");
        }
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !String.valueOf(admissionNumber).equals(authentication.getName())) {
            throw new IllegalArgumentException("A student can only create payments for their own admission number");
        }

        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization == null || authorization.isBlank()) {
            throw new IllegalStateException("Authenticated user token is required");
        }
        JsonNode response = restClientBuilder.build().get()
                .uri(userServiceBaseUrl + "/api/v1/students/admission/{admissionNumber}", admissionNumber)
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .retrieve()
                .body(JsonNode.class);
        JsonNode student = response == null ? null : response.path("data");
        if (student == null || !student.path("id").isNumber()
                || !student.path("admissionNumber").canConvertToLong()
                || student.path("admissionNumber").asLong() != admissionNumber) {
            throw new IllegalArgumentException("Admission number does not identify a student");
        }
        return student.path("id").asLong();
    }

    private void assertStudentOwns(Long admissionNumber) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new IllegalStateException("Authenticated user is required");
        }
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !String.valueOf(admissionNumber).equals(authentication.getName())) {
            throw new IllegalArgumentException("A student can only check payments for their own admission number");
        }
    }
}
