package com.medinexus.navigation;

import com.medinexus.model.Patient;

@FunctionalInterface
public interface NavigationHandler {

    void showMedicalRecords(Patient patient);
}