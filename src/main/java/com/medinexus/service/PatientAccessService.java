package com.medinexus.service;

import com.medinexus.model.Permission;
import com.medinexus.model.Patient;
import com.medinexus.model.Role;
import com.medinexus.model.User;

public class PatientAccessService {

    private final AuthorizationService authorizationService;

    public PatientAccessService() {
        authorizationService =
                new AuthorizationService();
    }


    public boolean canViewPatient(Patient patient) {

        User currentUser =
                SessionManager.getCurrentUser();

        if (currentUser == null || patient == null) {
            return false;
        }


        Role role =
                currentUser.getRole();


        /*
         * ADMIN
         *
         * Admin can view patients.
         */
        if (authorizationService.hasPermission(
                role,
                Permission.VIEW_PATIENTS)) {

            return true;
        }


        /*
         * PATIENT
         *
         * Patient can only view
         * their own patient record.
         */
        if (role == Role.PATIENT) {

            return patient.getUserId()
                    == currentUser.getUserId();
        }


        return false;
    }


    public boolean canEditPatient(Patient patient) {

        User currentUser =
                SessionManager.getCurrentUser();

        if (currentUser == null || patient == null) {
            return false;
        }


        Role role =
                currentUser.getRole();


        /*
         * Admin can edit any patient.
         */
        if (authorizationService.hasPermission(
                role,
                Permission.EDIT_PATIENT)) {

            return true;
        }


        /*
         * Receptionist can edit
         * patient information.
         */
        if (role == Role.RECEPTIONIST) {
            return true;
        }


        /*
         * Patients cannot edit
         * general patient records here.
         *
         * They will use a separate
         * "Edit My Profile" feature.
         */
        return false;
    }


    public boolean canDeletePatient(Patient patient) {

        User currentUser =
                SessionManager.getCurrentUser();

        if (currentUser == null || patient == null) {
            return false;
        }


        return authorizationService.hasPermission(
                currentUser.getRole(),
                Permission.DELETE_PATIENT
        );
    }
}