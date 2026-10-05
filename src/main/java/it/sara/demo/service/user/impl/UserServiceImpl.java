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

/**
 * Default implementation of {@link UserService}.
 * <p>
 * Applies mandatory-field validation before delegating persistence to
 * {@link UserRepository} and DTO conversion to {@link UserAssembler}.
 * All unexpected runtime exceptions are wrapped in a {@link GenericException}
 * with code 500 so the caller receives a consistent error structure.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserAssembler userAssembler;

    /**
     * Validates mandatory fields, persists a new user and returns the created entity.
     * <p>
     * Field-level format validation (email, phone) is expected to have already been
     * performed by Jakarta Bean Validation on the web layer; this method performs
     * an additional presence check (non-blank) as a safety net at the service boundary.
     *
     * @param criteria the user data to persist
     * @return {@link AddUserResult} containing the persisted {@link it.sara.demo.dto.UserDTO}
     *         with its generated GUID
     * @throws GenericException code 400 if any mandatory field is blank;
     *                          code 500 if the repository fails to save the entity
     */
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

    /**
     * Searches users with optional filtering, sorting and pagination.
     * <p>
     * The {@code query} field in {@code criteria} is matched case-insensitively
     * against {@code firstName}, {@code lastName} and {@code email} using a
     * {@code contains} check. If {@code query} is blank, all users are returned
     * (subject to pagination).
     *
     * @param criteria search query, pagination (offset/limit) and sort order
     * @return {@link GetUsersResult} with the matching page of users and total count
     * @throws GenericException code 500 if an unexpected error occurs
     */
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