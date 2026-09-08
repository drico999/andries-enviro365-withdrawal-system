package com.enviro.assessment.junior.andries.config;

import com.enviro.assessment.junior.andries.model.Investor;
import com.enviro.assessment.junior.andries.model.Product;
import com.enviro.assessment.junior.andries.model.ProductType;
import com.enviro.assessment.junior.andries.repository.InvestorRepository;
import com.enviro.assessment.junior.andries.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Populates the in-memory H2 database with a handful of demo investors on
 * every startup, deliberately covering both sides of the age-65 retirement
 * rule so the business logic is easy to demonstrate end to end:
 *   - Thandiwe Nkosi (70) -> may withdraw from her retirement annuity
 *   - Sipho Dlamini (42)  -> may NOT withdraw from his retirement annuity
 *   - Amara van Wyk (65)  -> exactly on the boundary (must still be declined,
 *                            since the rule is "age > 65", not "age >= 65")
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final InvestorRepository investorRepository;
    private final ProductRepository productRepository;

    public DataSeeder(InvestorRepository investorRepository, ProductRepository productRepository) {
        this.investorRepository = investorRepository;
        this.productRepository = productRepository;
    }

    @Override
    public void run(String... args) {
        Investor thandiwe = investorRepository.save(
                new Investor("Thandiwe Nkosi", "thandiwe.nkosi@example.co.za", LocalDate.now().minusYears(70)));
        Investor sipho = investorRepository.save(
                new Investor("Sipho Dlamini", "sipho.dlamini@example.co.za", LocalDate.now().minusYears(42)));
        Investor amara = investorRepository.save(
                new Investor("Amara van Wyk", "amara.vanwyk@example.co.za", LocalDate.now().minusYears(65)));

        productRepository.save(new Product(thandiwe, "Enviro365 Retirement Annuity", ProductType.RETIREMENT_ANNUITY, new BigDecimal("850000.00")));
        productRepository.save(new Product(thandiwe, "Enviro365 Unit Trust - Balanced Fund", ProductType.UNIT_TRUST, new BigDecimal("120500.50")));

        productRepository.save(new Product(sipho, "Enviro365 Retirement Annuity", ProductType.RETIREMENT_ANNUITY, new BigDecimal("310000.00")));
        productRepository.save(new Product(sipho, "Enviro365 Tax-Free Savings Account", ProductType.TAX_FREE_SAVINGS, new BigDecimal("36000.00")));
        productRepository.save(new Product(sipho, "Enviro365 Money Market Fund", ProductType.MONEY_MARKET, new BigDecimal("58250.75")));

        productRepository.save(new Product(amara, "Enviro365 Retirement Annuity", ProductType.RETIREMENT_ANNUITY, new BigDecimal("640000.00")));
        productRepository.save(new Product(amara, "Enviro365 Unit Trust - Growth Fund", ProductType.UNIT_TRUST, new BigDecimal("95000.00")));
    }
}
