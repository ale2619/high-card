package it.sara.demo.service.user.impl;

import it.sara.demo.dto.UserDTO;
import it.sara.demo.exception.GenericException;
import it.sara.demo.service.assembler.UserAssembler;
import it.sara.demo.service.database.UserRepository;
import it.sara.demo.service.database.model.User;
import it.sara.demo.service.user.criteria.CriteriaAddUser;
import it.sara.demo.service.user.criteria.CriteriaGetUsers;
import it.sara.demo.service.user.result.AddUserResult;
import it.sara.demo.service.user.result.GetUsersResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserAssembler userAssembler;

    @InjectMocks
    private UserServiceImpl userService;

    // --- addUser success ---

    @Test
    void addUser_allFieldsPresent_returnsCreatedUser() throws GenericException {
        CriteriaAddUser criteria = CriteriaAddUser.builder()
                .firstName("Mario")
                .lastName("Rossi")
                .email("mario.rossi@example.com")
                .phoneNumber("+393331234567")
                .build();

        when(userRepository.save(any(User.class))).thenReturn(true);
        UserDTO expectedDTO = UserDTO.builder()
                .firstName("Mario")
                .lastName("Rossi")
                .email("mario.rossi@example.com")
                .phoneNumber("+393331234567")
                .build();
        when(userAssembler.toDTO(any(User.class))).thenReturn(expectedDTO);

        AddUserResult result = userService.addUser(criteria);

        assertNotNull(result);
        assertEquals("Mario", result.getUser().getFirstName());
        assertEquals("mario.rossi@example.com", result.getUser().getEmail());
    }

    // --- addUser missing mandatory fields ---

    @Test
    void addUser_missingFirstName_throwsGenericException400() {
        CriteriaAddUser criteria = CriteriaAddUser.builder()
                .lastName("Rossi")
                .email("mario@example.com")
                .phoneNumber("+393331234567")
                .build();

        GenericException ex = assertThrows(GenericException.class, () -> userService.addUser(criteria));
        assertEquals(400, ex.getStatus().getCode());
        assertEquals("First name is required", ex.getStatus().getMessage());
    }

    @Test
    void addUser_blankFirstName_throwsGenericException400() {
        CriteriaAddUser criteria = CriteriaAddUser.builder()
                .firstName("   ")
                .lastName("Rossi")
                .email("mario@example.com")
                .phoneNumber("+393331234567")
                .build();

        GenericException ex = assertThrows(GenericException.class, () -> userService.addUser(criteria));
        assertEquals(400, ex.getStatus().getCode());
    }

    @Test
    void addUser_missingLastName_throwsGenericException400() {
        CriteriaAddUser criteria = CriteriaAddUser.builder()
                .firstName("Mario")
                .email("mario@example.com")
                .phoneNumber("+393331234567")
                .build();

        GenericException ex = assertThrows(GenericException.class, () -> userService.addUser(criteria));
        assertEquals(400, ex.getStatus().getCode());
        assertEquals("Last name is required", ex.getStatus().getMessage());
    }

    @Test
    void addUser_missingEmail_throwsGenericException400() {
        CriteriaAddUser criteria = CriteriaAddUser.builder()
                .firstName("Mario")
                .lastName("Rossi")
                .phoneNumber("+393331234567")
                .build();

        GenericException ex = assertThrows(GenericException.class, () -> userService.addUser(criteria));
        assertEquals(400, ex.getStatus().getCode());
        assertEquals("Email is required", ex.getStatus().getMessage());
    }

    @Test
    void addUser_missingPhone_throwsGenericException400() {
        CriteriaAddUser criteria = CriteriaAddUser.builder()
                .firstName("Mario")
                .lastName("Rossi")
                .email("mario@example.com")
                .build();

        GenericException ex = assertThrows(GenericException.class, () -> userService.addUser(criteria));
        assertEquals(400, ex.getStatus().getCode());
        assertEquals("Phone is required", ex.getStatus().getMessage());
    }

    // --- addUser repository failures ---

    @Test
    void addUser_repositoryReturnsFalse_throwsGenericException500() {
        CriteriaAddUser criteria = CriteriaAddUser.builder()
                .firstName("Mario")
                .lastName("Rossi")
                .email("mario@example.com")
                .phoneNumber("+393331234567")
                .build();

        when(userRepository.save(any(User.class))).thenReturn(false);

        GenericException ex = assertThrows(GenericException.class, () -> userService.addUser(criteria));
        assertEquals(500, ex.getStatus().getCode());
        assertEquals("Error saving user", ex.getStatus().getMessage());
    }

    @Test
    void addUser_repositoryThrowsUnexpected_throwsGenericException500() {
        CriteriaAddUser criteria = CriteriaAddUser.builder()
                .firstName("Mario")
                .lastName("Rossi")
                .email("mario@example.com")
                .phoneNumber("+393331234567")
                .build();

        when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("Unexpected DB failure"));

        GenericException ex = assertThrows(GenericException.class, () -> userService.addUser(criteria));
        assertEquals(500, ex.getStatus().getCode());
    }

    // --- getUsers ---

    @Test
    void getUsers_returnsPagedResult() throws GenericException {
        CriteriaGetUsers criteria = CriteriaGetUsers.builder().offset(0).limit(10).build();

        List<User> users = List.of(
                User.builder().firstName("Alice").lastName("Rossi").email("alice@example.com").build()
        );
        when(userRepository.search(criteria)).thenReturn(users);
        when(userRepository.count(criteria)).thenReturn(1);
        when(userAssembler.toDTO(any(User.class)))
                .thenReturn(UserDTO.builder().firstName("Alice").build());

        GetUsersResult result = userService.getUsers(criteria);

        assertNotNull(result);
        assertThat(result.getUsers()).hasSize(1);
        assertEquals(1, result.getTotal());
        assertEquals(0, result.getOffset());
        assertEquals(10, result.getLimit());
    }

    @Test
    void getUsers_repositoryThrowsUnexpected_throwsGenericException500() {
        CriteriaGetUsers criteria = CriteriaGetUsers.builder().offset(0).limit(10).build();
        when(userRepository.search(criteria)).thenThrow(new RuntimeException("Unexpected failure"));

        GenericException ex = assertThrows(GenericException.class, () -> userService.getUsers(criteria));
        assertEquals(500, ex.getStatus().getCode());
    }
}