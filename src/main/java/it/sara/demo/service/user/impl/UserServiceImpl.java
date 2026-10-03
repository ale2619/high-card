package it.sara.demo.service.user.impl;

import it.sara.demo.exception.GenericException;
import it.sara.demo.service.database.UserRepository;
import it.sara.demo.service.database.model.User;
import it.sara.demo.service.user.UserService;
import it.sara.demo.service.user.criteria.CriteriaAddUser;
import it.sara.demo.service.user.criteria.CriteriaGetUsers;
import it.sara.demo.service.user.result.AddUserResult;
import it.sara.demo.service.user.result.GetUsersResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public AddUserResult addUser(CriteriaAddUser criteria) throws GenericException {
        try {
            if (!StringUtils.hasText(criteria.getFirstName())) {
                throw new GenericException(400, "First name is required");
            }
            if (!StringUtils.hasText(criteria.getLastName())) {
                throw new GenericException(400, "Last name is required");
            }
            if (!StringUtils.hasText(criteria.getEmail())) {
                throw new GenericException(400, "Email is required");
            }
            if (!StringUtils.hasText(criteria.getPhoneNumber())) {
                throw new GenericException(400, "Phone is required");
            }

            User user = User.builder()
                    .firstName(criteria.getFirstName())
                    .lastName(criteria.getLastName())
                    .email(criteria.getEmail())
                    .phoneNumber(criteria.getPhoneNumber())
                    .build();

            if (!userRepository.save(user)) {
                throw new GenericException(500, "Error saving user");
            }

            return new AddUserResult();

        } catch (GenericException e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage(), e);
            }
            throw e;
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage(), e);
            }
            throw new GenericException(GenericException.GENERIC_ERROR);
        }
    }

    @Override
    public GetUsersResult getUsers(CriteriaGetUsers criteriaGetUsers) throws GenericException {
        return null;
    }
}