package com.dineo_backend.dineo.shared.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

/**
 * Standardized API response structure for all endpoints
 */
public class ApiResponse<T> {
    
    private boolean success;
    private int status;
    private String message;
    private T data;
    
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
    
    // Default constructor
    public ApiResponse() {
        this.timestamp = LocalDateTime.now();
    }
    
    // Constructor for success responses with data
    public ApiResponse(boolean success, int status, String message, T data) {
        this();
        this.success = success;
        this.status = status;
        this.message = message;
        this.data = data;
    }
    
    // Constructor for responses without data
    public ApiResponse(boolean success, int status, String message) {
        this(success, status, message, null);
    }
    
    // Static methods for common response types
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, HttpStatus.OK.value(), message, data);
    }
    
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, HttpStatus.OK.value(), message, null);
    }
    
    public static <T> ApiResponse<T> created(String message, T data) {
        return new ApiResponse<>(true, HttpStatus.CREATED.value(), message, data);
    }
    
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, HttpStatus.BAD_REQUEST.value(), message, null);
    }
    
    public static <T> ApiResponse<T> error(int status, String message) {
        return new ApiResponse<>(false, status, message, null);
    }
    
    public static <T> ApiResponse<T> internalError(String message) {
        return new ApiResponse<>(false, HttpStatus.INTERNAL_SERVER_ERROR.value(), message, null);
    }
    
    public static <T> ApiResponse<T> unauthorized(String message) {
        return new ApiResponse<>(false, HttpStatus.UNAUTHORIZED.value(), message, null);
    }
    
    public static <T> ApiResponse<T> forbidden(String message) {
        return new ApiResponse<>(false, HttpStatus.FORBIDDEN.value(), message, null);
    }
    
    public static <T> ApiResponse<T> notFound(String message) {
        return new ApiResponse<>(false, HttpStatus.NOT_FOUND.value(), message, null);
    }
    
    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public int getStatus() {
        return status;
    }
    
    public void setStatus(int status) {
        this.status = status;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public T getData() {
        return data;
    }
    
    public void setData(T data) {
        this.data = data;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}