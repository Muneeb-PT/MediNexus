package com.medinexus;

import com.medinexus.model.Patient;
import com.medinexus.model.Role;
import com.medinexus.model.User;
import com.medinexus.service.PatientAccessService;
import com.medinexus.service.SessionManager;

public class PatientAccessTest {

    public static void main(String[] args) {

        PatientAccessService accessService =
                new PatientAccessService();


        /*
         * Login as Patient/User #1
         */
        User user =
                new User(
                        1,
                        "muneeb",
                        "Mohammed Muneeb",
                        Role.PATIENT
                );

        SessionManager.login(user);


        /*
         * Patient belonging to User #1
         */
        Patient ownPatient =
                new Patient(
                        1,
                        1,
                        "Mohammed Muneeb",
                        20,
                        "Male",
                        "9876543210",
                        "O+"
                );


        /*
         * Patient belonging to User #2
         */
        Patient otherPatient =
                new Patient(
                        2,
                        2,
                        "Aisha Rahman",
                        25,
                        "Female",
                        "9876543211",
                        "A+"
                );


        System.out.println(
                "Can view own patient record: "
                        + accessService.canViewPatient(
                        ownPatient
                )
        );


        System.out.println(
                "Can view another patient record: "
                        + accessService.canViewPatient(
                        otherPatient
                )
        );


        SessionManager.logout();
    }
}