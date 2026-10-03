package it.sara.demo.service.database;

import it.sara.demo.service.database.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FakeDatabase {

    public static final List<User> TABLE_USER = new ArrayList<>();

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