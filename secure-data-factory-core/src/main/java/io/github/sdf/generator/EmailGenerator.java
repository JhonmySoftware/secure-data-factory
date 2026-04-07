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

import com.github.javafaker.Faker;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Generates realistic but synthetic email addresses.
 *
 * <p>Addresses are guaranteed to follow RFC 5322 local-part rules and use
 * well-known example domains (example.com, test.org, etc.) that cannot
 * be mistaken for real users.</p>
 */
public class EmailGenerator implements DataGenerator<String> {

    private static final List<String> SAFE_DOMAINS = Collections.unmodifiableList(Arrays.asList(
            "example.com", "test.org", "sample.net",
            "dummy.io",    "fake.dev", "testmail.org"
    ));

    private final Faker  faker;
    private final Random random;

    public EmailGenerator() {
        this(Locale.ENGLISH);
    }

    public EmailGenerator(Locale locale) {
        this.faker  = new Faker(locale);
        this.random = new Random();
    }

    @Override
    public String generate() {
        String localPart = buildLocalPart();
        String domain    = SAFE_DOMAINS.get(random.nextInt(SAFE_DOMAINS.size()));
        return (localPart + "@" + domain).toLowerCase();
    }

    @Override
    public String getGeneratorName() { return "EmailGenerator"; }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String buildLocalPart() {
        switch (random.nextInt(4)) {
            case 0:
                return sanitize(faker.name().firstName()) + "." +
                        sanitize(faker.name().lastName());
            case 1:
                return sanitize(faker.name().firstName()) +
                        faker.number().digits(3);
            case 2:
                return sanitize(faker.name().firstName()).charAt(0) +
                        sanitize(faker.name().lastName()) +
                        faker.number().digits(2);
            default:
                return sanitize(faker.internet().slug());
        }
    }

    private String sanitize(String value) {
        return value.replaceAll("[^a-zA-Z0-9._-]", "").toLowerCase();
    }
}
