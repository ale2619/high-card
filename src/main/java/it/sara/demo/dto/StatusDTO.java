package it.sara.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Outcome descriptor included in every response body. HTTP transport is always 200; the semantic result is carried here.")
public class StatusDTO {

    @Schema(description = "Semantic HTTP-equivalent status code: 200 = success, 400 = bad request, 401 = unauthorized, 404 = not found, 500 = internal error", example = "200")
    private int code;

    @Schema(description = "Human-readable outcome message", example = "User added.")
    private String message;

    @Schema(description = "UUID identifying this specific response instance, useful for log correlation", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private String traceId;
}