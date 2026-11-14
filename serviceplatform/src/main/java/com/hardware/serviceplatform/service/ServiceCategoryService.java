
package com.hardware.serviceplatform.service;

import com.hardware.serviceplatform.entity.ServiceCategory;
import com.hardware.serviceplatform.repository.ServiceCategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ServiceCategoryService {

    private final ServiceCategoryRepository serviceCategoryRepository;

    public ServiceCategoryService(
            ServiceCategoryRepository serviceCategoryRepository) {
        this.serviceCategoryRepository = serviceCategoryRepository;
    }

    public List<ServiceCategory> getActiveCategories() {
        return serviceCategoryRepository.findByActiveTrue();
    }

    public Optional<ServiceCategory> findById(Long id) {
        return serviceCategoryRepository.findById(id);
    }

    @Transactional
    public ServiceCategory createCategory(ServiceCategory category) {
        if (serviceCategoryRepository.existsByName(category.getName())) {
            throw new IllegalArgumentException(
                    "Service category already exists"
            );
        }

        return serviceCategoryRepository.save(category);
    }

    @Transactional
    public ServiceCategory updateCategory(
            Long id, ServiceCategory updatedCategory) {

        ServiceCategory existingCategory = serviceCategoryRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Service category not found"
                ));

        existingCategory.setName(updatedCategory.getName());
        existingCategory.setDescription(updatedCategory.getDescription());

        return serviceCategoryRepository.save(existingCategory);
    }

    @Transactional
    public void deactivateCategory(Long id) {
        ServiceCategory category = serviceCategoryRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Service category not found"
                ));

        category.setActive(false);
        serviceCategoryRepository.save(category);
    }
}
