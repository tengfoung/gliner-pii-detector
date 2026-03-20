package com.piidetector.service;

import com.piidetector.config.GlinerConfig;
import com.piidetector.dto.PiiDetectionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PiiDetectionService {

    private final GlinerConfig config;
    private final RestTemplate restTemplate;
    
    @Value("${gliner.python.service.url:http://localhost:5001}")
    private String pythonServiceUrl;

    public PiiDetectionService(GlinerConfig config) {
        this.config = config;
        this.restTemplate = new RestTemplate();
    }

    public PiiDetectionResponse detectPii(String text, List<String> entityTypes) {
        long startTime = System.currentTimeMillis();
        
        if (entityTypes == null || entityTypes.isEmpty()) {
            entityTypes = getAllEntityTypes();
        }
        
        List<PiiDetectionResponse.DetectedEntity> detectedEntities = new ArrayList<>();
        
        try {
            detectedEntities = detectWithModel(text, entityTypes);
        } catch (Exception e) {
            log.error("Error during model inference", e);
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

    private List<PiiDetectionResponse.DetectedEntity> detectWithModel(String text,
                                                                       List<String> entityTypes) throws Exception {
        List<PiiDetectionResponse.DetectedEntity> entities = new ArrayList<>();
        
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("text", text);
            requestBody.put("entity_types", entityTypes);
            requestBody.put("threshold", config.getModel().getThreshold());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            String url = pythonServiceUrl + "/detect";
            log.info("Calling Python service at: {}", url);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                List<Map<String, Object>> detectedEntities = (List<Map<String, Object>>) responseBody.get("entities");
                
                if (detectedEntities != null) {
                    for (Map<String, Object> entity : detectedEntities) {
                        entities.add(PiiDetectionResponse.DetectedEntity.builder()
                                .text((String) entity.get("text"))
                                .type((String) entity.get("type"))
                                .startIndex(((Number) entity.get("start_index")).intValue())
                                .endIndex(((Number) entity.get("end_index")).intValue())
                                .confidence(((Number) entity.get("confidence")).doubleValue())
                                .build());
                    }
                }
                
                log.info("Python service returned {} entities", entities.size());
            }
        } catch (Exception e) {
            log.error("Error calling Python service: {}", e.getMessage());
            throw e;
        }
        
        return entities;
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
