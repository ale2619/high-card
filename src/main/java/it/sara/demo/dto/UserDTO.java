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
@Schema(description = "User data transfer object returned by the API")
public class UserDTO {

    @Schema(description = "Unique identifier assigned at creation time (UUID)", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private String guid;

    @Schema(description = "User's first name", example = "Mario")
    private String firstName;

    @Schema(description = "User's last name", example = "Rossi")
    private String lastName;

    @Schema(description = "Email address — unique within the system", example = "mario.rossi@example.com")
    private String email;

    @Schema(description = "Italian phone number — mobile (3XXXXXXXXX / +393XXXXXXXXX) or landline (+390XXXXXXXXX)", example = "+393331234567")
    private String phoneNumber;
}