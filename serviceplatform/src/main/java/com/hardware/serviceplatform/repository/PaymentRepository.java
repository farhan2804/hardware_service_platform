
package com.hardware.serviceplatform.repository;

import com.hardware.serviceplatform.entity.Payment;
import com.hardware.serviceplatform.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByQuotationId(Long quotationId);

    List<Payment> findByStatus(PaymentStatus status);

    boolean existsByQuotationId(Long quotationId);
}
