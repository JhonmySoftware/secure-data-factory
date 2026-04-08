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
package io.github.sdf.annotation;

import io.github.sdf.generator.*;
import io.github.sdf.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Processes classes annotated with {@link SdfField} to generate synthetic data.
 *
 * <p>This processor scans all fields in a class, identifies those marked with
 * {@code @SdfField}, and generates appropriate synthetic values based on the
 * specified {@link DataType}.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * Person person = AnnotationProcessor.process(Person.class);
 * }</pre>
 *
 * @author Jhon Quiñones Arboleda
 */
public class AnnotationProcessor {

    private static final Logger log = LoggerFactory.getLogger(AnnotationProcessor.class);

    private static final PersonGenerator personGenerator = new PersonGenerator();
    private static final AddressGenerator addressGenerator = new AddressGenerator();
    private static final CompanyGenerator companyGenerator = new CompanyGenerator();
    private static final ProductGenerator productGenerator = new ProductGenerator();
    private static final BankAccountGenerator bankAccountGenerator = new BankAccountGenerator();
    private static final DeviceProfileGenerator deviceProfileGenerator = new DeviceProfileGenerator();
    private static final EmailGenerator emailGenerator = new EmailGenerator();
    private static final PhoneGenerator phoneGenerator = new PhoneGenerator();

    private static final Map<DataType, DocumentGenerator> documentGenerators = new HashMap<>();

    static {
        documentGenerators.put(DataType.INVOICE_NUMBER, new DocumentGenerator(DocumentGenerator.DocumentType.INVOICE));
        documentGenerators.put(DataType.ORDER_NUMBER, new DocumentGenerator(DocumentGenerator.DocumentType.ORDER));
    }

    private static String generateDocument(DataType type) {
        DocumentGenerator gen = documentGenerators.get(type);
        return gen != null ? gen.generate() : "DOC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private AnnotationProcessor() {
    }

    /**
     * Processes a class and populates its fields with synthetic data based on annotations.
     *
     * @param clazz the class to process
     * @param <T>   the type of the class
     * @return a new instance with populated annotated fields
     */
    public static <T> T process(Class<T> clazz) {
        try {
            T instance = clazz.getDeclaredConstructor().newInstance();
            return processInto(instance);
        } catch (Exception e) {
            log.error("Failed to process class: {}", clazz.getName(), e);
            throw new RuntimeException("Failed to generate annotated class: " + clazz.getName(), e);
        }
    }

    /**
     * Processes an existing instance, populating annotated fields with synthetic data.
     *
     * @param instance the instance to populate
     * @param <T>      the type of the instance
     * @return the same instance with populated fields
     */
    public static <T> T processInto(T instance) {
        Class<?> clazz = instance.getClass();
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true);
            SdfField annotation = field.getAnnotation(SdfField.class);

            if (annotation == null) {
                continue;
            }

            if (annotation.ignore()) {
                log.debug("Ignoring field: {}", field.getName());
                continue;
            }

