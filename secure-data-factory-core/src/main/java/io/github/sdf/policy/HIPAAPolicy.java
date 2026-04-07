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

public class HIPAAPolicy implements SecurityPolicy {

    private static final Logger log = LoggerFactory.getLogger(HIPAAPolicy.class);

    private static final String NAME = "HIPAA";
    private static final String DESCRIPTION =
            "US Health Insurance Portability and Accountability Act — protects PHI (Protected Health Information) " +
            "by anonymizing health-related identifiers and ensuring secure handling of patient data in test environments.";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public SecurityLevel minimumSecurityLevel() {
        return SecurityLevel.HIGH;
    }

    @Override
    public boolean isCompliant(Person person) {
        if (person == null) return false;
        if (person.getNationalId() != null && !person.getNationalId().startsWith("hashed_")) {
            log.warn("HIPAA violation: plain-text nationalId (potential health plan ID) detected for person id={}", person.getId());
            return false;
        }
        return true;
    }

    @Override
    public Person apply(Person person) {
        if (person == null) return null;

        log.debug("Applying HIPAA policy to person id={}", person.getId());

        return Person.builder()
                .id(person.getId())
                .firstName(pseudonymizeName(person.getFirstName()))
                .lastName(pseudonymizeName(person.getLastName()))
                .email(maskEmailCompletely(person.getEmail()))
                .phone(maskCompletely(person.getPhone()))
                .birthDate(shiftYearOfBirth(person.getBirthDate()))
                .address(null)
                .city(maskCompletely(person.getCity()))
                .country(person.getCountry())
                .nationalId(AnonymizationStrategy.HASHING.apply(person.getNationalId()))
                .build();
    }

    private String pseudonymizeName(String name) {
        if (name == null || name.isEmpty()) return "REDACTED";
        return name.charAt(0) + AnonymizationStrategy.MASKING.apply(name.substring(1));
    }

    private String maskEmailCompletely(String email) {
        if (email == null) return null;
        int atIndex = email.indexOf('@');
        if (atIndex <= 0) return "***@***";
        return AnonymizationStrategy.MASKING.apply(email.substring(0, atIndex)) + "@***.example";
    }

    private String maskCompletely(String value) {
        if (value == null) return null;
        return AnonymizationStrategy.MASKING.apply(value);
    }

    private java.time.LocalDate shiftYearOfBirth(java.time.LocalDate birthDate) {
        if (birthDate == null) return null;
        return birthDate.withYear(1990);
    }
}
