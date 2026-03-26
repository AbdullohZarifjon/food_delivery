package com.example.food.exception;

import com.example.food.dto.response.ApiResponse;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Content-Type xatolarini ushlash (Siz so'ragan asosiy qism)
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<Object>> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex,
            HttpServletRequest request) {

        String supportedTypes = ex.getSupportedMediaTypes().stream()
                .map(MediaType::toString)
                .collect(Collectors.joining(", "));

        String message = String.format(
                "Yuborilgan Content-Type '%s' qo'llab-quvvatlanmaydi. Ruxsat etilgan turlar: %s. " +
                        "Maslahat: Agar multipart so'rov bo'lsa, 'data' qismi uchun 'Content-Type: application/json' ekanligini tekshiring.",
                ex.getContentType(),
                supportedTypes
        );

        return createErrorResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE, message, request);
    }

    // 2. Multipart qismlari (data yoki file) umuman kelmasa ushlash
    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ApiResponse<Object>> handleMissingServletRequestPart(
            org.springframework.web.multipart.support.MissingServletRequestPartException ex,
            HttpServletRequest request) {

        String message = String.format("So'rovning majburiy qismi '%s' topilmadi", ex.getRequestPartName());
        return createErrorResponse(HttpStatus.BAD_REQUEST, message, request);
    }


    // Security xatolarini ushlash (401 va 403)
    @ExceptionHandler({AuthenticationException.class, InsufficientAuthenticationException.class})
    public ResponseEntity<ApiResponse<Object>> handleSecurityError(Exception ex, HttpServletRequest request) {
        return createErrorResponse(HttpStatus.UNAUTHORIZED, "Autentifikatsiya xatosi: Login yoki token xato", request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return createErrorResponse(HttpStatus.FORBIDDEN, "Sizga ushbu amalni bajarish ruxsat etilmagan", request);
    }


    // Custom biznes xatolar (BaseException)
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiResponse<Object>> handleBaseException(BaseException ex, HttpServletRequest request) {
        return createErrorResponse(ex.getHttpStatus(), ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        // Senior Touch: Xatoni aniqroq ko'rsatamiz
        String message = String.format("Parametr '%s' noto'g'ri formatda. Kutilayotgan tur: %s",
                ex.getName(), ex.getRequiredType().getSimpleName());

        return createErrorResponse(HttpStatus.BAD_REQUEST, message, request);
    }

    // Majburiy parametrlar kelmaganda
    // Enum bo'lgan ma'lumotlar agarda menda bo'lmasa shu chiqadi!
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Object>> handleMissingParameter(MissingServletRequestParameterException ex, HttpServletRequest request) {
        String message = String.format("Majburiy parametr '%s' yuborilmagan", ex.getParameterName());
        return createErrorResponse(HttpStatus.BAD_REQUEST, message, request);
    }

    // Validatsiya xatolari (DTO @Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String msg = ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        return createErrorResponse(HttpStatus.BAD_REQUEST, msg, request);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGeneral(Exception ex, HttpServletRequest request) {
        System.out.println(ex.getMessage());
        ex.printStackTrace();
        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Kutilmagan server xatoligi", request);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNoHandlerFound(NoHandlerFoundException ex, HttpServletRequest request) {
        String message = String.format("Kechirasiz, siz so'ragan manzil '%s' serverda topilmadi.", ex.getRequestURL());
        return createErrorResponse(HttpStatus.NOT_FOUND, message, request);
    }


    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Object>> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {

        String message = "JSON formatida xatolik aniqlandi. ";

        // Senior Touch: Xatoni ichidan aniqroq sababni qidiramiz
        if (ex.getCause() instanceof MismatchedInputException mismatchedEx) {
            String fieldName = mismatchedEx.getPath().stream()
                    .map(JsonMappingException.Reference::getFieldName)
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining("."));

            message += String.format("'%s' maydoniga noto'g'ri turdagi ma'lumot yuborilgan. ", fieldName);
        }

        message += "Iltimos, yuborilayotgan JSON strukturasini tekshiring (Massiv o'rniga ob'ekt yuborilmaganiga ishonch hosil qiling).";

        return createErrorResponse(HttpStatus.BAD_REQUEST, message, request);
    }

    private ResponseEntity<ApiResponse<Object>> createErrorResponse(HttpStatus status, String message, HttpServletRequest request) {
        ApiResponse<Object> response = ApiResponse.builder()
                .success(false)
                .statusCode(status.value())
                .timestamp(LocalDateTime.now().toString())
                .path(request.getRequestURI())
                .method(request.getMethod())
                .message(message)
                .build();
        return new ResponseEntity<>(response, status);
    }
}
