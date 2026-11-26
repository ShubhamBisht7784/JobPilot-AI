package com.jobpilot.email.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class TokenEncryptionServiceTest {

    private static final String TEST_KEY =
            "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";

    @Test
    void encryptAndDecryptShouldRoundTrip() {
        TokenEncryptionService service = new TokenEncryptionService(TEST_KEY);

        String encrypted = service.encrypt("refresh-token-value");

        assertThat(encrypted).isNotEqualTo("refresh-token-value");
        assertThat(service.decrypt(encrypted)).isEqualTo("refresh-token-value");
    }

    @Test
    void encryptionShouldFailClearlyWhenKeyIsNotConfigured() {
        TokenEncryptionService service = new TokenEncryptionService("");

        assertThatThrownBy(() -> service.encrypt("token"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void constructorShouldRejectWrongKeyLength() {
        assertThatThrownBy(() -> new TokenEncryptionService("YWJj"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
