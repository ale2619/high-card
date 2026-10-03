package it.sara.demo.service.user.impl;

import it.sara.demo.exception.GenericException;
import it.sara.demo.service.assembler.UserAssembler;
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

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserAssembler userAssembler;

    @Override
    public AddUserResult addUser(CriteriaAddUser criteria) throws GenericException {
        try {
            log.info("Validating mandatory fields for new user");
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
            log.info("User persisted with GUID [{}]", user.getGuid());

            return AddUserResult.builder()
                    .user(userAssembler.toDTO(user))
                    .build();

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
    public GetUsersResult getUsers(CriteriaGetUsers criteria) throws GenericException {
        try {
            log.info("Searching users — query=[{}] offset=[{}] limit=[{}] order=[{}]",
                    criteria.getQuery(), criteria.getOffset(), criteria.getLimit(), criteria.getOrder());

            List<User> users = userRepository.search(criteria);
            int total = userRepository.count(criteria);
            log.info("Search returned [{}] users (total matching: [{}])", users.size(), total);

            return GetUsersResult.builder()
                    .total(total)
                    .offset(criteria.getOffset())
                    .limit(criteria.getLimit())
                    .users(users.stream().map(userAssembler::toDTO).toList())
                    .build();

        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage(), e);
            }
            throw new GenericException(GenericException.GENERIC_ERROR);
        }
    }
}