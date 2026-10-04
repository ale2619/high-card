package it.sara.demo.web.assembler;

import it.sara.demo.dto.UserDTO;
import it.sara.demo.service.user.criteria.CriteriaGetUsers;
import it.sara.demo.service.user.result.GetUsersResult;
import it.sara.demo.web.user.request.GetUsersRequest;
import it.sara.demo.web.user.response.GetUsersResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class GetUsersAssemblerTest {

    private GetUsersAssembler assembler;

    @BeforeEach
    void setUp() {
        assembler = new GetUsersAssembler();
    }

    // --- toCriteria ---

    @Test
    void toCriteria_mapsAllRequestFields() {
        GetUsersRequest request = new GetUsersRequest();
        request.setQuery("mario");
        request.setOffset(5);
        request.setLimit(20);
        request.setOrder(GetUsersRequest.OrderType.BY_FIRSTNAME);

        CriteriaGetUsers criteria = assembler.toCriteria(request);

        assertNotNull(criteria);
        assertEquals("mario", criteria.getQuery());
        assertEquals(5, criteria.getOffset());
        assertEquals(20, criteria.getLimit());
        assertEquals(CriteriaGetUsers.OrderType.BY_FIRSTNAME, criteria.getOrder());
    }

    @Test
    void toCriteria_nullQuery_propagatesNull() {
        GetUsersRequest request = new GetUsersRequest();
        request.setQuery(null);
        request.setOrder(GetUsersRequest.OrderType.BY_LASTNAME);

        assertNull(assembler.toCriteria(request).getQuery());
    }

    @Test
    void toCriteria_mapsAllOrderTypes() {
        for (GetUsersRequest.OrderType reqOrder : GetUsersRequest.OrderType.values()) {
            GetUsersRequest request = new GetUsersRequest();
            request.setOrder(reqOrder);
            CriteriaGetUsers criteria = assembler.toCriteria(request);
            assertEquals(reqOrder.name(), criteria.getOrder().name());
        }
    }

    // --- toResponse ---

    @Test
    void toResponse_mapsAllPaginationFields() {
        List<UserDTO> users = List.of(UserDTO.builder().firstName("Alice").build());
        GetUsersResult result = GetUsersResult.builder()
                .users(users)
                .total(25)
                .offset(10)
                .limit(10)
                .build();

        GetUsersResponse response = assembler.toResponse(result);

        assertNotNull(response);
        assertEquals(25, response.getTotal());
        assertEquals(10, response.getOffset());
        assertEquals(10, response.getLimit());
        assertThat(response.getUsers()).hasSize(1);
    }

    @Test
    void toResponse_setsSuccessStatusCode200() {
        GetUsersResult result = GetUsersResult.builder()
                .users(List.of()).total(0).offset(0).limit(10).build();
        assertEquals(200, assembler.toResponse(result).getStatus().getCode());
    }

    // --- pageCount computation ---

    @Test
    void toResponse_pageCount_exactMultiple() {
        GetUsersResult result = GetUsersResult.builder()
                .users(List.of()).total(20).offset(0).limit(10).build();
        assertEquals(2, assembler.toResponse(result).getPageCount());
    }

    @Test
    void toResponse_pageCount_nonExactMultiple() {
        GetUsersResult result = GetUsersResult.builder()
                .users(List.of()).total(21).offset(0).limit(10).build();
        assertEquals(3, assembler.toResponse(result).getPageCount());
    }

    @Test
    void toResponse_pageCount_totalZero_isZero() {
        GetUsersResult result = GetUsersResult.builder()
                .users(List.of()).total(0).offset(0).limit(10).build();
        assertEquals(0, assembler.toResponse(result).getPageCount());
    }

    @Test
    void toResponse_pageCount_limitZero_isZero() {
        GetUsersResult result = GetUsersResult.builder()
                .users(List.of()).total(10).offset(0).limit(0).build();
        assertEquals(0, assembler.toResponse(result).getPageCount());
    }

    @Test
    void toResponse_pageCount_singlePage() {
        GetUsersResult result = GetUsersResult.builder()
                .users(List.of()).total(5).offset(0).limit(10).build();
        assertEquals(1, assembler.toResponse(result).getPageCount());
    }
}