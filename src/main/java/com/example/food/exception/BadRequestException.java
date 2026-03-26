package com.example.food.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends BaseException {
    public BadRequestException(String message) {
        super(message, "BAD_REQUEST_ERROR", HttpStatus.BAD_REQUEST);
    }
}
