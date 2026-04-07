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

import io.github.sdf.policy.HIPAAPolicy;
import io.github.sdf.policy.PCIDSSPolicy;
import io.github.sdf.model.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Security Policies")
class SecurityPoliciesTest {

    private HIPAAPolicy hipaaPolicy;
    private PCIDSSPolicy pciDssPolicy;

    @BeforeEach
    void setUp() {
        hipaaPolicy = new HIPAAPolicy();
        pciDssPolicy = new PCIDSSPolicy();
    }

    @Test
    @DisplayName("HIPAA policy returns correct name and description")
    void hipaaPolicy_nameAndDescription() {
        assertEquals("HIPAA", hipaaPolicy.getName());
        assertTrue(hipaaPolicy.getDescription().contains("Health Insurance Portability"));
    }

    @Test
    @DisplayName("HIPAA policy requires HIGH security level")
    void hipaaPolicy_requiresHighSecurityLevel() {
        assertEquals(io.github.sdf.crypto.SecurityLevel.HIGH, hipaaPolicy.minimumSecurityLevel());
    }

    @Test
    @DisplayName("HIPAA policy applies pseudonymization to names")
    void hipaaPolicy_pseudonymizesNames() {
        Person original = Person.builder()
                .id("test-id")
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .phone("+1-555-123-4567")
                .nationalId("123-45-6789")
                .build();

        Person result = hipaaPolicy.apply(original);

        assertEquals("J***", result.getFirstName());
        assertEquals("D**", result.getLastName());
    }

    @Test
    @DisplayName("HIPAA policy masks email completely")
    void hipaaPolicy_masksEmailCompletely() {
        Person original = Person.builder()
                .id("test-id")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@healthcare.org")
                .build();

        Person result = hipaaPolicy.apply(original);

        assertTrue(result.getEmail().endsWith("@***.example"));
        assertFalse(result.getEmail().contains("healthcare"));
    }

    @Test
    @DisplayName("HIPAA policy masks phone completely")
    void hipaaPolicy_masksPhoneCompletely() {
        Person original = Person.builder()
                .id("test-id")
                .firstName("John")
                .lastName("Doe")
                .phone("+1-555-123-4567")
                .build();

        Person result = hipaaPolicy.apply(original);

        assertTrue(result.getPhone().chars().allMatch(c -> c == '*'));
    }

    @Test
    @DisplayName("HIPAA policy hashes national ID")
    void hipaaPolicy_hashesNationalId() {
        Person original = Person.builder()
                .id("test-id")
                .firstName("John")
                .lastName("Doe")
                .nationalId("123-45-6789")
                .build();

        Person result = hipaaPolicy.apply(original);

        assertNotNull(result.getNationalId());
        assertNotEquals("123-45-6789", result.getNationalId());
        assertTrue(result.getNationalId().startsWith("hashed_"));
    }

    @Test
    @DisplayName("HIPAA policy shifts birth year to 1990")
    void hipaaPolicy_shiftsBirthYear() {
        Person original = Person.builder()
                .id("test-id")
                .firstName("John")
                .lastName("Doe")
                .birthDate(java.time.LocalDate.of(1985, 6, 15))
                .build();

        Person result = hipaaPolicy.apply(original);

        assertEquals(1990, result.getBirthDate().getYear());
        assertEquals(6, result.getBirthDate().getMonthValue());
        assertEquals(15, result.getBirthDate().getDayOfMonth());
    }

    @Test
    @DisplayName("HIPAA policy removes address")
    void hipaaPolicy_removesAddress() {
        Person original = Person.builder()
                .id("test-id")
                .firstName("John")
                .lastName("Doe")
                .address("123 Medical Center Dr")
                .city("Boston")
                .country("USA")
                .build();

        Person result = hipaaPolicy.apply(original);

        assertNull(result.getAddress());
        assertTrue(result.getCity().chars().allMatch(c -> c == '*'));
    }

