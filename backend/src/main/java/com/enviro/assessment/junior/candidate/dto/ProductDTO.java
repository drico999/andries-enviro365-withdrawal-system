package com.enviro.assessment.junior.candidate.dto;

import com.enviro.assessment.junior.candidate.model.Product;

import java.math.BigDecimal;

/** Read-only view of a Product returned to the frontend - keeps JPA entities off the wire. */
public class ProductDTO {
    private Long id;
    private String productName;
    private String productType;
    private boolean retirementProduct;
    private BigDecimal balance;
    private BigDecimal maxWithdrawable; // 90% of balance, pre-calculated for the UI

    public ProductDTO(Product product) {
        this.id = product.getId();
        this.productName = product.getProductName();
        this.productType = product.getProductType().name();
        this.retirementProduct = product.getProductType().isRetirementProduct();
        this.balance = product.getBalance();
        this.maxWithdrawable = product.getBalance().multiply(BigDecimal.valueOf(0.90));
    }

    public Long getId() {
        return id;
    }

    public String getProductName() {
        return productName;
    }

    public String getProductType() {
        return productType;
    }

    public boolean isRetirementProduct() {
        return retirementProduct;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public BigDecimal getMaxWithdrawable() {
        return maxWithdrawable;
    }
}
