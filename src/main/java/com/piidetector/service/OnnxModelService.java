package com.piidetector.service;

import ai.onnxruntime.*;
import com.piidetector.config.GlinerConfig;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class OnnxModelService {

    private final GlinerConfig config;
    private OrtEnvironment environment;
    private OrtSession session;

    public OnnxModelService(GlinerConfig config) {
        this.config = config;
    }

    @PostConstruct
    public void initialize() throws OrtException {
        log.info("Initializing ONNX Runtime environment");
        this.environment = OrtEnvironment.getEnvironment();
        
        File modelFile = new File(config.getModel().getPath());
        log.info("Loading ONNX model from: {}", modelFile.getAbsolutePath());
        
        OrtSession.SessionOptions options = new OrtSession.SessionOptions();
        options.setOptimizationLevel(OrtSession.SessionOptions.OptLevel.BASIC_OPT);
        
        this.session = environment.createSession(modelFile.getAbsolutePath(), options);
        
        log.info("ONNX model loaded successfully");
        log.info("Model inputs: {}", session.getInputNames());
        log.info("Model outputs: {}", session.getOutputNames());
    }

    public OrtSession.Result runInference(long[] inputIds, long[] attentionMask, long[] entityIds) throws OrtException {
        long batchSize = 1;
        long seqLength = inputIds.length;
        long numEntities = entityIds.length;
        
        OnnxTensor inputIdsTensor = OnnxTensor.createTensor(environment, 
            reshape(inputIds, (int)batchSize, (int)seqLength));
        OnnxTensor attentionMaskTensor = OnnxTensor.createTensor(environment, 
            reshape(attentionMask, (int)batchSize, (int)seqLength));
        OnnxTensor entityIdsTensor = OnnxTensor.createTensor(environment, 
            reshape(entityIds, (int)batchSize, (int)numEntities));
        
        Map<String, OnnxTensor> inputs = new HashMap<>();
        inputs.put("input_ids", inputIdsTensor);
        inputs.put("attention_mask", attentionMaskTensor);
        inputs.put("entity_ids", entityIdsTensor);
        
        return session.run(inputs);
    }

    private long[][] reshape(long[] array, int rows, int cols) {
        long[][] result = new long[rows][cols];
        
        for (int i = 0; i < rows; i++) {
            System.arraycopy(array, i * cols, result[i], 0, cols);
        }
        
        return result;
    }

    @PreDestroy
    public void cleanup() throws OrtException {
        if (session != null) {
            session.close();
            log.info("ONNX session closed");
        }
        if (environment != null) {
            environment.close();
            log.info("ONNX environment closed");
        }
    }
}
