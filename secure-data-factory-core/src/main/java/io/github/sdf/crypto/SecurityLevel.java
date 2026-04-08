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

/**
 * Defines the available security levels for data encryption and anonymization.
 * Each level determines the algorithm strength and anonymization depth.
 *
 * @author Jhon Quiñones Arboleda
 */
public enum SecurityLevel {

    /**
     * Basic level: AES-128-CBC, light anonymization.
     * Suitable for non-sensitive development environments.
     */
    LOW(128, "AES/CBC/PKCS5Padding", "SHA-256"),

    /**
     * Standard level: AES-256-CBC, moderate anonymization.
     * Recommended for staging and test environments with realistic data.
     */
    MEDIUM(256, "AES/CBC/PKCS5Padding", "SHA-256"),

    /**
     * High level: AES-256-CBC with stronger hashing and mandatory audit.
     * Required for data that mirrors production PII.
     */
    HIGH(256, "AES/CBC/PKCS5Padding", "SHA-512"),

    /**
     * Maximum level: AES-256-GCM with stronger hashing and mandatory audit.
     * Required for GDPR-critical scenarios and financial data.
     */
    CRITICAL(256, "AES/GCM/NoPadding", "SHA-512");

    private final int keySize;
    private final String cipherAlgorithm;
    private final String hashAlgorithm;

    SecurityLevel(int keySize, String cipherAlgorithm, String hashAlgorithm) {
        this.keySize = keySize;
        this.cipherAlgorithm = cipherAlgorithm;
        this.hashAlgorithm = hashAlgorithm;
    }

    public int getKeySize() {
        return keySize;
    }

    public String getCipherAlgorithm() {
        return cipherAlgorithm;
    }

    public String getHashAlgorithm() {
        return hashAlgorithm;
    }

    /** Returns true if this level requires GCM mode (authenticated encryption). */
    public boolean requiresGCM() {
        return this == CRITICAL;
    }

    /** Returns true if this level mandates a full audit trail. */
    public boolean requiresAuditTrail() {
        return this == HIGH || this == CRITICAL;
    }

    /** Returns true when this level is at least as strong as the required level. */
    public boolean isAtLeast(SecurityLevel requiredLevel) {
        return requiredLevel == null || this.ordinal() >= requiredLevel.ordinal();
    }
}
