package com.medinexus.model;

public class PriorityResult {

    private final int score;
    private final String category;
    private final String explanation;

    public PriorityResult(
            int score,
            String category,
            String explanation) {

        this.score = score;
        this.category = category;
        this.explanation = explanation;
    }

    public int getScore() {
        return score;
    }

    public String getCategory() {
        return category;
    }

    public String getExplanation() {
        return explanation;
    }
}