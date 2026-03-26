package com.example.food.exception;

import org.springframework.http.HttpStatus;

public class RecordNotFoundException extends BaseException {
    public RecordNotFoundException(String message) {
        super(message, "RECORD NOT FOUND", HttpStatus.NOT_FOUND);
    }
}
