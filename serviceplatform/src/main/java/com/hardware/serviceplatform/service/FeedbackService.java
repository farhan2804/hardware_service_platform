
package com.hardware.serviceplatform.service;

import com.hardware.serviceplatform.entity.Booking;
import com.hardware.serviceplatform.entity.Feedback;
import com.hardware.serviceplatform.entity.User;
import com.hardware.serviceplatform.enums.BookingStatus;
import com.hardware.serviceplatform.enums.Role;
import com.hardware.serviceplatform.repository.BookingRepository;
import com.hardware.serviceplatform.repository.FeedbackRepository;
import com.hardware.serviceplatform.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    public FeedbackService(
            FeedbackRepository feedbackRepository,
            BookingRepository bookingRepository,
            UserRepository userRepository) {

        this.feedbackRepository = feedbackRepository;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Feedback submitFeedback(
            Long bookingId,
            Long customerId,
            Integer rating,
            String comment) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Booking not found"));

        User customer = userRepository.findById(customerId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Customer not found"));

        if (customer.getRole() != Role.CUSTOMER) {
            throw new IllegalArgumentException(
                    "Only customers can submit feedback");
        }

        if (!booking.getCustomer().getId().equals(customerId)) {
            throw new IllegalArgumentException(
                    "You cannot submit feedback for another customer's booking");
        }

        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new IllegalArgumentException(
                    "Feedback can only be submitted after service completion");
        }

        if (feedbackRepository.existsByBookingId(bookingId)) {
            throw new IllegalArgumentException(
                    "Feedback has already been submitted for this booking");
        }

        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException(
                    "Rating must be between 1 and 5");
        }

        Feedback feedback = new Feedback();
        feedback.setBooking(booking);
        feedback.setCustomer(customer);
        feedback.setRating(rating);
        feedback.setComment(comment);

        return feedbackRepository.save(feedback);
    }

    public Optional<Feedback> getFeedbackByBookingId(Long bookingId) {
        return feedbackRepository.findByBookingId(bookingId);
    }

    public List<Feedback> getCustomerFeedback(Long customerId) {
        return feedbackRepository.findByCustomerId(customerId);
    }
}
