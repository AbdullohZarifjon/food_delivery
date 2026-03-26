package com.example.food.exception;

import org.springframework.http.HttpStatus;

public class RecordAlreadyException extends BaseException {

    public RecordAlreadyException(String message) {
        super(message, "RECORD_ALREADY_EXISTS", HttpStatus.CONFLICT);
    }

}