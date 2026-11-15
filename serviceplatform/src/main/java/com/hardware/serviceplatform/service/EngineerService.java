
package com.hardware.serviceplatform.service;

import com.hardware.serviceplatform.dto.request.AssignEngineerSkillRequest;
import com.hardware.serviceplatform.dto.request.CreateEngineerRequest;
import com.hardware.serviceplatform.entity.EngineerProfile;
import com.hardware.serviceplatform.entity.EngineerSkill;
import com.hardware.serviceplatform.entity.ServiceCategory;
import com.hardware.serviceplatform.entity.User;
import com.hardware.serviceplatform.enums.Role;
import com.hardware.serviceplatform.repository.EngineerProfileRepository;
import com.hardware.serviceplatform.repository.EngineerSkillRepository;
import com.hardware.serviceplatform.repository.ServiceCategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

@Service
public class EngineerService {

        private final EngineerProfileRepository engineerProfileRepository;
        private final EngineerSkillRepository engineerSkillRepository;
        private final ServiceCategoryRepository serviceCategoryRepository;
        private final UserService userService;

        public EngineerService(
                        EngineerProfileRepository engineerProfileRepository,
                        EngineerSkillRepository engineerSkillRepository,
                        ServiceCategoryRepository serviceCategoryRepository,
                        UserService userService) {

                this.engineerProfileRepository = engineerProfileRepository;
                this.engineerSkillRepository = engineerSkillRepository;
                this.serviceCategoryRepository = serviceCategoryRepository;
                this.userService = userService;
        }

        @Transactional
        public EngineerProfile createEngineer(CreateEngineerRequest request) {

                User user = new User();
                user.setFullName(request.getFullName());
                user.setEmail(request.getEmail());
                user.setPassword(request.getPassword());
                user.setPhone(request.getPhone());
                user.setRole(Role.ENGINEER);

                User savedUser = userService.registerUser(user);

                EngineerProfile profile = new EngineerProfile();
                profile.setUser(savedUser);
                profile.setServiceArea(request.getServiceArea());
                profile.setLatitude(request.getLatitude());
                profile.setLongitude(request.getLongitude());
                profile.setAvailable(true);
                profile.setServiceRadiusKm(
                                request.getServiceRadiusKm() != null
                                                ? request.getServiceRadiusKm()
                                                : 15.0);

                profile.setWorkingHoursStart(LocalTime.of(9, 0));
                profile.setWorkingHoursEnd(LocalTime.of(18, 0));

                return engineerProfileRepository.save(profile);
        }

        @Transactional
        public EngineerSkill assignSkill(
                        Long engineerId,
                        AssignEngineerSkillRequest request) {

                EngineerProfile engineer = engineerProfileRepository.findById(engineerId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Engineer profile not found"));

                ServiceCategory category = serviceCategoryRepository
                                .findById(request.getServiceCategoryId())
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Service category not found"));

                if (!category.getActive()) {
                        throw new IllegalArgumentException(
                                        "Cannot assign an inactive service category");
                }

                if (engineerSkillRepository
                                .existsByEngineerProfileIdAndServiceCategoryIdAndServiceType(
                                                engineerId,
                                                category.getId(),
                                                request.getServiceType())) {

                        throw new IllegalArgumentException(
                                        "This skill is already assigned to the engineer");
                }

                EngineerSkill skill = new EngineerSkill();
                skill.setEngineerProfile(engineer);
                skill.setServiceCategory(category);
                skill.setServiceType(request.getServiceType());

                return engineerSkillRepository.save(skill);
        }

        public List<EngineerProfile> getAllEngineers() {
                return engineerProfileRepository.findAll();
        }

        public EngineerProfile findById(Long engineerId) {
                return engineerProfileRepository.findById(engineerId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Engineer profile not found"));
        }

        public List<EngineerProfile> getAvailableEngineers() {
                return engineerProfileRepository.findByAvailableTrue();
        }

        public EngineerProfile findByUserId(Long userId) {
                return engineerProfileRepository.findByUserId(userId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Engineer profile not found"));
        }

        public EngineerProfile findByEmail(String email) {
                User user = userService.findByEmail(email)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "User not found"));

                return findByUserId(user.getId());
        }

        @Transactional
        public EngineerProfile updateAvailability(String email, boolean available) {
                EngineerProfile profile = findByEmail(email);
                profile.setAvailable(available);

                return engineerProfileRepository.save(profile);
        }

        // ADMIN: Retrieve all skills assigned to an engineer
        public List<EngineerSkill> getEngineerSkills(Long engineerId) {

                engineerProfileRepository.findById(engineerId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Engineer profile not found"));

                return engineerSkillRepository.findByEngineerProfileId(engineerId);
        }
}
