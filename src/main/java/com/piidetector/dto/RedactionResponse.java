package com.piidetector.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RedactionResponse {

    private String originalText;
    private String redactedText;
    private List<RedactedEntity> redactedEntities;
    private int entityCount;
    private String redactionToken;
    private long processingTimeMs;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RedactedEntity {
        private String originalValue;
        private String maskedValue;
        private String type;
        private int startIndex;
        private int endIndex;
        private double confidence;
        private String entityToken;
    }
}
