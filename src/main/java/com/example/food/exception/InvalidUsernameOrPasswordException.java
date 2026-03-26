package com.example.food.exception;

import org.springframework.http.HttpStatus;

public class InvalidUsernameOrPasswordException extends BaseException {
    public InvalidUsernameOrPasswordException(String message) {
        super(message, "Invalid username or password", HttpStatus.BAD_REQUEST);
    }
}
