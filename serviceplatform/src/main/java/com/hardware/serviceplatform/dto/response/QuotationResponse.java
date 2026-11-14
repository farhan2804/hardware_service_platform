
package com.hardware.serviceplatform.dto.response;

import com.hardware.serviceplatform.enums.QuotationStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class QuotationResponse {

    private Long quotationId;
    private Long bookingId;
    private BigDecimal labourCost;
    private BigDecimal partsCost;
    private BigDecimal additionalCharges;
    private BigDecimal totalAmount;
    private QuotationStatus status;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime respondedAt;

    public QuotationResponse() {
    }

    public QuotationResponse(
            Long quotationId,
            Long bookingId,
            BigDecimal labourCost,
            BigDecimal partsCost,
            BigDecimal additionalCharges,
            BigDecimal totalAmount,
            QuotationStatus status,
            String remarks,
            LocalDateTime createdAt,
            LocalDateTime respondedAt) {

        this.quotationId = quotationId;
        this.bookingId = bookingId;
        this.labourCost = labourCost;
        this.partsCost = partsCost;
        this.additionalCharges = additionalCharges;
        this.totalAmount = totalAmount;
        this.status = status;
        this.remarks = remarks;
        this.createdAt = createdAt;
        this.respondedAt = respondedAt;
    }

    public Long getQuotationId() {
        return quotationId;
    }

    public void setQuotationId(Long quotationId) {
        this.quotationId = quotationId;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public BigDecimal getLabourCost() {
        return labourCost;
    }

    public void setLabourCost(BigDecimal labourCost) {
        this.labourCost = labourCost;
    }

    public BigDecimal getPartsCost() {
        return partsCost;
    }

    public void setPartsCost(BigDecimal partsCost) {
        this.partsCost = partsCost;
    }

    public BigDecimal getAdditionalCharges() {
        return additionalCharges;
    }

    public void setAdditionalCharges(BigDecimal additionalCharges) {
        this.additionalCharges = additionalCharges;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public QuotationStatus getStatus() {
        return status;
    }

    public void setStatus(QuotationStatus status) {
        this.status = status;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getRespondedAt() {
        return respondedAt;
    }

    public void setRespondedAt(LocalDateTime respondedAt) {
        this.respondedAt = respondedAt;
    }
}
