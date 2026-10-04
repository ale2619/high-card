package it.sara.demo.web.assembler;

import it.sara.demo.dto.UserDTO;
import it.sara.demo.service.user.criteria.CriteriaAddUser;
import it.sara.demo.service.user.result.AddUserResult;
import it.sara.demo.web.user.request.AddUserRequest;
import it.sara.demo.web.user.response.AddUserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AddUserAssemblerTest {

    private AddUserAssembler assembler;

    @BeforeEach
    void setUp() {
        assembler = new AddUserAssembler();
    }

    // --- toCriteria ---

    @Test
    void toCriteria_mapsAllRequestFields() {
        AddUserRequest request = new AddUserRequest();
        request.setFirstName("Mario");
        request.setLastName("Rossi");
        request.setEmail("mario@example.com");
        request.setPhoneNumber("+393331234567");

        CriteriaAddUser criteria = assembler.toCriteria(request);

        assertNotNull(criteria);
        assertEquals("Mario", criteria.getFirstName());
        assertEquals("Rossi", criteria.getLastName());
        assertEquals("mario@example.com", criteria.getEmail());
        assertEquals("+393331234567", criteria.getPhoneNumber());
    }

    // --- toResponse ---

    @Test
    void toResponse_mapsUserAndSetsSuccessStatus() {
        UserDTO userDTO = UserDTO.builder()
                .guid("test-guid")
                .firstName("Mario")
                .lastName("Rossi")
                .email("mario@example.com")
                .phoneNumber("+393331234567")
                .build();

        AddUserResult result = AddUserResult.builder().user(userDTO).build();

        AddUserResponse response = assembler.toResponse(result);

        assertNotNull(response);
        assertNotNull(response.getStatus());
        assertEquals(200, response.getStatus().getCode());
        assertEquals(userDTO, response.getUser());
    }
}