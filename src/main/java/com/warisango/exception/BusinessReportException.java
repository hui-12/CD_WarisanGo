package com.warisango.exception;

public class BusinessReportException extends RuntimeException {
    public BusinessReportException(String message, Throwable cause) {
        super(message, cause);
    }
}
