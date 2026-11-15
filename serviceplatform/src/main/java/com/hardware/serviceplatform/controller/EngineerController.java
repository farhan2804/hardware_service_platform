
package com.hardware.serviceplatform.controller;

import com.hardware.serviceplatform.dto.request.AssignEngineerSkillRequest;
import com.hardware.serviceplatform.dto.request.CreateEngineerRequest;
import com.hardware.serviceplatform.dto.response.BookingResponse;
import com.hardware.serviceplatform.entity.Booking;
import com.hardware.serviceplatform.entity.EngineerProfile;
import com.hardware.serviceplatform.entity.EngineerSkill;
import com.hardware.serviceplatform.entity.User;
import com.hardware.serviceplatform.enums.BookingStatus;
import com.hardware.serviceplatform.service.BookingService;
import com.hardware.serviceplatform.service.EngineerService;
import com.hardware.serviceplatform.service.UserService;
import com.hardware.serviceplatform.enums.BookingStatus;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/engineers")
public class EngineerController {

        private final EngineerService engineerService;
        private final UserService userService;
        private final BookingService bookingService;

        public EngineerController(
                        EngineerService engineerService,
                        UserService userService,
                        BookingService bookingService) {

                this.engineerService = engineerService;
                this.userService = userService;
                this.bookingService = bookingService;
        }

        // ADMIN: Create engineer account and profile
        @PostMapping
        @ResponseStatus(HttpStatus.CREATED)
        @PreAuthorize("hasRole('ADMIN')")
        public EngineerResponse createEngineer(
                        @Valid @RequestBody CreateEngineerRequest request) {

                EngineerProfile profile = engineerService.createEngineer(request);

                return mapToResponse(profile);
        }

        // ADMIN: View all engineers
        @GetMapping
        @PreAuthorize("hasRole('ADMIN')")
        public List<EngineerResponse> getAllEngineers() {

                return engineerService.getAllEngineers()
                                .stream()
                                .map(this::mapToResponse)
                                .toList();
        }

        // ADMIN: Assign a skill to an engineer
        @PostMapping("/{engineerId}/skills")
        @ResponseStatus(HttpStatus.CREATED)
        @PreAuthorize("hasRole('ADMIN')")
        public SkillResponse assignSkill(
                        @PathVariable Long engineerId,
                        @Valid @RequestBody AssignEngineerSkillRequest request) {

                EngineerSkill skill = engineerService.assignSkill(engineerId, request);

                return new SkillResponse(
                                skill.getId(),
                                skill.getServiceCategory().getName(),
                                skill.getServiceType().name());
        }

        // ADMIN: View skills assigned to an engineer
        @GetMapping("/{engineerId}/skills")
        @PreAuthorize("hasRole('ADMIN')")
        public List<SkillResponse> getEngineerSkills(
                        @PathVariable Long engineerId) {

                return engineerService.getEngineerSkills(engineerId)
                                .stream()
                                .map(skill -> new SkillResponse(
                                                skill.getId(),
                                                skill.getServiceCategory().getName(),
                                                skill.getServiceType().name()))
                                .toList();
        }

        // ENGINEER: View own profile
        @GetMapping("/me")
        @PreAuthorize("hasRole('ENGINEER')")
        public EngineerResponse getMyProfile(
                        Authentication authentication) {

                EngineerProfile profile = engineerService.findByEmail(authentication.getName());

                return mapToResponse(profile);
        }

        // ENGINEER: Update own availability
        @PutMapping("/me/availability")
        @PreAuthorize("hasRole('ENGINEER')")
        public EngineerResponse updateAvailability(
                        @RequestParam boolean available,
                        Authentication authentication) {

                EngineerProfile profile = engineerService.updateAvailability(
                                authentication.getName(),
                                available);

                return mapToResponse(profile);
        }

        // ENGINEER: View assigned jobs
        @GetMapping("/me/jobs")
        @PreAuthorize("hasRole('ENGINEER')")
        public List<BookingResponse> getMyJobs(
                        Authentication authentication) {

                EngineerProfile engineer = engineerService.findByEmail(authentication.getName());

                return bookingService.getEngineerBookings(engineer.getId())
                                .stream()
                                .map(this::mapBookingResponse)
                                .toList();
        }

        // ENGINEER: Accept an assigned booking
        @PostMapping("/me/jobs/{bookingId}/accept")
        @PreAuthorize("hasRole('ENGINEER')")
        public BookingResponse acceptBooking(
                        @PathVariable Long bookingId,
                        Authentication authentication) {

                EngineerProfile engineer = engineerService.findByEmail(authentication.getName());

                Booking booking = bookingService.acceptBooking(
                                bookingId,
                                engineer.getId(),
                                engineer.getUser());

                return mapBookingResponse(booking);
        }

        // ENGINEER: Update booking progress
        @PutMapping("/me/jobs/{bookingId}/status")
        @PreAuthorize("hasRole('ENGINEER')")
        public BookingResponse updateBookingStatus(
                        @PathVariable Long bookingId,
                        @RequestParam BookingStatus status,
                        Authentication authentication) {

                EngineerProfile engineer = engineerService.findByEmail(authentication.getName());

                Booking booking = bookingService.updateEngineerBookingStatus(
                                bookingId,
                                engineer.getId(),
                                engineer.getUser(),
                                status);

                return mapBookingResponse(booking);
        }

        // Map engineer entity to response
        private EngineerResponse mapToResponse(EngineerProfile profile) {

                User user = profile.getUser();

                return new EngineerResponse(
                                profile.getId(),
                                user.getId(),
                                user.getFullName(),
                                user.getEmail(),
                                user.getPhone(),
                                profile.getServiceArea(),
                                profile.getLatitude(),
                                profile.getLongitude(),
                                profile.getServiceRadiusKm(),
                                profile.getAvailable(),
                                profile.getWorkingHoursStart(),
                                profile.getWorkingHoursEnd());
        }

        private BookingResponse mapBookingResponse(Booking booking) {

                BookingResponse response = new BookingResponse();

                response.setBookingId(booking.getId());
                response.setServiceCategory(
                                booking.getServiceCategory().getName());
                response.setServiceType(booking.getServiceType());
                response.setIssueDescription(booking.getIssueDescription());
                response.setPreferredDateTime(booking.getPreferredDateTime());
                response.setPreferredEndDateTime(
                                booking.getPreferredEndDateTime());
                response.setServiceAddress(booking.getServiceAddress());
                response.setStatus(booking.getStatus());

                if (booking.getAssignedEngineer() != null) {
                        response.setAssignedEngineerName(
                                        booking.getAssignedEngineer()
                                                        .getUser()
                                                        .getFullName());
                }

                response.setCreatedAt(booking.getCreatedAt());

                return response;
        }

        // Engineer profile response
        public record EngineerResponse(
                        Long engineerId,
                        Long userId,
                        String fullName,
                        String email,
                        String phone,
                        String serviceArea,
                        Double latitude,
                        Double longitude,
                        Double serviceRadiusKm,
                        Boolean available,
                        LocalTime workingHoursStart,
                        LocalTime workingHoursEnd) {
        }

        // Engineer skill response
        public record SkillResponse(
                        Long skillId,
                        String serviceCategory,
                        String serviceType) {
        }
}
