package com.piidetector.service;

import com.piidetector.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class RedactionService {

    private final PiiDetectionService piiDetectionService;
    private final Map<String, RedactionContext> redactionCache = new ConcurrentHashMap<>();

    public RedactionService(PiiDetectionService piiDetectionService) {
        this.piiDetectionService = piiDetectionService;
    }

    public RedactionResponse redactPii(RedactionRequest request) {
        long startTime = System.currentTimeMillis();

        PiiDetectionResponse detectionResponse = piiDetectionService.detectPii(
                request.getText(),
                request.getEntityTypes()
        );

        String maskChar = request.getMaskCharacter() != null ? request.getMaskCharacter() : "*";
        RedactionRequest.MaskingStrategy strategy = request.getMaskingStrategy() != null ? request.getMaskingStrategy() : RedactionRequest.MaskingStrategy.FULL;

        String redactionToken = generateRedactionToken();
        StringBuilder redactedText = new StringBuilder(request.getText());
        List<RedactionResponse.RedactedEntity> redactedEntities = new ArrayList<>();
        Map<String, String> entityTokenMap = new HashMap<>();

        List<PiiDetectionResponse.DetectedEntity> sortedEntities = new ArrayList<>(detectionResponse.getEntities());
        sortedEntities.sort(Comparator.comparingInt(PiiDetectionResponse.DetectedEntity::getStartIndex).reversed());

        for (PiiDetectionResponse.DetectedEntity entity : sortedEntities) {
            String originalValue = entity.getText();
            String maskedValue = maskValue(originalValue, strategy, maskChar, entity.getType());
            String entityToken = generateEntityToken(redactionToken, entity.getStartIndex());

            redactedText.replace(entity.getStartIndex(), entity.getEndIndex(), maskedValue);

            RedactionResponse.RedactedEntity redactedEntity = RedactionResponse.RedactedEntity.builder()
                    .originalValue(originalValue)
                    .maskedValue(maskedValue)
                    .type(entity.getType())
                    .startIndex(entity.getStartIndex())
                    .endIndex(entity.getStartIndex() + maskedValue.length())
                    .confidence(entity.getConfidence())
                    .entityToken(entityToken)
                    .build();

            redactedEntities.add(redactedEntity);
            entityTokenMap.put(entityToken, originalValue);
        }

        redactedEntities.sort(Comparator.comparingInt(RedactionResponse.RedactedEntity::getStartIndex));

        RedactionContext context = new RedactionContext(
                request.getText(),
                redactedText.toString(),
                entityTokenMap,
                System.currentTimeMillis()
        );
        redactionCache.put(redactionToken, context);

        long processingTime = System.currentTimeMillis() - startTime;

        log.info("Redacted {} PII entities in {}ms", redactedEntities.size(), processingTime);

        return RedactionResponse.builder()
                .originalText(request.getText())
                .redactedText(redactedText.toString())
                .redactedEntities(redactedEntities)
                .entityCount(redactedEntities.size())
                .redactionToken(redactionToken)
                .processingTimeMs(processingTime)
                .build();
    }

    public RemaskingResponse remaskPii(RemaskingRequest request) {
        RedactionContext context = redactionCache.get(request.getRedactionToken());

        if (context == null) {
            log.warn("Redaction token not found or expired: {}", request.getRedactionToken());
            return RemaskingResponse.builder()
                    .redactedText(request.getRedactedText())
                    .restoredText(null)
                    .entitiesRestored(0)
                    .success(false)
                    .message("Redaction token not found or expired")
                    .build();
        }

        String restoredText = context.originalText();
        int entitiesRestored = context.entityTokenMap().size();

        log.info("Restored {} PII entities using token: {}", entitiesRestored, request.getRedactionToken());

        return RemaskingResponse.builder()
                .redactedText(request.getRedactedText())
                .restoredText(restoredText)
                .entitiesRestored(entitiesRestored)
                .success(true)
                .message("Successfully restored original text")
                .build();
    }

    private String maskValue(String value, RedactionRequest.MaskingStrategy strategy, String maskChar, String entityType) {
        if (value == null || value.isEmpty()) {
            return value;
        }

        return switch (strategy) {
            case FULL -> maskChar.repeat(value.length());
            case PARTIAL -> maskPartial(value, maskChar);
            case HASH -> hashValue(value);
            case TOKEN -> generateTypeToken(entityType);
        };
    }

    private String maskPartial(String value, String maskChar) {
        int length = value.length();
        if (length <= 4) {
            return maskChar.repeat(length);
        }

        int visibleChars = Math.min(4, length / 4);
        int maskLength = length - (visibleChars * 2);

        return value.substring(0, visibleChars) +
                maskChar.repeat(maskLength) +
                value.substring(length - visibleChars);
    }

    private String hashValue(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (int i = 0; i < Math.min(8, hash.length); i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return "[HASH:" + hexString.toString().toUpperCase() + "]";
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256 algorithm not available", e);
            return "[HASH:UNAVAILABLE]";
        }
    }

    private String generateTypeToken(String entityType) {
        return "[" + entityType + "]";
    }

    private String generateRedactionToken() {
        return "RDT-" + UUID.randomUUID().toString();
    }

    private String generateEntityToken(String redactionToken, int index) {
        return redactionToken + "-E" + index;
    }

    public void clearExpiredTokens(long expirationTimeMs) {
        long currentTime = System.currentTimeMillis();
        redactionCache.entrySet().removeIf(entry ->
                currentTime - entry.getValue().timestamp() > expirationTimeMs
        );
    }

    private static record RedactionContext(
            String originalText,
            String redactedText,
            Map<String, String> entityTokenMap,
            long timestamp
    ) {}
}
