package it.sara.demo.service.user.criteria;

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
public class CriteriaAddUser {
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
}