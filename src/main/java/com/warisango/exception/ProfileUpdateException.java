package com.warisango.exception;

public class ProfileUpdateException extends RuntimeException {

    private final String field;
    private final Long remainingDays;

    public ProfileUpdateException(String field, String message) {
        this(field, message, null);
    }

    public ProfileUpdateException(String field, String message, Long remainingDays) {
        super(message);
        this.field = field;
        this.remainingDays = remainingDays;
    }

    public String getField() {
        return field;
    }

    public Long getRemainingDays() {
        return remainingDays;
    }
}