            try {
                Object value = generateValue(field, annotation);
                if (value != null) {
                    field.set(instance, value);
                    log.debug("Set field {} = {}", field.getName(), value);
                }
            } catch (Exception e) {
                log.warn("Failed to set field {}: {}", field.getName(), e.getMessage());
            }
        }

        return instance;
    }

    private static Object generateValue(Field field, SdfField annotation) {
        DataType dataType = annotation.value();

        if (DataType.AUTO.equals(dataType)) {
            dataType = inferDataType(field);
        }

        if (!annotation.fixedValue().isEmpty()) {
            return annotation.fixedValue();
        }

        return generateByType(dataType, annotation);
    }

    private static DataType inferDataType(Field field) {
        String name = field.getName().toLowerCase();
        Class<?> type = field.getType();

        if (type == String.class) {
            if (name.contains("email")) return DataType.EMAIL;
            if (name.contains("phone") || name.contains("mobile")) return DataType.PHONE;
            if (name.contains("firstname") || name.contains("name") && name.contains("first")) return DataType.FIRST_NAME;
            if (name.contains("lastname") || name.contains("surname")) return DataType.LAST_NAME;
            if (name.contains("fullname") || name.equals("name")) return DataType.FULL_NAME;
            if (name.contains("city")) return DataType.CITY;
            if (name.contains("country")) return DataType.COUNTRY;
            if (name.contains("state") || name.contains("province")) return DataType.STATE;
            if (name.contains("zip") || name.contains("postal")) return DataType.ZIP_CODE;
            if (name.contains("address") || name.contains("street")) return DataType.FULL_ADDRESS;
            if (name.contains("company") && name.contains("name")) return DataType.COMPANY_NAME;
            if (name.contains("website") || name.contains("domain")) return DataType.DOMAIN;
            if (name.contains("password")) return DataType.PASSWORD;
            if (name.contains("username")) return DataType.USERNAME;
            if (name.contains("url")) return DataType.URL;
            if (name.contains("ip") && name.contains("v6")) return DataType.IPV6_ADDRESS;
            if (name.contains("ip") || name.contains("address")) return DataType.IP_ADDRESS;
            if (name.contains("mac")) return DataType.MAC_ADDRESS;
            if (name.contains("hostname") || name.contains("host")) return DataType.HOSTNAME;
            if (name.contains("sku")) return DataType.PRODUCT_SKU;
            if (name.contains("card") || name.contains("credit")) return DataType.CREDIT_CARD;
            if (name.contains("account") && name.contains("bank")) return DataType.BANK_ACCOUNT;
            if (name.contains("invoice")) return DataType.INVOICE_NUMBER;
            if (name.contains("order")) return DataType.ORDER_NUMBER;
            if (name.contains("transaction")) return DataType.TRANSACTION_ID;
            if (name.contains("id") && name.contains("national")) return DataType.NATIONAL_ID;
            if (name.contains("passport")) return DataType.PASSPORT;
            if (name.contains("uuid")) return DataType.UUID;
            return DataType.WORD;
        }

        if (type == int.class || type == Integer.class) {
            return DataType.INTEGER;
        }
        if (type == long.class || type == Long.class) {
            return DataType.LONG;
        }
        if (type == double.class || type == Double.class || type == float.class || type == Float.class) {
            return DataType.DOUBLE;
        }
        if (type == boolean.class || type == Boolean.class) {
            return DataType.BOOLEAN;
        }

        return DataType.AUTO;
    }

    private static Object generateByType(DataType dataType, SdfField annotation) {
        switch (dataType) {
            case FIRST_NAME:
                return personGenerator.generate().getFirstName();
            case LAST_NAME:
                return personGenerator.generate().getLastName();
            case FULL_NAME:
                return personGenerator.generate().getFullName();
            case EMAIL:
                return personGenerator.generate().getEmail();
            case PHONE:
                return generatePhone(annotation.country());
            case NATIONAL_ID:
                return "N" + String.format("%010d", ThreadLocalRandom.current().nextLong(1_000_000_000L));
            case PASSPORT:
                return generatePassport();
            case BIRTH_DATE:
            case DATE:
                return LocalDate.now().minusDays(ThreadLocalRandom.current().nextLong(1, 365 * 80));
            case TIME:
                return LocalTime.of(ThreadLocalRandom.current().nextInt(24),
                        ThreadLocalRandom.current().nextInt(60));
            case DATETIME:
                return LocalDateTime.now().minusDays(ThreadLocalRandom.current().nextLong(1, 365 * 80));
            case TIMESTAMP:
                return Instant.now().minusSeconds(ThreadLocalRandom.current().nextLong(0, 365L * 24 * 60 * 60));
            case STREET_ADDRESS:
                Address addr = addressGenerator.generate();
                return addr.getLine1() + (addr.getLine2() != null ? ", " + addr.getLine2() : "");
            case CITY:
                return addressGenerator.generate().getCity();
            case STATE:
                return addressGenerator.generate().getRegion();
            case COUNTRY:
                return addressGenerator.generate().getCountry();
            case ZIP_CODE:
                return addressGenerator.generate().getPostalCode();
            case FULL_ADDRESS:
                return addressGenerator.generate().getFullAddress();
            case COMPANY_NAME:
                return companyGenerator.generate().getLegalName();
            case COMPANY_WEBSITE:
                return companyGenerator.generate().getWebsite();
            case COMPANY_TAX_ID:
                return "TAX-" + String.format("%09d", ThreadLocalRandom.current().nextInt(1_000_000_000));
            case CREDIT_CARD:
                return "4" + String.format("%015d", ThreadLocalRandom.current().nextLong(1_000_000_000_000_000L));
            case BANK_ACCOUNT:
                return bankAccountGenerator.generate().getAccountNumber();
            case IBAN:
                return "XX" + String.format("%02d", ThreadLocalRandom.current().nextInt(1, 100)) + 
                       String.format("%023d", ThreadLocalRandom.current().nextLong(1_000_000_000_000_000_000L));
            case SWIFT_CODE:
                return String.format("%04s%02d%s", randomString("ABCD", 4).toUpperCase(), 
                       ThreadLocalRandom.current().nextInt(100), randomString("ABCDGH", 6).toUpperCase());
            case CURRENCY_CODE:
                return randomFrom("USD", "EUR", "GBP", "COP", "MXN", "CAD", "AUD");
            case AMOUNT:
            case PRICE:
                return Math.round(ThreadLocalRandom.current().nextDouble(annotation.min(), annotation.max()) * 100.0) / 100.0;
            case BITCOIN_ADDRESS:
                return "1" + randomString("0123456789abcdef", 33);
            case PRODUCT_NAME:
                return productGenerator.generate().getName();
            case PRODUCT_SKU:
                return productGenerator.generate().getSku();
            case PRODUCT_EAN:
                return String.format("%013d", ThreadLocalRandom.current().nextLong(1_000_000_000_000L));
            case IP_ADDRESS:
                return String.format("%d.%d.%d.%d",
                        ThreadLocalRandom.current().nextInt(1, 255),
                        ThreadLocalRandom.current().nextInt(0, 255),
                        ThreadLocalRandom.current().nextInt(0, 255),
                        ThreadLocalRandom.current().nextInt(1, 255));
            case IPV6_ADDRESS:
                return String.format("%x:%x:%x:%x:%x:%x:%x:%x",
                        ThreadLocalRandom.current().nextInt(0xFFFF),
                        ThreadLocalRandom.current().nextInt(0xFFFF),
                        ThreadLocalRandom.current().nextInt(0xFFFF),
                        ThreadLocalRandom.current().nextInt(0xFFFF),
                        ThreadLocalRandom.current().nextInt(0xFFFF),
                        ThreadLocalRandom.current().nextInt(0xFFFF),
                        ThreadLocalRandom.current().nextInt(0xFFFF),
                        ThreadLocalRandom.current().nextInt(0xFFFF));
            case MAC_ADDRESS:
                return String.format("%02X:%02X:%02X:%02X:%02X:%02X",
                        ThreadLocalRandom.current().nextInt(256),
                        ThreadLocalRandom.current().nextInt(256),
                        ThreadLocalRandom.current().nextInt(256),
                        ThreadLocalRandom.current().nextInt(256),
                        ThreadLocalRandom.current().nextInt(256),
                        ThreadLocalRandom.current().nextInt(256));
            case HOSTNAME:
                return deviceProfileGenerator.generate().getHostname();
            case USER_AGENT:
                return randomFrom(
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
                        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36",
                        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36");
            case IMEI:
                return String.format("%015d", ThreadLocalRandom.current().nextLong(1_000_000_000_000_000L));
            case PLATFORM:
                return randomFrom("WINDOWS", "MACOS", "LINUX", "ANDROID", "IOS");
            case INVOICE_NUMBER:
                return generateDocument(DataType.INVOICE_NUMBER);
            case ORDER_NUMBER:
                return generateDocument(DataType.ORDER_NUMBER);
            case TRANSACTION_ID:
                return UUID.randomUUID().toString();
            case UUID:
                return UUID.randomUUID().toString();
            case URL:
                return "https://example.com/" + randomString("abcdefghijklmnopqrstuvwxyz", 10);
            case DOMAIN:
                return randomString("abcdefghijklmnopqrstuvwxyz", 8) + ".example";
            case USERNAME:
                return personGenerator.generate().getFirstName().toLowerCase() +
                        "." + ThreadLocalRandom.current().nextInt(100, 999);
            case PASSWORD:
                return randomString("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%", 16);
            case INTEGER:
                return (int) ThreadLocalRandom.current().nextLong((long) annotation.min(), (long) annotation.max() + 1);
            case LONG:
                return ThreadLocalRandom.current().nextLong((long) annotation.min(), (long) annotation.max() + 1);
            case DOUBLE:
                return Math.round(ThreadLocalRandom.current().nextDouble(annotation.min(), annotation.max()) * 100.0) / 100.0;
            case BOOLEAN:
                return ThreadLocalRandom.current().nextBoolean();
            case WORD:
                return randomWord();
            case SENTENCE:
                return randomSentence();
            case PARAGRAPH:
                return randomParagraph();
            case HEX_COLOR:
                return String.format("#%06X", ThreadLocalRandom.current().nextInt(0xFFFFFF + 1));
            case LOCALE:
                return randomFrom("en_US", "es_CO", "en_GB", "fr_FR", "de_DE");
            case LANGUAGE:
                return randomFrom("en", "es", "fr", "de", "pt", "it");
            case TIMEZONE:
                return randomFrom("America/New_York", "America/Bogota", "Europe/London", "Europe/Paris", "Asia/Tokyo");

            default:
                return randomWord();
        }
    }

    private static String generatePhone(String country) {
        try {
            PhoneGenerator.Country c = PhoneGenerator.Country.valueOf(country.toUpperCase());
            return new PhoneGenerator(c).generate();
        } catch (Exception e) {
            return new PhoneGenerator(PhoneGenerator.Country.US).generate();
        }
    }

    private static String generatePassport() {
        String letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        char c1 = letters.charAt(ThreadLocalRandom.current().nextInt(letters.length()));
        char c2 = letters.charAt(ThreadLocalRandom.current().nextInt(letters.length()));
        int num = ThreadLocalRandom.current().nextInt(1_000_000);
        return "" + c1 + c2 + String.format("%06d", num);
    }

    private static String randomFrom(String... options) {
        return options[ThreadLocalRandom.current().nextInt(options.length)];
    }

    private static String randomString(String chars, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(ThreadLocalRandom.current().nextInt(chars.length())));
        }
        return sb.toString();
    }

    private static final String[] WORDS = {
            "lorem", "ipsum", "dolor", "sit", "amet", "consectetur", "adipiscing", "elit",
            "sed", "do", "eiusmod", "tempor", "incididunt", "ut", "labore", "et", "dolore"
    };

    private static String randomWord() {
        return WORDS[ThreadLocalRandom.current().nextInt(WORDS.length)];
    }

    private static String randomSentence() {
        int length = ThreadLocalRandom.current().nextInt(5, 15);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            if (i > 0) sb.append(" ");
            sb.append(randomWord());
        }
        sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
        sb.append(".");
        return sb.toString();
    }

    private static String randomParagraph() {
        int sentences = ThreadLocalRandom.current().nextInt(3, 8);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sentences; i++) {
            if (i > 0) sb.append(" ");
            sb.append(randomSentence());
        }
        return sb.toString();
    }
}