
package com.hardware.serviceplatform.service;

import com.hardware.serviceplatform.entity.EngineerProfile;
import com.hardware.serviceplatform.repository.EngineerProfileRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EngineerService {

    private final EngineerProfileRepository engineerProfileRepository;

    public EngineerService(
            EngineerProfileRepository engineerProfileRepository) {
        this.engineerProfileRepository = engineerProfileRepository;
    }

    public Optional<EngineerProfile> findByUserId(Long userId) {
        return engineerProfileRepository.findByUserId(userId);
    }

    public List<EngineerProfile> getAvailableEngineers() {
        return engineerProfileRepository.findByAvailableTrue();
    }

    public EngineerProfile saveEngineerProfile(EngineerProfile profile) {
        return engineerProfileRepository.save(profile);
    }
}
