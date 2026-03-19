package com.piidetector.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TokenizerData {

    private Model model;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Model {
        private String type;
        @JsonProperty("unk_id")
        private Integer unkId;
        private List<List<Object>> vocab;
    }

    @JsonProperty("added_tokens")
    private List<AddedToken> addedTokens;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AddedToken {
        private int id;
        private String content;
    }
}
