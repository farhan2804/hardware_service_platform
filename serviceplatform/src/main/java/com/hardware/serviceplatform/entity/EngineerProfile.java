
package com.hardware.serviceplatform.entity;

import jakarta.persistence.*;
import java.time.LocalTime;

@Entity
@Table(name = "engineer_profiles")
public class EngineerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private String serviceArea;

    private Double latitude;

    private Double longitude;

    @Column(nullable = false)
    private Boolean available = true;

    private LocalTime workingHoursStart;

    private LocalTime workingHoursEnd;
    @Column(name = "service_radius_km", nullable = false)
    private Double serviceRadiusKm = 15.0;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getServiceArea() {
        return serviceArea;
    }

    public void setServiceArea(String serviceArea) {
        this.serviceArea = serviceArea;
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

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

    public LocalTime getWorkingHoursStart() {
        return workingHoursStart;
    }

    public void setWorkingHoursStart(LocalTime workingHoursStart) {
        this.workingHoursStart = workingHoursStart;
    }

    public LocalTime getWorkingHoursEnd() {
        return workingHoursEnd;
    }

    public void setWorkingHoursEnd(LocalTime workingHoursEnd) {
        this.workingHoursEnd = workingHoursEnd;
    }

    public Double getServiceRadiusKm() {
        return serviceRadiusKm;
    }

    public void setServiceRadiusKm(Double serviceRadiusKm) {
        this.serviceRadiusKm = serviceRadiusKm;
    }
}
