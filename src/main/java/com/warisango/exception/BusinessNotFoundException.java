package com.warisango.exception;

public class BusinessNotFoundException extends RuntimeException {

    public BusinessNotFoundException(String businessId) {
        super("Pending heritage business not found: " + businessId);
    }
}
