package com.warisango.exception;

import com.warisango.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException exception) {

        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("Request validation failed.");

        return ResponseEntity.badRequest().body(new ErrorResponse(message));
    }

    @ExceptionHandler(AIProcessingException.class)
    public ResponseEntity<ErrorResponse> handleAIProcessing(
            AIProcessingException exception) {

        logger.warn("AI processing request failed: {}", exception.getMessage());
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(exception.getMessage()));
    }

    @ExceptionHandler(FirebasePersistenceException.class)
    public ResponseEntity<ErrorResponse> handleFirebasePersistence(
            FirebasePersistenceException exception) {

        logger.error("Firestore persistence failed.", exception);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorResponse("Unable to access application data."));
    }

    @ExceptionHandler(TikTokScrapingException.class)
    public ResponseEntity<ErrorResponse> handleTikTokScraping(
            TikTokScrapingException exception) {

        logger.warn("TikTok scraping request failed: {}", exception.getMessage(), exception);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorResponse(exception.getMessage()));
    }

    @ExceptionHandler(BusinessNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleBusinessNotFound(
            BusinessNotFoundException exception) {

        logger.warn("Heritage business lookup failed: {}", exception.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(exception.getMessage()));
    }

    @ExceptionHandler(DiscoveryJobNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleDiscoveryJobNotFound(
            DiscoveryJobNotFoundException exception) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(exception.getMessage()));
    }

    @ExceptionHandler(RoleAccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleRoleAccessDenied(
            RoleAccessDeniedException exception) {

        logger.warn("Login role rejected: {}", exception.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse(exception.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException exception) {
        logger.warn("Authorization rejected: {}", exception.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse(exception.getMessage()));
    }

    @ExceptionHandler(BusinessReportException.class)
    public ResponseEntity<ErrorResponse> handleBusinessReport(
            BusinessReportException exception) {
        logger.error("Business report operation failed.", exception);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorResponse(exception.getMessage()));
    }

    @ExceptionHandler(SavedListingException.class)
    public ResponseEntity<ErrorResponse> handleSavedListing(
            SavedListingException exception) {
        logger.error("Saved-listing operation failed.", exception);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorResponse(exception.getMessage()));
    }

    @ExceptionHandler(ProhibitedContentException.class)
    public ResponseEntity<ErrorResponse> handleProhibitedContent(
            ProhibitedContentException exception) {
        logger.warn("User-generated content was rejected by moderation.");
        return ResponseEntity.unprocessableContent()
                .body(new ErrorResponse(exception.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleInvalidArgument(IllegalArgumentException exception) {
        logger.warn("Request rejected: {}", exception.getMessage());
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(exception.getMessage()));
    }

    @ExceptionHandler(OperationConflictException.class)
    public ResponseEntity<ErrorResponse> handleOperationConflict(OperationConflictException exception) {
        logger.warn("Operation could not be completed in the current state: {}", exception.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(exception.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception exception) {
        logger.error("Unexpected application error.", exception);
        return ResponseEntity.internalServerError()
                .body(new ErrorResponse("An unexpected server error occurred."));
    }
}
