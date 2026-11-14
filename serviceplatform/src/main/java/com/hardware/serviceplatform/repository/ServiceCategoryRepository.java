
package com.hardware.serviceplatform.repository;

import com.hardware.serviceplatform.entity.ServiceCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ServiceCategoryRepository
        extends JpaRepository<ServiceCategory, Long> {

    List<ServiceCategory> findByActiveTrue();

    Optional<ServiceCategory> findByName(String name);

    boolean existsByName(String name);
}
