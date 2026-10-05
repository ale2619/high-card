package it.sara.demo.service.database;

import it.sara.demo.service.database.model.User;
import it.sara.demo.service.user.criteria.CriteriaGetUsers;
import it.sara.demo.service.user.criteria.CriteriaGetUsers.OrderType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests {@link UserRepository} search, count, and sort logic against a
 * controlled in-memory dataset. FakeDatabase is reset before and after each
 * test to ensure isolation from the static seed data.
 */
class UserRepositorySearchTest {

    private UserRepository repository;

    @BeforeEach
    void setUp() {
        repository = new UserRepository();
        FakeDatabase.TABLE_USER.clear();
        FakeDatabase.TABLE_USER.addAll(List.of(
                user("Alice",  "Zanetti",  "alice@example.com",  "+393331111111"),
                user("Bob",    "Marley",   "bob@test.com",       "+393332222222"),
                user("Carlo",  "Bianchi",  "carlo@example.com",  "+393333333333"),
                user("Diana",  "Zanetti",  "diana@example.com",  "+393334444444")
        ));
    }

    @AfterEach
    void tearDown() {
        FakeDatabase.TABLE_USER.clear();
    }

    // --- pagination ---

    @Test
    void search_noFilterNoOffset_returnsAllWithinLimit() {
        assertThat(repository.search(criteria(null, 0, 10, null))).hasSize(4);
    }

    @Test
    void search_limitOne_returnsFirstRecord() {
        List<User> result = repository.search(criteria(null, 0, 1, OrderType.BY_FIRSTNAME));
        assertThat(result).hasSize(1);
        assertEquals("Alice", result.get(0).getFirstName());
    }

    @Test
    void search_offsetTwo_skipsFirstTwo() {
        List<User> result = repository.search(criteria(null, 2, 10, OrderType.BY_FIRSTNAME));
        assertThat(result).hasSize(2);
        assertEquals("Carlo", result.get(0).getFirstName());
    }

    @Test
    void search_offsetBeyondTotal_returnsEmpty() {
        assertThat(repository.search(criteria(null, 100, 10, null))).isEmpty();
    }

    // --- sorting ---

    @Test
    void search_sortByFirstNameAsc_correctOrder() {
        List<User> result = repository.search(criteria(null, 0, 10, OrderType.BY_FIRSTNAME));
        assertThat(result).extracting(User::getFirstName)
                .containsExactly("Alice", "Bob", "Carlo", "Diana");
    }

    @Test
    void search_sortByFirstNameDesc_correctOrder() {
        List<User> result = repository.search(criteria(null, 0, 10, OrderType.BY_FIRSTNAME_DESC));
        assertThat(result).extracting(User::getFirstName)
                .containsExactly("Diana", "Carlo", "Bob", "Alice");
    }

    @Test
    void search_sortByLastNameAsc_correctOrder() {
        List<User> result = repository.search(criteria(null, 0, 10, OrderType.BY_LASTNAME));
        assertThat(result).extracting(User::getLastName)
                .containsExactly("Bianchi", "Marley", "Zanetti", "Zanetti");
    }

    @Test
    void search_sortByLastNameDesc_firstIsZanetti_lastIsBianchi() {
        List<User> result = repository.search(criteria(null, 0, 10, OrderType.BY_LASTNAME_DESC));
        assertEquals("Zanetti", result.get(0).getLastName());
        assertEquals("Bianchi", result.get(result.size() - 1).getLastName());
    }

    @Test
    void search_nullOrder_defaultsToFirstNameAsc() {
        List<User> result = repository.search(criteria(null, 0, 10, null));
        assertThat(result).extracting(User::getFirstName)
                .containsExactly("Alice", "Bob", "Carlo", "Diana");
    }

    // --- filtering ---

    @Test
    void search_filterByFirstName_caseInsensitive() {
        List<User> result = repository.search(criteria("ALICE", 0, 10, null));
        assertThat(result).hasSize(1);
        assertEquals("Alice", result.get(0).getFirstName());
    }

    @Test
    void search_filterByLastNamePartial_returnsMatches() {
        List<User> result = repository.search(criteria("zanetti", 0, 10, OrderType.BY_FIRSTNAME));
        assertThat(result).hasSize(2);
        assertThat(result).extracting(User::getFirstName).containsExactly("Alice", "Diana");
    }

    @Test
    void search_filterByEmailDomain_returnsMatches() {
        // alice, carlo, diana are @example.com; bob is @test.com
        assertThat(repository.search(criteria("example.com", 0, 10, null))).hasSize(3);
    }

    @Test
    void search_filterNoMatch_returnsEmpty() {
        assertThat(repository.search(criteria("nonexistent", 0, 10, null))).isEmpty();
    }

    // --- count ---

    @Test
    void count_noFilter_returnsTotalUsers() {
        assertEquals(4, repository.count(criteria(null, 0, 10, null)));
    }

    @Test
    void count_withFilter_returnsFilteredCount() {
        assertEquals(3, repository.count(criteria("example.com", 0, 10, null)));
    }

    @Test
    void count_filterNoMatch_returnsZero() {
        assertEquals(0, repository.count(criteria("nonexistent", 0, 10, null)));
    }

    // --- helpers ---

    private static User user(String first, String last, String email, String phone) {
        return User.builder()
                .guid(UUID.randomUUID().toString())
                .firstName(first).lastName(last)
                .email(email).phoneNumber(phone)
                .build();
    }

    private static CriteriaGetUsers criteria(String query, int offset, int limit, OrderType order) {
        return CriteriaGetUsers.builder()
                .query(query).offset(offset).limit(limit).order(order)
                .build();
    }
}