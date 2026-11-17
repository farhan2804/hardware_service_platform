
package com.hardware.serviceplatform.controller;

import com.hardware.serviceplatform.entity.Payment;
import com.hardware.serviceplatform.entity.User;
import com.hardware.serviceplatform.enums.PaymentStatus;
import com.hardware.serviceplatform.service.PaymentService;
import com.hardware.serviceplatform.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final UserService userService;

    public PaymentController(
            PaymentService paymentService,
            UserService userService) {

        this.paymentService = paymentService;
        this.userService = userService;
    }

    // Customer creates a payment for an approved quotation.
    @PostMapping("/quotations/{quotationId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public PaymentResponse createPayment(
            @PathVariable Long quotationId,
            @RequestBody CreatePaymentRequest request,
            Authentication authentication) {

        User customer = getAuthenticatedUser(authentication);

        Payment payment = paymentService.createPayment(
                quotationId,
                customer.getId(),
                request.paymentMethod());

        return mapResponse(payment);
    }

    // Customer simulates a successful or failed payment.
    @PostMapping("/{paymentId}/simulate")
    @PreAuthorize("hasRole('CUSTOMER')")
    public PaymentResponse simulatePayment(
            @PathVariable Long paymentId,
            @RequestBody SimulatePaymentRequest request,
            Authentication authentication) {

        User customer = getAuthenticatedUser(authentication);

        Payment payment = paymentService.simulatePayment(
                paymentId,
                customer.getId(),
                request.successful());

        return mapResponse(payment);
    }

    // Customer views payment details for a quotation.
@GetMapping("/quotations/{quotationId}")
@PreAuthorize("hasRole('CUSTOMER')")
public ResponseEntity<PaymentResponse> getPayment(
        @PathVariable Long quotationId,
        Authentication authentication) {

    User customer = getAuthenticatedUser(authentication);

    return paymentService
            .getPaymentByQuotationId(quotationId, customer.getId())
            .map(payment -> ResponseEntity.ok(mapResponse(payment)))
            .orElseGet(() -> ResponseEntity.notFound().build());
}

    private User getAuthenticatedUser(Authentication authentication) {
        return userService.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));
    }

    private PaymentResponse mapResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getQuotation().getId(),
                payment.getAmount(),
                payment.getPaymentMethod(),
                payment.getTransactionReference(),
                payment.getStatus(),
                payment.getPaidAt(),
                payment.getCreatedAt()
        );
    }

    public record CreatePaymentRequest(
            String paymentMethod
    ) {}

    public record SimulatePaymentRequest(
            boolean successful
    ) {}

    public record PaymentResponse(
            Long paymentId,
            Long quotationId,
            BigDecimal amount,
            String paymentMethod,
            String transactionReference,
            PaymentStatus status,
            LocalDateTime paidAt,
            LocalDateTime createdAt
    ) {}
}
