
package com.hardware.serviceplatform.controller;

import com.hardware.serviceplatform.entity.EngineerProfile;
import com.hardware.serviceplatform.entity.Quotation;
import com.hardware.serviceplatform.entity.User;
import com.hardware.serviceplatform.enums.QuotationStatus;
import com.hardware.serviceplatform.service.EngineerService;
import com.hardware.serviceplatform.service.QuotationService;
import com.hardware.serviceplatform.service.UserService;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/quotations")
public class QuotationController {

    private final QuotationService quotationService;
    private final EngineerService engineerService;
    private final UserService userService;

    public QuotationController(
            QuotationService quotationService,
            EngineerService engineerService,
            UserService userService) {

        this.quotationService = quotationService;
        this.engineerService = engineerService;
        this.userService = userService;
    }

    // Engineer creates a quotation for an assigned booking.
    @PostMapping("/bookings/{bookingId}")
    @PreAuthorize("hasRole('ENGINEER')")
    public QuotationResponse createQuotation(
            @PathVariable Long bookingId,
            @RequestBody CreateQuotationRequest request,
            Authentication authentication) {

        EngineerProfile engineer =
                engineerService.findByEmail(authentication.getName());

        Quotation quotation = new Quotation();
        quotation.setLabourCost(request.labourCost());
        quotation.setPartsCost(request.partsCost());
        quotation.setAdditionalCharges(request.additionalCharges());
        quotation.setRemarks(request.remarks());

        Quotation saved = quotationService.createQuotation(
                bookingId,
                engineer.getId(),
                engineer.getUser(),
                quotation);

        return mapResponse(saved);
    }

    // Customer or assigned engineer can view the quotation.
    @GetMapping("/bookings/{bookingId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ENGINEER')")
    public QuotationResponse getQuotation(
            @PathVariable Long bookingId,
            Authentication authentication) {

        User user = userService.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        Quotation quotation = quotationService
                .getQuotationByBookingId(bookingId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Quotation not found"));

        boolean isCustomer =
                quotation.getBooking().getCustomer().getId()
                        .equals(user.getId());

        boolean isAssignedEngineer =
                quotation.getBooking().getAssignedEngineer() != null
                && quotation.getBooking().getAssignedEngineer()
                        .getUser().getId().equals(user.getId());

        if (!isCustomer && !isAssignedEngineer) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not authorized to view this quotation");
        }

        return mapResponse(quotation);
    }

    // Customer approves or rejects the quotation.
    @PutMapping("/{quotationId}/respond")
    @PreAuthorize("hasRole('CUSTOMER')")
    public QuotationResponse respondToQuotation(
            @PathVariable Long quotationId,
            @RequestBody RespondQuotationRequest request,
            Authentication authentication) {

        User customer = userService.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        Quotation updated = quotationService.respondToQuotation(
                quotationId,
                customer.getId(),
                customer,
                request.status());

        return mapResponse(updated);
    }

    private QuotationResponse mapResponse(Quotation quotation) {

        return new QuotationResponse(
                quotation.getId(),
                quotation.getBooking().getId(),
                quotation.getLabourCost(),
                quotation.getPartsCost(),
                quotation.getAdditionalCharges(),
                quotation.getTotalAmount(),
                quotation.getStatus(),
                quotation.getRemarks(),
                quotation.getCreatedAt(),
                quotation.getRespondedAt()
        );
    }

    public record CreateQuotationRequest(
            BigDecimal labourCost,
            BigDecimal partsCost,
            BigDecimal additionalCharges,
            String remarks
    ) {}

    public record RespondQuotationRequest(
            QuotationStatus status
    ) {}

    public record QuotationResponse(
            Long quotationId,
            Long bookingId,
            BigDecimal labourCost,
            BigDecimal partsCost,
            BigDecimal additionalCharges,
            BigDecimal totalAmount,
            QuotationStatus status,
            String remarks,
            LocalDateTime createdAt,
            LocalDateTime respondedAt
    ) {}
}
