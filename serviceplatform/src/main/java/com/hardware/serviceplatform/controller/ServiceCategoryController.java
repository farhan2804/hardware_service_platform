
package com.hardware.serviceplatform.controller;

import com.hardware.serviceplatform.entity.ServiceCategory;
import com.hardware.serviceplatform.service.ServiceCategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class ServiceCategoryController {

    private final ServiceCategoryService serviceCategoryService;

    public ServiceCategoryController(
            ServiceCategoryService serviceCategoryService) {
        this.serviceCategoryService = serviceCategoryService;
    }

    // Get all active service categories
    @GetMapping
    public List<ServiceCategory> getActiveCategories() {
        return serviceCategoryService.getActiveCategories();
    }

    // Create a new service category
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public ServiceCategory createCategory(
            @RequestBody ServiceCategory category) {
        return serviceCategoryService.createCategory(category);
    }

    // Update an existing category
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ServiceCategory updateCategory(
            @PathVariable Long id,
            @RequestBody ServiceCategory category) {
        return serviceCategoryService.updateCategory(id, category);
    }

    // Deactivate a category
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deactivateCategory(@PathVariable Long id) {
        serviceCategoryService.deactivateCategory(id);
    }
}
