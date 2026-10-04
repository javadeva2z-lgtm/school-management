package com.school.paymentservice.repository;

import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.school.paymentservice.entity.GatewayPaymentOrder;

@Repository
public interface GatewayPaymentOrderRepository extends JpaRepository<GatewayPaymentOrder, Long> {
    Optional<GatewayPaymentOrder> findByReference(String reference);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select gatewayOrder from GatewayPaymentOrder gatewayOrder where gatewayOrder.reference = :reference")
    Optional<GatewayPaymentOrder> findByReferenceForUpdate(@Param("reference") String reference);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select gatewayOrder from GatewayPaymentOrder gatewayOrder where gatewayOrder.providerReference = :providerReference")
    Optional<GatewayPaymentOrder> findByProviderReferenceForUpdate(
            @Param("providerReference") String providerReference);
}
