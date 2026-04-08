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
package io.github.sdf.policy;

import io.github.sdf.crypto.AnonymizationStrategy;
import io.github.sdf.crypto.SecurityLevel;
import io.github.sdf.model.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Enforces GDPR (General Data Protection Regulation) compliance on generated data.
 *
 * <p>Key rules applied:
 * <ul>
 *   <li>National IDs are always fully anonymized (hashed).</li>
 *   <li>Phone numbers are partially masked.</li>
 *   <li>Emails are partially masked.</li>
 *   <li>Addresses are reduced to city + country only.</li>
 *   <li>Minimum security level: {@link SecurityLevel#HIGH}.</li>
 * </ul>
 */
public class GDPRPolicy implements SecurityPolicy {

    private static final Logger log = LoggerFactory.getLogger(GDPRPolicy.class);

    private static final String NAME        = "GDPR";
    private static final String DESCRIPTION =
            "EU General Data Protection Regulation — anonymizes PII to ensure " +
            "data minimization and purpose limitation principles.";

    @Override
    public String getName() { return NAME; }

    @Override
    public String getDescription() { return DESCRIPTION; }

    @Override
    public SecurityLevel minimumSecurityLevel() {
        return SecurityLevel.HIGH;
    }

    @Override
    public boolean isCompliant(Person person) {
        if (person == null) return false;

        // Under GDPR a national ID must never appear in plain text in test data
        if (person.getNationalId() != null && !person.getNationalId().startsWith("hashed_")) {
            log.warn("GDPR violation: plain-text nationalId detected for person id={}", person.getId());
            return false;
        }
        return true;
    }

    @Override
    public Person apply(Person person) {
        if (person == null) return null;

        log.debug("Applying GDPR policy to person id={}", person.getId());

        return Person.builder()
                .id(person.getId())
                // Name: keep realistic but pseudonymized
                .firstName(person.getFirstName())
                .lastName(AnonymizationStrategy.PARTIAL_MASKING.apply(person.getLastName()))
                // Email: partial mask
                .email(maskEmail(person.getEmail()))
                // Phone: partial mask
                .phone(AnonymizationStrategy.PARTIAL_MASKING.apply(person.getPhone()))
                // Birth date: keep (needed for age verification use-cases)
                .birthDate(person.getBirthDate())
                // Address: suppress to city/country only
                .address(null)
                .city(person.getCity())
                .country(person.getCountry())
                // National ID: always hashed
                .nationalId(AnonymizationStrategy.HASHING.apply(person.getNationalId()))
                .build();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String maskEmail(String email) {
        if (email == null) return null;
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) return "***@***";
        String local  = email.substring(0, atIndex);
        String domain = email.substring(atIndex); // includes '@'
        return AnonymizationStrategy.PARTIAL_MASKING.apply(local) + domain;
    }
}
