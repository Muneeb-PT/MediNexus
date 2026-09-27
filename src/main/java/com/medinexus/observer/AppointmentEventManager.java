package com.medinexus.observer;

import com.medinexus.model.Appointment;

import java.util.ArrayList;
import java.util.List;

public class AppointmentEventManager {

    private final List<AppointmentObserver> observers;

    public AppointmentEventManager() {
        observers = new ArrayList<>();
    }

    public void addObserver(
            AppointmentObserver observer) {

        if (observer != null
                && !observers.contains(observer)) {

            observers.add(observer);
        }
    }

    public void removeObserver(
            AppointmentObserver observer) {

        observers.remove(observer);
    }

    public void notifyAppointmentChanged(
            Appointment appointment,
            String event) {

        if (appointment == null) {
            return;
        }

        if (event == null || event.isBlank()) {
            return;
        }

        for (AppointmentObserver observer
                : new ArrayList<>(observers)) {

            observer.onAppointmentChanged(
                    appointment,
                    event
            );
        }
    }

    public int getObserverCount() {
        return observers.size();
    }
}