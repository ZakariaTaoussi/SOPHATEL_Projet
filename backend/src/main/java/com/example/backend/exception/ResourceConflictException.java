package com.example.backend.exception;

import org.springframework.http.HttpStatus;

public class ResourceConflictException extends BusinessException {

    public ResourceConflictException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
