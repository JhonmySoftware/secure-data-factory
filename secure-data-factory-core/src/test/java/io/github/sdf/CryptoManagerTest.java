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
package io.github.sdf;

import io.github.sdf.crypto.AnonymizationStrategy;
import io.github.sdf.crypto.CryptoManager;
import io.github.sdf.crypto.SecurityLevel;
import io.github.sdf.exception.VerificationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("CryptoManager")
class CryptoManagerTest {

    @ParameterizedTest(name = "round-trip with SecurityLevel.{0}")
    @EnumSource(SecurityLevel.class)
    @DisplayName("encrypt/decrypt round-trip succeeds for all security levels")
    void encryptDecrypt_allLevels(SecurityLevel level) {
        CryptoManager crypto = new CryptoManager(level);
        String plaintext = "Hello Secure World";

        String ciphertext = crypto.encrypt(plaintext);
        String decrypted = crypto.decrypt(ciphertext);

        assertNotEquals(plaintext, ciphertext, "ciphertext must differ from plaintext");
        assertEquals(plaintext, decrypted, "decrypted must match original");
    }

    @Test
    @DisplayName("encrypt(null) returns null")
    void encrypt_nullInput() {
        CryptoManager crypto = new CryptoManager(SecurityLevel.MEDIUM);
        assertNull(crypto.encrypt(null));
    }

    @Test
    @DisplayName("decrypt(null) returns null")
    void decrypt_nullInput() {
        CryptoManager crypto = new CryptoManager(SecurityLevel.MEDIUM);
        assertNull(crypto.decrypt(null));
    }

    @Test
    @DisplayName("Two encryptions of the same plaintext produce different ciphertexts")
    void encrypt_ivRandomness() {
        CryptoManager crypto = new CryptoManager(SecurityLevel.HIGH);
        String plaintext = "same-value";
        String first = crypto.encrypt(plaintext);
        String second = crypto.encrypt(plaintext);
        assertNotEquals(first, second, "Different IVs must produce different ciphertexts");
    }

    @ParameterizedTest(name = "ciphertext payload includes IV with SecurityLevel.{0}")
    @EnumSource(value = SecurityLevel.class, names = {"LOW", "MEDIUM", "HIGH"})
    @DisplayName("CBC-based encryption prefixes a 16-byte IV")
    void encrypt_cbcPayloadIncludesExpectedIvLength(SecurityLevel level) {
        CryptoManager crypto = new CryptoManager(level);
        byte[] decoded = Base64.getDecoder().decode(crypto.encrypt("CBC payload"));

        assertTrue(decoded.length > 16, "CBC payload must contain IV + ciphertext");
    }

    @Test
    @DisplayName("GCM-based encryption prefixes a 12-byte IV plus authentication tag")
    void encrypt_gcmPayloadIncludesExpectedIvLength() {
        CryptoManager crypto = new CryptoManager(SecurityLevel.CRITICAL);
        byte[] decoded = Base64.getDecoder().decode(crypto.encrypt("GCM payload"));

        assertTrue(decoded.length > 12 + 16, "GCM payload must contain IV + tag + ciphertext");
    }

    @Test
    @DisplayName("Decrypting with wrong key throws VerificationException")
    void decrypt_wrongKeyThrows() {
        CryptoManager crypto1 = new CryptoManager(SecurityLevel.MEDIUM);
        CryptoManager crypto2 = new CryptoManager(SecurityLevel.MEDIUM);

        String ciphertext = crypto1.encrypt("secret");
        assertThrows(VerificationException.class, () -> crypto2.decrypt(ciphertext));
    }

    @Test
    @DisplayName("Tampering with GCM ciphertext is detected")
    void decrypt_tamperedGcmCiphertextThrows() {
        CryptoManager crypto = new CryptoManager(SecurityLevel.CRITICAL);
        String ciphertext = crypto.encrypt("critical-payload");
        byte[] decoded = Base64.getDecoder().decode(ciphertext);
        decoded[decoded.length - 1] = (byte) (decoded[decoded.length - 1] ^ 0x01);
        String tampered = Base64.getEncoder().encodeToString(decoded);

        assertThrows(VerificationException.class, () -> crypto.decrypt(tampered));
    }

    @ParameterizedTest(name = "hash is deterministic with SecurityLevel.{0}")
    @EnumSource(SecurityLevel.class)
    @DisplayName("hash() is deterministic for the same input")
    void hash_isDeterministic(SecurityLevel level) {
        CryptoManager crypto = new CryptoManager(level);
        String value = "deterministic-input";
        assertEquals(crypto.hash(value), crypto.hash(value));
    }

    @Test
    @DisplayName("hash(null) returns null")
    void hash_nullInput() {
        CryptoManager crypto = new CryptoManager(SecurityLevel.MEDIUM);
        assertNull(crypto.hash(null));
    }

    @Test
    @DisplayName("Different inputs produce different hashes")
    void hash_differentInputs() {
        CryptoManager crypto = new CryptoManager(SecurityLevel.MEDIUM);
        assertNotEquals(crypto.hash("abc"), crypto.hash("xyz"));
    }

    @Test
    @DisplayName("SHA-512 hash is longer than SHA-256 hash")
    void hash_sha512LongerThanSha256() {
        CryptoManager sha256 = new CryptoManager(SecurityLevel.MEDIUM);
        CryptoManager sha512 = new CryptoManager(SecurityLevel.HIGH);
        assertTrue(sha512.hash("test").length() > sha256.hash("test").length());
    }

