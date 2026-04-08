/*
 * Copyright (c) 2024 Secure Data Factory Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.sdf.crypto;

import io.github.sdf.exception.VerificationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;

/**
 * Manages all cryptographic operations for the Secure Data Factory.
 * Supports AES-CBC, AES-GCM, SHA-256/SHA-512 hashing and anonymization.
 *
 * @author Jhon Quiñones Arboleda
 */
public class CryptoManager {

    private static final Logger log = LoggerFactory.getLogger(CryptoManager.class);

    private static final int GCM_TAG_LENGTH = 128;
    private static final int GCM_IV_LENGTH = 12;
    private static final int CBC_IV_LENGTH = 16;

    private final SecurityLevel securityLevel;
    private final SecretKey secretKey;
    private final SecureRandom secureRandom;

    /**
     * Creates a CryptoManager with a freshly generated key for the given level.
     */
    public CryptoManager(SecurityLevel securityLevel) {
        this.securityLevel = Objects.requireNonNull(securityLevel, "securityLevel must not be null");
        this.secureRandom = new SecureRandom();
        this.secretKey = generateKey();
        log.info("CryptoManager initialized with level={}", securityLevel);
    }

    /**
     * Creates a CryptoManager with an externally provided key (Base64-encoded).
     */
    public CryptoManager(SecurityLevel securityLevel, String base64Key) {
        this.securityLevel = Objects.requireNonNull(securityLevel, "securityLevel must not be null");
        this.secureRandom = new SecureRandom();
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        validateKeyLength(keyBytes);
        this.secretKey = new SecretKeySpec(keyBytes, "AES");
        log.info("CryptoManager initialized with external key, level={}", securityLevel);
    }

    /**
     * Encrypts the given plaintext.
     *
     * @param plaintext the text to encrypt
     * @return Base64-encoded ciphertext with IV prepended
     */
    public String encrypt(String plaintext) {
        if (plaintext == null) {
            return null;
        }
        try {
            return securityLevel.requiresGCM()
                    ? encryptGcm(plaintext)
                    : encryptCbc(plaintext);
        } catch (Exception e) {
            throw new VerificationException("Encryption failed", "encrypt", e);
        }
    }

    /**
     * Decrypts a previously encrypted value.
     *
     * @param ciphertext Base64-encoded ciphertext with IV prepended
     * @return the original plaintext
     */
    public String decrypt(String ciphertext) {
        if (ciphertext == null) {
            return null;
        }
        try {
            return securityLevel.requiresGCM()
                    ? decryptGcm(ciphertext)
                    : decryptCbc(ciphertext);
        } catch (Exception e) {
            throw new VerificationException("Decryption failed", "decrypt", e);
        }
    }

    /**
     * Produces a one-way hash of the value using the algorithm
     * configured for the current security level.
     *
     * @param value the value to hash
     * @return hex-encoded hash string
     */
    public String hash(String value) {
        if (value == null) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance(securityLevel.getHashAlgorithm());
            byte[] hashBytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new VerificationException("Hashing failed: unsupported algorithm", "hash", e);
        }
    }

    /**
     * Produces an HMAC-SHA256 of the value using the current secret key.
     */
    public String hmac(String value) {
        if (value == null) {
            return null;
        }
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKey);
            byte[] hmacBytes = mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hmacBytes);
        } catch (Exception e) {
            throw new VerificationException("HMAC computation failed", "hmac", e);
        }
    }

    /**
     * Anonymizes a value using the given strategy.
     *
     * @param value the original PII value
     * @param strategy the anonymization strategy to apply
     * @return the anonymized result
     */
    public String anonymize(String value, AnonymizationStrategy strategy) {
        if (value == null) {
            return null;
        }

        AnonymizationStrategy resolvedStrategy =
                Objects.requireNonNull(strategy, "strategy must not be null");

        String result = resolvedStrategy == AnonymizationStrategy.HASHING
                ? "hashed_" + hash(value)
                : resolvedStrategy.apply(value);

        log.debug("Anonymized value using strategy={}", resolvedStrategy);
        return result;
    }

    /**
     * Returns the current secret key encoded as a Base64 string.
     * Store this securely if you need to decrypt later.
     */
    public String exportKeyAsBase64() {
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }

    public SecurityLevel getSecurityLevel() {
        return securityLevel;
    }

    private SecretKey generateKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(securityLevel.getKeySize(), secureRandom);
            return keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException e) {
            throw new VerificationException("AES key generation failed", "init", e);
        }
    }

    private void validateKeyLength(byte[] keyBytes) {
        int actualKeySize = keyBytes.length * Byte.SIZE;
        if (actualKeySize != securityLevel.getKeySize()) {
            throw new VerificationException(
                    "Invalid AES key length " + actualKeySize + " for " + securityLevel
                            + "; expected " + securityLevel.getKeySize(),
                    "init");
        }
    }

    private String encryptGcm(String plaintext) throws Exception {
        byte[] iv = new byte[GCM_IV_LENGTH];
        secureRandom.nextBytes(iv);

        Cipher cipher = Cipher.getInstance(securityLevel.getCipherAlgorithm());
        GCMParameterSpec params = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, params);

        byte[] cipherBytes = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
        ByteBuffer buffer = ByteBuffer.allocate(GCM_IV_LENGTH + cipherBytes.length);
        buffer.put(iv);
        buffer.put(cipherBytes);

        return Base64.getEncoder().encodeToString(buffer.array());
    }

    private String decryptGcm(String ciphertext) throws Exception {
        byte[] decoded = Base64.getDecoder().decode(ciphertext);
        ByteBuffer buffer = ByteBuffer.wrap(decoded);

        byte[] iv = new byte[GCM_IV_LENGTH];
        buffer.get(iv);
        byte[] cipherBytes = new byte[buffer.remaining()];
        buffer.get(cipherBytes);

        Cipher cipher = Cipher.getInstance(securityLevel.getCipherAlgorithm());
        GCMParameterSpec params = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, params);

        return new String(cipher.doFinal(cipherBytes), StandardCharsets.UTF_8);
    }

    private String encryptCbc(String plaintext) throws Exception {
        byte[] iv = new byte[CBC_IV_LENGTH];
        secureRandom.nextBytes(iv);

        Cipher cipher = Cipher.getInstance(securityLevel.getCipherAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));

        byte[] cipherBytes = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
        ByteBuffer buffer = ByteBuffer.allocate(CBC_IV_LENGTH + cipherBytes.length);
        buffer.put(iv);
        buffer.put(cipherBytes);

        return Base64.getEncoder().encodeToString(buffer.array());
    }

    private String decryptCbc(String ciphertext) throws Exception {
        byte[] decoded = Base64.getDecoder().decode(ciphertext);
        ByteBuffer buffer = ByteBuffer.wrap(decoded);

        byte[] iv = new byte[CBC_IV_LENGTH];
        buffer.get(iv);
        byte[] cipherBytes = new byte[buffer.remaining()];
        buffer.get(cipherBytes);

        Cipher cipher = Cipher.getInstance(securityLevel.getCipherAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));

        return new String(cipher.doFinal(cipherBytes), StandardCharsets.UTF_8);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte item : bytes) {
            builder.append(String.format("%02x", item));
        }
        return builder.toString();
    }
}
