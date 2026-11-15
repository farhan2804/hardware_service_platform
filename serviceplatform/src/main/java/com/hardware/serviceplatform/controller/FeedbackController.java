
package com.hardware.serviceplatform.controller;

import com.hardware.serviceplatform.entity.Feedback;
import com.hardware.serviceplatform.entity.User;
import com.hardware.serviceplatform.service.FeedbackService;
import com.hardware.serviceplatform.service.UserService;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    private final FeedbackService feedbackService;
    private final UserService userService;

    public FeedbackController(
            FeedbackService feedbackService,
            UserService userService) {
        this.feedbackService = feedbackService;
        this.userService = userService;
    }

    @PostMapping("/bookings/{bookingId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public FeedbackResponse submitFeedback(
            @PathVariable Long bookingId,
            @RequestBody FeedbackRequest request,
            Authentication authentication) {

        User customer = getAuthenticatedUser(authentication);

        Feedback feedback = feedbackService.submitFeedback(
                bookingId,
                customer.getId(),
                request.rating(),
                request.comment()
        );

        return mapToResponse(feedback);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    public List<FeedbackResponse> getMyFeedback(
            Authentication authentication) {

        User customer = getAuthenticatedUser(authentication);

        return feedbackService.getCustomerFeedback(customer.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private User getAuthenticatedUser(Authentication authentication) {
        return userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));
    }

    private FeedbackResponse mapToResponse(Feedback feedback) {
        return new FeedbackResponse(
                feedback.getId(),
                feedback.getBooking().getId(),
                feedback.getRating(),
                feedback.getComment(),
                feedback.getCreatedAt()
        );
    }

    public record FeedbackRequest(
            Integer rating,
            String comment
    ) {}

    public record FeedbackResponse(
            Long feedbackId,
            Long bookingId,
            Integer rating,
            String comment,
            LocalDateTime createdAt
    ) {}
}