    @Test
    @DisplayName("hmac() returns non-null non-empty string")
    void hmac_nonNull() {
        CryptoManager crypto = new CryptoManager(SecurityLevel.MEDIUM);
        String result = crypto.hmac("data");
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("hmac() is deterministic for the same key and payload")
    void hmac_samePayloadSameKeyDeterministic() {
        CryptoManager crypto = new CryptoManager(SecurityLevel.MEDIUM);

        assertEquals(crypto.hmac("data"), crypto.hmac("data"));
    }

    @Test
    @DisplayName("hmac() changes when the key changes")
    void hmac_differentKeysProduceDifferentOutput() {
        CryptoManager first = new CryptoManager(SecurityLevel.MEDIUM);
        CryptoManager second = new CryptoManager(SecurityLevel.MEDIUM);

        assertNotEquals(first.hmac("data"), second.hmac("data"));
    }

    @Test
    @DisplayName("hmac(null) returns null")
    void hmac_nullInput() {
        CryptoManager crypto = new CryptoManager(SecurityLevel.MEDIUM);
        assertNull(crypto.hmac(null));
    }

    @ParameterizedTest(name = "MASKING on \"{0}\" returns all asterisks")
    @ValueSource(strings = {"hello", "a", "longer-value-here"})
    @DisplayName("MASKING strategy returns only asterisks")
    void anonymize_masking(String input) {
        CryptoManager crypto = new CryptoManager(SecurityLevel.MEDIUM);
        String result = crypto.anonymize(input, AnonymizationStrategy.MASKING);
        assertTrue(result.chars().allMatch(c -> c == '*'),
                "Expected all asterisks but got: " + result);
        assertEquals(input.length(), result.length());
    }

    @Test
    @DisplayName("DELETION strategy returns null")
    void anonymize_deletion() {
        CryptoManager crypto = new CryptoManager(SecurityLevel.MEDIUM);
        assertNull(crypto.anonymize("value", AnonymizationStrategy.DELETION));
    }

    @Test
    @DisplayName("anonymize(null, any) returns null")
    void anonymize_nullValue() {
        CryptoManager crypto = new CryptoManager(SecurityLevel.MEDIUM);
        assertNull(crypto.anonymize(null, AnonymizationStrategy.MASKING));
    }

    @ParameterizedTest(name = "HASHING uses the configured digest size for SecurityLevel.{0}")
    @EnumSource(SecurityLevel.class)
    @DisplayName("HASHING anonymization uses cryptographic digests")
    void anonymize_hashingUsesCryptographicDigest(SecurityLevel level) {
        CryptoManager crypto = new CryptoManager(level);
        String hashed = crypto.anonymize("abc", AnonymizationStrategy.HASHING);
        int digestLength = "SHA-512".equals(level.getHashAlgorithm()) ? 128 : 64;

        assertTrue(hashed.startsWith("hashed_"));
        assertEquals("hashed_".length() + digestLength, hashed.length());
        assertNotEquals("hashed_" + Integer.toHexString("abc".hashCode()), hashed);
    }

    @Test
    @DisplayName("AnonymizationStrategy.HASHING uses a cryptographic hash")
    void strategyHashingUsesCryptographicDigest() {
        String hashed = AnonymizationStrategy.HASHING.apply("abc");
        assertTrue(hashed.startsWith("hashed_"));
        assertEquals(71, hashed.length());
        assertNotEquals("hashed_" + Integer.toHexString("abc".hashCode()), hashed);
    }

    @ParameterizedTest(name = "exported key length matches SecurityLevel.{0}")
    @EnumSource(SecurityLevel.class)
    @DisplayName("exported AES key length matches the configured security level")
    void exportKey_matchesConfiguredKeySize(SecurityLevel level) {
        CryptoManager crypto = new CryptoManager(level);
        byte[] decoded = Base64.getDecoder().decode(crypto.exportKeyAsBase64());
        assertEquals(level.getKeySize() / Byte.SIZE, decoded.length);
    }

    @Test
    @DisplayName("exportKeyAsBase64() returns valid Base64")
    void exportKey_validBase64() {
        CryptoManager crypto = new CryptoManager(SecurityLevel.HIGH);
        String key = crypto.exportKeyAsBase64();
        assertNotNull(key);
        assertDoesNotThrow(() -> Base64.getDecoder().decode(key));
    }

    @Test
    @DisplayName("Re-importing exported key allows decryption")
    void exportImportKey_roundTrip() {
        CryptoManager original = new CryptoManager(SecurityLevel.MEDIUM);
        String exportedKey = original.exportKeyAsBase64();
        String ciphertext = original.encrypt("import-test");

        CryptoManager reimported = new CryptoManager(SecurityLevel.MEDIUM, exportedKey);
        assertEquals("import-test", reimported.decrypt(ciphertext));
    }

    @Test
    @DisplayName("Importing a key with the wrong length for the level fails")
    void importKey_wrongLengthFails() {
        CryptoManager medium = new CryptoManager(SecurityLevel.MEDIUM);
        String exportedKey = medium.exportKeyAsBase64();

        assertThrows(VerificationException.class, () -> new CryptoManager(SecurityLevel.LOW, exportedKey));
    }
}
