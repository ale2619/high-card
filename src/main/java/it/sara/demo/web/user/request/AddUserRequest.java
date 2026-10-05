package it.sara.demo.web.user.request;

import io.swagger.v3.oas.annotations.media.Schema;
import it.sara.demo.web.request.GenericRequest;
import it.sara.demo.web.user.validation.ValidEmail;
import it.sara.demo.web.user.validation.ValidItalianPhoneNumber;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Payload for creating a new user — requires ADMIN role")
public class AddUserRequest extends GenericRequest {

    @NotBlank(message = "First name is required")
    @Schema(description = "User's first name", example = "Mario", requiredMode = Schema.RequiredMode.REQUIRED)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Schema(description = "User's last name", example = "Rossi", requiredMode = Schema.RequiredMode.REQUIRED)
    private String lastName;

    @ValidEmail
    @Schema(description = "Valid email address (format: local@domain.tld)", example = "mario.rossi@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @ValidItalianPhoneNumber
    @Schema(description = "Italian phone number — mobile (3XXXXXXXXX / +393XXXXXXXXX) or landline (+390XXXXXXXXX)", example = "+393331234567", requiredMode = Schema.RequiredMode.REQUIRED)
    private String phoneNumber;
}