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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Generates generic synthetic document and reference identifiers for QA workflows.
 *
 * @author Jhon Quiñones Arboleda
 */
public class DocumentGenerator implements DataGenerator<String> {

    private static final DateTimeFormatter YEAR_MONTH = DateTimeFormatter.ofPattern("yyyyMM");
    private static final DateTimeFormatter YEAR_MONTH_DAY = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final List<String> CURRENCY_CODES = Arrays.asList("USD", "EUR", "COP", "MXN", "GBP");

    public enum DocumentType {
        INVOICE("INV"),
        ORDER("ORD"),
        TICKET("TCK"),
        CONTRACT("CTR"),
        CUSTOMER("CUS"),
        POLICY("PLC"),
        SHIPMENT("SHP"),
        TAX_ID("TAX"),
        REGISTRATION("REG"),
        ACCOUNT_REFERENCE("ACC"),
        PAYMENT_REFERENCE("PAY"),
        GENERIC_ID("DOC");

        private final String prefix;

        DocumentType(String prefix) {
            this.prefix = prefix;
        }

        public String getPrefix() {
            return prefix;
        }
    }

    private final DocumentType documentType;
    private final Random random;

    public DocumentGenerator(DocumentType documentType) {
        this.documentType = documentType;
        this.random = new Random();
    }

    @Override
    public String generate() {
        LocalDate today = LocalDate.now();
        switch (documentType) {
            case INVOICE:
                return documentType.getPrefix() + "-" + YEAR_MONTH.format(today) + "-" + randomAlphaNumeric(6);
            case ORDER:
                return documentType.getPrefix() + "-" + YEAR_MONTH_DAY.format(today) + "-" + randomDigits(5);
            case TICKET:
                return documentType.getPrefix() + "-" + YEAR_MONTH_DAY.format(today) + "-" + randomAlphaNumeric(4);
            case CONTRACT:
                return documentType.getPrefix() + "-" + today.getYear() + "-" + randomAlphaNumeric(8);
            case CUSTOMER:
                return documentType.getPrefix() + "-" + randomLetters(3) + "-" + randomDigits(6);
            case POLICY:
                return documentType.getPrefix() + "-" + today.getYear() + "-" + randomDigits(7);
            case SHIPMENT:
                return documentType.getPrefix() + "-" + randomLetters(3) + "-" + randomDigits(8);
            case TAX_ID:
                return documentType.getPrefix() + "-" + randomLetters(2) + "-" + randomDigits(8);
            case REGISTRATION:
                return documentType.getPrefix() + "-" + randomLetters(2) + "-" + randomDigits(7);
            case ACCOUNT_REFERENCE:
                return documentType.getPrefix() + "-" + randomDigits(8) + "-" + pick(CURRENCY_CODES);
            case PAYMENT_REFERENCE:
                return documentType.getPrefix() + "-" + YEAR_MONTH_DAY.format(today) + "-" + randomDigits(8);
            case GENERIC_ID:
            default:
                return documentType.getPrefix() + "-" + randomLetters(4) + "-" + randomDigits(6);
        }
    }

    @Override
    public String getGeneratorName() {
        return "DocumentGenerator[" + documentType + "]";
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    private String randomDigits(int count) {
        StringBuilder builder = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            builder.append(random.nextInt(10));
        }
        return builder.toString();
    }

    private String randomLetters(int count) {
        StringBuilder builder = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            builder.append((char) ('A' + random.nextInt(26)));
        }
        return builder.toString();
    }

    private String randomAlphaNumeric(int count) {
        StringBuilder builder = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            if (random.nextBoolean()) {
                builder.append((char) ('A' + random.nextInt(26)));
            } else {
                builder.append(random.nextInt(10));
            }
        }
        return builder.toString();
    }

    private String pick(List<String> values) {
        return values.get(random.nextInt(values.size()));
    }
}
