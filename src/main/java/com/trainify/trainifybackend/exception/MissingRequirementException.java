package com.trainify.trainifybackend.exception;

public class MissingRequirementException extends RuntimeException {
    public MissingRequirementException(String message) {
        super(message);
    }
}
