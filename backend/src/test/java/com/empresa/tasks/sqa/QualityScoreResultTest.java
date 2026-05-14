package com.empresa.tasks.sqa.engine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QualityScoreResultTest {

    @Test
    void deberiaRetornarValoresCorrectos() {
        QualityScoreResult result = new QualityScoreResult(95.0, 88.0, 72.0, 85.0);

        assertEquals(95.0, result.getCorrectness());
        assertEquals(88.0, result.getMaintainability());
        assertEquals(72.0, result.getTestability());
        assertEquals(85.0, result.getTotalScore());
    }

    @Test
    void deberiaAceptarValoresCero() {
        QualityScoreResult result = new QualityScoreResult(0.0, 0.0, 0.0, 0.0);

        assertEquals(0.0, result.getCorrectness());
        assertEquals(0.0, result.getTotalScore());
    }
}
