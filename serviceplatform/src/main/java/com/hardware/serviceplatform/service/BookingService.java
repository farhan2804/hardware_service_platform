
package com.hardware.serviceplatform.service;

import com.hardware.serviceplatform.entity.Booking;
import com.hardware.serviceplatform.entity.BookingStatusHistory;
import com.hardware.serviceplatform.entity.ServiceCategory;
import com.hardware.serviceplatform.entity.User;
import com.hardware.serviceplatform.enums.BookingStatus;
import com.hardware.serviceplatform.enums.Role;
import com.hardware.serviceplatform.repository.BookingRepository;
import com.hardware.serviceplatform.repository.BookingStatusHistoryRepository;
import com.hardware.serviceplatform.repository.ServiceCategoryRepository;
import com.hardware.serviceplatform.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ServiceCategoryRepository serviceCategoryRepository;
    private final BookingStatusHistoryRepository statusHistoryRepository;

    public BookingService(
            BookingRepository bookingRepository,
            UserRepository userRepository,
            ServiceCategoryRepository serviceCategoryRepository,
            BookingStatusHistoryRepository statusHistoryRepository) {

        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.serviceCategoryRepository = serviceCategoryRepository;
        this.statusHistoryRepository = statusHistoryRepository;
    }

    @Transactional
    public Booking createBooking(Booking booking) {

        if (booking.getCustomer() == null
                || booking.getCustomer().getId() == null) {
            throw new IllegalArgumentException("Customer is required");
        }

        if (booking.getServiceCategory() == null
                || booking.getServiceCategory().getId() == null) {
            throw new IllegalArgumentException("Service category is required");
        }

        User customer = userRepository
                .findById(booking.getCustomer().getId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Customer not found"));

        if (customer.getRole() != Role.CUSTOMER) {
            throw new IllegalArgumentException(
                    "Only customers can create bookings"
            );
        }

        ServiceCategory category = serviceCategoryRepository
                .findById(booking.getServiceCategory().getId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Service category not found"
                        ));

        if (!Boolean.TRUE.equals(category.getActive())) {
            throw new IllegalArgumentException(
                    "This service category is currently unavailable"
            );
        }

        if (booking.getPreferredDateTime() == null
                || !booking.getPreferredDateTime().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException(
                    "Please select a future appointment date and time"
            );
        }

        if (booking.getPreferredEndDateTime() == null
                || !booking.getPreferredEndDateTime()
                        .isAfter(booking.getPreferredDateTime())) {
            throw new IllegalArgumentException(
                    "Appointment end time must be after start time"
            );
        }

        if (booking.getIssueDescription() == null
                || booking.getIssueDescription().isBlank()) {
            throw new IllegalArgumentException(
                    "Issue description is required"
            );
        }

        if (booking.getServiceAddress() == null
                || booking.getServiceAddress().isBlank()) {
            throw new IllegalArgumentException(
                    "Service address is required"
            );
        }

        if (booking.getLatitude() == null
                || booking.getLongitude() == null
                || booking.getLatitude() < -90
                || booking.getLatitude() > 90
                || booking.getLongitude() < -180
                || booking.getLongitude() > 180) {
            throw new IllegalArgumentException(
                    "Valid service location coordinates are required"
            );
        }

        booking.setCustomer(customer);
        booking.setServiceCategory(category);

        // Engineer allocation happens separately.
        booking.setAssignedEngineer(null);

        booking.setStatus(BookingStatus.REQUEST_RECEIVED);

        Booking savedBooking = bookingRepository.save(booking);

        // Record the initial booking status.
        BookingStatusHistory history = new BookingStatusHistory();
        history.setBooking(savedBooking);
        history.setPreviousStatus(null);
        history.setNewStatus(BookingStatus.REQUEST_RECEIVED);
        history.setChangedBy(null);
        history.setNote("Booking request created");

        statusHistoryRepository.save(history);

        return savedBooking;
    }

    public Optional<Booking> findById(Long bookingId) {
        return bookingRepository.findById(bookingId);
    }

    public List<Booking> getCustomerBookings(Long customerId) {
        return bookingRepository
                .findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    public List<Booking> getEngineerBookings(Long engineerProfileId) {
        return bookingRepository
                .findByAssignedEngineerId(engineerProfileId);
    }

    public List<Booking> getBookingsByStatus(BookingStatus status) {
        return bookingRepository.findByStatus(status);
    }
}
