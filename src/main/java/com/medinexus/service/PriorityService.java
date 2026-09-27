package com.medinexus.service;

import com.medinexus.model.Appointment;
import com.medinexus.model.PriorityResult;
import com.medinexus.strategy.PriorityStrategy;

public class PriorityService {

    private final PriorityStrategy strategy;

    public PriorityService(
            PriorityStrategy strategy) {

        this.strategy = strategy;
    }

    public PriorityResult calculatePriority(
            Appointment appointment) {

        return strategy.calculatePriority(
                appointment
        );
    }
}