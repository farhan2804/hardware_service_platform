
package com.hardware.serviceplatform.dto.request;

import com.hardware.serviceplatform.enums.ServiceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

import java.time.LocalDateTime;

public class BookingRequest {

    @NotNull(message = "Service category is required")
    private Long serviceCategoryId;

    @NotNull(message = "Service type is required")
    private ServiceType serviceType;

    @NotBlank(message = "Issue description is required")
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String issueDescription;

    @NotNull(message = "Preferred start time is required")
    private LocalDateTime preferredDateTime;

    @NotNull(message = "Preferred end time is required")
    private LocalDateTime preferredEndDateTime;

    @NotBlank(message = "Service address is required")
    @Size(max = 500, message = "Address cannot exceed 500 characters")
    private String serviceAddress;

    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    private Double longitude;

    public Long getServiceCategoryId() {
        return serviceCategoryId;
    }

    public void setServiceCategoryId(Long serviceCategoryId) {
        this.serviceCategoryId = serviceCategoryId;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public void setServiceType(ServiceType serviceType) {
        this.serviceType = serviceType;
    }

    public String getIssueDescription() {
        return issueDescription;
    }

    public void setIssueDescription(String issueDescription) {
        this.issueDescription = issueDescription;
    }

    public LocalDateTime getPreferredDateTime() {
        return preferredDateTime;
    }

    public void setPreferredDateTime(LocalDateTime preferredDateTime) {
        this.preferredDateTime = preferredDateTime;
    }

    public LocalDateTime getPreferredEndDateTime() {
        return preferredEndDateTime;
    }

    public void setPreferredEndDateTime(LocalDateTime preferredEndDateTime) {
        this.preferredEndDateTime = preferredEndDateTime;
    }

    public String getServiceAddress() {
        return serviceAddress;
    }

    public void setServiceAddress(String serviceAddress) {
        this.serviceAddress = serviceAddress;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}
