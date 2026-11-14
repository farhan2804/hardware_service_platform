package com.hardware.serviceplatform.enums;


//This enum will control the complete service booking lifecycle.


public enum BookingStatus {
    REQUEST_RECEIVED,
    AWAITING_ASSIGNMENT,
    ENGINEER_ASSIGNED,
    ACCEPTED,
    ON_THE_WAY,
    ARRIVED,
    QUOTATION_SHARED,
    WORK_IN_PROGRESS,
    COMPLETED,
    CANCELLED
}
