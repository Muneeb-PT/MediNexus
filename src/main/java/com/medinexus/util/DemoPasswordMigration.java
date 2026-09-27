package com.medinexus.util;

import com.medinexus.repository.UserJdbcRepository;
import com.medinexus.service.PasswordService;

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

            String passwordHash =
                    PasswordService.hashPassword(
                            temporaryPassword
                    );

            repository.updatePasswordHash(
                    userId,
                    passwordHash
            );

            System.out.println(
                    "Password migrated for user ID: "
                            + userId
            );
        }

        System.out.println(
                "Demo password migration completed."
        );
    }
}