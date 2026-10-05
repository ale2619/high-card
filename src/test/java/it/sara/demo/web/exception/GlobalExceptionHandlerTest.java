package it.sara.demo.web.exception;

import it.sara.demo.exception.GenericException;
import it.sara.demo.web.response.GenericResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @Mock
    private MethodArgumentNotValidException validationException;

    @Mock
    private BindingResult bindingResult;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    // --- HTTP status mirrors the semantic error code ---

    @Test
    void handleGenericException_httpStatusMatchesExceptionCode() {
        ResponseEntity<GenericResponse> response = handler.handleGenericException(new GenericException(404, "Not found"));
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void handleValidation_returnsHttp400() {
        when(validationException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(
                List.of(new FieldError("obj", "email", "Invalid email address"))
        );
        assertEquals(400, handler.handleValidation(validationException).getStatusCode().value());
    }

    @Test
    void handleUnexpected_returnsHttp500() {
        assertEquals(500, handler.handleUnexpected(new RuntimeException("boom")).getStatusCode().value());
    }

    // --- status code in body reflects the semantic error ---

    @Test
    void handleGenericException_statusCodeFromException() {
        ResponseEntity<GenericResponse> response = handler.handleGenericException(new GenericException(404, "User not found"));
        assertEquals(404, response.getBody().getStatus().getCode());
    }

    @Test
    void handleGenericException_messageFromException() {
        ResponseEntity<GenericResponse> response = handler.handleGenericException(new GenericException(409, "Duplicate email"));
        assertEquals("Duplicate email", response.getBody().getStatus().getMessage());
    }

    @Test
    void handleValidation_statusCode400InBody() {
        when(validationException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(
                List.of(new FieldError("obj", "email", "Invalid email address"))
        );
        assertEquals(400, handler.handleValidation(validationException).getBody().getStatus().getCode());
    }

    @Test
    void handleValidation_aggregatesMultipleFieldErrors() {
        when(validationException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(
                new FieldError("obj", "email",       "Invalid email address"),
                new FieldError("obj", "phoneNumber", "Invalid Italian phone number")
        ));
        String message = handler.handleValidation(validationException).getBody().getStatus().getMessage();
        assertThat(message).contains("Invalid email address", "Invalid Italian phone number");
    }

    @Test
    void handleUnexpected_statusCode500InBody() {
        assertEquals(500, handler.handleUnexpected(new RuntimeException("DB timeout")).getBody().getStatus().getCode());
    }

    @Test
    void handleUnexpected_messageFromException() {
        ResponseEntity<GenericResponse> response = handler.handleUnexpected(new RuntimeException("Something went wrong"));
        assertEquals("Something went wrong", response.getBody().getStatus().getMessage());
    }

    // --- traceId is always populated ---

    @Test
    void handleGenericException_traceIdPresent() {
        ResponseEntity<GenericResponse> response = handler.handleGenericException(new GenericException(500, "Error"));
        assertNotNull(response.getBody().getStatus().getTraceId());
    }
}