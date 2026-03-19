package com.piidetector.exception;

public class ModelLoadException extends RuntimeException {
    
    public ModelLoadException(String message) {
        super(message);
    }
    
    public ModelLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
