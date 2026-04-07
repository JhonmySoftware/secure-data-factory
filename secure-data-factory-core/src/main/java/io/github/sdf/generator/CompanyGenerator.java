package io.github.sdf.generator;

import io.github.sdf.model.Address;
import io.github.sdf.model.Company;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

/**
 * Generates synthetic company records intended for software development and QA.
 *
 * <p>Company names, addresses, references and websites are dummy-only and are
 * intentionally produced with clearly synthetic markers plus the reserved
 * {@code .example} domain.</p>
 */
public class CompanyGenerator implements DataGenerator<Company> {

    private static final List<String> NAME_PREFIXES = Arrays.asList(
            "Northbridge", "BlueHarbor", "IronPeak", "CloudMesa", "SilverOak",
            "NovaField", "VertexPoint", "GreenRiver", "PilotStone", "CodeHarbor"
    );
    private static final List<String> NAME_MARKERS = Arrays.asList(
            "Dummy", "Sample", "Mock", "Sandbox", "Test", "Dev"
    );
    private static final List<String> BUSINESS_TERMS = Arrays.asList(
            "Systems", "Logistics", "Retail", "Foods", "Analytics",
            "Networks", "Labs", "Services", "Platform", "Works"
    );
    private static final List<String> LEGAL_SUFFIXES = Arrays.asList(
            "S.A.S.", "LLC", "Ltd.", "Inc.", "Group"
    );
    private static final List<String> INDUSTRIES = Arrays.asList(
            "Retail", "Fintech", "HealthTech", "Logistics", "E-commerce",
            "Manufacturing", "Education", "Insurance", "Telecom", "SaaS"
    );
    private static final List<String> MAILBOXES = Arrays.asList(
            "hello", "support", "ops", "ventas", "qa"
    );

    private final AddressGenerator addressGenerator;
    private final Random random;

    public CompanyGenerator() {
        this(Locale.ENGLISH);
    }

    public CompanyGenerator(Locale locale) {
        this.addressGenerator = new AddressGenerator(locale);
        this.random = new Random();
    }

    @Override
    public Company generate() {
        Address address = addressGenerator.generate();
        String legalName = buildLegalName();
        String tradeName = buildTradeName(legalName);
        String slug = slugify(tradeName);
        String domain = slug + ".example";

        return Company.builder()
                .id(UUID.randomUUID().toString())
                .legalName(legalName)
                .tradeName(tradeName)
                .industry(pick(INDUSTRIES))
                .supportEmail(pick(MAILBOXES) + "@" + domain)
                .phone(generatePhone(address.getCountryCode()))
                .website("https://www." + domain)
                .taxId("TAX-" + address.getCountryCode() + "-" + randomDigits(8))
                .registrationNumber("REG-" + address.getCountryCode() + "-" + randomDigits(7))
                .address(address)
                .build();
    }

    @Override
    public String getGeneratorName() {
        return "CompanyGenerator";
    }

    private String buildLegalName() {
        return pick(NAME_PREFIXES) + " " +
                pick(NAME_MARKERS) + " " +
                pick(BUSINESS_TERMS) + " " +
                pick(LEGAL_SUFFIXES);
    }

    private String buildTradeName(String legalName) {
        if (legalName.endsWith(" S.A.S.") || legalName.endsWith(" LLC")
                || legalName.endsWith(" Ltd.") || legalName.endsWith(" Inc.")
                || legalName.endsWith(" Group")) {
            return legalName.replace(" S.A.S.", "")
                    .replace(" LLC", "")
                    .replace(" Ltd.", "")
                    .replace(" Inc.", "")
                    .replace(" Group", "");
        }
        return legalName;
    }

    private String generatePhone(String countryCode) {
        PhoneGenerator.Country phoneCountry = PhoneGenerator.Country.US;
        if ("CO".equalsIgnoreCase(countryCode)) {
            phoneCountry = PhoneGenerator.Country.CO;
        } else if ("MX".equalsIgnoreCase(countryCode)) {
            phoneCountry = PhoneGenerator.Country.MX;
        } else if ("ES".equalsIgnoreCase(countryCode)) {
            phoneCountry = PhoneGenerator.Country.ES;
        } else if ("GB".equalsIgnoreCase(countryCode)) {
            phoneCountry = PhoneGenerator.Country.UK;
        } else if ("DE".equalsIgnoreCase(countryCode)) {
            phoneCountry = PhoneGenerator.Country.DE;
        } else if ("FR".equalsIgnoreCase(countryCode)) {
            phoneCountry = PhoneGenerator.Country.FR;
        }
        return new PhoneGenerator(phoneCountry).generate();
    }

    private String slugify(String value) {
        return value.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-+|-+$)", "");
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
}
