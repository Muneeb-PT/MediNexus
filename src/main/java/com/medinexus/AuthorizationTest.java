package com.medinexus;

import com.medinexus.model.Permission;
import com.medinexus.model.Role;
import com.medinexus.service.AuthorizationService;

public class AuthorizationTest {

    public static void main(String[] args) {

        AuthorizationService authorizationService =
                new AuthorizationService();


        System.out.println(
                "================================"
        );

        System.out.println(
                "MediNexus Authorization Test"
        );

        System.out.println(
                "================================"
        );


        System.out.println(
                "Admin delete patient: "
                        + authorizationService.hasPermission(
                        Role.ADMIN,
                        Permission.DELETE_PATIENT
                )
        );


        System.out.println(
                "Doctor delete patient: "
                        + authorizationService.hasPermission(
                        Role.DOCTOR,
                        Permission.DELETE_PATIENT
                )
        );


        System.out.println(
                "Receptionist add patient: "
                        + authorizationService.hasPermission(
                        Role.RECEPTIONIST,
                        Permission.ADD_PATIENT
                )
        );


        System.out.println(
                "Patient delete patient: "
                        + authorizationService.hasPermission(
                        Role.PATIENT,
                        Permission.DELETE_PATIENT
                )
        );


        System.out.println(
                "Patient view own profile: "
                        + authorizationService.hasPermission(
                        Role.PATIENT,
                        Permission.VIEW_OWN_PROFILE
                )
        );


        System.out.println(
                "Patient verify identity: "
                        + authorizationService.hasPermission(
                        Role.PATIENT,
                        Permission.VERIFY_OWN_IDENTITY
                )
        );


        System.out.println(
                "Doctor create prescription: "
                        + authorizationService.hasPermission(
                        Role.DOCTOR,
                        Permission.CREATE_PRESCRIPTION
                )
        );


        System.out.println(
                "Receptionist create prescription: "
                        + authorizationService.hasPermission(
                        Role.RECEPTIONIST,
                        Permission.CREATE_PRESCRIPTION
                )
        );
    }
}