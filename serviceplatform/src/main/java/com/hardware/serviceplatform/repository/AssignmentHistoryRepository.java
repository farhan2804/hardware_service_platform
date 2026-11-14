
package com.hardware.serviceplatform.repository;

import com.hardware.serviceplatform.entity.AssignmentHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssignmentHistoryRepository
        extends JpaRepository<AssignmentHistory, Long> {

    List<AssignmentHistory> findByBookingIdOrderByAssignedAtAsc(
            Long bookingId
    );
}
