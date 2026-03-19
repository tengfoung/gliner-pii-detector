package com.piidetector.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PiiDetectionRequest {

    @NotBlank(message = "Text cannot be blank")
    private String text;

    private List<String> entityTypes;

    private Double threshold;
}
