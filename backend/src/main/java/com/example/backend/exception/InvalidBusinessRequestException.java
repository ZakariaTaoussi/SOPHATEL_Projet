package com.example.backend.exception;

import org.springframework.http.HttpStatus;

public class InvalidBusinessRequestException extends BusinessException {

    public InvalidBusinessRequestException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
