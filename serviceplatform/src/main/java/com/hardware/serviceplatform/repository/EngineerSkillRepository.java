
package com.hardware.serviceplatform.repository;

import com.hardware.serviceplatform.entity.EngineerSkill;
import com.hardware.serviceplatform.enums.ServiceType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EngineerSkillRepository
        extends JpaRepository<EngineerSkill, Long> {

    List<EngineerSkill> findByEngineerProfileId(Long engineerProfileId);

    List<EngineerSkill> findByServiceCategoryIdAndServiceType(
            Long serviceCategoryId,
            ServiceType serviceType
    );

    boolean existsByEngineerProfileIdAndServiceCategoryIdAndServiceType(
            Long engineerProfileId,
            Long serviceCategoryId,
            ServiceType serviceType
    );
}
