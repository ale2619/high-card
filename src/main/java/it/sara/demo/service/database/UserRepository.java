package it.sara.demo.service.database;

import it.sara.demo.service.database.model.User;
import it.sara.demo.service.user.criteria.CriteriaGetUsers;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Component
public class UserRepository {

    public boolean save(User user) {
        user.setGuid(java.util.UUID.randomUUID().toString());
        FakeDatabase.TABLE_USER.add(user);
        return true;
    }

    public Optional<User> getByGuid(String guid) {
        return FakeDatabase.TABLE_USER.stream()
                .filter(u -> u.getGuid().equals(guid))
                .findFirst();
    }

    public List<User> getAll() {
        return FakeDatabase.TABLE_USER;
    }

    public List<User> search(CriteriaGetUsers criteria) {
        return filteredStream(criteria)
                .sorted(buildComparator(criteria.getOrder()))
                .skip(criteria.getOffset())
                .limit(criteria.getLimit())
                .toList();
    }

    public int count(CriteriaGetUsers criteria) {
        return (int) filteredStream(criteria).count();
    }

    private Stream<User> filteredStream(CriteriaGetUsers criteria) {
        return FakeDatabase.TABLE_USER.stream()
                .filter(u -> matchesQuery(u, criteria.getQuery()));
    }

    private boolean matchesQuery(User user, String query) {
        if (!StringUtils.hasText(query)) {
            return true;
        }
        String q = query.toLowerCase();
        return user.getFirstName().toLowerCase().contains(q)
                || user.getLastName().toLowerCase().contains(q)
                || user.getEmail().toLowerCase().contains(q);
    }

    private Comparator<User> buildComparator(CriteriaGetUsers.OrderType order) {
        if (order == null) {
            return Comparator.comparing(User::getFirstName);
        }
        return switch (order) {
            case BY_FIRSTNAME -> Comparator.comparing(User::getFirstName);
            case BY_FIRSTNAME_DESC -> Comparator.comparing(User::getFirstName).reversed();
            case BY_LASTNAME -> Comparator.comparing(User::getLastName);
            case BY_LASTNAME_DESC -> Comparator.comparing(User::getLastName).reversed();
        };
    }
}