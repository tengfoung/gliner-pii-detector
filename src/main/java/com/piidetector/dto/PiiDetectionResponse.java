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
public class PiiDetectionResponse {

    private String originalText;
    private List<DetectedEntity> entities;
    private int entityCount;
    private long processingTimeMs;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetectedEntity {
        private String text;
        private String type;
        private int startIndex;
        private int endIndex;
        private double confidence;
    }
}
