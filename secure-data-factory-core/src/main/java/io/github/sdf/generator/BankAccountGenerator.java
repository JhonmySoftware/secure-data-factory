package io.github.sdf.generator;

import io.github.sdf.model.BankAccount;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

/**
 * Generates synthetic financial account records for dev and QA scenarios.
 */
public class BankAccountGenerator implements DataGenerator<BankAccount> {

    private static final List<String> BANK_PREFIXES = Arrays.asList(
            "Northbridge", "BlueHarbor", "VertexPoint", "CloudMesa", "PilotStone"
    );
    private static final List<String> BANK_MARKERS = Arrays.asList(
            "Dummy", "Sample", "Mock", "Sandbox", "Test", "Dev"
    );
    private static final List<String> BANK_TERMS = Arrays.asList(
            "Bank", "Treasury", "Finance", "Clearing", "Payments"
    );
    private static final List<String> ACCOUNT_HOLDERS = Arrays.asList(
            "Sandbox Treasury", "Mock Operations", "Sample Receivables",
            "Dev Wallet", "QA Settlement", "Pilot Billing"
    );
    private static final List<String> ACCOUNT_TYPES = Arrays.asList(
            "CHECKING", "SAVINGS", "TREASURY", "SETTLEMENT"
    );
    private static final List<String> STATUSES = Arrays.asList(
            "ACTIVE", "PENDING", "ON_HOLD"
    );

    private static final CountryProfile US = new CountryProfile("US", "USD");
    private static final CountryProfile CO = new CountryProfile("CO", "COP");
    private static final CountryProfile MX = new CountryProfile("MX", "MXN");
    private static final CountryProfile ES = new CountryProfile("ES", "EUR");
    private static final CountryProfile UK = new CountryProfile("GB", "GBP");
    private static final CountryProfile DE = new CountryProfile("DE", "EUR");
    private static final CountryProfile FR = new CountryProfile("FR", "EUR");

    private final List<CountryProfile> profiles;
    private final Random random;

    public BankAccountGenerator() {
        this(Locale.ENGLISH);
    }

    public BankAccountGenerator(Locale locale) {
        this.profiles = resolveProfiles(locale);
        this.random = new Random();
    }

    @Override
    public BankAccount generate() {
        CountryProfile profile = profiles.get(random.nextInt(profiles.size()));

        return BankAccount.builder()
                .id(UUID.randomUUID().toString())
                .bankName(buildBankName())
                .accountHolderName(pick(ACCOUNT_HOLDERS) + " " + randomDigits(2))
                .accountType(pick(ACCOUNT_TYPES))
                .currency(profile.currency)
                .accountNumber("ACC-" + profile.countryCode + "-" + randomDigits(10))
                .routingReference("ROUTE-" + profile.countryCode + "-" + randomDigits(6))
                .ibanReference("IBAN-TEST-" + profile.countryCode + "-" + randomAlphaNumeric(14))
                .status(pick(STATUSES))
                .countryCode(profile.countryCode)
                .build();
    }

    @Override
    public String getGeneratorName() {
        return "BankAccountGenerator";
    }

    private String buildBankName() {
        return pick(BANK_PREFIXES) + " " + pick(BANK_MARKERS) + " " + pick(BANK_TERMS);
    }

    private List<CountryProfile> resolveProfiles(Locale locale) {
        String language = locale == null ? "" : locale.getLanguage();
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

    private String randomDigits(int count) {
        StringBuilder builder = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            builder.append(random.nextInt(10));
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

    private static final class CountryProfile {
        private final String countryCode;
        private final String currency;

        private CountryProfile(String countryCode, String currency) {
            this.countryCode = countryCode;
            this.currency = currency;
        }
    }
}
