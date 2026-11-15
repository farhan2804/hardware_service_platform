
package com.hardware.serviceplatform.service;

import com.hardware.serviceplatform.entity.Payment;
import com.hardware.serviceplatform.entity.Quotation;
import com.hardware.serviceplatform.enums.PaymentStatus;
import com.hardware.serviceplatform.enums.QuotationStatus;
import com.hardware.serviceplatform.repository.PaymentRepository;
import com.hardware.serviceplatform.repository.QuotationRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final QuotationRepository quotationRepository;

    public PaymentService(
            PaymentRepository paymentRepository,
            QuotationRepository quotationRepository) {

        this.paymentRepository = paymentRepository;
        this.quotationRepository = quotationRepository;
    }

    @Transactional
    public Payment createPayment(
            Long quotationId,
            Long customerId,
            String paymentMethod) {

        Quotation quotation = quotationRepository.findById(quotationId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Quotation not found"));

        if (!quotation.getBooking().getCustomer().getId()
                .equals(customerId)) {
            throw new IllegalArgumentException(
                    "You are not authorized to pay for this quotation");
        }

        if (quotation.getStatus() != QuotationStatus.APPROVED) {
            throw new IllegalArgumentException(
                    "Quotation must be approved before payment");
        }

        if (paymentRepository.existsByQuotationId(quotationId)) {
            throw new IllegalArgumentException(
                    "A payment record already exists for this quotation");
        }

        if (paymentMethod == null || paymentMethod.isBlank()) {
            throw new IllegalArgumentException(
                    "Payment method is required");
        }

        Payment payment = new Payment();

        payment.setQuotation(quotation);
        payment.setAmount(quotation.getTotalAmount());
        payment.setPaymentMethod(paymentMethod);
        payment.setTransactionReference(
                "TXN-" + UUID.randomUUID());
        payment.setStatus(PaymentStatus.PENDING);

        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment simulatePayment(
            Long paymentId,
            Long customerId,
            boolean successful) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Payment not found"));

        Long bookingCustomerId = payment.getQuotation()
                .getBooking()
                .getCustomer()
                .getId();

        if (!bookingCustomerId.equals(customerId)) {
            throw new IllegalArgumentException(
                    "You are not authorized to process this payment");
        }

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new IllegalArgumentException(
                    "Only pending payments can be processed");
        }

        if (successful) {
            payment.setStatus(PaymentStatus.SUCCESSFUL);
            payment.setPaidAt(LocalDateTime.now());
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setPaidAt(null);
        }

        return paymentRepository.save(payment);
    }

    public Optional<Payment> getPaymentByQuotationId(
            Long quotationId,
            Long customerId) {

        Quotation quotation = quotationRepository.findById(quotationId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Quotation not found"));

        if (!quotation.getBooking().getCustomer().getId()
                .equals(customerId)) {
            throw new IllegalArgumentException(
                    "You are not authorized to view this payment");
        }

        return paymentRepository.findByQuotationId(quotationId);
    }
}
