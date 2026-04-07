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

import io.github.sdf.generator.CompanyGenerator;
import io.github.sdf.model.Company;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("CompanyGenerator")
class CompanyGeneratorTest {

    private static final List<String> COMPANY_MARKERS = Arrays.asList(
            "Dummy", "Sample", "Mock", "Sandbox", "Test", "Dev"
    );

    private CompanyGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new CompanyGenerator();
    }

    @Test
    @DisplayName("generate() returns a non-null Company")
    void generate_nonNull() {
        assertNotNull(generator.generate());
    }

    @Test
    @DisplayName("generate() populates dummy business fields")
    void generate_populatesFields() {
        Company company = generator.generate();

        assertNotNull(company.getId());
        assertNotNull(company.getLegalName());
        assertNotNull(company.getTradeName());
        assertNotNull(company.getIndustry());
        assertNotNull(company.getSupportEmail());
        assertNotNull(company.getPhone());
        assertNotNull(company.getWebsite());
        assertNotNull(company.getTaxId());
        assertNotNull(company.getRegistrationNumber());
        assertNotNull(company.getAddress());
    }

    @Test
    @DisplayName("generate() uses explicit synthetic company markers and reserved domains")
    void generate_usesSyntheticMarkersAndReservedDomains() {
        Company company = generator.generate();

        assertTrue(COMPANY_MARKERS.stream().anyMatch(marker -> company.getLegalName().contains(marker)),
                "Expected a synthetic company marker in legalName, got: " + company.getLegalName());
        assertTrue(company.getSupportEmail().endsWith(".example"));
        assertTrue(company.getWebsite().endsWith(".example"));
    }

    @Test
    @DisplayName("generate(n) returns exactly n unique companies")
    void generate_batchUniqueIds() {
        List<Company> batch = generator.generate(20);
        List<String> ids = batch.stream().map(Company::getId).distinct().collect(Collectors.toList());

        assertEquals(20, batch.size());
        assertEquals(20, ids.size());
    }
}
