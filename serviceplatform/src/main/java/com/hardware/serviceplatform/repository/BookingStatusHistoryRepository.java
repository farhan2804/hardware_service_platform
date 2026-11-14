
package com.hardware.serviceplatform.repository;

import com.hardware.serviceplatform.entity.BookingStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingStatusHistoryRepository
        extends JpaRepository<BookingStatusHistory, Long> {

    List<BookingStatusHistory> findByBookingIdOrderByChangedAtAsc(
            Long bookingId
    );
}
