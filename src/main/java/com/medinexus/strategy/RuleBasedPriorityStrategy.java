package com.medinexus.strategy;

import com.medinexus.model.Appointment;
import com.medinexus.model.PriorityResult;

public class RuleBasedPriorityStrategy
        implements PriorityStrategy {

    @Override
    public PriorityResult calculatePriority(
            Appointment appointment) {

        if (appointment == null) {

            return new PriorityResult(
                    0,
                    "ROUTINE",
                    "No appointment information available."
            );
        }

        int score = 0;

        String urgency =
                appointment.getUrgency();

        String reason =
                appointment.getReason();


        // -------------------------------------------------
        // URGENCY SCORE
        // -------------------------------------------------

        if (urgency != null) {

            switch (urgency.toLowerCase()) {

                case "emergency":
                    score += 80;
                    break;

                case "chronic":
                    score += 40;
                    break;

                case "routine":
                    score += 10;
                    break;

                default:
                    break;
            }
        }


        // -------------------------------------------------
        // REASON SCORE
        // -------------------------------------------------

        if (reason != null) {

            String lowerReason =
                    reason.toLowerCase();

            if (lowerReason.contains("critical")
                    || lowerReason.contains("severe")) {

                score += 20;

            } else if (lowerReason.contains("urgent")) {

                score += 15;

            } else if (lowerReason.contains("follow-up")
                    || lowerReason.contains("follow up")) {

                score += 5;
            }
        }


        // -------------------------------------------------
        // CATEGORY
        // -------------------------------------------------

        String category;

        if (score >= 90) {

            category = "EMERGENCY";

        } else if (score >= 60) {

            category = "HIGH";

        } else if (score >= 30) {

            category = "MEDIUM";

        } else {

            category = "ROUTINE";
        }


        // -------------------------------------------------
        // EXPLANATION
        // -------------------------------------------------

        String explanation =
                "Priority calculated from "
                        + "urgency and appointment reason.";


        return new PriorityResult(
                score,
                category,
                explanation
        );
    }
}