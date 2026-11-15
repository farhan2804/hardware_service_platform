
package com.hardware.serviceplatform.service;

import com.hardware.serviceplatform.entity.Booking;
import com.hardware.serviceplatform.entity.BookingStatusHistory;
import com.hardware.serviceplatform.entity.EngineerProfile;
import com.hardware.serviceplatform.entity.Quotation;
import com.hardware.serviceplatform.entity.User;
import com.hardware.serviceplatform.enums.BookingStatus;
import com.hardware.serviceplatform.enums.QuotationStatus;
import com.hardware.serviceplatform.repository.BookingRepository;
import com.hardware.serviceplatform.repository.BookingStatusHistoryRepository;
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
    private final BookingStatusHistoryRepository statusHistoryRepository;

    public QuotationService(
            QuotationRepository quotationRepository,
            BookingRepository bookingRepository,
            BookingStatusHistoryRepository statusHistoryRepository) {

        this.quotationRepository = quotationRepository;
        this.bookingRepository = bookingRepository;
        this.statusHistoryRepository = statusHistoryRepository;
    }

    @Transactional
    public Quotation createQuotation(
            Long bookingId,
            Long engineerProfileId,
            User changedBy,
            Quotation quotation) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Booking not found"));

        if (booking.getAssignedEngineer() == null
                || !booking.getAssignedEngineer().getId()
                        .equals(engineerProfileId)) {
            throw new IllegalArgumentException(
                    "You are not assigned to this booking");
        }

        if (booking.getStatus() != BookingStatus.ARRIVED) {
            throw new IllegalArgumentException(
                    "Quotation can only be created after engineer arrival");
        }

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

        Quotation savedQuotation = quotationRepository.save(quotation);

        BookingStatus previousStatus = booking.getStatus();
        booking.setStatus(BookingStatus.QUOTATION_SHARED);
        bookingRepository.save(booking);

        saveStatusHistory(
                booking,
                previousStatus,
                BookingStatus.QUOTATION_SHARED,
                changedBy,
                "Quotation shared with customer");

        return savedQuotation;
    }

    @Transactional
    public Quotation respondToQuotation(
            Long quotationId,
            Long customerId,
            User changedBy,
            QuotationStatus response) {

        Quotation quotation = quotationRepository.findById(quotationId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Quotation not found"));

        Booking booking = quotation.getBooking();

        if (!booking.getCustomer().getId().equals(customerId)) {
            throw new IllegalArgumentException(
                    "You are not authorized to respond to this quotation");
        }

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

        Quotation savedQuotation = quotationRepository.save(quotation);

        if (response == QuotationStatus.APPROVED) {

            BookingStatus previousStatus = booking.getStatus();

            booking.setStatus(BookingStatus.WORK_IN_PROGRESS);
            bookingRepository.save(booking);

            saveStatusHistory(
                    booking,
                    previousStatus,
                    BookingStatus.WORK_IN_PROGRESS,
                    changedBy,
                    "Customer approved the quotation");
        }

        return savedQuotation;
    }

    public Optional<Quotation> getQuotationByBookingId(Long bookingId) {
        return quotationRepository.findByBookingId(bookingId);
    }

    private void saveStatusHistory(
            Booking booking,
            BookingStatus previousStatus,
            BookingStatus newStatus,
            User changedBy,
            String note) {

        BookingStatusHistory history = new BookingStatusHistory();

        history.setBooking(booking);
        history.setPreviousStatus(previousStatus);
        history.setNewStatus(newStatus);
        history.setChangedBy(changedBy);
        history.setNote(note);

        statusHistoryRepository.save(history);
    }

    private void validateAmount(BigDecimal amount, String fieldName) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    fieldName + " cannot be null or negative");
        }
    }
}
