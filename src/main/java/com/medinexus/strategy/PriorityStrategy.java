package com.medinexus.strategy;

import com.medinexus.model.Appointment;
import com.medinexus.model.PriorityResult;

public interface PriorityStrategy {

    PriorityResult calculatePriority(
            Appointment appointment
    );
}