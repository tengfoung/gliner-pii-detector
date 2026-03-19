package com.piidetector.controller;

import com.piidetector.config.GlinerConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/entity-types")
public class EntityTypesController {

    private final GlinerConfig config;

    public EntityTypesController(GlinerConfig config) {
        this.config = config;
    }

    @GetMapping
    public ResponseEntity<EntityTypesResponse> getAllEntityTypes() {
        Map<String, List<String>> entities = config.getEntities();
        
        List<String> allTypes = new ArrayList<>();
        entities.values().forEach(allTypes::addAll);
        
        EntityTypesResponse response = new EntityTypesResponse(
                entities,
                allTypes,
                allTypes.size()
        );
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/financial")
    public ResponseEntity<List<String>> getFinancialEntityTypes() {
        return ResponseEntity.ok(config.getEntities().get("financial"));
    }

    @GetMapping("/trading")
    public ResponseEntity<List<String>> getTradingEntityTypes() {
        return ResponseEntity.ok(config.getEntities().get("trading"));
    }

    @GetMapping("/personal")
    public ResponseEntity<List<String>> getPersonalEntityTypes() {
        return ResponseEntity.ok(config.getEntities().get("personal"));
    }

    public record EntityTypesResponse(
            Map<String, List<String>> categorized,
            List<String> all,
            int count
    ) {}
}
