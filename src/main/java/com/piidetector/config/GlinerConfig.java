package com.piidetector.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "gliner")
public class GlinerConfig {

    private Model model = new Model();
    private Map<String, List<String>> entities;

    @Data
    public static class Model {
        private double threshold = 0.5;
    }
}
