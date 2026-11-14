
package com.hardware.serviceplatform.entity;

import com.hardware.serviceplatform.enums.ServiceType;
import jakarta.persistence.*;

@Entity
@Table(
    name = "engineer_skills",
    uniqueConstraints = {
        @UniqueConstraint(
            columnNames = {"engineer_profile_id", "service_category_id", "service_type"}
        )
    }
)
public class EngineerSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "engineer_profile_id", nullable = false)
    private EngineerProfile engineerProfile;

    @ManyToOne(optional = false)
    @JoinColumn(name = "service_category_id", nullable = false)
    private ServiceCategory serviceCategory;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServiceType serviceType;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public EngineerProfile getEngineerProfile() {
        return engineerProfile;
    }

    public void setEngineerProfile(EngineerProfile engineerProfile) {
        this.engineerProfile = engineerProfile;
    }

    public ServiceCategory getServiceCategory() {
        return serviceCategory;
    }

    public void setServiceCategory(ServiceCategory serviceCategory) {
        this.serviceCategory = serviceCategory;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public void setServiceType(ServiceType serviceType) {
        this.serviceType = serviceType;
    }
}
