package com.enviro.assessment.junior.candidate.repository;

import com.enviro.assessment.junior.candidate.model.WithdrawalNotice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface WithdrawalNoticeRepository extends JpaRepository<WithdrawalNotice, Long> {

    List<WithdrawalNotice> findByInvestorIdOrderByRequestedAtDesc(Long investorId);

    List<WithdrawalNotice> findByInvestorIdAndProductIdOrderByRequestedAtDesc(Long investorId, Long productId);

    List<WithdrawalNotice> findByInvestorIdAndRequestedAtBetweenOrderByRequestedAtDesc(
            Long investorId, LocalDateTime start, LocalDateTime end);

    List<WithdrawalNotice> findByInvestorIdAndProductIdAndRequestedAtBetweenOrderByRequestedAtDesc(
            Long investorId, Long productId, LocalDateTime start, LocalDateTime end);
}
