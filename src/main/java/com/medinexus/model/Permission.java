package com.medinexus.model;

public enum Permission {

    // =========================
    // PATIENT MANAGEMENT
    // =========================

    VIEW_PATIENTS,
    ADD_PATIENT,
    EDIT_PATIENT,
    DELETE_PATIENT,

    // =========================
    // OWN PATIENT PROFILE
    // =========================

    VIEW_OWN_PROFILE,
    EDIT_OWN_PROFILE,

    // =========================
    // MEDICAL RECORDS
    // =========================

    VIEW_MEDICAL_HISTORY,
    EDIT_MEDICAL_HISTORY,

    // =========================
    // DIAGNOSIS & PRESCRIPTION
    // =========================

    CREATE_DIAGNOSIS,
    CREATE_PRESCRIPTION,
    VIEW_PRESCRIPTIONS,

    // =========================
    // APPOINTMENTS
    // =========================

    VIEW_APPOINTMENTS,
    CREATE_APPOINTMENT,
    EDIT_APPOINTMENT,
    DELETE_APPOINTMENT,

    VIEW_OWN_APPOINTMENTS,

    // =========================
    // MEDICINES
    // =========================

    VIEW_MEDICINES,
    ADD_MEDICINE,
    EDIT_MEDICINE,
    DELETE_MEDICINE,

    // =========================
    // BILLING
    // =========================

    VIEW_BILLS,
    CREATE_BILL,
    EDIT_BILL,

    VIEW_OWN_BILLS,

    // =========================
    // DOCTOR MANAGEMENT
    // =========================

    VIEW_DOCTORS,
    ADD_DOCTOR,
    EDIT_DOCTOR,
    DELETE_DOCTOR,

    // =========================
    // USER MANAGEMENT
    // =========================

    MANAGE_USERS,

    // =========================
    // REPORTS
    // =========================

    VIEW_REPORTS,

    // =========================
    // AUDIT
    // =========================

    VIEW_AUDIT_LOGS,

    // =========================
    // SETTINGS
    // =========================

    MANAGE_SETTINGS,

    // =========================
    // IDENTITY VERIFICATION
    // =========================

    VERIFY_OWN_IDENTITY,
    VIEW_OWN_VERIFICATION
}