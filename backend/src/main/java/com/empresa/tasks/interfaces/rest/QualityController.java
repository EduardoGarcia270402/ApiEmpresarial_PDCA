package com.empresa.tasks.interfaces.rest;

import com.empresa.tasks.sqa.engine.QualityScoreResult;
import com.empresa.tasks.sqa.engine.QualityScoringEngine;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/quality")
@Tag(name = "Quality", description = "Motor de scoring McCall (requiere autenticación)")
@SecurityRequirement(name = "bearerAuth")
public class QualityController {

    private final QualityScoringEngine scoringEngine;

    public QualityController(QualityScoringEngine scoringEngine) {
        this.scoringEngine = scoringEngine;
    }

    @GetMapping("/score")
    @Operation(summary = "Obtener score de calidad", description = "Calcula y retorna el score McCall basado en reportes JaCoCo y SpotBugs")
    public ResponseEntity<QualityScoreResult> getScore() {
        return ResponseEntity.ok(scoringEngine.calculate());
    }
}