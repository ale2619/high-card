package it.sara.demo.web.exception;

import it.sara.demo.exception.GenericException;
import it.sara.demo.web.response.GenericResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Centralized exception handler for all controllers.
 * All responses return HTTP 200; errors are communicated via {@code StatusDTO.code} in the body.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles known application exceptions carrying an explicit status code and message.
     */
    @ExceptionHandler(GenericException.class)
    public ResponseEntity<GenericResponse> handleGenericException(GenericException e) {
        log.error("Application error [{}]: {}", e.getStatus().getCode(), e.getStatus().getMessage());
        return ResponseEntity.ok(GenericResponse.error(e.getStatus()));
    }

    /**
     * Handles bean validation failures ({@code @Valid} on request bodies).
     * Aggregates all field violation messages into a single response.
     * StatusDTO code 400 mirrors the HTTP Bad Request semantic in the body.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GenericResponse> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.error("Validation error: {}", message);
        return ResponseEntity.ok(GenericResponse.error(400, message));
    }

    /**
     * Catch-all handler for any unexpected exception.
     * StatusDTO code 500 mirrors the HTTP Internal Server Error semantic in the body.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<GenericResponse> handleUnexpected(Exception e) {
        log.error("Unexpected error: {}", e.getMessage(), e);
        return ResponseEntity.ok(GenericResponse.error(500, e.getMessage()));
    }
}