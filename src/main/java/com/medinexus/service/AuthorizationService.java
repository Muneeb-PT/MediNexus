package com.medinexus.service;

import com.medinexus.model.Permission;
import com.medinexus.model.Role;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public class AuthorizationService {

    private final Map<Role, Set<Permission>> rolePermissions;

    public AuthorizationService() {

        rolePermissions =
                new EnumMap<>(Role.class);

        configurePermissions();
    }

    private void configurePermissions() {

        // =========================
        // ADMIN
        // =========================

        rolePermissions.put(
                Role.ADMIN,
                EnumSet.allOf(Permission.class)
        );


        // =========================
        // DOCTOR
        // =========================

        rolePermissions.put(
                Role.DOCTOR,
                EnumSet.of(

                        Permission.VIEW_PATIENTS,

                        Permission.VIEW_MEDICAL_HISTORY,
                        Permission.EDIT_MEDICAL_HISTORY,

                        Permission.CREATE_DIAGNOSIS,
                        Permission.CREATE_PRESCRIPTION,
                        Permission.VIEW_PRESCRIPTIONS,

                        Permission.VIEW_APPOINTMENTS,
                        Permission.CREATE_APPOINTMENT,
                        Permission.EDIT_APPOINTMENT,

                        Permission.VIEW_MEDICINES,

                        Permission.VIEW_BILLS
                )
        );


        // =========================
        // RECEPTIONIST
        // =========================

        rolePermissions.put(
                Role.RECEPTIONIST,
                EnumSet.of(

                        Permission.VIEW_PATIENTS,
                        Permission.ADD_PATIENT,
                        Permission.EDIT_PATIENT,

                        Permission.VIEW_APPOINTMENTS,
                        Permission.CREATE_APPOINTMENT,
                        Permission.EDIT_APPOINTMENT,

                        Permission.VIEW_DOCTORS,

                        Permission.VIEW_BILLS,
                        Permission.CREATE_BILL,
                        Permission.EDIT_BILL
                )
        );


        // =========================
        // PATIENT
        // =========================

        rolePermissions.put(
                Role.PATIENT,
                EnumSet.of(

                        Permission.VIEW_OWN_PROFILE,
                        Permission.EDIT_OWN_PROFILE,

                        Permission.VIEW_OWN_APPOINTMENTS,
                        Permission.CREATE_APPOINTMENT,

                        Permission.VIEW_PRESCRIPTIONS,

                        Permission.VIEW_OWN_BILLS,

                        Permission.VIEW_MEDICAL_HISTORY,

                        Permission.VERIFY_OWN_IDENTITY,
                        Permission.VIEW_OWN_VERIFICATION
                )
        );
    }


    public boolean hasPermission(
            Role role,
            Permission permission) {

        if (role == null || permission == null) {
            return false;
        }

        Set<Permission> permissions =
                rolePermissions.get(role);

        if (permissions == null) {
            return false;
        }

        return permissions.contains(permission);
    }
}