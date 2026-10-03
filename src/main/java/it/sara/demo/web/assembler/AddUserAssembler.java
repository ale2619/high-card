package it.sara.demo.web.assembler;

import it.sara.demo.service.user.criteria.CriteriaAddUser;
import it.sara.demo.service.user.result.AddUserResult;
import it.sara.demo.web.response.GenericResponse;
import it.sara.demo.web.user.request.AddUserRequest;
import it.sara.demo.web.user.response.AddUserResponse;
import org.springframework.stereotype.Component;

@Component
public class AddUserAssembler {

    public CriteriaAddUser toCriteria(AddUserRequest request) {
        return CriteriaAddUser.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .build();
    }

    public AddUserResponse toResponse(AddUserResult result) {
        return AddUserResponse.builder()
                .status(GenericResponse.success("User added.").getStatus())
                .user(result.getUser())
                .build();
    }
}