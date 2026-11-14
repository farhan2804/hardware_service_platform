
package com.hardware.serviceplatform.repository;

import com.hardware.serviceplatform.entity.EngineerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EngineerProfileRepository
        extends JpaRepository<EngineerProfile, Long> {

    Optional<EngineerProfile> findByUserId(Long userId);

    List<EngineerProfile> findByAvailableTrue();
}
