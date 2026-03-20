package com.piidetector.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RemaskingResponse {

    private String redactedText;
    private String restoredText;
    private int entitiesRestored;
    private boolean success;
    private String message;
}
