package com.enviro.assessment.junior.candidate.dto;

import com.enviro.assessment.junior.candidate.model.Investor;

/** Lightweight investor listing used to populate the investor selector in the UI. */
public class InvestorSummaryDTO {
    private Long id;
    private String fullName;
    private int age;

    public InvestorSummaryDTO(Investor investor) {
        this.id = investor.getId();
        this.fullName = investor.getFullName();
        this.age = investor.getAge();
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public int getAge() {
        return age;
    }
}
