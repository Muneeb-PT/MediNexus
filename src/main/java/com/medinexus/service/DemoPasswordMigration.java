package com.medinexus.service;

import com.medinexus.model.User;
import com.medinexus.repository.UserJdbcRepository;

public class DemoPasswordMigration {


    public static void main(String[] args) {

        UserJdbcRepository repository =
                new UserJdbcRepository();

        String temporaryPassword = "1234";

        int[] demoUserIds = {
                1,
                2,
                3,
                101,
                102,
                103,
                500,
                900,
                901,
                902
        };

        for (int userId : demoUserIds) {

            User user =
                    repository.findById(userId);

            if (user == null) {

                System.out.println(
                        "User not found: " + userId
                );

                continue;
            }

            String hash =
                    PasswordService.hashPassword(
                            temporaryPassword
                    );

            repository.updatePasswordHash(
                    userId,
                    hash
            );

            System.out.println(
                    "Password hash created for: "
                            + user.getUsername()
                            + " ("
                            + user.getUserId()
                            + ")"
            );
        }

        System.out.println();
        System.out.println(
                "Demo password migration completed."
        );
        System.out.println(
                "Temporary password: 1234"
        );
        System.out.println(
                "Change these passwords before production use."
        );
    }


}
