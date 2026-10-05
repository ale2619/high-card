package it.sara.demo.web.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.sara.demo.security.JwtTokenProvider;
import it.sara.demo.web.auth.request.LoginRequest;
import it.sara.demo.web.auth.response.LoginResponse;
import it.sara.demo.web.response.GenericResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Obtain and refresh JWT tokens")
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    /**
     * Authenticates a user and returns a signed JWT on success.
     * <p>
     * Credential verification is fully delegated to Spring Security's
     * {@link AuthenticationManager}, which consults the configured
     * {@link org.springframework.security.core.userdetails.UserDetailsService}.
     * The role embedded in the token is derived from the first authority
     * returned by the authenticated principal, stripped of the {@code ROLE_} prefix
     * that Spring Security adds internally.
     *
     * @param request login payload with {@code username} and {@code password}
     * @return {@code 200 OK} with a JWT on success; {@code 400 Bad Request} for blank fields;
     *         {@code 401 Unauthorized} for wrong credentials
     */
    @Operation(
            summary = "Login",
            description = "Authenticate with username and password. Returns a signed JWT valid for the configured expiration window."
    )
    @ApiResponse(responseCode = "200", description = "Credentials valid — JWT returned",
            content = @Content(schema = @Schema(implementation = LoginResponse.class)))
    @ApiResponse(responseCode = "400", description = "Blank username or password (bean validation failure)",
            content = @Content(schema = @Schema(implementation = GenericResponse.class)))
    @ApiResponse(responseCode = "401", description = "Wrong credentials",
            content = @Content(schema = @Schema(implementation = LoginResponse.class)))
    @SecurityRequirements   // login endpoint requires no token
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for user [{}]", request.getUsername());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            String role = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .map(a -> a.startsWith("ROLE_") ? a.substring(5) : a)
                    .findFirst()
                    .orElse("USER");

            String token = jwtTokenProvider.generateToken(request.getUsername(), role);
            log.info("Login successful for user [{}] with role [{}]", request.getUsername(), role);

            return ResponseEntity.ok(LoginResponse.builder()
                    .status(GenericResponse.success("Login successful").getStatus())
                    .token(token)
                    .expiresIn(jwtTokenProvider.getExpiration() / 1000)
                    .build());

        } catch (BadCredentialsException e) {
            log.info("Login failed — invalid credentials for user [{}]", request.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(LoginResponse.builder()
                            .status(GenericResponse.error(401, e.getMessage()).getStatus())
                            .build()
                    );
        }
    }
}