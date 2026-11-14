
package com.hardware.serviceplatform.repository;

import com.hardware.serviceplatform.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    Optional<Feedback> findByBookingId(Long bookingId);

    List<Feedback> findByCustomerId(Long customerId);

    boolean existsByBookingId(Long bookingId);
}
