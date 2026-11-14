
package com.hardware.serviceplatform.service;

import com.hardware.serviceplatform.entity.AssignmentHistory;
import com.hardware.serviceplatform.entity.Booking;
import com.hardware.serviceplatform.entity.BookingStatusHistory;
import com.hardware.serviceplatform.entity.EngineerProfile;
import com.hardware.serviceplatform.entity.EngineerSkill;
import com.hardware.serviceplatform.enums.BookingStatus;
import com.hardware.serviceplatform.repository.AssignmentHistoryRepository;
import com.hardware.serviceplatform.repository.BookingRepository;
import com.hardware.serviceplatform.repository.BookingStatusHistoryRepository;
import com.hardware.serviceplatform.repository.EngineerSkillRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class EngineerAllocationService {

    private static final double EARTH_RADIUS_KM = 6371.0;

    private static final Set<BookingStatus> EXCLUDED_STATUSES =
            EnumSet.of(
                    BookingStatus.CANCELLED,
                    BookingStatus.COMPLETED
            );

    private final BookingRepository bookingRepository;
    private final EngineerSkillRepository engineerSkillRepository;
    private final BookingStatusHistoryRepository statusHistoryRepository;
    private final AssignmentHistoryRepository assignmentHistoryRepository;

    public EngineerAllocationService(
            BookingRepository bookingRepository,
            EngineerSkillRepository engineerSkillRepository,
            BookingStatusHistoryRepository statusHistoryRepository,
            AssignmentHistoryRepository assignmentHistoryRepository) {

        this.bookingRepository = bookingRepository;
        this.engineerSkillRepository = engineerSkillRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.assignmentHistoryRepository = assignmentHistoryRepository;
    }

    @Transactional
    public Optional<EngineerProfile> allocateBooking(Long bookingId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Booking not found"));

        LocalDateTime start = booking.getPreferredDateTime();
        LocalDateTime end = booking.getPreferredEndDateTime();

        if (start == null || end == null || !end.isAfter(start)) {
            throw new IllegalArgumentException(
                    "A valid appointment start and end time are required"
            );
        }

        // Coordinates are required for automatic distance calculation.
        if (booking.getLatitude() == null
                || booking.getLongitude() == null) {
            moveToAwaitingAssignment(booking);
            return Optional.empty();
        }

        // Find engineers with the required category and service skill.
        List<EngineerSkill> matchingSkills =
                engineerSkillRepository
                        .findByServiceCategoryIdAndServiceType(
                                booking.getServiceCategory().getId(),
                                booking.getServiceType()
                        );

        List<EngineerProfile> eligibleEngineers = matchingSkills.stream()
                .map(EngineerSkill::getEngineerProfile)
                .filter(this::isAvailable)
                .filter(engineer -> hasCoordinates(engineer))
                .filter(engineer -> isWithinWorkingHours(engineer, start, end))
                .filter(engineer -> isWithinServiceRadius(engineer, booking))
                .filter(engineer -> !bookingRepository.hasOverlappingBooking(
                        engineer.getId(),
                        start,
                        end,
                        EXCLUDED_STATUSES
                ))
                .distinct()
                .toList();

        if (eligibleEngineers.isEmpty()) {
            moveToAwaitingAssignment(booking);
            return Optional.empty();
        }

        // Select the nearest engineer.
        // If distances are equal, prefer the engineer with fewer active jobs.
        EngineerProfile selectedEngineer = eligibleEngineers.stream()
                .min(Comparator
                        .comparingDouble(
                                (EngineerProfile engineer) ->
                                        calculateDistance(
                                                booking.getLatitude(),
                                                booking.getLongitude(),
                                                engineer.getLatitude(),
                                                engineer.getLongitude()
                                        )
                        )
                        .thenComparingLong(this::countActiveBookings)
                )
                .orElseThrow();

        EngineerProfile previousEngineer = booking.getAssignedEngineer();
        BookingStatus previousStatus = booking.getStatus();

        booking.setAssignedEngineer(selectedEngineer);
        booking.setStatus(BookingStatus.ENGINEER_ASSIGNED);

        bookingRepository.save(booking);

        // Record the status change.
        BookingStatusHistory statusHistory = new BookingStatusHistory();
        statusHistory.setBooking(booking);
        statusHistory.setPreviousStatus(previousStatus);
        statusHistory.setNewStatus(BookingStatus.ENGINEER_ASSIGNED);
        statusHistory.setChangedBy(null);
        statusHistory.setNote("Engineer automatically assigned");

        statusHistoryRepository.save(statusHistory);

        // Keep an audit record of the assignment.
        AssignmentHistory assignmentHistory = new AssignmentHistory();
        assignmentHistory.setBooking(booking);
        assignmentHistory.setPreviousEngineer(previousEngineer);
        assignmentHistory.setNewEngineer(selectedEngineer);
        assignmentHistory.setAssignedBy(null);
        assignmentHistory.setReason("Automatic allocation based on skill, availability and distance");

        assignmentHistoryRepository.save(assignmentHistory);

        return Optional.of(selectedEngineer);
    }

    private boolean isAvailable(EngineerProfile engineer) {
        return Boolean.TRUE.equals(engineer.getAvailable());
    }

    private boolean hasCoordinates(EngineerProfile engineer) {
        return engineer.getLatitude() != null
                && engineer.getLongitude() != null;
    }

    private boolean isWithinWorkingHours(
            EngineerProfile engineer,
            LocalDateTime start,
            LocalDateTime end) {

        LocalTime workingStart = engineer.getWorkingHoursStart();
        LocalTime workingEnd = engineer.getWorkingHoursEnd();

        if (workingStart == null || workingEnd == null) {
            return false;
        }

        // This MVP does not support appointments spanning midnight.
        if (!start.toLocalDate().equals(end.toLocalDate())) {
            return false;
        }

        return !start.toLocalTime().isBefore(workingStart)
                && !end.toLocalTime().isAfter(workingEnd);
    }

    private boolean isWithinServiceRadius(
            EngineerProfile engineer,
            Booking booking) {

        double distance = calculateDistance(
                booking.getLatitude(),
                booking.getLongitude(),
                engineer.getLatitude(),
                engineer.getLongitude()
        );

        Double radius = engineer.getServiceRadiusKm();

        return radius != null && distance <= radius;
    }

    private long countActiveBookings(EngineerProfile engineer) {

        return bookingRepository
                .findByAssignedEngineerId(engineer.getId())
                .stream()
                .filter(booking ->
                        !EXCLUDED_STATUSES.contains(booking.getStatus()))
                .count();
    }

    private double calculateDistance(
            double lat1,
            double lon1,
            double lat2,
            double lon2) {

        double latitudeDifference = Math.toRadians(lat2 - lat1);
        double longitudeDifference = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latitudeDifference / 2)
                * Math.sin(latitudeDifference / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(longitudeDifference / 2)
                * Math.sin(longitudeDifference / 2);

        double c = 2 * Math.atan2(
                Math.sqrt(a),
                Math.sqrt(1 - a)
        );

        return EARTH_RADIUS_KM * c;
    }

    private void moveToAwaitingAssignment(Booking booking) {

        BookingStatus previousStatus = booking.getStatus();

        if (previousStatus == BookingStatus.AWAITING_ASSIGNMENT) {
            return;
        }

        booking.setStatus(BookingStatus.AWAITING_ASSIGNMENT);
        bookingRepository.save(booking);

        BookingStatusHistory history = new BookingStatusHistory();
        history.setBooking(booking);
        history.setPreviousStatus(previousStatus);
        history.setNewStatus(BookingStatus.AWAITING_ASSIGNMENT);
        history.setChangedBy(null);
        history.setNote("No eligible engineer found. Admin assignment required.");

        statusHistoryRepository.save(history);
    }
}
