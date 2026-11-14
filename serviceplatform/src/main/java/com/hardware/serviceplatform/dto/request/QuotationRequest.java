
package com.hardware.serviceplatform.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class QuotationRequest {

    @NotNull(message = "Labour cost is required")
    @DecimalMin(value = "0.0", message = "Labour cost cannot be negative")
    private BigDecimal labourCost;

    @NotNull(message = "Parts cost is required")
    @DecimalMin(value = "0.0", message = "Parts cost cannot be negative")
    private BigDecimal partsCost;

    @DecimalMin(value = "0.0", message = "Additional charges cannot be negative")
    private BigDecimal additionalCharges = BigDecimal.ZERO;

    @Size(max = 1000, message = "Remarks cannot exceed 1000 characters")
    private String remarks;

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

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
