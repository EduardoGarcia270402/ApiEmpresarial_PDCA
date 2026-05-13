package com.empresa.tasks.interfaces.rest;

import com.empresa.tasks.sqa.engine.QualityScoreResult;
import com.empresa.tasks.sqa.engine.QualityScoringEngine;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/quality")
public class QualityController {

    private final QualityScoringEngine scoringEngine;

    public QualityController(QualityScoringEngine scoringEngine) {
        this.scoringEngine = scoringEngine;
    }

    @GetMapping("/score")
    public ResponseEntity<QualityScoreResult> getScore() {
        return ResponseEntity.ok(scoringEngine.calculate());
    }
}