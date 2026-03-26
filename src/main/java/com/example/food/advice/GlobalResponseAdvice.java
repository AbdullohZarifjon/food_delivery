package com.example.food.advice;

import com.example.food.dto.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestControllerAdvice
public class GlobalResponseAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        if (returnType.getParameterType().equals(ApiResponse.class)) {
            return false;
        }

        String className = returnType.getDeclaringClass().getName();

        return !className.contains("springdoc")
                && !className.contains("swagger")
                && !className.contains("BasicErrorController")
                && !className.contains("FileDisplayController");
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {

        if (body == null
                || body instanceof ApiResponse
                || body instanceof SseEmitter
                || body instanceof Resource) {
            return body;
        }

        String path = request.getURI().getPath();
        if (path.contains("/v3/api-docs") || path.contains("/swagger-ui")) {
            return body;
        }

        if (body instanceof String) {
            return body;
        }

        int status = 200;
        if (response instanceof ServletServerHttpResponse) {
            status = ((ServletServerHttpResponse) response).getServletResponse().getStatus();
        }

        HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
        String method = servletRequest.getMethod();

        return ApiResponse.success(
                status,
                method,
                path,
                getCustomMessage(method),
                body
        );
    }

    private String getCustomMessage(String method) {
        return switch (method) {
            case "POST" -> "Created Successfully";
            case "PUT", "PATCH" -> "Updated Successfully";
            case "DELETE" -> "Deleted Successfully";
            default -> "Fetched Successfully";
        };
    }
}
