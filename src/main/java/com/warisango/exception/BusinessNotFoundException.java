package com.warisango.exception;

public class BusinessNotFoundException extends RuntimeException {

    public BusinessNotFoundException(String businessId) {
        super("Heritage business not found: " + businessId);
    }
}
