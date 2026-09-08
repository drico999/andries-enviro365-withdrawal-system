package com.enviro.assessment.junior.andries.repository;

import com.enviro.assessment.junior.andries.model.Investor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvestorRepository extends JpaRepository<Investor, Long> {
}
