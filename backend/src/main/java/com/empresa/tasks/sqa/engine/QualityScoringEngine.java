package com.empresa.tasks.sqa.engine;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class QualityScoringEngine {

    @Value("${sqa.weights.testability:0.4}")
    private double weightTestability;

    @Value("${sqa.weights.correctness:0.4}")
    private double weightCorrectness;

    @Value("${sqa.weights.maintainability:0.2}")
    private double weightMaintainability;

    @Value("${sqa.jacoco.path:target/site/jacoco/jacoco.xml}")
    private String jacocoPath;

    @Value("${sqa.spotbugs.path:target/spotbugsXml.xml}")
    private String spotbugsPath;

    private final McCallReportParser parser;

    public QualityScoringEngine(McCallReportParser parser) {
        this.parser = parser;
    }

    public QualityScoreResult calculate() {
        // Testability: cobertura de código (0-100)
        double coverage = parser.parseCoverageFromJacoco(jacocoPath);

        // Correctness: bugs detectados (0 bugs = 100, cada bug resta 10)
        int bugs = parser.parseBugsFromSpotBugs(spotbugsPath);
        double correctness = Math.max(0, 100.0 - (bugs * 10.0));

        // Maintainability: fijo en 100 si Clean Architecture está correcta
        double maintainability = 100.0;

        // Score total ponderado
        double total = (coverage * weightTestability)
                     + (correctness * weightCorrectness)
                     + (maintainability * weightMaintainability);

        return new QualityScoreResult(
            Math.round(correctness * 10.0) / 10.0,
            Math.round(maintainability * 10.0) / 10.0,
            Math.round(coverage * 10.0) / 10.0,
            Math.round(total * 10.0) / 10.0
        );
    }
}