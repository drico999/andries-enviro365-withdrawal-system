package com.enviro.assessment.junior.candidate.exception;

/** Thrown when a withdrawal amount exceeds the product's available balance. */
public class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException(String message) {
        super(message);
    }
}
