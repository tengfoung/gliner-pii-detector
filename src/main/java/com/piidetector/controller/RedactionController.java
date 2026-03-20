package com.piidetector.controller;

import com.piidetector.dto.RedactionRequest;
import com.piidetector.dto.RedactionResponse;
import com.piidetector.dto.RemaskingRequest;
import com.piidetector.dto.RemaskingResponse;
import com.piidetector.service.RedactionService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/redaction")
public class RedactionController {

    private final RedactionService redactionService;

    public RedactionController(RedactionService redactionService) {
        this.redactionService = redactionService;
    }

    @PostMapping("/redact")
    public ResponseEntity<RedactionResponse> redactPii(@Valid @RequestBody RedactionRequest request) {
        log.info("Received redaction request for text length: {}, strategy: {}", 
                request.getText().length(), 
                request.getMaskingStrategy());
        
        RedactionResponse response = redactionService.redactPii(request);
        
        log.info("Redacted {} PII entities in {}ms", 
                response.getEntityCount(), 
                response.getProcessingTimeMs());
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/remask")
    public ResponseEntity<RemaskingResponse> remaskPii(@Valid @RequestBody RemaskingRequest request) {
        log.info("Received remasking request for token: {}", request.getRedactionToken());
        
        RemaskingResponse response = redactionService.remaskPii(request);
        
        if (response.isSuccess()) {
            log.info("Successfully restored {} entities", response.getEntitiesRestored());
        } else {
            log.warn("Remasking failed: {}", response.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/strategies")
    public ResponseEntity<MaskingStrategiesResponse> getMaskingStrategies() {
        return ResponseEntity.ok(new MaskingStrategiesResponse(
                RedactionRequest.MaskingStrategy.values(),
                """
                FULL: Completely masks the value with mask characters (e.g., ********)
                PARTIAL: Shows first and last few characters (e.g., 4532****9010)
                HASH: Replaces with a hash value (e.g., [HASH:A1B2C3D4])
                TOKEN: Replaces with entity type token (e.g., [CREDIT_CARD])
                """
        ));
    }

    public record MaskingStrategiesResponse(
            RedactionRequest.MaskingStrategy[] strategies,
            String description
    ) {}
}
