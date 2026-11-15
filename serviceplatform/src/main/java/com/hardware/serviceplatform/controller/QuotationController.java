
package com.hardware.serviceplatform.controller;

import com.hardware.serviceplatform.entity.Booking;
import com.hardware.serviceplatform.entity.EngineerProfile;
import com.hardware.serviceplatform.entity.Quotation;
import com.hardware.serviceplatform.entity.User;
import com.hardware.serviceplatform.enums.QuotationStatus;
import com.hardware.serviceplatform.service.BookingService;
import com.hardware.serviceplatform.service.EngineerService;
import com.hardware.serviceplatform.service.QuotationService;
import com.hardware.serviceplatform.service.UserService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    private final BookingService bookingService;

    public QuotationController(
            QuotationService quotationService,
            EngineerService engineerService,
            UserService userService,
            BookingService bookingService) {

        this.quotationService = quotationService;
        this.engineerService = engineerService;
        this.userService = userService;
        this.bookingService = bookingService;
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
    public ResponseEntity<QuotationResponse> getQuotation(
            @PathVariable Long bookingId,
            Authentication authentication) {

        System.out.println("\n========== QUOTATION DEBUG START ==========");
        System.out.println("Requested Booking ID: " + bookingId);
        System.out.println("Authenticated User: " +
                (authentication != null ? authentication.getName() : "NULL"));
        System.out.println("Authenticated Authorities: " +
                (authentication != null ? authentication.getAuthorities() : "NULL"));

        User user = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"));

        // Retrieve the booking.
        Booking booking = bookingService.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Booking not found"));

        // Diagnostic information.
        System.out.println("Logged-in User ID: " + user.getId());

        System.out.println("Booking Customer ID: " +
                (booking.getCustomer() != null
                        ? booking.getCustomer().getId()
                        : "NULL"));

        System.out.println("Assigned Engineer User ID: " +
                (booking.getAssignedEngineer() != null
                        && booking.getAssignedEngineer().getUser() != null
                        ? booking.getAssignedEngineer().getUser().getId()
                        : "NONE"));

        // Verify customer ownership.
        boolean isCustomer = booking.getCustomer() != null
                && booking.getCustomer().getId().equals(user.getId());

        // Verify assigned engineer ownership.
        boolean isAssignedEngineer =
                booking.getAssignedEngineer() != null
                && booking.getAssignedEngineer().getUser() != null
                && booking.getAssignedEngineer()
                        .getUser().getId().equals(user.getId());

        System.out.println("Is Customer: " + isCustomer);
        System.out.println("Is Assigned Engineer: " + isAssignedEngineer);
        System.out.println("========== QUOTATION DEBUG END ==========\n");

        // Only the booking customer or assigned engineer can view it.
        if (!isCustomer && !isAssignedEngineer) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not authorized to view this quotation");
        }

        // Retrieve quotation after verifying access.
        Quotation quotation = quotationService
                .getQuotationByBookingId(bookingId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Quotation has not been created yet"));

        return ResponseEntity.ok(mapResponse(quotation));
    }

    // Customer approves or rejects the quotation.
    @PutMapping("/{quotationId}/respond")
    @PreAuthorize("hasRole('CUSTOMER')")
    public QuotationResponse respondToQuotation(
            @PathVariable Long quotationId,
            @RequestBody RespondQuotationRequest request,
            Authentication authentication) {

        User customer = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"));

        Quotation updated = quotationService.respondToQuotation(
                quotationId,
                customer.getId(),
                customer,
                request.status());

        return mapResponse(updated);
    }

    // Maps the quotation entity to its response DTO.
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
                quotation.getRespondedAt());
    }

    public record CreateQuotationRequest(
            BigDecimal labourCost,
            BigDecimal partsCost,
            BigDecimal additionalCharges,
            String remarks) {
    }

    public record RespondQuotationRequest(
            QuotationStatus status) {
    }

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
            LocalDateTime respondedAt) {
    }
}
