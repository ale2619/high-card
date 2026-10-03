package it.sara.demo.service.assembler;

import it.sara.demo.dto.UserDTO;
import it.sara.demo.service.database.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserAssembler {

    public UserDTO toDTO(User user) {
        return UserDTO.builder()
                .guid(user.getGuid())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .build();
    }
}