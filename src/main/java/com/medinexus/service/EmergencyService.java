package com.medinexus.service;

import com.medinexus.model.EmergencyRequest;
import com.medinexus.model.EmergencyStatus;
import com.medinexus.model.Role;
import com.medinexus.model.User;
import com.medinexus.model.VehicleStatus;
import com.medinexus.repository.EmergencyRequestRepository;
import com.medinexus.repository.EmergencyVehicleRepository;

import java.time.LocalDateTime;
import java.util.List;

public class EmergencyService {

    private final EmergencyRequestRepository
            emergencyRepository;

    private final EmergencyVehicleRepository
            vehicleRepository;

    public EmergencyService() {

        emergencyRepository =
                new EmergencyRequestRepository();

        vehicleRepository =
                new EmergencyVehicleRepository();
    }

    /*
     * Create an emergency request.
     *
     * Patients can create requests for themselves.
     * Admin and Receptionist can create requests
     * for registered patients.
     */
    public long requestEmergency(
            EmergencyRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Emergency request cannot be null."
            );
        }

        User user = getCurrentUser();

        if (user.getRole() == Role.PATIENT) {

            PatientService patientService =
                    new PatientService();

            var patient =
                    patientService.getPatientByUserId(
                            user.getUserId()
                    );

            if (patient == null
                    || patient.getPatientId()
                    != request.getPatientId()) {

                throw new SecurityException(
                        "Patients can only create emergency requests for themselves."
                );
            }

        } else if (user.getRole() != Role.ADMIN
                && user.getRole() != Role.RECEPTIONIST) {

            throw new SecurityException(
                    "You are not authorized to create emergency requests."
            );
        }

        validateRequest(request);

        request.setRequestedBy(
                user.getUserId()
        );

        request.setStatus(
                EmergencyStatus.REQUESTED
        );

        request.setRequestedAt(
                LocalDateTime.now()
        );

        return emergencyRepository.create(
                request
        );
    }

    /*
     * Returns emergency requests according
     * to the logged-in user's role.
     */
    public List<EmergencyRequest>
    getEmergencyRequests() {

        User user = getCurrentUser();

        if (user.getRole() == Role.PATIENT) {

            PatientService patientService =
                    new PatientService();

            var patient =
                    patientService.getPatientByUserId(
                            user.getUserId()
                    );

            if (patient == null) {
                throw new SecurityException(
                        "Patient profile not found."
                );
            }

            return emergencyRepository.findByPatientId(
                    patient.getPatientId()
            );
        }

        if (user.getRole() == Role.ADMIN
                || user.getRole() == Role.RECEPTIONIST
                || user.getRole() == Role.DOCTOR) {

            return emergencyRepository.findAll();
        }

        throw new SecurityException(
                "You are not authorized to view emergency requests."
        );
    }

    /*
     * Dispatch an available emergency vehicle.
     */
    public void dispatchVehicle(
            long emergencyId,
            long vehicleId) {

        requireStaff();

        EmergencyRequest request =
                requireEmergency(
                        emergencyId
                );

        if (request.getStatus()
                != EmergencyStatus.REQUESTED) {

            throw new IllegalStateException(
                    "Only REQUESTED emergencies can be dispatched."
            );
        }

        var vehicle =
                vehicleRepository.findById(
                        vehicleId
                );

        if (vehicle == null) {

            throw new IllegalArgumentException(
                    "Emergency vehicle not found."
            );
        }

        if (vehicle.getStatus()
                != VehicleStatus.AVAILABLE) {

            throw new IllegalStateException(
                    "Selected vehicle is not available."
            );
        }

        /*
         * Vehicle reservation and emergency assignment
         * are now performed atomically.
         */
        vehicleRepository.dispatchVehicleAtomically(
                emergencyId,
                vehicleId
        );
    }

    /*
     * Mark dispatched emergency as EN_ROUTE.
     */
    public void markEnRoute(
            long emergencyId) {

        requireStaff();

        EmergencyRequest request =
                requireEmergency(
                        emergencyId
                );

        requireStatus(
                request,
                EmergencyStatus.DISPATCHED
        );

        requireAssignedVehicle(request);

        vehicleRepository.updateStatus(
                request.getAssignedVehicleId(),
                VehicleStatus.EN_ROUTE,
                "En route to emergency"
        );

        emergencyRepository.updateStatus(
                emergencyId,
                EmergencyStatus.EN_ROUTE
        );
    }

    /*
     * Mark emergency as ON_SCENE.
     */
    public void markOnScene(
            long emergencyId) {

        requireStaff();

        EmergencyRequest request =
                requireEmergency(
                        emergencyId
                );

        requireStatus(
                request,
                EmergencyStatus.EN_ROUTE
        );

        requireAssignedVehicle(request);

        vehicleRepository.updateStatus(
                request.getAssignedVehicleId(),
                VehicleStatus.ON_SCENE,
                "At emergency location"
        );

        emergencyRepository.updateStatus(
                emergencyId,
                EmergencyStatus.ON_SCENE
        );
    }

    /*
     * Complete the emergency.
     *
     * The ambulance becomes RETURNING first.
     */
    public void completeEmergency(
            long emergencyId) {

        requireStaff();

        EmergencyRequest request =
                requireEmergency(
                        emergencyId
                );

        if (request.getStatus()
                != EmergencyStatus.ON_SCENE) {

            throw new IllegalStateException(
                    "Only ON_SCENE emergencies can be completed."
            );
        }

        Long vehicleId =
                request.getAssignedVehicleId();

        emergencyRepository.updateStatus(
                emergencyId,
                EmergencyStatus.COMPLETED
        );

        if (vehicleId != null) {

            vehicleRepository.updateStatus(
                    vehicleId,
                    VehicleStatus.RETURNING,
                    "Returning to hospital"
            );
        }
    }
    public void returnVehicle(
            long vehicleId) {

        requireStaff();

        var vehicle =
                vehicleRepository.findById(
                        vehicleId
                );

        if (vehicle == null) {

            throw new IllegalArgumentException(
                    "Emergency vehicle not found."
            );
        }

        if (vehicle.getStatus()
                != VehicleStatus.RETURNING) {

            throw new IllegalStateException(
                    "Only RETURNING vehicles can be marked as available."
            );
        }

        vehicleRepository.updateStatus(
                vehicleId,
                VehicleStatus.AVAILABLE,
                "MediNexus Hospital"
        );
    }
    /*
     * Cancel an emergency before completion.
     */
    public void cancelEmergency(
            long emergencyId) {

        User user = getCurrentUser();

        EmergencyRequest request =
                requireEmergency(
                        emergencyId
                );

        boolean authorized =
                user.getRole() == Role.ADMIN
                        || user.getRole() == Role.RECEPTIONIST;

        if (user.getRole() == Role.PATIENT) {

            PatientService patientService =
                    new PatientService();

            var patient =
                    patientService.getPatientByUserId(
                            user.getUserId()
                    );

            authorized =
                    patient != null
                            && patient.getPatientId()
                            == request.getPatientId();
        }

        if (!authorized) {
            throw new SecurityException(
                    "You are not authorized to cancel this emergency."
            );
        }

        if (request.getStatus()
                == EmergencyStatus.COMPLETED
                || request.getStatus()
                == EmergencyStatus.CANCELLED) {

            throw new IllegalStateException(
                    "This emergency cannot be cancelled."
            );
        }

        if (request.hasAssignedVehicle()) {

            var vehicle =
                    vehicleRepository.findById(
                            request.getAssignedVehicleId()
                    );

            if (vehicle != null) {

                vehicleRepository.updateStatus(
                        vehicle.getVehicleId(),
                        VehicleStatus.AVAILABLE,
                        vehicle.getCurrentLocation()
                );
            }
        }

        emergencyRepository.updateStatus(
                emergencyId,
                EmergencyStatus.CANCELLED
        );
    }

    /*
     * Return vehicles currently available
     * for emergency dispatch.
     */
    public List<com.medinexus.model.EmergencyVehicle>
    getAvailableVehicles() {

        requireStaff();

        return vehicleRepository
                .findAvailableVehicles();
    }

    /*
     * Return all emergency vehicles.
     */
    public List<com.medinexus.model.EmergencyVehicle>
    getAllVehicles() {

        requireStaff();

        return vehicleRepository.findAll();
    }

    /*
     * Return one emergency request.
     */
    public EmergencyRequest
    getEmergency(long emergencyId) {

        User user = getCurrentUser();

        EmergencyRequest request =
                requireEmergency(
                        emergencyId
                );

        if (user.getRole() == Role.PATIENT) {

            PatientService patientService =
                    new PatientService();

            var patient =
                    patientService.getPatientByUserId(
                            user.getUserId()
                    );

            if (patient == null
                    || patient.getPatientId()
                    != request.getPatientId()) {

                throw new SecurityException(
                        "Patients can only access their own emergency requests."
                );
            }
        }

        return request;
    }

    private void validateRequest(
            EmergencyRequest request) {

        if (request.getPatientId() <= 0) {
            throw new IllegalArgumentException(
                    "A valid patient is required."
            );
        }

        if (isBlank(
                request.getEmergencyType())) {

            throw new IllegalArgumentException(
                    "Emergency type is required."
            );
        }

        if (isBlank(
                request.getPickupLocation())) {

            throw new IllegalArgumentException(
                    "Pickup location is required."
            );
        }

        if (isBlank(
                request.getSeverity())) {

            throw new IllegalArgumentException(
                    "Emergency severity is required."
            );
        }
    }

    private EmergencyRequest
    requireEmergency(long emergencyId) {

        EmergencyRequest request =
                emergencyRepository.findById(
                        emergencyId
                );

        if (request == null) {
            throw new IllegalArgumentException(
                    "Emergency request not found: "
                            + emergencyId
            );
        }

        return request;
    }

    private void requireStatus(
            EmergencyRequest request,
            EmergencyStatus expectedStatus) {

        if (request.getStatus()
                != expectedStatus) {

            throw new IllegalStateException(
                    "Emergency must be in "
                            + expectedStatus
                            + " state."
            );
        }
    }

    private void requireAssignedVehicle(
            EmergencyRequest request) {

        if (!request.hasAssignedVehicle()) {
            throw new IllegalStateException(
                    "No emergency vehicle is assigned."
            );
        }
    }

    private void requireStaff() {

        User user = getCurrentUser();

        if (user.getRole() != Role.ADMIN
                && user.getRole() != Role.RECEPTIONIST) {

            throw new SecurityException(
                    "Only Admin or Receptionist can perform this operation."
            );
        }
    }

    private User getCurrentUser() {

        User user =
                SessionManager.getCurrentUser();

        if (user == null) {
            throw new SecurityException(
                    "You must be logged in."
            );
        }

        return user;
    }

    private boolean isBlank(String value) {

        return value == null
                || value.trim().isEmpty();
    }
}