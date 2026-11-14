
package com.hardware.serviceplatform.repository;

import com.hardware.serviceplatform.entity.Booking;
import com.hardware.serviceplatform.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.Collection;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByCustomerId(Long customerId);

    List<Booking> findByAssignedEngineerId(Long engineerProfileId);

    List<Booking> findByStatus(BookingStatus status);

    List<Booking> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    List<Booking> findByAssignedEngineerIdAndStatus(
            Long engineerProfileId,
            BookingStatus status);

    @Query("""
                SELECT COUNT(b) > 0
                FROM Booking b
                WHERE b.assignedEngineer.id = :engineerId
                AND b.status NOT IN :excludedStatuses
                AND b.preferredDateTime < :requestedEnd
                AND b.preferredEndDateTime > :requestedStart
            """)
    boolean hasOverlappingBooking(
            @Param("engineerId") Long engineerId,
            @Param("requestedStart") LocalDateTime requestedStart,
            @Param("requestedEnd") LocalDateTime requestedEnd,
            @Param("excludedStatuses") Collection<BookingStatus> excludedStatuses);
}
