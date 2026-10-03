package it.sara.demo.web.user;

import it.sara.demo.exception.GenericException;
import it.sara.demo.service.user.UserService;
import it.sara.demo.service.user.criteria.CriteriaGetUsers;
import it.sara.demo.web.assembler.AddUserAssembler;
import it.sara.demo.web.assembler.GetUsersAssembler;
import it.sara.demo.web.response.GenericResponse;
import it.sara.demo.web.user.request.AddUserRequest;
import it.sara.demo.web.user.request.GetUsersRequest;
import it.sara.demo.web.user.response.GetUsersResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AddUserAssembler addUserAssembler;
    private final GetUsersAssembler getUsersAssembler;

    @PutMapping("/users")
    public ResponseEntity<GenericResponse> addUser(@Valid @RequestBody AddUserRequest request) throws GenericException {
        userService.addUser(addUserAssembler.toCriteria(request));
        return ResponseEntity.ok(GenericResponse.success("User added."));
    }

    @PostMapping("/users")
    public ResponseEntity<GetUsersResponse> getUsers(@Valid @RequestBody GetUsersRequest request) throws GenericException {
        CriteriaGetUsers criteria = getUsersAssembler.toCriteria(request);
        GetUsersResponse response = getUsersAssembler.toResponse(userService.getUsers(criteria));
        return ResponseEntity.ok(response);
    }
}