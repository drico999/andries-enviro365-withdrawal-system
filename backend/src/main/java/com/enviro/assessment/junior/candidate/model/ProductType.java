package com.enviro.assessment.junior.candidate.model;

/**
 * The type of investment product held by an investor.
 * RETIREMENT products are subject to the age-based withdrawal restriction
 * (investor must be older than 65).
 */
public enum ProductType {
    RETIREMENT_ANNUITY(true),
    UNIT_TRUST(false),
    MONEY_MARKET(false),
    TAX_FREE_SAVINGS(false);

    private final boolean retirementProduct;

    ProductType(boolean retirementProduct) {
        this.retirementProduct = retirementProduct;
    }

    public boolean isRetirementProduct() {
        return retirementProduct;
    }
}
