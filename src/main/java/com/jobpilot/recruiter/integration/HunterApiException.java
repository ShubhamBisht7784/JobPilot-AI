package com.jobpilot.recruiter.integration;

import lombok.Getter;

@Getter
public class HunterApiException extends RuntimeException {

    private final int statusCode;

    public HunterApiException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }
}