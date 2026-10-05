package it.sara.demo.service.database;

import it.sara.demo.service.database.model.User;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * In-memory user store used as a stand-in for a real database.
 * <p>
 * <strong>Demo only</strong> — not thread-safe, not persistent. All data is
 * lost on application restart. Replace with a JPA/JDBC repository in production.
 * <p>
 * The static initializer seeds {@link #TABLE_USER} with 10 sample records so the
 * application starts with searchable data out of the box.
 */
public class FakeDatabase {

    public static final List<User> TABLE_USER = new CopyOnWriteArrayList<>();

    static {
        for (int i = 0; i < 10; i++) {
            TABLE_USER.add(User.builder()
                    .guid(UUID.randomUUID().toString())
                    .firstName("First name " + i)
                    .lastName("Last name " + i)
                    .email("user" + i + "@example.com")
                    .phoneNumber("+393331234" + String.format("%03d", i))
                    .build());
        }
    }

    private FakeDatabase() {
    }
}