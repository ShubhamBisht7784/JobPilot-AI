package com.jobpilot.recruiter.integration;

import lombok.Getter;

@Getter
public class SerpApiException extends RuntimeException {

    private final int statusCode;

    public SerpApiException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }
}