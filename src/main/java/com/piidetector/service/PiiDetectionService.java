package com.piidetector.service;

import com.piidetector.config.GlinerConfig;
import com.piidetector.dto.PiiDetectionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PiiDetectionService {

    private final GlinerConfig config;
    private final TokenizerService tokenizerService;
    private final OnnxModelService onnxModelService;

    public PiiDetectionService(GlinerConfig config, 
                               TokenizerService tokenizerService,
                               OnnxModelService onnxModelService) {
        this.config = config;
        this.tokenizerService = tokenizerService;
        this.onnxModelService = onnxModelService;
    }

    public PiiDetectionResponse detectPii(String text, List<String> entityTypes, Double threshold) {
        long startTime = System.currentTimeMillis();
        
        if (entityTypes == null || entityTypes.isEmpty()) {
            entityTypes = getAllEntityTypes();
        }
        
        double confidenceThreshold = threshold != null ? threshold : config.getModel().getThreshold();
        
        List<PiiDetectionResponse.DetectedEntity> detectedEntities = new ArrayList<>();
        
        detectedEntities.addAll(detectWithPatterns(text, entityTypes, confidenceThreshold));
        
        try {
            detectedEntities.addAll(detectWithModel(text, entityTypes, confidenceThreshold));
        } catch (Exception e) {
            log.error("Error during model inference, falling back to pattern matching only", e);
        }
        
        detectedEntities = mergeDuplicates(detectedEntities);
        
        long processingTime = System.currentTimeMillis() - startTime;
        
        return PiiDetectionResponse.builder()
                .originalText(text)
                .entities(detectedEntities)
                .entityCount(detectedEntities.size())
                .processingTimeMs(processingTime)
                .build();
    }

    private List<PiiDetectionResponse.DetectedEntity> detectWithPatterns(String text, 
                                                                         List<String> entityTypes, 
                                                                         double threshold) {
        List<PiiDetectionResponse.DetectedEntity> entities = new ArrayList<>();
        
        Map<String, Pattern> patterns = getPatterns();
        
        for (String entityType : entityTypes) {
            Pattern pattern = patterns.get(entityType);
            if (pattern != null) {
                Matcher matcher = pattern.matcher(text);
                while (matcher.find()) {
                    entities.add(PiiDetectionResponse.DetectedEntity.builder()
                            .text(matcher.group())
                            .type(entityType)
                            .startIndex(matcher.start())
                            .endIndex(matcher.end())
                            .confidence(0.95)
                            .build());
                }
            }
        }
        
        return entities;
    }

    private List<PiiDetectionResponse.DetectedEntity> detectWithModel(String text, 
                                                                      List<String> entityTypes, 
                                                                      double threshold) {
        List<PiiDetectionResponse.DetectedEntity> entities = new ArrayList<>();
        
        return entities;
    }

    private Map<String, Pattern> getPatterns() {
        Map<String, Pattern> patterns = new HashMap<>();
        
        patterns.put("CREDIT_CARD", Pattern.compile("\\b(?:\\d{4}[- ]?){3}\\d{4}\\b"));
        patterns.put("SSN", Pattern.compile("\\b\\d{3}-\\d{2}-\\d{4}\\b"));
        patterns.put("EMAIL", Pattern.compile("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b"));
        patterns.put("PHONE_NUMBER", Pattern.compile("\\b(?:\\+?1[-.]?)?\\(?\\d{3}\\)?[-.]?\\d{3}[-.]?\\d{4}\\b"));
        patterns.put("BANK_ACCOUNT", Pattern.compile("\\b\\d{8,17}\\b"));
        patterns.put("ROUTING_NUMBER", Pattern.compile("\\b\\d{9}\\b"));
        patterns.put("SWIFT_CODE", Pattern.compile("\\b[A-Z]{6}[A-Z0-9]{2}([A-Z0-9]{3})?\\b"));
        patterns.put("IBAN", Pattern.compile("\\b[A-Z]{2}\\d{2}[A-Z0-9]{1,30}\\b"));
        patterns.put("TICKER_SYMBOL", Pattern.compile("\\b[A-Z]{1,5}\\b"));
        patterns.put("CUSIP", Pattern.compile("\\b[0-9]{3}[0-9A-Z]{5}[0-9]\\b"));
        patterns.put("ISIN", Pattern.compile("\\b[A-Z]{2}[A-Z0-9]{9}\\d\\b"));
        
        return patterns;
    }

    private List<String> getAllEntityTypes() {
        List<String> allTypes = new ArrayList<>();
        config.getEntities().values().forEach(allTypes::addAll);
        return allTypes;
    }

    private List<PiiDetectionResponse.DetectedEntity> mergeDuplicates(
            List<PiiDetectionResponse.DetectedEntity> entities) {
        
        Map<String, PiiDetectionResponse.DetectedEntity> uniqueEntities = new HashMap<>();
        
        for (PiiDetectionResponse.DetectedEntity entity : entities) {
            String key = entity.getStartIndex() + "-" + entity.getEndIndex();
            PiiDetectionResponse.DetectedEntity existing = uniqueEntities.get(key);
            
            if (existing == null || entity.getConfidence() > existing.getConfidence()) {
                uniqueEntities.put(key, entity);
            }
        }
        
        return uniqueEntities.values().stream()
                .sorted(Comparator.comparingInt(PiiDetectionResponse.DetectedEntity::getStartIndex))
                .collect(Collectors.toList());
    }
}
