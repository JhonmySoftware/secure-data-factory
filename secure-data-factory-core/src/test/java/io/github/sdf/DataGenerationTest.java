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

import io.github.sdf.crypto.SecurityLevel;
import io.github.sdf.model.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DataGeneration")
class DataGenerationTest {

    private SecureDataFactory factory;

    @BeforeEach
    void setUp() {
        factory = SecureDataFactory.builder()
                .securityLevel(SecurityLevel.MEDIUM)
                .locale(Locale.ENGLISH)
                .enableAudit(true)
                .applyPolicies(false)
                .build();
    }

    @Test
    @DisplayName("generatePerson() returns non-null SecureData with Person")
    void generatePerson_returnsSecureData() {
        SecureData<Person> securePerson = factory.generatePerson();

        assertNotNull(securePerson);
        assertNotNull(securePerson.getData());

        Person person = securePerson.getData();
        assertNotNull(person.getId());
        assertNotNull(person.getFirstName());
        assertNotNull(person.getLastName());
        assertNotNull(person.getEmail());
        assertNotNull(person.getPhone());
        assertNotNull(person.getCountry());

        // Verificar que el checksum esté presente
        assertNotNull(securePerson.getChecksum());
        assertFalse(securePerson.getChecksum().trim().isEmpty());

        System.out.println("Generated Person: " + person);
    }

    @Test
    @DisplayName("generatePersons() returns list of SecureData with correct count")
    void generatePersons_returnsBatch() {
        int count = 5;
        List<SecureData<Person>> batch = factory.generatePersons(count);

        assertNotNull(batch);
        assertEquals(count, batch.size());

        for (SecureData<Person> securePerson : batch) {
            assertNotNull(securePerson);
            assertNotNull(securePerson.getData());
            Person person = securePerson.getData();
            assertNotNull(person.getId());
            assertNotNull(person.getFirstName());
            assertNotNull(person.getLastName());
            assertNotNull(person.getEmail());
            assertNotNull(person.getPhone());
            assertNotNull(person.getCountry());
        }

        System.out.println("Generated batch of " + count + " persons");
    }

    @Test
    @DisplayName("encrypt() and decrypt() work correctly")
    void encryptDecrypt_works() {
        String original = "Test data for encryption";
        String encrypted = factory.encrypt(original);
        String decrypted = factory.decrypt(encrypted);

        assertNotNull(encrypted);
        assertNotEquals(original, encrypted); // Encrypted should be different
        assertEquals(original, decrypted); // Decrypted should match original

        System.out.println("Encryption test passed");
    }

    @Test
    @DisplayName("anonymize() modifies the data")
    void anonymize_modifiesData() {
        String original = "john.doe@example.com";
        String anonymized = factory.anonymize(original, io.github.sdf.crypto.AnonymizationStrategy.PARTIAL_MASKING);

        assertNotNull(anonymized);
        assertNotEquals(original, anonymized); // Should be different

        System.out.println("Original: " + original + " -> Anonymized: " + anonymized);
    }
}
