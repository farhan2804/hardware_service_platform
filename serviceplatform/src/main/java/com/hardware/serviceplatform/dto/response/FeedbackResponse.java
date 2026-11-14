
package com.hardware.serviceplatform.dto.response;

import java.time.LocalDateTime;

public class FeedbackResponse {

    private Long feedbackId;
    private Long bookingId;
    private String customerName;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;

    public FeedbackResponse() {
    }

    public FeedbackResponse(
            Long feedbackId,
            Long bookingId,
            String customerName,
            Integer rating,
            String comment,
            LocalDateTime createdAt) {

        this.feedbackId = feedbackId;
        this.bookingId = bookingId;
        this.customerName = customerName;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    public Long getFeedbackId() {
        return feedbackId;
    }

    public void setFeedbackId(Long feedbackId) {
        this.feedbackId = feedbackId;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
