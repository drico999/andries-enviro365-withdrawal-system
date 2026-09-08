package com.enviro.assessment.junior.candidate.exception;

/** Thrown when a requested investor, product, or withdrawal id does not exist. */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
