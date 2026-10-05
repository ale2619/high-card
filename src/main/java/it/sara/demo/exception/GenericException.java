package it.sara.demo.exception;

import it.sara.demo.dto.StatusDTO;
import lombok.Getter;

import java.util.UUID;

@Getter
public class GenericException extends Exception {

    private final StatusDTO status;

    /**
     * Constructs a GenericException with a pre-built {@link StatusDTO}.
     * The status object should not be a shared static instance — construct a fresh one per throw site
     * to ensure each exception carries its own unique {@code traceId}.
     */

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