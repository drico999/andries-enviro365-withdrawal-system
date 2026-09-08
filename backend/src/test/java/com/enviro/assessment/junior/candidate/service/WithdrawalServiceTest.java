package com.enviro.assessment.junior.candidate.service;

import com.enviro.assessment.junior.candidate.dto.WithdrawalRequestDTO;
import com.enviro.assessment.junior.candidate.dto.WithdrawalResponseDTO;
import com.enviro.assessment.junior.candidate.exception.InsufficientBalanceException;
import com.enviro.assessment.junior.candidate.exception.ResourceNotFoundException;
import com.enviro.assessment.junior.candidate.exception.RetirementAgeRestrictionException;
import com.enviro.assessment.junior.candidate.exception.WithdrawalLimitExceededException;
import com.enviro.assessment.junior.candidate.model.Investor;
import com.enviro.assessment.junior.candidate.model.Product;
import com.enviro.assessment.junior.candidate.model.ProductType;
import com.enviro.assessment.junior.candidate.repository.ProductRepository;
import com.enviro.assessment.junior.candidate.repository.WithdrawalNoticeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the three Enviro365 withdrawal business rules:
 *   1. Retirement product withdrawals require age > 65.
 *   2. A withdrawal may never exceed the product's balance.
 *   3. A withdrawal may never exceed 90% of the product's balance.
 * Repositories are mocked so these tests run in isolation, without a database.
 */
@ExtendWith(MockitoExtension.class)
class WithdrawalServiceTest {

    @Mock
    private InvestorService investorService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private WithdrawalNoticeRepository withdrawalNoticeRepository;

    @InjectMocks
    private WithdrawalService withdrawalService;

    private Investor youngInvestor;   // age 42 - not eligible for retirement withdrawals
    private Investor retiredInvestor; // age 70 - eligible for retirement withdrawals
    private Product retirementProduct;
    private Product unitTrustProduct;

    @BeforeEach
    void setUp() throws Exception {
        youngInvestor = newInvestor(1L, "Sipho Dlamini", 42);
        retiredInvestor = newInvestor(2L, "Thandiwe Nkosi", 70);

        retirementProduct = new Product(youngInvestor, "Retirement Annuity", ProductType.RETIREMENT_ANNUITY, new BigDecimal("100000.00"));
        setId(retirementProduct, 10L);

        unitTrustProduct = new Product(youngInvestor, "Unit Trust", ProductType.UNIT_TRUST, new BigDecimal("50000.00"));
        setId(unitTrustProduct, 11L);
    }

    @Test
    void retirementWithdrawal_declinedWhenInvestorIs65OrYounger() {
        when(investorService.findInvestorOrThrow(youngInvestor.getId())).thenReturn(youngInvestor);
        when(productRepository.findById(retirementProduct.getId())).thenReturn(Optional.of(retirementProduct));

        WithdrawalRequestDTO request = request(youngInvestor.getId(), retirementProduct.getId(), "1000.00");

        assertThatThrownBy(() -> withdrawalService.createWithdrawal(request))
                .isInstanceOf(RetirementAgeRestrictionException.class)
                .hasMessageContaining("older than 65");
    }

    @Test
    void retirementWithdrawal_allowedWhenInvestorOlderThan65() {
        retirementProduct = new Product(retiredInvestor, "Retirement Annuity", ProductType.RETIREMENT_ANNUITY, new BigDecimal("100000.00"));
        setIdQuietly(retirementProduct, 10L);

        when(investorService.findInvestorOrThrow(retiredInvestor.getId())).thenReturn(retiredInvestor);
        when(productRepository.findById(retirementProduct.getId())).thenReturn(Optional.of(retirementProduct));
        when(withdrawalNoticeRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        WithdrawalRequestDTO request = request(retiredInvestor.getId(), retirementProduct.getId(), "5000.00");

        WithdrawalResponseDTO response = withdrawalService.createWithdrawal(request);

        assertThat(response.getStatus()).isEqualTo("APPROVED");
        assertThat(response.getBalanceAfter()).isEqualByComparingTo("95000.00");
        verify(productRepository).save(retirementProduct);
    }

    @Test
    void withdrawal_declinedWhenAmountExceedsBalance() {
        when(investorService.findInvestorOrThrow(youngInvestor.getId())).thenReturn(youngInvestor);
        when(productRepository.findById(unitTrustProduct.getId())).thenReturn(Optional.of(unitTrustProduct));

        WithdrawalRequestDTO request = request(youngInvestor.getId(), unitTrustProduct.getId(), "60000.00");

        assertThatThrownBy(() -> withdrawalService.createWithdrawal(request))
                .isInstanceOf(InsufficientBalanceException.class)
                .hasMessageContaining("exceeds available balance");
    }

    @Test
    void withdrawal_declinedWhenAmountExceeds90PercentOfBalance() {
        when(investorService.findInvestorOrThrow(youngInvestor.getId())).thenReturn(youngInvestor);
        when(productRepository.findById(unitTrustProduct.getId())).thenReturn(Optional.of(unitTrustProduct));

        // 46000 is under the 50000 balance but over the 45000 (90%) limit
        WithdrawalRequestDTO request = request(youngInvestor.getId(), unitTrustProduct.getId(), "46000.00");

        assertThatThrownBy(() -> withdrawalService.createWithdrawal(request))
                .isInstanceOf(WithdrawalLimitExceededException.class)
                .hasMessageContaining("90% withdrawal limit");
    }

    @Test
    void withdrawal_approvedAndBalanceUpdated_whenWithinLimits() {
        when(investorService.findInvestorOrThrow(youngInvestor.getId())).thenReturn(youngInvestor);
        when(productRepository.findById(unitTrustProduct.getId())).thenReturn(Optional.of(unitTrustProduct));
        when(withdrawalNoticeRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        WithdrawalRequestDTO request = request(youngInvestor.getId(), unitTrustProduct.getId(), "10000.00");

        WithdrawalResponseDTO response = withdrawalService.createWithdrawal(request);

        assertThat(response.getStatus()).isEqualTo("APPROVED");
        assertThat(response.getBalanceBefore()).isEqualByComparingTo("50000.00");
        assertThat(response.getBalanceAfter()).isEqualByComparingTo("40000.00");
        assertThat(unitTrustProduct.getBalance()).isEqualByComparingTo("40000.00");
    }

    @Test
    void withdrawal_declinedWhenProductDoesNotExist() {
        when(investorService.findInvestorOrThrow(youngInvestor.getId())).thenReturn(youngInvestor);
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        WithdrawalRequestDTO request = request(youngInvestor.getId(), 999L, "100.00");

        assertThatThrownBy(() -> withdrawalService.createWithdrawal(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private WithdrawalRequestDTO request(Long investorId, Long productId, String amount) {
        WithdrawalRequestDTO dto = new WithdrawalRequestDTO();
        dto.setInvestorId(investorId);
        dto.setProductId(productId);
        dto.setAmount(new BigDecimal(amount));
        return dto;
    }

    private Investor newInvestor(Long id, String name, int age) throws Exception {
        Investor investor = new Investor(name, name.toLowerCase().replace(" ", ".") + "@example.co.za",
                LocalDate.now().minusYears(age));
        setId(investor, id);
        return investor;
    }

    /** Test-only helper: entities use JPA-managed identity ids with no public setter, so reflection sets them for fixtures. */
    private void setId(Object entity, Long id) throws Exception {
        Field field = entity.getClass().getDeclaredField("id");
        field.setAccessible(true);
        field.set(entity, id);
    }

    private void setIdQuietly(Object entity, Long id) {
        try {
            setId(entity, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
