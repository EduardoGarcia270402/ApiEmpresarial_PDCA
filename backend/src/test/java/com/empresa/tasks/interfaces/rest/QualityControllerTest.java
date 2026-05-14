package com.empresa.tasks.interfaces.rest;

import com.empresa.tasks.sqa.engine.QualityScoreResult;
import com.empresa.tasks.sqa.engine.QualityScoringEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QualityControllerTest {

    @Mock
    private QualityScoringEngine scoringEngine;

    private QualityController controller;

    @BeforeEach
    void setUp() {
        controller = new QualityController(scoringEngine);
    }

    @Test
    void deberiaRetornarScoreDeCalidad() {
        QualityScoreResult result = new QualityScoreResult(90.0, 85.0, 80.0, 85.0);
        when(scoringEngine.calculate()).thenReturn(result);

        ResponseEntity<QualityScoreResult> response = controller.getScore();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(85.0, response.getBody().getTotalScore());
    }
}
