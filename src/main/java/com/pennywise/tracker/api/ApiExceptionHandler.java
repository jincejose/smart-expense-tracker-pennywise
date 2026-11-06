package com.pennywise.tracker.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, String>> invalid(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream().findFirst().map(error -> error.getField() + " " + error.getDefaultMessage()).orElse("Request validation failed");
        return ResponseEntity.badRequest().body(Map.of("error", message));
    }
    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<Map<String, String>> invalidBudget(IllegalArgumentException exception) { return ResponseEntity.badRequest().body(Map.of("error", exception.getMessage())); }
    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<Map<String, String>> status(ResponseStatusException exception) { return ResponseEntity.status(exception.getStatusCode()).body(Map.of("error", exception.getReason() == null ? "Request failed" : exception.getReason())); }
    @ExceptionHandler(Exception.class)
    ResponseEntity<Map<String, String>> unexpected(Exception exception) { return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Something went wrong while processing the request.")); }
}
