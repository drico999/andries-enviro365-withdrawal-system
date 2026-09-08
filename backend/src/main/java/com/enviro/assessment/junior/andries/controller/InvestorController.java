package com.enviro.assessment.junior.andries.controller;

import com.enviro.assessment.junior.andries.dto.InvestorSummaryDTO;
import com.enviro.assessment.junior.andries.dto.PortfolioResponseDTO;
import com.enviro.assessment.junior.andries.service.InvestorService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/investors")
public class InvestorController {

    private final InvestorService investorService;

    public InvestorController(InvestorService investorService) {
        this.investorService = investorService;
    }

    @GetMapping
    public List<InvestorSummaryDTO> getAllInvestors() {
        return investorService.getAllInvestors();
    }

    @GetMapping("/{id}/portfolio")
    public PortfolioResponseDTO getPortfolio(@PathVariable Long id) {
        return investorService.getPortfolio(id);
    }
}
