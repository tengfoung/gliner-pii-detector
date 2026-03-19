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
public class BatchDetectionResponse {

    private List<PiiDetectionResponse> results;
    private int totalTexts;
    private int totalEntitiesFound;
    private long totalProcessingTimeMs;
}
