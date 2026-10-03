package it.sara.demo.web.user.request;

import it.sara.demo.web.request.GenericRequest;
import it.sara.demo.web.user.validation.ValidEmail;
import it.sara.demo.web.user.validation.ValidItalianPhoneNumber;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddUserRequest extends GenericRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @ValidEmail
    private String email;

    @ValidItalianPhoneNumber
    private String phoneNumber;
}