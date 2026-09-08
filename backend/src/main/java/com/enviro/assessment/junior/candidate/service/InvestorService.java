package com.enviro.assessment.junior.candidate.service;

import com.enviro.assessment.junior.candidate.dto.InvestorSummaryDTO;
import com.enviro.assessment.junior.candidate.dto.PortfolioResponseDTO;
import com.enviro.assessment.junior.candidate.exception.ResourceNotFoundException;
import com.enviro.assessment.junior.candidate.model.Investor;
import com.enviro.assessment.junior.candidate.repository.InvestorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InvestorService {

    private final InvestorRepository investorRepository;

    public InvestorService(InvestorRepository investorRepository) {
        this.investorRepository = investorRepository;
    }

    public List<InvestorSummaryDTO> getAllInvestors() {
        return investorRepository.findAll().stream()
                .map(InvestorSummaryDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Read-only transaction as defense-in-depth. Investor.products is EAGER
     * (see its field comment) so this isn't strictly required for correctness
     * today, but keeps this method safe if that ever changes back.
     */
    @Transactional(readOnly = true)
    public PortfolioResponseDTO getPortfolio(Long investorId) {
        Investor investor = findInvestorOrThrow(investorId);
        return new PortfolioResponseDTO(investor);
    }

    public Investor findInvestorOrThrow(Long investorId) {
        return investorRepository.findById(investorId)
                .orElseThrow(() -> new ResourceNotFoundException("No investor found with id " + investorId));
    }
}
