package com.warisango.exception;

/**
 * Raised when user-generated review content contains a prohibited term.
 */
public class ProhibitedContentException extends RuntimeException {

    public ProhibitedContentException(String message) {
        super(message);
    }
}
