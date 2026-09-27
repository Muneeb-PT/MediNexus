package com.medinexus.controller;

import com.medinexus.model.Patient;

public interface PatientDataListener {

    void onPatientAdded(Patient patient);
}