package it.sara.demo.web.assembler;

import it.sara.demo.service.user.criteria.CriteriaAddUser;
import it.sara.demo.web.user.request.AddUserRequest;
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
}