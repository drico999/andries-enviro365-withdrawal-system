package com.enviro.assessment.junior.candidate.dto;

import com.enviro.assessment.junior.candidate.model.Investor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/** Investor details plus the products in their portfolio - the payload for GET /api/investors/{id}/portfolio */
public class PortfolioResponseDTO {

    private Long investorId;
    private String fullName;
    private String email;
    private LocalDate dateOfBirth;
    private int age;
    private BigDecimal totalBalance;
    private List<ProductDTO> products;

    public PortfolioResponseDTO(Investor investor) {
        this.investorId = investor.getId();
        this.fullName = investor.getFullName();
        this.email = investor.getEmail();
        this.dateOfBirth = investor.getDateOfBirth();
        this.age = investor.getAge();
        this.products = investor.getProducts().stream().map(ProductDTO::new).collect(Collectors.toList());
        this.totalBalance = investor.getProducts().stream()
                .map(p -> p.getBalance())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Long getInvestorId() {
        return investorId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public int getAge() {
        return age;
    }

    public BigDecimal getTotalBalance() {
        return totalBalance;
    }

    public List<ProductDTO> getProducts() {
        return products;
    }
}
