package it.sara.demo.web.response;

import it.sara.demo.dto.StatusDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class GenericResponse {

    private StatusDTO status;

    public static GenericResponse success(String message) {
        GenericResponse response = new GenericResponse();
        response.setStatus(StatusDTO.builder()
                .code(200)
                .message(message != null ? message : "Success")
                .traceId(UUID.randomUUID().toString())
                .build());
        return response;
    }

    public static GenericResponse error(String message) {
        GenericResponse response = new GenericResponse();
        response.setStatus(StatusDTO.builder()
                .code(200)
                .message(message)
                .traceId(UUID.randomUUID().toString())
                .build());
        return response;
    }

    public static GenericResponse error(StatusDTO status) {
        GenericResponse response = new GenericResponse();
        response.setStatus(status);
        return response;
    }
}