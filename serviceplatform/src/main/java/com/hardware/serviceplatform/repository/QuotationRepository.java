
package com.hardware.serviceplatform.repository;

import com.hardware.serviceplatform.entity.Quotation;
import com.hardware.serviceplatform.enums.QuotationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuotationRepository
        extends JpaRepository<Quotation, Long> {

    Optional<Quotation> findByBookingId(Long bookingId);

    List<Quotation> findByStatus(QuotationStatus status);

    boolean existsByBookingId(Long bookingId);
}
