package com.empresa.tasks.sqa.engine;

public class QualityScoreResult {

    private double correctness;
    private double maintainability;
    private double testability;
    private double totalScore;

    public QualityScoreResult(double correctness, double maintainability,
                               double testability, double totalScore) {
        this.correctness = correctness;
        this.maintainability = maintainability;
        this.testability = testability;
        this.totalScore = totalScore;
    }

    public double getCorrectness() { return correctness; }
    public double getMaintainability() { return maintainability; }
    public double getTestability() { return testability; }
    public double getTotalScore() { return totalScore; }
}