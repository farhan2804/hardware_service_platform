
package com.hardware.serviceplatform.dto.request;

import com.hardware.serviceplatform.enums.ServiceType;
import jakarta.validation.constraints.NotNull;

public class AssignEngineerSkillRequest {

    @NotNull
    private Long serviceCategoryId;

    @NotNull
    private ServiceType serviceType;

    public Long getServiceCategoryId() { return serviceCategoryId; }
    public void setServiceCategoryId(Long serviceCategoryId) {
        this.serviceCategoryId = serviceCategoryId;
    }

    public ServiceType getServiceType() { return serviceType; }
    public void setServiceType(ServiceType serviceType) {
        this.serviceType = serviceType;
    }
}
