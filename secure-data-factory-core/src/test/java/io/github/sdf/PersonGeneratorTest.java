package io.github.sdf;

import io.github.sdf.generator.PersonGenerator;
import io.github.sdf.generator.PhoneGenerator;
import io.github.sdf.model.Person;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PersonGenerator")
class PersonGeneratorTest {

    private PersonGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new PersonGenerator();
    }

    // ── Single generation ─────────────────────────────────────────────────────

    @Test
    @DisplayName("generate() returns a non-null Person")
    void generate_nonNull() {
        assertNotNull(generator.generate());
    }

    @Test
    @DisplayName("generate() assigns a non-null UUID id")
    void generate_hasId() {
        String id = generator.generate().getId();
        assertNotNull(id);
        assertFalse(id.trim().isEmpty());
    }

    @Test
    @DisplayName("generate() populates firstName and lastName")
    void generate_hasName() {
        Person p = generator.generate();
        assertNotNull(p.getFirstName());
        assertNotNull(p.getLastName());
        assertFalse(p.getFirstName().trim().isEmpty());
        assertFalse(p.getLastName().trim().isEmpty());
    }

    @Test
    @DisplayName("generate() produces a valid email containing '@'")
    void generate_validEmail() {
        String email = generator.generate().getEmail();
        assertNotNull(email);
        assertTrue(email.contains("@"), "email must contain '@'");
        assertTrue(email.contains("."),  "email must contain '.'");
    }

    @Test
    @DisplayName("generate() produces a phone starting with '+'")
    void generate_validPhone() {
        String phone = generator.generate().getPhone();
        assertNotNull(phone);
        assertTrue(phone.startsWith("+"), "E.164 phone must start with '+'");
    }

    @Test
    @DisplayName("generate() uses synthetic address markers for dummy data")
    void generate_syntheticAddressMarkers() {
        String address = generator.generate().getAddress();

        assertTrue(Arrays.asList("Test", "Sample", "Mock", "Dummy", "Sandbox", "Dev", "QA", "Staging", "Pilot", "Fixture")
                        .stream()
                        .anyMatch(address::contains),
                "Expected a synthetic address marker, got: " + address);
    }

    @Test
    @DisplayName("generate() birthDate is in the past")
    void generate_birthDateInPast() {
        LocalDate birthDate = generator.generate().getBirthDate();
        assertNotNull(birthDate);
        assertTrue(birthDate.isBefore(LocalDate.now()),
                "birthDate must be before today");
    }

    @Test
    @DisplayName("generate() birthDate is at least 18 years ago")
    void generate_adultBirthDate() {
        LocalDate eighteenYearsAgo = LocalDate.now().minusYears(18);
        for (int i = 0; i < 20; i++) {
            LocalDate bd = generator.generate().getBirthDate();
            assertTrue(bd.isBefore(eighteenYearsAgo) || bd.isEqual(eighteenYearsAgo),
                    "Person must be at least 18 years old");
        }
    }

    @Test
    @DisplayName("generate() produces a synthetic national ID matching expected pattern")
    void generate_validNationalId() {
        String id = generator.generate().getNationalId();
        assertNotNull(id);
        assertTrue(id.matches("[A-Z]{2}[0-9]{6}[A-Z]{2}"),
                "nationalId format should be 2 letters + 6 digits + 2 letters, got: " + id);
    }

    @Test
    @DisplayName("getGeneratorName() returns expected value")
    void getGeneratorName() {
        assertEquals("PersonGenerator", generator.getGeneratorName());
    }

    // ── Batch generation ──────────────────────────────────────────────────────

    @Test
    @DisplayName("generate(n) returns exactly n records")
    void generate_batch_count() {
        assertEquals(25, generator.generate(25).size());
    }

    @Test
    @DisplayName("generate(n) produces unique IDs across the batch")
    void generate_batch_uniqueIds() {
        List<Person> batch = generator.generate(200);
        Set<String> ids = batch.stream().map(Person::getId).collect(Collectors.toSet());
        assertEquals(200, ids.size(), "All IDs in the batch must be unique");
    }

    @Test
    @DisplayName("generate(0) throws IllegalArgumentException")
    void generate_zeroThrows() {
        assertThrows(IllegalArgumentException.class, () -> generator.generate(0));
    }

    // ── Locale / country customization ───────────────────────────────────────

    @Test
    @DisplayName("PersonGenerator with custom locale generates data")
    void generate_customLocale() {
        PersonGenerator es = new PersonGenerator(Locale.forLanguageTag("es"));
        Person p = es.generate();
        assertNotNull(p.getFirstName());
    }

    @Test
    @DisplayName("PersonGenerator with Colombian phone generates +57 numbers")
    void generate_colombianPhone() {
        PersonGenerator co = new PersonGenerator(Locale.ENGLISH, PhoneGenerator.Country.CO);
        for (int i = 0; i < 10; i++) {
            String phone = co.generate().getPhone();
            assertTrue(phone.startsWith("+57"),
                    "Colombian phone must start with +57, got: " + phone);
        }
    }

    // ── getFullName ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("getFullName() combines firstName and lastName")
    void getFullName_combinesNames() {
        Person p = Person.builder()
                .firstName("Jane")
                .lastName("Doe")
                .build();
        assertEquals("Jane Doe", p.getFullName());
    }
}
