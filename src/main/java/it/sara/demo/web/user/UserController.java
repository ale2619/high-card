package it.sara.demo.web.user;

import it.sara.demo.exception.GenericException;
import it.sara.demo.service.user.UserService;
import it.sara.demo.service.user.criteria.CriteriaAddUser;
import it.sara.demo.web.assembler.AddUserAssembler;
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
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AddUserAssembler addUserAssembler;

    @PutMapping("/v1/user")
    public ResponseEntity<GenericResponse> addUser(@Valid @RequestBody AddUserRequest request) throws GenericException {
        CriteriaAddUser criteria = addUserAssembler.toCriteria(request);
        userService.addUser(criteria);
        return ResponseEntity.ok(GenericResponse.success("User added."));
    }

    @PostMapping("/v1/user")
    public ResponseEntity<GetUsersResponse> getUsers(@RequestBody GetUsersRequest request) throws GenericException {
        return ResponseEntity.ok().build();
    }
}