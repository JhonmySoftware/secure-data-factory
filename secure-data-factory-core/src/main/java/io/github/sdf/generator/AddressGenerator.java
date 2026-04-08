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
package io.github.sdf.generator;

import io.github.sdf.model.Address;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

/**
 * Generates clearly synthetic but structurally useful addresses for QA and development.
 *
 * @author Jhon Quiñones Arboleda
 */
public class AddressGenerator implements DataGenerator<Address> {

    private static final List<String> SAFE_STREET_NAMES = Arrays.asList(
            "Test", "Sample", "Mock", "Dummy", "Sandbox", "Dev", "QA", "Staging", "Pilot", "Fixture"
    );
    private static final List<String> SAFE_STREET_TYPES = Arrays.asList(
            "Avenue", "Street", "Road", "Boulevard", "Lane", "Way", "Park", "Place"
    );
    private static final List<String> SAFE_CITY_NAMES = Arrays.asList(
            "Testville", "Sample City", "Mocktown", "Sandbox Point", "Dev Harbor",
            "QA Heights", "Dummy Springs", "Pilot Grove", "Fixture Bay", "Validation Park"
    );
    private static final List<String> SAFE_REGION_NAMES = Arrays.asList(
            "North Zone", "Central Zone", "Integration Region", "Lab District",
            "Validation Region", "Quality County", "Testing Department"
    );
    private static final List<String> SAFE_LINE2_TYPES = Arrays.asList(
            "Suite", "Floor", "Office", "Block"
    );

    private static final CountryProfile US = new CountryProfile("US", "United States", 5);
    private static final CountryProfile CO = new CountryProfile("CO", "Colombia", 6);
    private static final CountryProfile MX = new CountryProfile("MX", "Mexico", 5);
    private static final CountryProfile ES = new CountryProfile("ES", "Spain", 5);
    private static final CountryProfile UK = new CountryProfile("GB", "United Kingdom", 6);
    private static final CountryProfile DE = new CountryProfile("DE", "Germany", 5);
    private static final CountryProfile FR = new CountryProfile("FR", "France", 5);

    private final Locale locale;
    private final Random random;
    private final List<CountryProfile> profiles;

    public AddressGenerator() {
        this(Locale.ENGLISH);
    }

    public AddressGenerator(Locale locale) {
        this.locale = locale;
        this.random = new Random();
        this.profiles = resolveProfiles(locale);
    }

    @Override
    public Address generate() {
        CountryProfile profile = profiles.get(random.nextInt(profiles.size()));
        return Address.builder()
                .id(UUID.randomUUID().toString())
                .line1(buildLine1())
                .line2(buildLine2())
                .city(pick(SAFE_CITY_NAMES))
                .region(pick(SAFE_REGION_NAMES))
                .postalCode(randomDigits(profile.postalDigits))
                .countryCode(profile.countryCode)
                .country(profile.country)
                .build();
    }

    @Override
    public String getGeneratorName() {
        return "AddressGenerator";
    }

    private String buildLine1() {
        return (100 + random.nextInt(9800)) + " " + pick(SAFE_STREET_NAMES) + " " + pick(SAFE_STREET_TYPES);
    }

    private String buildLine2() {
        if (random.nextInt(100) >= 40) {
            return null;
        }
        return pick(SAFE_LINE2_TYPES) + " " + (1 + random.nextInt(40));
    }

    private String randomDigits(int count) {
        StringBuilder builder = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            builder.append(random.nextInt(10));
        }
        return builder.toString();
    }

    private String pick(List<String> values) {
        return values.get(random.nextInt(values.size()));
    }

    private List<CountryProfile> resolveProfiles(Locale activeLocale) {
        String language = activeLocale == null ? "" : activeLocale.getLanguage();
        if ("es".equalsIgnoreCase(language)) {
            return Arrays.asList(CO, MX, ES);
        }
        if ("de".equalsIgnoreCase(language)) {
            return Arrays.asList(DE, UK, US);
        }
        if ("fr".equalsIgnoreCase(language)) {
            return Arrays.asList(FR, ES, US);
        }
        return Arrays.asList(US, UK, DE, ES, CO, MX, FR);
    }

    private static final class CountryProfile {
        private final String countryCode;
        private final String country;
        private final int postalDigits;

        private CountryProfile(String countryCode, String country, int postalDigits) {
            this.countryCode = countryCode;
            this.country = country;
            this.postalDigits = postalDigits;
        }
    }
}
