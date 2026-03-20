package com.piidetector.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RedactionRequest {

    @NotBlank(message = "Text is required")
    private String text;

    private List<String> entityTypes;

    private Double threshold;

    @Builder.Default
    private String maskCharacter = "*";

    @Builder.Default
    private MaskingStrategy maskingStrategy = MaskingStrategy.FULL;

    public enum MaskingStrategy {
        FULL,
        PARTIAL,
        HASH,
        TOKEN
    }
}
