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
 * Enforces Colombian data protection law (Ley 1581 of 2012) compliance.
 *
 * @author Jhon Quiñones Arboleda
 */
public class ColombianDataProtectionPolicy implements SecurityPolicy {

    private static final Logger log = LoggerFactory.getLogger(ColombianDataProtectionPolicy.class);

    private static final String NAME = "COL-DP";
    private static final String DESCRIPTION =
            "Politica de Proteccion de Datos Colombia (Ley 1581 de 2012 - Habeas Data) — " +
            "Protege datos personales segun la normativa colombiana de tratamiento de datos. " +
            "Requiere autorizacion explicita, finalidad legitima y medidas de seguridad apropiadas.";

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
            log.warn("Colombia Habeas Data violation: plain-text nationalId (CC/CE) detected for person id={}", 
                    person.getId());
            return false;
        }
        
        return true;
    }

    @Override
    public Person apply(Person person) {
        if (person == null) return null;

        log.debug("Applying Colombian Data Protection policy (Ley 1581) to person id={}", person.getId());

        return Person.builder()
                .id(person.getId())
                .firstName(applyPartialMasking(person.getFirstName()))
                .lastName(applyPartialMasking(person.getLastName()))
                .email(applyEmailProtection(person.getEmail()))
                .phone(applyPhoneProtection(person.getPhone()))
                .birthDate(normalizeBirthDate(person.getBirthDate()))
                .address(maskAddress(person.getAddress()))
                .city(applyPartialMasking(person.getCity()))
                .country("CO")
                .nationalId(AnonymizationStrategy.HASHING.apply(person.getNationalId()))
                .build();
    }

    private String applyPartialMasking(String value) {
        if (value == null || value.isEmpty()) return value;
        if (value.length() <= 2) return value.charAt(0) + "*";
        int visibleChars = Math.max(1, value.length() / 4);
        int maskedChars = value.length() - visibleChars;
        return value.substring(0, visibleChars) + repeat('*', maskedChars);
    }

    private String applyEmailProtection(String email) {
        if (email == null) return null;
        int atIndex = email.indexOf('@');
        if (atIndex <= 2) return "**@*.example";
        return email.substring(0, 2) + repeat('*', atIndex - 2) + "@*.example";
    }

    private String repeat(char c, int count) {
        StringBuilder sb = new StringBuilder(count);
        for (int i = 0; i < count; i++) sb.append(c);
        return sb.toString();
    }

    private String applyPhoneProtection(String phone) {
        if (phone == null || phone.length() < 6) return "***";
        return "***" + phone.substring(phone.length() - 3);
    }

    private String maskAddress(String address) {
        if (address == null) return null;
        return "DIR-PROTEGIDA";
    }

    private java.time.LocalDate normalizeBirthDate(java.time.LocalDate birthDate) {
        if (birthDate == null) return null;
        return birthDate.withYear(1990).withMonth(1).withDayOfMonth(1);
    }
}
