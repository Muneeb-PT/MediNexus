package com.medinexus.service;

public final class PasswordPolicy {

    private PasswordPolicy() {
        // Utility class.
    }

    public static void validate(String password) {

        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException(
                    "Password is required."
            );
        }

        if (password.length() < 8) {
            throw new IllegalArgumentException(
                    "Password must contain at least 8 characters."
            );
        }

        if (password.length() > 72) {
            throw new IllegalArgumentException(
                    "Password cannot contain more than 72 characters."
            );
        }

        if (password.chars().anyMatch(Character::isWhitespace)) {
            throw new IllegalArgumentException(
                    "Password must not contain spaces."
            );
        }

        boolean hasUppercase =
                password.chars()
                        .anyMatch(Character::isUpperCase);

        boolean hasLowercase =
                password.chars()
                        .anyMatch(Character::isLowerCase);

        boolean hasDigit =
                password.chars()
                        .anyMatch(Character::isDigit);

        boolean hasSpecial =
                password.chars()
                        .anyMatch(ch ->
                                !Character.isLetterOrDigit(ch));

        if (!hasUppercase) {
            throw new IllegalArgumentException(
                    "Password must contain at least one uppercase letter."
            );
        }

        if (!hasLowercase) {
            throw new IllegalArgumentException(
                    "Password must contain at least one lowercase letter."
            );
        }

        if (!hasDigit) {
            throw new IllegalArgumentException(
                    "Password must contain at least one number."
            );
        }

        if (!hasSpecial) {
            throw new IllegalArgumentException(
                    "Password must contain at least one special character."
            );
        }
    }

    public static boolean isValid(String password) {

        try {
            validate(password);
            return true;

        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}