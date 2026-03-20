package com.piidetector.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RemaskingRequest {

    @NotBlank(message = "Redacted text is required")
    private String redactedText;

    @NotBlank(message = "Redaction token is required")
    private String redactionToken;
}
