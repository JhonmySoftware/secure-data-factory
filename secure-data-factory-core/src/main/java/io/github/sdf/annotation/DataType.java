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

import java.util.HashMap;
import java.util.Map;

/**
 * Enumeration of supported data types for synthetic data generation.
 *
 * <p>Each type corresponds to a specific kind of generated data, from names
 * and addresses to financial and identification data.</p>
 *
 * <p>The AUTO type attempts to infer the appropriate generator based on
 * the field's name and type.</p>
 *
 * @author Jhon Quiñones Arboleda
 */
public enum DataType {

    // --- Person Data ---
    AUTO("auto", "Auto-detect based on field name"),
    FIRST_NAME("firstName", "Person first name"),
    LAST_NAME("lastName", "Person last name"),
    FULL_NAME("fullName", "Full person name"),
    EMAIL("email", "Email address (synthetic domain)"),
    PHONE("phone", "Phone number"),
    NATIONAL_ID("nationalId", "National identification number"),
    PASSPORT("passport", "Passport number"),
    BIRTH_DATE("birthDate", "Date of birth"),

    // --- Address Data ---
    STREET_ADDRESS("streetAddress", "Street address"),
    CITY("city", "City name"),
    STATE("state", "State or province"),
    COUNTRY("country", "Country name"),
    ZIP_CODE("zipCode", "Postal/ZIP code"),
    FULL_ADDRESS("fullAddress", "Complete address"),

    // --- Company Data ---
    COMPANY_NAME("companyName", "Company business name"),
    COMPANY_WEBSITE("companyWebsite", "Company website (.example)"),
    COMPANY_TAX_ID("companyTaxId", "Tax identification number"),

    // --- Financial Data ---
    CREDIT_CARD("creditCard", "Credit card number"),
    BANK_ACCOUNT("bankAccount", "Bank account number"),
    IBAN("iban", "International Bank Account Number"),
    SWIFT_CODE("swiftCode", "SWIFT/BIC code"),
    CURRENCY_CODE("currencyCode", "Currency code (USD, EUR, etc.)"),
    AMOUNT("amount", "Monetary amount"),
    BITCOIN_ADDRESS("bitcoinAddress", "Bitcoin address"),

    // --- Product Data ---
    PRODUCT_NAME("productName", "Product name"),
    PRODUCT_SKU("productSku", "Stock keeping unit"),
    PRODUCT_EAN("productEan", "EAN/UPC barcode"),
    PRICE("price", "Product price"),

    // --- Device & Network ---
    IP_ADDRESS("ipAddress", "IPv4 address"),
    IPV6_ADDRESS("ipv6Address", "IPv6 address"),
    MAC_ADDRESS("MAC address", "Network MAC address"),
    HOSTNAME("hostname", "Computer hostname"),
    USER_AGENT("userAgent", "Browser user agent"),
    IMEI("imei", "Mobile device IMEI"),
    PLATFORM("platform", "OS platform (Windows, Mac, Linux, Android, iOS)"),

    // --- Document & Reference ---
    INVOICE_NUMBER("invoiceNumber", "Invoice reference"),
    ORDER_NUMBER("orderNumber", "Order reference"),
    TRANSACTION_ID("transactionId", "Transaction identifier"),
    UUID("uuid", "Universally unique identifier"),

    // --- Web & Internet ---
    URL("url", "Full URL"),
    DOMAIN("domain", "Domain name"),
    USERNAME("username", "Username"),
    PASSWORD("password", "Secure random password"),

    // --- Technical ---
    INTEGER("integer", "Integer number"),
    LONG("long", "Long integer"),
    DOUBLE("double", "Double precision number"),
    BOOLEAN("boolean", "Boolean true/false"),
    TEXT("text", "Lorem ipsum text"),
    WORD("word", "Single random word"),
    SENTENCE("sentence", "Random sentence"),
    PARAGRAPH("paragraph", "Random paragraph"),
    DATE("date", "Date (any)"),
    TIME("time", "Time of day"),
    DATETIME("dateTime", "Date and time"),
    TIMESTAMP("timestamp", "Unix timestamp"),
    HEX_COLOR("hexColor", "Hex color code"),
    LOCALE("locale", "Locale identifier (en_US, es_CO)"),
    LANGUAGE("language", "Language code"),
    TIMEZONE("timezone", "Timezone identifier");

    private final String key;
    private final String description;

    private static final Map<String, DataType> BY_KEY = new HashMap<>();

    static {
        for (DataType type : values()) {
            BY_KEY.put(type.key, type);
        }
    }

    DataType(String key, String description) {
        this.key = key;
        this.description = description;
    }

    public String getKey() {
        return key;
    }

    public String getDescription() {
        return description;
    }

    public static DataType fromKey(String key) {
        return BY_KEY.getOrDefault(key.toLowerCase(), AUTO);
    }

    public static DataType fromKey(String key, DataType defaultType) {
        return BY_KEY.getOrDefault(key.toLowerCase(), defaultType);
    }
}