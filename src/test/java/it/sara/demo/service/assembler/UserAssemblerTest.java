package it.sara.demo.service.assembler;

import it.sara.demo.dto.UserDTO;
import it.sara.demo.service.database.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class UserAssemblerTest {

    private UserAssembler assembler;

    @BeforeEach
    void setUp() {
        assembler = new UserAssembler();
    }

    @Test
    void toDTO_mapsAllFields() {
        User user = User.builder()
                .guid("test-guid")
                .firstName("Mario")
                .lastName("Rossi")
                .email("mario.rossi@example.com")
                .phoneNumber("+393331234567")
                .build();

        UserDTO dto = assembler.toDTO(user);

        assertNotNull(dto);
        assertEquals("test-guid", dto.getGuid());
        assertEquals("Mario", dto.getFirstName());
        assertEquals("Rossi", dto.getLastName());
        assertEquals("mario.rossi@example.com", dto.getEmail());
        assertEquals("+393331234567", dto.getPhoneNumber());
    }
}