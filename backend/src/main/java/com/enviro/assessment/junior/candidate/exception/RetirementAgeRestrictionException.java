package com.enviro.assessment.junior.candidate.exception;

/** Thrown when a withdrawal is requested from a retirement product by an investor aged 65 or younger. */
public class RetirementAgeRestrictionException extends RuntimeException {
    public RetirementAgeRestrictionException(String message) {
        super(message);
    }
}
