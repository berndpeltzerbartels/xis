package one.xis.totp;

import one.xis.context.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Component
class TOTPSecretEncryption {

    private static final int IV_BYTES = 12;
    private static final int TAG_BITS = 128;

    private final TOTPConfig config;
    private final SecureRandom secureRandom = new SecureRandom();

    TOTPSecretEncryption(TOTPConfig config) {
        this.config = config;
    }

    String encrypt(String plainText) {
        try {
            byte[] iv = new byte[IV_BYTES];
            secureRandom.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key(), new GCMParameterSpec(TAG_BITS, iv));
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(iv) + "." + Base64.getEncoder().encodeToString(encrypted);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Unable to encrypt TOTP secret", e);
        }
    }

    String decrypt(String encryptedText) {
        try {
            String[] parts = encryptedText.split("\\.", 2);
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid encrypted TOTP secret");
            }
            byte[] iv = Base64.getDecoder().decode(parts[0]);
            byte[] encrypted = Base64.getDecoder().decode(parts[1]);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key(), new GCMParameterSpec(TAG_BITS, iv));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Unable to decrypt TOTP secret", e);
        }
    }

    private SecretKeySpec key() {
        String configuredKey = config.encryptionKey();
        if (configuredKey == null || configuredKey.isBlank()) {
            throw new IllegalStateException("xis.totp.encryption-key must be configured when xis-totp is used");
        }
        byte[] keyBytes = sha256(configuredKey);
        return new SecretKeySpec(keyBytes, "AES");
    }

    private byte[] sha256(String text) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
