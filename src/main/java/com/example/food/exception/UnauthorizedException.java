package com.example.food.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends BaseException {
    public UnauthorizedException(String message) {
        super(message, "UNAUTHORIZED_ACCESS", HttpStatus.UNAUTHORIZED);
    }
}


