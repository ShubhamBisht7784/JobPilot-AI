package com.jobpilot.recruiter.controller;

import com.jobpilot.recruiter.integration.HunterApiException;
import com.jobpilot.recruiter.integration.SerpApiException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class RecruiterProviderExceptionHandler {

    @ExceptionHandler(HunterApiException.class)
    public ResponseEntity<Map<String, Object>> handleHunterException(
            HunterApiException exception) {

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(Map.of(
                        "provider", "HUNTER",
                        "message", exception.getMessage(),
                        "providerStatus", exception.getStatusCode()
                ));
    }

    @ExceptionHandler(SerpApiException.class)
    public ResponseEntity<Map<String, Object>> handleSerpApiException(
            SerpApiException exception) {

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(Map.of(
                        "provider", "SERPAPI",
                        "message", exception.getMessage(),
                        "providerStatus", exception.getStatusCode()
                ));
    }
}