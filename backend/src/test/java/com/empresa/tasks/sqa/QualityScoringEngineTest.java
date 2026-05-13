package com.empresa.tasks.sqa;

import com.empresa.tasks.sqa.engine.McCallReportParser;
import com.empresa.tasks.sqa.engine.QualityScoreResult;
import com.empresa.tasks.sqa.engine.QualityScoringEngine;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QualityScoringEngineTest {

    @Mock
    private McCallReportParser parser;

    @Test
    void deberiaCalcularScoreCorrectamente() {
        QualityScoringEngine engine = new QualityScoringEngine(parser);

        // Inyectar pesos manualmente (simula application.properties)
        ReflectionTestUtils.setField(engine, "weightTestability", 0.4);
        ReflectionTestUtils.setField(engine, "weightCorrectness", 0.4);
        ReflectionTestUtils.setField(engine, "weightMaintainability", 0.2);
        ReflectionTestUtils.setField(engine, "jacocoPath", "fake/jacoco.xml");
        ReflectionTestUtils.setField(engine, "spotbugsPath", "fake/spotbugs.xml");

        // Simular: 90% cobertura, 0 bugs
        when(parser.parseCoverageFromJacoco("fake/jacoco.xml")).thenReturn(90.0);
        when(parser.parseBugsFromSpotBugs("fake/spotbugs.xml")).thenReturn(0);

        QualityScoreResult result = engine.calculate();

        // correctness=100, maintainability=100, testability=90
        // total = (90*0.4) + (100*0.4) + (100*0.2) = 36+40+20 = 96
        assertEquals(100.0, result.getCorrectness());
        assertEquals(100.0, result.getMaintainability());
        assertEquals(90.0, result.getTestability());
        assertEquals(96.0, result.getTotalScore());
    }

    @Test
    void deberiaPenalizarPorBugs() {
        QualityScoringEngine engine = new QualityScoringEngine(parser);

        ReflectionTestUtils.setField(engine, "weightTestability", 0.4);
        ReflectionTestUtils.setField(engine, "weightCorrectness", 0.4);
        ReflectionTestUtils.setField(engine, "weightMaintainability", 0.2);
        ReflectionTestUtils.setField(engine, "jacocoPath", "fake/jacoco.xml");
        ReflectionTestUtils.setField(engine, "spotbugsPath", "fake/spotbugs.xml");

        // Simular: 80% cobertura, 3 bugs
        when(parser.parseCoverageFromJacoco("fake/jacoco.xml")).thenReturn(80.0);
        when(parser.parseBugsFromSpotBugs("fake/spotbugs.xml")).thenReturn(3);

        QualityScoreResult result = engine.calculate();

        // correctness = 100 - (3*10) = 70
        assertEquals(70.0, result.getCorrectness());
    }

    @Test
    void correctnessNuncaDebeSerNegativa() {
        QualityScoringEngine engine = new QualityScoringEngine(parser);

        ReflectionTestUtils.setField(engine, "weightTestability", 0.4);
        ReflectionTestUtils.setField(engine, "weightCorrectness", 0.4);
        ReflectionTestUtils.setField(engine, "weightMaintainability", 0.2);
        ReflectionTestUtils.setField(engine, "jacocoPath", "fake/jacoco.xml");
        ReflectionTestUtils.setField(engine, "spotbugsPath", "fake/spotbugs.xml");

        // 20 bugs → debería dar 0, no negativo
        when(parser.parseCoverageFromJacoco("fake/jacoco.xml")).thenReturn(50.0);
        when(parser.parseBugsFromSpotBugs("fake/spotbugs.xml")).thenReturn(20);

        QualityScoreResult result = engine.calculate();

        assertEquals(0.0, result.getCorrectness());
        assertTrue(result.getTotalScore() >= 0);
    }
}