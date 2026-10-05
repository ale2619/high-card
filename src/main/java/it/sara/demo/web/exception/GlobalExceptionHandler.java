package it.sara.demo.web.exception;

import it.sara.demo.exception.GenericException;
import it.sara.demo.web.response.GenericResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Centralized exception handler for all controllers.
 * HTTP status codes follow REST conventions: the code carried by the exception
 * (or the semantic of the error) is reflected in both the HTTP status and {@code StatusDTO.code}.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles known application exceptions carrying an explicit status code and message.
     * The HTTP status is derived from {@code StatusDTO.code}; unrecognised codes fall back to 500.
     */
    @ExceptionHandler(GenericException.class)
    public ResponseEntity<GenericResponse> handleGenericException(GenericException e) {
        log.error("Application error [{}]: {}", e.getStatus().getCode(), e.getStatus().getMessage());
        HttpStatus httpStatus = Objects.requireNonNullElse(
                HttpStatus.resolve(e.getStatus().getCode()),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
        return ResponseEntity.status(httpStatus).body(GenericResponse.error(e.getStatus()));
    }

    /**
     * Handles bean validation failures ({@code @Valid} on request bodies).
     * Returns {@code 400 Bad Request}; all field violation messages are aggregated into one.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GenericResponse> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.error("Validation error: {}", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(GenericResponse.error(400, message));
    }

    /**
     * Catch-all handler for any unexpected exception.
     * Returns {@code 500 Internal Server Error}.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<GenericResponse> handleUnexpected(Exception e) {
        log.error("Unexpected error: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.error(500, e.getMessage()));
    }
}