package it.sara.demo.web.auth.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Credentials for obtaining a JWT token")
public class LoginRequest {

    @NotBlank(message = "Username is required")
    @Schema(description = "Account username", example = "admin")
    private String username;

    @NotBlank(message = "Password is required")
    @Schema(description = "Account password", example = "admin123")
    private String password;
}