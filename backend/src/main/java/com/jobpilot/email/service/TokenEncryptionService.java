
package com.jobpilot.email.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class TokenEncryptionService {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH = 128;

    private final SecretKeySpec secretKey;
    private final SecureRandom secureRandom = new SecureRandom();

    public TokenEncryptionService(
            @Value("${app.crypto.encryption-key:}") String encryptionKey) {

        if (encryptionKey == null || encryptionKey.isBlank()) {
            this.secretKey = null;
            return;
        }

        final byte[] decodedKey;
        try {
            decodedKey = Base64.getDecoder().decode(encryptionKey.trim());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "app.crypto.encryption-key must be a valid Base64-encoded 32-byte key.",
                    exception
            );
        }

        if (decodedKey.length != 32) {
            throw new IllegalArgumentException(
                    "app.crypto.encryption-key must decode to exactly 32 bytes."
            );
        }

        this.secretKey = new SecretKeySpec(decodedKey, "AES");
    }

    private SecretKeySpec requireSecretKey() {
        if (secretKey == null) {
            throw new IllegalStateException(
                    "Gmail token encryption is not configured. Set app.crypto.encryption-key to a Base64-encoded 32-byte key."
            );
        }
        return secretKey;
    }

    public String encrypt(String plainText) {

        try {
            byte[] iv = new byte[IV_LENGTH];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);

            cipher.init(
                Cipher.ENCRYPT_MODE,
                requireSecretKey(),
                new GCMParameterSpec(TAG_LENGTH, iv)
            );

            byte[] encrypted = cipher.doFinal(
                plainText.getBytes(java.nio.charset.StandardCharsets.UTF_8)
            );

            byte[] combined = new byte[iv.length + encrypted.length];

            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(
                encrypted, 0, combined, iv.length, encrypted.length
            );

            return Base64.getEncoder().encodeToString(combined);

        } catch (Exception exception) {
            throw new IllegalStateException(
                "Token encryption failed", exception
            );
        }
    }

    public String decrypt(String encryptedText) {

        try {
            byte[] combined = Base64.getDecoder().decode(encryptedText);

            if (combined.length <= IV_LENGTH) {
                throw new IllegalArgumentException("Invalid encrypted token");
            }

            byte[] iv = new byte[IV_LENGTH];
            byte[] encrypted = new byte[combined.length - IV_LENGTH];

            System.arraycopy(combined, 0, iv, 0, IV_LENGTH);
            System.arraycopy(
                combined, IV_LENGTH, encrypted, 0, encrypted.length
            );

            Cipher cipher = Cipher.getInstance(ALGORITHM);

            cipher.init(
                Cipher.DECRYPT_MODE,
                requireSecretKey(),
                new GCMParameterSpec(TAG_LENGTH, iv)
            );

            byte[] decrypted = cipher.doFinal(encrypted);

            return new String(
                decrypted,
                java.nio.charset.StandardCharsets.UTF_8
            );

        } catch (Exception exception) {
            throw new IllegalStateException(
                "Token decryption failed", exception
            );
        }
    }
}
