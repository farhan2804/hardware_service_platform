package com.hardware.serviceplatform.enums;


// Purpose: Tracks the payment state of a booking. For example, 
// a payment starts as PENDING and changes to SUCCESSFUL 
// only after successful payment confirmation.

public enum PaymentStatus {
    PENDING,
    SUCCESSFUL,
    REFUNDED,
    FAILED
}
