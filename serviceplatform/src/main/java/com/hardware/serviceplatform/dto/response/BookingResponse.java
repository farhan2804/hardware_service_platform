
package com.hardware.serviceplatform.dto.response;

import com.hardware.serviceplatform.enums.BookingStatus;
import com.hardware.serviceplatform.enums.ServiceType;

import java.time.LocalDateTime;

public class BookingResponse {

    private Long bookingId;
    private String serviceCategory;
    private ServiceType serviceType;
    private String issueDescription;
    private LocalDateTime preferredDateTime;
    private LocalDateTime preferredEndDateTime;
    private String serviceAddress;
    private BookingStatus status;
    private String assignedEngineerName;
    private LocalDateTime createdAt;

    public BookingResponse() {
    }

    public BookingResponse(
            Long bookingId,
            String serviceCategory,
            ServiceType serviceType,
            String issueDescription,
            LocalDateTime preferredDateTime,
            LocalDateTime preferredEndDateTime,
            String serviceAddress,
            BookingStatus status,
            String assignedEngineerName,
            LocalDateTime createdAt) {

        this.bookingId = bookingId;
        this.serviceCategory = serviceCategory;
        this.serviceType = serviceType;
        this.issueDescription = issueDescription;
        this.preferredDateTime = preferredDateTime;
        this.preferredEndDateTime = preferredEndDateTime;
        this.serviceAddress = serviceAddress;
        this.status = status;
        this.assignedEngineerName = assignedEngineerName;
        this.createdAt = createdAt;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public String getServiceCategory() {
        return serviceCategory;
    }

    public void setServiceCategory(String serviceCategory) {
        this.serviceCategory = serviceCategory;
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

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public String getAssignedEngineerName() {
        return assignedEngineerName;
    }

    public void setAssignedEngineerName(String assignedEngineerName) {
        this.assignedEngineerName = assignedEngineerName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
