package com.hardware.serviceplatform.dto.response;

import com.hardware.serviceplatform.enums.BookingStatus;
import java.time.LocalDateTime;

public record BookingStatusHistoryResponse(
        BookingStatus previousStatus,
        BookingStatus newStatus,
        String changedByName,
        String note,
        LocalDateTime changedAt
) {
}