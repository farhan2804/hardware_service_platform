
package com.hardware.serviceplatform.service;

import com.hardware.serviceplatform.entity.Booking;
import com.hardware.serviceplatform.entity.Quotation;
import com.hardware.serviceplatform.enums.QuotationStatus;
import com.hardware.serviceplatform.repository.BookingRepository;
import com.hardware.serviceplatform.repository.QuotationRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class QuotationService {

    private final QuotationRepository quotationRepository;
    private final BookingRepository bookingRepository;

    public QuotationService(
            QuotationRepository quotationRepository,
            BookingRepository bookingRepository) {

        this.quotationRepository = quotationRepository;
        this.bookingRepository = bookingRepository;
    }

    @Transactional
    public Quotation createQuotation(Long bookingId, Quotation quotation) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Booking not found"));

        if (quotationRepository.existsByBookingId(bookingId)) {
            throw new IllegalArgumentException(
                    "A quotation already exists for this booking");
        }

        validateAmount(quotation.getLabourCost(), "Labour cost");
        validateAmount(quotation.getPartsCost(), "Parts cost");

        BigDecimal additionalCharges =
                quotation.getAdditionalCharges() == null
                        ? BigDecimal.ZERO
                        : quotation.getAdditionalCharges();

        validateAmount(additionalCharges, "Additional charges");

        BigDecimal total = quotation.getLabourCost()
                .add(quotation.getPartsCost())
                .add(additionalCharges);

        quotation.setBooking(booking);
        quotation.setAdditionalCharges(additionalCharges);
        quotation.setTotalAmount(total);
        quotation.setStatus(QuotationStatus.PENDING);
        quotation.setRespondedAt(null);

        return quotationRepository.save(quotation);
    }

    @Transactional
    public Quotation respondToQuotation(
            Long quotationId,
            QuotationStatus response) {

        Quotation quotation = quotationRepository.findById(quotationId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Quotation not found"));

        if (quotation.getStatus() != QuotationStatus.PENDING) {
            throw new IllegalArgumentException(
                    "This quotation has already been responded to");
        }

        if (response != QuotationStatus.APPROVED
                && response != QuotationStatus.REJECTED) {
            throw new IllegalArgumentException(
                    "Invalid quotation response");
        }

        quotation.setStatus(response);
        quotation.setRespondedAt(LocalDateTime.now());

        return quotationRepository.save(quotation);
    }

    public Optional<Quotation> getQuotationByBookingId(Long bookingId) {
        return quotationRepository.findByBookingId(bookingId);
    }

    private void validateAmount(BigDecimal amount, String fieldName) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    fieldName + " cannot be null or negative");
        }
    }
}
