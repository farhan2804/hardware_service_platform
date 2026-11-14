
package com.hardware.serviceplatform.controller;

import com.hardware.serviceplatform.dto.request.AssignEngineerSkillRequest;
import com.hardware.serviceplatform.dto.request.CreateEngineerRequest;
import com.hardware.serviceplatform.entity.EngineerProfile;
import com.hardware.serviceplatform.entity.EngineerSkill;
import com.hardware.serviceplatform.entity.User;
import com.hardware.serviceplatform.enums.Role;
import com.hardware.serviceplatform.service.EngineerService;
import com.hardware.serviceplatform.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/engineers")
public class EngineerController {

    private final EngineerService engineerService;
    private final UserService userService;

    public EngineerController(
            EngineerService engineerService,
            UserService userService) {
        this.engineerService = engineerService;
        this.userService = userService;
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
                skill.getServiceType().name()
        );
    }

    // ENGINEER: View own profile
    @GetMapping("/me")
    @PreAuthorize("hasRole('ENGINEER')")
    public EngineerResponse getMyProfile(Authentication authentication) {
        EngineerProfile profile =
                engineerService.findByEmail(authentication.getName());

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
                available
        );

        return mapToResponse(profile);
    }

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
                profile.getWorkingHoursEnd()
        );
    }

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
            java.time.LocalTime workingHoursStart,
            java.time.LocalTime workingHoursEnd
    ) {}

    public record SkillResponse(
            Long skillId,
            String serviceCategory,
            String serviceType
    ) {}
}
