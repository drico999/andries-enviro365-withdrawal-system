package com.enviro.assessment.junior.candidate.repository;

import com.enviro.assessment.junior.candidate.model.Investor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvestorRepository extends JpaRepository<Investor, Long> {
}