    @Test
    @DisplayName("HIPAA policy validates compliance")
    void hipaaPolicy_validatesCompliance() {
        Person compliant = Person.builder()
                .id("test-id")
                .firstName("John")
                .lastName("Doe")
                .nationalId("hashed_abc123")
                .build();

        Person nonCompliant = Person.builder()
                .id("test-id")
                .firstName("John")
                .lastName("Doe")
                .nationalId("123-45-6789")
                .build();

        assertTrue(hipaaPolicy.isCompliant(compliant));
        assertFalse(hipaaPolicy.isCompliant(nonCompliant));
    }

    @Test
    @DisplayName("PCIDSS policy returns correct name and description")
    void pciDssPolicy_nameAndDescription() {
        assertEquals("PCIDSS", pciDssPolicy.getName());
        assertTrue(pciDssPolicy.getDescription().contains("Payment Card Industry"));
    }

    @Test
    @DisplayName("PCIDSS policy requires CRITICAL security level")
    void pciDssPolicy_requiresCriticalSecurityLevel() {
        assertEquals(io.github.sdf.crypto.SecurityLevel.CRITICAL, pciDssPolicy.minimumSecurityLevel());
    }

    @Test
    @DisplayName("PCIDSS policy applies partial masking to names")
    void pciDssPolicy_partialMasksNames() {
        Person original = Person.builder()
                .id("test-id")
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .build();

        Person result = pciDssPolicy.apply(original);

        assertFalse(result.getFirstName().equals("John"));
        assertFalse(result.getLastName().equals("Doe"));
    }

    @Test
    @DisplayName("PCIDSS policy masks email with PAN protection")
    void pciDssPolicy_masksEmailWithPanProtection() {
        Person original = Person.builder()
                .id("test-id")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@paymentcorp.com")
                .build();

        Person result = pciDssPolicy.apply(original);

        assertTrue(result.getEmail().endsWith("@*.example"));
        assertFalse(result.getEmail().contains("paymentcorp"));
    }

    @Test
    @DisplayName("PCIDSS policy masks phone keeping only last 4 digits")
    void pciDssPolicy_masksPhoneKeepingLastFour() {
        Person original = Person.builder()
                .id("test-id")
                .firstName("John")
                .lastName("Doe")
                .phone("+1-555-123-4567")
                .build();

        Person result = pciDssPolicy.apply(original);

        assertTrue(result.getPhone().endsWith("4567"));
        assertTrue(result.getPhone().startsWith("****"));
    }

    @Test
    @DisplayName("PCIDSS policy hashes national ID")
    void pciDssPolicy_hashesNationalId() {
        Person original = Person.builder()
                .id("test-id")
                .firstName("John")
                .lastName("Doe")
                .nationalId("123-45-6789")
                .build();

        Person result = pciDssPolicy.apply(original);

        assertNotNull(result.getNationalId());
        assertTrue(result.getNationalId().startsWith("hashed_"));
    }

    @Test
    @DisplayName("PCIDSS policy shifts birth year to 1995")
    void pciDssPolicy_shiftsBirthYear() {
        Person original = Person.builder()
                .id("test-id")
                .firstName("John")
                .lastName("Doe")
                .birthDate(java.time.LocalDate.of(1985, 6, 15))
                .build();

        Person result = pciDssPolicy.apply(original);

        assertEquals(1995, result.getBirthDate().getYear());
    }

    @Test
    @DisplayName("PCIDSS policy validates compliance")
    void pciDssPolicy_validatesCompliance() {
        Person compliant = Person.builder()
                .id("test-id")
                .firstName("John")
                .lastName("Doe")
                .nationalId("hashed_abc123")
                .build();

        Person nonCompliant = Person.builder()
                .id("test-id")
                .firstName("John")
                .lastName("Doe")
                .nationalId("123-45-6789")
                .build();

        assertTrue(pciDssPolicy.isCompliant(compliant));
        assertFalse(pciDssPolicy.isCompliant(nonCompliant));
    }

    @Test
    @DisplayName("Policies handle null person gracefully")
    void policies_handleNullPerson() {
        assertNull(hipaaPolicy.apply(null));
        assertNull(pciDssPolicy.apply(null));
    }

    @Test
    @DisplayName("Policies validate null person compliance")
    void policies_validateNullPerson() {
        assertFalse(hipaaPolicy.isCompliant(null));
        assertFalse(pciDssPolicy.isCompliant(null));
    }
}
