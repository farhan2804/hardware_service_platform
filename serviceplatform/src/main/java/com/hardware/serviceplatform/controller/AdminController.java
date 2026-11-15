
package com.hardware.serviceplatform.controller;

import com.hardware.serviceplatform.entity.Booking;
import com.hardware.serviceplatform.entity.EngineerProfile;
import com.hardware.serviceplatform.entity.User;
import com.hardware.serviceplatform.service.BookingService;
import com.hardware.serviceplatform.service.EngineerAllocationService;
import com.hardware.serviceplatform.service.EngineerService;
import com.hardware.serviceplatform.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final EngineerAllocationService allocationService;
    private final EngineerService engineerService;
    private final UserService userService;
    private final BookingService bookingService;

    public AdminController(
            EngineerAllocationService allocationService,
            EngineerService engineerService,
            UserService userService,
            BookingService bookingService) {

        this.allocationService = allocationService;
        this.engineerService = engineerService;
        this.userService = userService;
        this.bookingService = bookingService;
    }

    // Manually assign or reassign an engineer to a booking
    @PutMapping("/bookings/{bookingId}/assign/{engineerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ManualAssignmentResponse> manuallyAssignBooking(
            @PathVariable Long bookingId,
            @PathVariable Long engineerId,
            @RequestParam(required = false) String reason,
            Authentication authentication) {

        User admin = userService.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new IllegalArgumentException("Admin user not found"));

        EngineerProfile engineer = engineerService.findById(engineerId);

        String assignmentReason =
                (reason == null || reason.isBlank())
                        ? "Manually assigned by admin"
                        : reason;

        Booking booking = allocationService.manuallyAssignBooking(
                bookingId,
                engineer,
                admin,
                assignmentReason
        );

        return ResponseEntity.ok(
                new ManualAssignmentResponse(
                        booking.getId(),
                        booking.getAssignedEngineer().getId(),
                        booking.getStatus().name(),
                        "Engineer successfully assigned"
                )
        );
    }

    // Get all bookings for the admin dashboard
    @GetMapping("/bookings")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AdminBookingResponse> getAllBookings() {

        return bookingService.getAllBookings()
                .stream()
                .map(booking -> new AdminBookingResponse(
                        booking.getId(),
                        booking.getCustomer().getFullName(),
                        booking.getCustomer().getEmail(),
                        booking.getCustomer().getPhone(),
                        booking.getServiceCategory().getName(),
                        booking.getServiceType().name(),
                        booking.getIssueDescription(),
                        booking.getPreferredDateTime(),
                        booking.getPreferredEndDateTime(),
                        booking.getServiceAddress(),
                        booking.getAssignedEngineer() != null
                                ? booking.getAssignedEngineer()
                                        .getUser()
                                        .getFullName()
                                : null,
                        booking.getStatus().name(),
                        booking.getCreatedAt()
                ))
                .toList();
    }

    // Admin booking response DTO
    public record AdminBookingResponse(
            Long bookingId,
            String customerName,
            String customerEmail,
            String customerPhone,
            String serviceCategory,
            String serviceType,
            String issueDescription,
            LocalDateTime appointmentStart,
            LocalDateTime appointmentEnd,
            String serviceAddress,
            String assignedEngineerName,
            String status,
            LocalDateTime createdAt
    ) {}

    // Manual assignment response DTO
    public record ManualAssignmentResponse(
            Long bookingId,
            Long assignedEngineerId,
            String status,
            String message
    ) {}
}
