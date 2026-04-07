package io.github.sdf.generator;

import com.github.javafaker.Faker;
import io.github.sdf.model.Address;
import io.github.sdf.model.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Generates realistic {@link Person} records using Java Faker.
 *
 * <p>All generated data is entirely synthetic — no real person's information
 * is used. Email addresses point to safe domains (example.com, test.org)
 * and phone numbers use non-allocatable prefixes.</p>
 *
 * <pre>{@code
 * PersonGenerator generator = new PersonGenerator();
 * Person person  = generator.generate();
 * List<Person> batch = generator.generate(1000);
 * }</pre>
 */
public class PersonGenerator implements DataGenerator<Person> {

    private static final Logger log = LoggerFactory.getLogger(PersonGenerator.class);

    private final Faker         faker;
    private final EmailGenerator emailGenerator;
    private final AddressGenerator addressGenerator;
    private final PhoneGenerator.Country phoneCountryOverride;

    // ── Constructors ──────────────────────────────────────────────────────────

    public PersonGenerator() {
        this(Locale.ENGLISH);
    }

    public PersonGenerator(Locale locale) {
        this.faker          = new Faker(locale);
        this.emailGenerator = new EmailGenerator(locale);
        this.addressGenerator = new AddressGenerator(locale);
        this.phoneCountryOverride = null;
    }

    public PersonGenerator(Locale locale, PhoneGenerator.Country phoneCountry) {
        this.faker          = new Faker(locale);
        this.emailGenerator = new EmailGenerator(locale);
        this.addressGenerator = new AddressGenerator(locale);
        this.phoneCountryOverride = phoneCountry;
    }

    // ── DataGenerator ─────────────────────────────────────────────────────────

    @Override
    public Person generate() {
        String firstName = faker.name().firstName();
        String lastName  = faker.name().lastName();
        Address address = addressGenerator.generate();
        PhoneGenerator phoneGenerator = new PhoneGenerator(resolvePhoneCountry(address.getCountryCode()));

        Person person = Person.builder()
                .id(UUID.randomUUID().toString())
                .firstName(firstName)
                .lastName(lastName)
                .email(emailGenerator.generate())
                .phone(phoneGenerator.generate())
                .birthDate(randomBirthDate())
                .address(buildAddressLine(address))
                .city(address.getCity())
                .country(address.getCountry())
                .nationalId(generateNationalId())
                .build();

        log.debug("Generated person: {}", person.getId());
        return person;
    }

    @Override
    public String getGeneratorName() { return "PersonGenerator"; }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Returns a random birth date for a person aged 18–80. */
    private LocalDate randomBirthDate() {
        return faker.date()
                .past(365 * 80, 365 * 18, TimeUnit.DAYS)
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    /**
     * Generates a synthetic national ID.
     * Format: two letters + 6 digits + two letters (purely fictitious).
     */
    private String generateNationalId() {
        return faker.regexify("[A-Z]{2}[0-9]{6}[A-Z]{2}");
    }

    private String buildAddressLine(Address address) {
        if (address.getLine2() == null || address.getLine2().trim().isEmpty()) {
            return address.getLine1();
        }
        return address.getLine1() + ", " + address.getLine2();
    }

    private PhoneGenerator.Country resolvePhoneCountry(String countryCode) {
        if (phoneCountryOverride != null) {
            return phoneCountryOverride;
        }
        if ("CO".equalsIgnoreCase(countryCode)) {
            return PhoneGenerator.Country.CO;
        }
        if ("MX".equalsIgnoreCase(countryCode)) {
            return PhoneGenerator.Country.MX;
        }
        if ("ES".equalsIgnoreCase(countryCode)) {
            return PhoneGenerator.Country.ES;
        }
        if ("GB".equalsIgnoreCase(countryCode)) {
            return PhoneGenerator.Country.UK;
        }
        if ("DE".equalsIgnoreCase(countryCode)) {
            return PhoneGenerator.Country.DE;
        }
        if ("FR".equalsIgnoreCase(countryCode)) {
            return PhoneGenerator.Country.FR;
        }
        return PhoneGenerator.Country.US;
    }
}
