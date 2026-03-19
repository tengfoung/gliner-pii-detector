package com.piidetector.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.piidetector.config.GlinerConfig;
import com.piidetector.model.TokenizerData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.*;

@Slf4j
@Service
public class TokenizerService {

    private final GlinerConfig config;
    private final ObjectMapper objectMapper;
    private Map<String, Integer> vocab;
    private Map<Integer, String> reverseVocab;
    private static final int CLS_TOKEN_ID = 101;
    private static final int SEP_TOKEN_ID = 102;
    private static final int PAD_TOKEN_ID = 0;

    public TokenizerService(GlinerConfig config, ObjectMapper objectMapper) {
        this.config = config;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void initialize() throws IOException {
        log.info("Loading tokenizer from: {}", config.getModel().getTokenizerPath());
        File tokenizerFile = new File(config.getModel().getTokenizerPath());
        
        TokenizerData tokenizerData = objectMapper.readValue(tokenizerFile, TokenizerData.class);
        
        this.vocab = new HashMap<>();
        if (tokenizerData.getModel() != null && tokenizerData.getModel().getVocab() != null) {
            List<List<Object>> vocabList = tokenizerData.getModel().getVocab();
            for (int i = 0; i < vocabList.size(); i++) {
                List<Object> entry = vocabList.get(i);
                if (entry.size() >= 1 && entry.get(0) instanceof String) {
                    String token = (String) entry.get(0);
                    vocab.put(token, i);
                }
            }
        }
        
        this.reverseVocab = new HashMap<>();
        vocab.forEach((key, value) -> reverseVocab.put(value, key));
        
        log.info("Tokenizer loaded with vocab size: {}", vocab.size());
    }

    public long[] tokenize(String text, int maxLength) {
        List<Long> tokens = new ArrayList<>();
        tokens.add((long) CLS_TOKEN_ID);
        
        String[] words = text.toLowerCase().split("\\s+");
        for (String word : words) {
            if (tokens.size() >= maxLength - 1) break;
            
            Integer tokenId = vocab.getOrDefault(word, vocab.getOrDefault("[UNK]", 100));
            tokens.add(tokenId.longValue());
        }
        
        tokens.add((long) SEP_TOKEN_ID);
        
        while (tokens.size() < maxLength) {
            tokens.add((long) PAD_TOKEN_ID);
        }
        
        return tokens.stream().mapToLong(Long::longValue).toArray();
    }

    public long[] createAttentionMask(long[] inputIds) {
        long[] mask = new long[inputIds.length];
        for (int i = 0; i < inputIds.length; i++) {
            mask[i] = (inputIds[i] != PAD_TOKEN_ID) ? 1L : 0L;
        }
        return mask;
    }
}
