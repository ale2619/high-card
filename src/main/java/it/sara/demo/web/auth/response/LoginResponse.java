package it.sara.demo.web.auth.response;

import it.sara.demo.web.response.GenericResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class LoginResponse extends GenericResponse {
    private String token;
    private long expiresIn;
}