package com.enviro.assessment.junior.candidate.dto;

import com.enviro.assessment.junior.candidate.model.WithdrawalNotice;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Outgoing payload describing a persisted (i.e. approved) withdrawal notice. */
public class WithdrawalResponseDTO {

    private Long id;
    private Long investorId;
    private Long productId;
    private String productName;
    private BigDecimal amount;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private String status;
    private LocalDateTime requestedAt;

    public WithdrawalResponseDTO(WithdrawalNotice notice) {
        this.id = notice.getId();
        this.investorId = notice.getInvestor().getId();
        this.productId = notice.getProduct().getId();
        this.productName = notice.getProduct().getProductName();
        this.amount = notice.getAmount();
        this.balanceBefore = notice.getBalanceBefore();
        this.balanceAfter = notice.getBalanceAfter();
        this.status = notice.getStatus().name();
        this.requestedAt = notice.getRequestedAt();
    }

    public Long getId() {
        return id;
    }

    public Long getInvestorId() {
        return investorId;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getBalanceBefore() {
        return balanceBefore;
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }
}
