package com.medinexus.service;

import java.util.Locale;

public class IdentityMatchService {

    public boolean matchesName(
            String patientName,
            String verifiedName) {

        if (patientName == null
                || patientName.isBlank()) {

            return false;
        }

        if (verifiedName == null
                || verifiedName.isBlank()) {

            return false;
        }

        return normalize(patientName)
                .equals(normalize(verifiedName));
    }

    private String normalize(String value) {

        return value
                .trim()
                .replaceAll("\\s+", " ")
                .toLowerCase(Locale.ROOT);
    }
}