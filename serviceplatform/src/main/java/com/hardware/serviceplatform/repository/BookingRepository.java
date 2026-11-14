
package com.hardware.serviceplatform.repository;

import com.hardware.serviceplatform.entity.Booking;
import com.hardware.serviceplatform.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByCustomerId(Long customerId);

    List<Booking> findByAssignedEngineerId(Long engineerProfileId);

    List<Booking> findByStatus(BookingStatus status);

    List<Booking> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    List<Booking> findByAssignedEngineerIdAndStatus(
            Long engineerProfileId,
            BookingStatus status
    );
}
