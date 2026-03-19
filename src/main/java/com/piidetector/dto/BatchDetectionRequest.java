package com.piidetector.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchDetectionRequest {

    @NotEmpty(message = "Texts list cannot be empty")
    private List<String> texts;

    private List<String> entityTypes;

    private Double threshold;
}
