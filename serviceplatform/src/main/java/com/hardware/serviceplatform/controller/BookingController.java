
package com.hardware.serviceplatform.controller;

import com.hardware.serviceplatform.dto.request.BookingRequest;
import com.hardware.serviceplatform.dto.response.BookingResponse;
import com.hardware.serviceplatform.entity.Booking;
import com.hardware.serviceplatform.entity.ServiceCategory;
import com.hardware.serviceplatform.entity.User;
import com.hardware.serviceplatform.repository.UserRepository;
import com.hardware.serviceplatform.service.BookingService;
import com.hardware.serviceplatform.service.EngineerAllocationService;
import jakarta.validation.Valid;
import com.hardware.serviceplatform.enums.Role;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import com.hardware.serviceplatform.dto.response.BookingStatusHistoryResponse;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

        private final BookingService bookingService;
        private final UserRepository userRepository;
        private final EngineerAllocationService engineerAllocationService;

        public BookingController(
                        BookingService bookingService,
                        UserRepository userRepository,
                        EngineerAllocationService engineerAllocationService) {

                this.bookingService = bookingService;
                this.userRepository = userRepository;
                this.engineerAllocationService = engineerAllocationService;
        }

        // Create a booking for the authenticated customer
        @PostMapping
        public ResponseEntity<BookingResponse> createBooking(
                        @Valid @RequestBody BookingRequest request,
                        Authentication authentication) {

                User customer = getAuthenticatedCustomer(authentication);

                ServiceCategory category = new ServiceCategory();
                category.setId(request.getServiceCategoryId());

                Booking booking = new Booking();
                booking.setCustomer(customer);
                booking.setServiceCategory(category);
                booking.setServiceType(request.getServiceType());
                booking.setIssueDescription(request.getIssueDescription());
                booking.setPreferredDateTime(request.getPreferredDateTime());
                booking.setPreferredEndDateTime(request.getPreferredEndDateTime());
                booking.setServiceAddress(request.getServiceAddress());
                booking.setLatitude(request.getLatitude());
                booking.setLongitude(request.getLongitude());

                Booking savedBooking = bookingService.createBooking(booking);

                // Automatically find and assign an eligible engineer.
                engineerAllocationService.allocateBooking(savedBooking.getId());

                // Fetch the updated booking with its allocation status.
                Booking updatedBooking = bookingService.findById(savedBooking.getId())
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Booking not found"));

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(mapToResponse(updatedBooking));
        }

        // Get status history for a booking owned by the authenticated customer
        @GetMapping("/{id}/history")
        public ResponseEntity<List<BookingStatusHistoryResponse>> getBookingStatusHistory(
                        @PathVariable Long id,
                        Authentication authentication) {

                User customer = getAuthenticatedCustomer(authentication);

                Booking booking = bookingService.findById(id)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Booking not found"));

                if (!booking.getCustomer().getId().equals(customer.getId())) {
                        throw new ResponseStatusException(
                                        HttpStatus.FORBIDDEN,
                                        "You are not authorized to view this booking history");
                }

                List<BookingStatusHistoryResponse> history = bookingService.getBookingStatusHistory(id);

                return ResponseEntity.ok(history);
        }

        // Get all bookings belonging to the authenticated customer
        @GetMapping("/my")
        public ResponseEntity<List<BookingResponse>> getMyBookings(
                        Authentication authentication) {

                User customer = getAuthenticatedCustomer(authentication);

                List<BookingResponse> responses = bookingService
                                .getCustomerBookings(customer.getId())
                                .stream()
                                .map(this::mapToResponse)
                                .toList();

                return ResponseEntity.ok(responses);
        }

        // Get a specific booking after checking ownership
        @GetMapping("/{id}")
        public ResponseEntity<BookingResponse> getBookingById(
                        @PathVariable Long id,
                        Authentication authentication) {

                User customer = getAuthenticatedCustomer(authentication);

                Booking booking = bookingService.findById(id)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Booking not found"));

                if (!booking.getCustomer().getId().equals(customer.getId())) {
                        throw new ResponseStatusException(
                                        HttpStatus.FORBIDDEN,
                                        "You are not authorized to view this booking");
                }

                return ResponseEntity.ok(mapToResponse(booking));
        }

        // Get the authenticated customer from the JWT identity
        private User getAuthenticatedCustomer(Authentication authentication) {

                if (authentication == null || !authentication.isAuthenticated()) {
                        throw new ResponseStatusException(
                                        HttpStatus.UNAUTHORIZED,
                                        "Authentication required");
                }

                User customer = userRepository.findByEmail(authentication.getName())
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "User account not found"));

                if (customer.getRole() != Role.CUSTOMER) {
                        throw new ResponseStatusException(
                                        HttpStatus.FORBIDDEN,
                                        "Only customers can access these booking operations");
                }

                return customer;
        }

        // Convert Booking entity into BookingResponse DTO
        private BookingResponse mapToResponse(Booking booking) {

                String engineerName = null;

                if (booking.getAssignedEngineer() != null
                                && booking.getAssignedEngineer().getUser() != null) {
                        engineerName = booking.getAssignedEngineer()
                                        .getUser()
                                        .getFullName();
                }

                return new BookingResponse(
                                booking.getId(),
                                booking.getServiceCategory().getName(),
                                booking.getServiceType(),
                                booking.getIssueDescription(),
                                booking.getPreferredDateTime(),
                                booking.getPreferredEndDateTime(),
                                booking.getServiceAddress(),
                                booking.getStatus(),
                                engineerName,
                                booking.getCreatedAt());
        }
}
