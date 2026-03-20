package com.piidetector.controller;

import com.piidetector.dto.BatchDetectionRequest;
import com.piidetector.dto.BatchDetectionResponse;
import com.piidetector.dto.PiiDetectionRequest;
import com.piidetector.dto.PiiDetectionResponse;
import com.piidetector.service.PiiDetectionService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/pii")
public class PiiDetectionController {

    private final PiiDetectionService piiDetectionService;

    public PiiDetectionController(PiiDetectionService piiDetectionService) {
        this.piiDetectionService = piiDetectionService;
    }

    @PostMapping("/detect")
    public ResponseEntity<PiiDetectionResponse> detectPii(@Valid @RequestBody PiiDetectionRequest request) {
        log.info("Received PII detection request for text length: {}", request.getText().length());
        
        PiiDetectionResponse response = piiDetectionService.detectPii(
                request.getText(),
                request.getEntityTypes()
        );
        
        log.info("Detected {} PII entities in {}ms", 
                response.getEntityCount(), 
                response.getProcessingTimeMs());
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/detect/batch")
    public ResponseEntity<BatchDetectionResponse> detectPiiBatch(@Valid @RequestBody BatchDetectionRequest request) {
        log.info("Received batch PII detection request for {} texts", request.getTexts().size());
        
        long startTime = System.currentTimeMillis();
        List<PiiDetectionResponse> results = new ArrayList<>();
        int totalEntities = 0;
        
        for (String text : request.getTexts()) {
            PiiDetectionResponse response = piiDetectionService.detectPii(
                    text,
                    request.getEntityTypes()
            );
            results.add(response);
            totalEntities += response.getEntityCount();
        }
        
        long totalTime = System.currentTimeMillis() - startTime;
        
        BatchDetectionResponse batchResponse = BatchDetectionResponse.builder()
                .results(results)
                .totalTexts(request.getTexts().size())
                .totalEntitiesFound(totalEntities)
                .totalProcessingTimeMs(totalTime)
                .build();
        
        log.info("Batch processing completed: {} texts, {} entities, {}ms", 
                batchResponse.getTotalTexts(),
                batchResponse.getTotalEntitiesFound(),
                batchResponse.getTotalProcessingTimeMs());
        
        return ResponseEntity.ok(batchResponse);
    }

    @GetMapping("/health")
    public ResponseEntity<HealthResponse> health() {
        return ResponseEntity.ok(new HealthResponse("UP", "PII Detection Service is running"));
    }

    public record HealthResponse(String status, String message) {}
}
