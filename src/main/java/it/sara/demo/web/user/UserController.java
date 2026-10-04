package it.sara.demo.web.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.sara.demo.exception.GenericException;
import it.sara.demo.service.user.UserService;
import it.sara.demo.service.user.criteria.CriteriaGetUsers;
import it.sara.demo.web.assembler.AddUserAssembler;
import it.sara.demo.web.assembler.GetUsersAssembler;
import it.sara.demo.web.user.request.AddUserRequest;
import it.sara.demo.web.user.request.GetUsersRequest;
import it.sara.demo.web.user.response.AddUserResponse;
import it.sara.demo.web.user.response.GetUsersResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller exposing user management endpoints under {@code /api/v1}.
 * <p>
 * All responses return HTTP {@code 200 OK}; business or validation errors are
 * communicated via {@code StatusDTO.code} in the response body, following the
 * project-wide convention defined in {@link it.sara.demo.web.exception.GlobalExceptionHandler}.
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management — create and search users")
public class UserController {

    private final UserService userService;
    private final AddUserAssembler addUserAssembler;
    private final GetUsersAssembler getUsersAssembler;

    /**
     * Creates a new user and returns the persisted entity with its generated GUID.
     * <p>
     * Requires {@code ADMIN} role (enforced by {@link it.sara.demo.security.SecurityConfig}).
     * The request body is validated via Jakarta Bean Validation before the service is called;
     * any constraint violations are handled by {@link it.sara.demo.web.exception.GlobalExceptionHandler}.
     *
     * @param request the user creation payload; must pass {@code @ValidEmail} and
     *                {@code @ValidItalianPhoneNumber} constraints
     * @return the created user wrapped in {@link AddUserResponse}
     * @throws GenericException if a mandatory field is blank (code 400) or if the
     *                          repository fails to persist the entity (code 500)
     */
    @Operation(
            summary = "Create user",
            description = "Creates a new user. Requires ADMIN role. Email must be a valid RFC 5322 address; phone number must comply with the Italian format. " +
                    "HTTP is always 200; StatusDTO.code carries the outcome: 200 = success, 400 = validation failure (email/phone format or blank mandatory field), 500 = persistence error. " +
                    "HTTP 403 is the only exception: when the authenticated user lacks the ADMIN role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Always returned for authenticated ADMIN requests — inspect StatusDTO.code for the actual outcome",
                    content = @Content(schema = @Schema(implementation = AddUserResponse.class))),
            @ApiResponse(responseCode = "403", description = "Authenticated user does not have ADMIN role, no response body")
    })
    @PutMapping("/users")
    public ResponseEntity<AddUserResponse> addUser(@Valid @RequestBody AddUserRequest request) throws GenericException {
        AddUserResponse response = addUserAssembler.toResponse(userService.addUser(addUserAssembler.toCriteria(request)));
        return ResponseEntity.ok(response);
    }

    /**
     * Returns a paginated, optionally filtered and sorted list of users.
     * <p>
     * The request body is optional: if omitted, default values (offset {@code 0},
     * limit {@code 10}, no filter, no explicit sort) are applied. Pagination metadata
     * ({@code total}, {@code offset}, {@code limit}, {@code pageCount}) is included
     * in the response.
     *
     * @param request optional search, pagination and sort parameters
     * @return a paginated user list wrapped in {@link GetUsersResponse}
     * @throws GenericException if an unexpected error occurs during the search (code 500)
     */
    @Operation(
            summary = "Search users",
            description = "Returns a paginated list of users. The request body is optional — omitting it applies defaults (offset=0, limit=10, order=BY_LASTNAME). " +
                    "The 'query' field performs a case-insensitive contains match on firstName, lastName, and email. " +
                    "HTTP is always 200; StatusDTO.code carries the outcome: 200 = success, 400 = invalid pagination parameters, 500 = unexpected error."
    )
    @ApiResponse(responseCode = "200", description = "Always returned — inspect StatusDTO.code for the actual outcome",
            content = @Content(schema = @Schema(implementation = GetUsersResponse.class)))
    @PostMapping("/users")
    public ResponseEntity<GetUsersResponse> getUsers(@Valid @RequestBody(required = false) GetUsersRequest request) throws GenericException {
        if (request == null) {
            request = new GetUsersRequest(); // Default request if none provided
        }
        CriteriaGetUsers criteria = getUsersAssembler.toCriteria(request);
        return ResponseEntity.ok(getUsersAssembler.toResponse(userService.getUsers(criteria)));
    }
}