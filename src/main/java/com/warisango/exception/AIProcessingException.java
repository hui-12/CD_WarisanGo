package com.warisango.exception;

/**
 * Represents an error that occurs during AI heritage discovery processing.
 */
public class AIProcessingException extends RuntimeException {

    /**
     * Creates an AI processing exception with a message.
     *
     * @param message error message
     */
    public AIProcessingException(String message) {
        super(message);
    }

    /**
     * Creates an AI processing exception with a message and cause.
     *
     * @param message error message
     * @param cause original exception
     */
    public AIProcessingException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }
}