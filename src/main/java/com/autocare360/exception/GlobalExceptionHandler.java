package com.autocare360.exception;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<Object> handleNotFound(ResourceNotFoundException ex) {
    Map<String, Object> body = new HashMap<>();
    body.put("timestamp", Instant.now());
    body.put("status", 404);
    body.put("error", "Not Found");
    body.put("message", ex.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Object> handleIllegalArgument(IllegalArgumentException ex) {
    Map<String, Object> body = new HashMap<>();
    body.put("error", Map.of("code", "BAD_REQUEST", "message", ex.getMessage()));
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
  }

  @ExceptionHandler(ConflictException.class)
  public ResponseEntity<Object> handleConflict(ConflictException ex) {
    Map<String, Object> body = new HashMap<>();
    body.put("error", Map.of("code", "EMAIL_ALREADY_IN_USE", "message", ex.getMessage()));
    return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Object> handleValidation(MethodArgumentNotValidException ex) {
    Map<String, Object> details = new HashMap<>();
    for (var error : ex.getBindingResult().getAllErrors()) {
      String field = error instanceof FieldError fe ? fe.getField() : error.getObjectName();
      details.put(field, error.getDefaultMessage());
    }
    Map<String, Object> body = new HashMap<>();
    body.put(
        "error",
        Map.of(
            "code", "VALIDATION_ERROR",
            "message", "Validation failed",
            "details", details));
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
  }
}
