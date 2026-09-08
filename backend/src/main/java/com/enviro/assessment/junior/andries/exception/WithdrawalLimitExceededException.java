package com.enviro.assessment.junior.andries.exception;

/** Thrown when a withdrawal amount exceeds 90% of the product's balance. */
public class WithdrawalLimitExceededException extends RuntimeException {
    public WithdrawalLimitExceededException(String message) {
        super(message);
    }
}
