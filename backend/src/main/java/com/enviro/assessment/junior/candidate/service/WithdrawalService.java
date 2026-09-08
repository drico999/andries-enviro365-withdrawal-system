package com.enviro.assessment.junior.candidate.service;

import com.enviro.assessment.junior.candidate.dto.WithdrawalRequestDTO;
import com.enviro.assessment.junior.candidate.dto.WithdrawalResponseDTO;
import com.enviro.assessment.junior.candidate.exception.InsufficientBalanceException;
import com.enviro.assessment.junior.candidate.exception.ResourceNotFoundException;
import com.enviro.assessment.junior.candidate.exception.RetirementAgeRestrictionException;
import com.enviro.assessment.junior.candidate.exception.WithdrawalLimitExceededException;
import com.enviro.assessment.junior.candidate.model.Investor;
import com.enviro.assessment.junior.candidate.model.Product;
import com.enviro.assessment.junior.candidate.model.WithdrawalNotice;
import com.enviro.assessment.junior.candidate.model.WithdrawalStatus;
import com.enviro.assessment.junior.candidate.repository.ProductRepository;
import com.enviro.assessment.junior.candidate.repository.WithdrawalNoticeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Owns the withdrawal-notice business rules for Enviro365 Investments:
 *   1. Retirement product withdrawals require the investor to be older than 65.
 *   2. A withdrawal may never exceed the product's current balance.
 *   3. A withdrawal may never exceed 90% of the product's current balance
 *      (a buffer the business keeps against every product, retirement or not).
 * All three are enforced here rather than in the controller so the rules stay
 * in one place and are reusable/testable independently of HTTP.
 */
@Service
public class WithdrawalService {

    private static final BigDecimal MAX_WITHDRAWAL_RATIO = BigDecimal.valueOf(0.90);
    private static final int RETIREMENT_MINIMUM_AGE = 65;

    private final InvestorService investorService;
    private final ProductRepository productRepository;
    private final WithdrawalNoticeRepository withdrawalNoticeRepository;

    public WithdrawalService(InvestorService investorService,
                              ProductRepository productRepository,
                              WithdrawalNoticeRepository withdrawalNoticeRepository) {
        this.investorService = investorService;
        this.productRepository = productRepository;
        this.withdrawalNoticeRepository = withdrawalNoticeRepository;
    }

    @Transactional
    public WithdrawalResponseDTO createWithdrawal(WithdrawalRequestDTO request) {
        Investor investor = investorService.findInvestorOrThrow(request.getInvestorId());

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("No product found with id " + request.getProductId()));

        if (!product.getInvestor().getId().equals(investor.getId())) {
            throw new ResourceNotFoundException(
                    "Product " + product.getId() + " does not belong to investor " + investor.getId());
        }

        BigDecimal amount = request.getAmount();
        BigDecimal balance = product.getBalance();

        // Rule 1: retirement withdrawals only allowed if age > 65
        if (product.getProductType().isRetirementProduct() && investor.getAge() <= RETIREMENT_MINIMUM_AGE) {
            throw new RetirementAgeRestrictionException(
                    "Withdrawal declined: retirement product withdrawals require the investor to be older than "
                            + RETIREMENT_MINIMUM_AGE + " (current age: " + investor.getAge() + ").");
        }

        // Rule 2: withdrawal must not exceed balance
        if (amount.compareTo(balance) > 0) {
            throw new InsufficientBalanceException(
                    "Withdrawal declined: amount (" + format(amount) + ") exceeds available balance (" + format(balance) + ").");
        }

        // Rule 3: withdrawal must not exceed 90% of balance
        BigDecimal maxAllowed = balance.multiply(MAX_WITHDRAWAL_RATIO).setScale(2, RoundingMode.HALF_UP);
        if (amount.compareTo(maxAllowed) > 0) {
            throw new WithdrawalLimitExceededException(
                    "Withdrawal declined: amount (" + format(amount) + ") exceeds the 90% withdrawal limit ("
                            + format(maxAllowed) + ") for this product.");
        }

        BigDecimal balanceAfter = balance.subtract(amount).setScale(2, RoundingMode.HALF_UP);
        product.setBalance(balanceAfter);
        productRepository.save(product);

        WithdrawalNotice notice = new WithdrawalNotice(
                investor, product, amount, balance, balanceAfter, WithdrawalStatus.APPROVED, LocalDateTime.now());
        notice = withdrawalNoticeRepository.save(notice);

        return new WithdrawalResponseDTO(notice);
    }

    @Transactional(readOnly = true)
    public List<WithdrawalResponseDTO> getHistory(Long investorId, Long productId) {
        investorService.findInvestorOrThrow(investorId);
        List<WithdrawalNotice> notices = (productId != null)
                ? withdrawalNoticeRepository.findByInvestorIdAndProductIdOrderByRequestedAtDesc(investorId, productId)
                : withdrawalNoticeRepository.findByInvestorIdOrderByRequestedAtDesc(investorId);
        return notices.stream().map(WithdrawalResponseDTO::new).collect(Collectors.toList());
    }

    /**
     * Builds a CSV statement of withdrawal notices for an investor, optionally
     * filtered by product and/or a requested-date range.
     */
    @Transactional(readOnly = true)
    public String exportCsv(Long investorId, Long productId, LocalDate startDate, LocalDate endDate) {
        investorService.findInvestorOrThrow(investorId);

        List<WithdrawalNotice> notices;
        if (startDate != null && endDate != null) {
            LocalDateTime start = startDate.atStartOfDay();
            LocalDateTime end = endDate.atTime(LocalTime.MAX);
            notices = (productId != null)
                    ? withdrawalNoticeRepository.findByInvestorIdAndProductIdAndRequestedAtBetweenOrderByRequestedAtDesc(investorId, productId, start, end)
                    : withdrawalNoticeRepository.findByInvestorIdAndRequestedAtBetweenOrderByRequestedAtDesc(investorId, start, end);
        } else {
            notices = (productId != null)
                    ? withdrawalNoticeRepository.findByInvestorIdAndProductIdOrderByRequestedAtDesc(investorId, productId)
                    : withdrawalNoticeRepository.findByInvestorIdOrderByRequestedAtDesc(investorId);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        StringBuilder csv = new StringBuilder();
        csv.append("Notice ID,Product,Amount,Balance Before,Balance After,Status,Requested At\n");
        for (WithdrawalNotice n : notices) {
            csv.append(n.getId()).append(',')
                    .append(escapeCsv(n.getProduct().getProductName())).append(',')
                    .append(n.getAmount()).append(',')
                    .append(n.getBalanceBefore()).append(',')
                    .append(n.getBalanceAfter()).append(',')
                    .append(n.getStatus()).append(',')
                    .append(n.getRequestedAt().format(formatter))
                    .append('\n');
        }
        return csv.toString();
    }

    private String escapeCsv(String value) {
        if (value.contains(",") || value.contains("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private String format(BigDecimal value) {
        return "R" + value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
