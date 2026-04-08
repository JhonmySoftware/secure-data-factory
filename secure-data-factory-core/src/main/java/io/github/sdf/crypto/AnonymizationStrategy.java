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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Strategies available for anonymizing PII fields.
 *
 * @author Jhon Quiñones Arboleda
 */
public enum AnonymizationStrategy {

    /**
     * Replaces the value with a fixed mask string.
     */
    MASKING {
        @Override
        public String apply(String value) {
            if (value == null || value.isEmpty()) {
                return value;
            }
            return repeat('*', value.length());
        }
    },

    /**
     * Replaces the value with a one-way cryptographic hash.
     * The original value cannot be recovered.
     */
    HASHING {
        @Override
        public String apply(String value) {
            if (value == null) {
                return null;
            }
            return "hashed_" + digest(value, "SHA-256");
        }
    },

    /**
     * Substitutes the value with a realistic but fictitious equivalent.
     */
    PSEUDONYMIZATION {
        @Override
        public String apply(String value) {
            if (value == null) {
                return null;
            }
            return "pseudo_" + value.substring(0, Math.min(3, value.length())) + "XXXX";
        }
    },

    /**
     * Removes the field entirely.
     */
    DELETION {
        @Override
        public String apply(String value) {
            return null;
        }
    },

    /**
     * Partially masks the value, keeping a few characters visible.
     */
    PARTIAL_MASKING {
        @Override
        public String apply(String value) {
            if (value == null || value.length() < 3) {
                return "***";
            }
            int visibleChars = Math.max(1, value.length() / 4);
            return value.substring(0, visibleChars) +
                    repeat('*', value.length() - visibleChars);
        }
    };

    /**
     * Applies this anonymization strategy to the given value.
     *
     * @param value the original PII value
     * @return the anonymized result
     */
    public abstract String apply(String value);

    private static String repeat(char character, int count) {
        if (count <= 0) {
            return "";
        }

        StringBuilder builder = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            builder.append(character);
        }
        return builder.toString();
    }

    private static String digest(String value, String algorithm) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
            byte[] digest = messageDigest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(digest.length * 2);
            for (byte item : digest) {
                builder.append(String.format("%02x", item));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new VerificationException("Hashing algorithm unavailable: " + algorithm, "anonymize", e);
        }
    }
}
