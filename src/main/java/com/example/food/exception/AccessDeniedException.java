package com.example.food.exception;

import org.springframework.http.HttpStatus;

public class AccessDeniedException extends BaseException {
  public AccessDeniedException(String message) {
    // "ACCESS_DENIED_ERROR" - bu front-end xatolikni tanib olishi uchun kalit so'z
    // HttpStatus.FORBIDDEN - ya'ni 403 status kodi
    super(message, "ACCESS_DENIED_ERROR", HttpStatus.FORBIDDEN);
  }
}
