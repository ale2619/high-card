package it.sara.demo.service.database;

import it.sara.demo.service.database.model.User;

import java.util.ArrayList;
import java.util.List;

public class FakeDatabase {

    public static final List<User> TABLE_USER = new ArrayList<>();

    static {
        for (int i = 0; i < 10; i++) {
            User user = new User();
            user.setGuid(java.util.UUID.randomUUID().toString());
            user.setFirstName("First name " + i);
            user.setLastName("Last name " + i);
            user.setEmail("user" + i + "@example.com");
            user.setPhoneNumber("+393331234" + String.format("%03d", i));
            TABLE_USER.add(user);
        }
    }

    private FakeDatabase() {

    }

}
