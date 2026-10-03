package it.sara.demo.exception;

import it.sara.demo.dto.StatusDTO;
import lombok.Getter;

import java.util.UUID;

@Getter
public class GenericException extends Exception {

    public static final StatusDTO GENERIC_ERROR = StatusDTO.builder()
            .code(500)
            .message("Generic error")
            .build();

    private final StatusDTO status;

    public GenericException(StatusDTO status) {
        this.status = status;
    }

    public GenericException(int code, String message) {
        this.status = StatusDTO.builder()
                .code(code)
                .message(message)
                .traceId(UUID.randomUUID().toString())
                .build();
    }
}