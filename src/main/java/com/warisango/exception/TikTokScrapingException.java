package com.warisango.exception;

public class TikTokScrapingException extends RuntimeException {

    public TikTokScrapingException(String message) {
        super(message);
    }

    public TikTokScrapingException(String message, Throwable cause) {
        super(message, cause);
    }
}
