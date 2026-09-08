package com.enviro.assessment.junior.andries.dto;

import java.time.LocalDateTime;
import java.util.List;

/** Uniform error shape returned by GlobalExceptionHandler for every failure case. */
public class ErrorResponseDTO {

    private LocalDateTime timestamp = LocalDateTime.now();
    private int status;
    private String error;
    private List<String> messages;
    private String path;

    public ErrorResponseDTO(int status, String error, List<String> messages, String path) {
        this.status = status;
        this.error = error;
        this.messages = messages;
        this.path = path;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public List<String> getMessages() {
        return messages;
    }

    public String getPath() {
        return path;
    }
}
