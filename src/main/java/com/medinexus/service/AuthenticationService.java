package com.medinexus.service;

import com.medinexus.model.Role;
import com.medinexus.model.User;
import com.medinexus.repository.UserJdbcRepository;

public class AuthenticationService {


    private final UserJdbcRepository userRepository;

    public AuthenticationService() {
        this.userRepository =
                new UserJdbcRepository();
    }

    /**
     * Authenticates a user using username, password and role.
     *
     * Returns the authenticated User or null when
     * authentication fails.
     */
    public User authenticate(
            String username,
            String password,
            Role selectedRole) {

        if (username == null
                || username.trim().isEmpty()
                || password == null
                || password.isEmpty()
                || selectedRole == null) {

            return null;
        }

        User user =
                userRepository.findByUsername(
                        username.trim()
                );

        /*
         * Do not reveal whether the username or password
         * was incorrect.
         */
        if (user == null) {
            return null;
        }

        /*
         * The selected role must match the role stored
         * in the database.
         */
        if (user.getRole() != selectedRole) {
            return null;
        }

        /*
         * Existing demo accounts without a password hash
         * are not authenticated by the new system.
         */
        if (user.getPasswordHash() == null
                || user.getPasswordHash().isBlank()) {

            return null;
        }

        if (!PasswordService.verifyPassword(
                password,
                user.getPasswordHash())) {

            return null;
        }

        return user;
    }

    /**
     * Creates a password hash for an existing account.
     */
    public void setPassword(
            int userId,
            String password) {

        String passwordHash =
                PasswordService.hashPassword(
                        password
                );

        userRepository.updatePasswordHash(
                userId,
                passwordHash
        );
    }


}
