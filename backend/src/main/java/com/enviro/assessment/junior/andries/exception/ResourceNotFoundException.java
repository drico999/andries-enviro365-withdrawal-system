package com.enviro.assessment.junior.andries.exception;

/** Thrown when a requested investor, product, or withdrawal id does not exist. */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
