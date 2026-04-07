package io.github.sdf;

import io.github.sdf.generator.AddressGenerator;
import io.github.sdf.model.Address;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("AddressGenerator")
class AddressGeneratorTest {

    private static final List<String> STREET_MARKERS = Arrays.asList(
            "Test", "Sample", "Mock", "Dummy", "Sandbox", "Dev", "QA", "Staging", "Pilot", "Fixture"
    );

    private AddressGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new AddressGenerator();
    }

    @Test
    @DisplayName("generate() returns a non-null Address")
    void generate_nonNull() {
        assertNotNull(generator.generate());
    }

    @Test
    @DisplayName("generate() populates the main address fields")
    void generate_populatesFields() {
        Address address = generator.generate();

        assertNotNull(address.getId());
        assertNotNull(address.getLine1());
        assertNotNull(address.getCity());
        assertNotNull(address.getRegion());
        assertNotNull(address.getPostalCode());
        assertNotNull(address.getCountryCode());
        assertNotNull(address.getCountry());
        assertFalse(address.getFullAddress().trim().isEmpty());
    }

    @Test
    @DisplayName("generate() uses clearly synthetic street markers")
    void generate_usesSyntheticStreetMarkers() {
        Address address = generator.generate();

        assertTrue(STREET_MARKERS.stream().anyMatch(marker -> address.getLine1().contains(marker)),
                "Expected a synthetic street marker in line1, got: " + address.getLine1());
    }

    @Test
    @DisplayName("generate(n) returns exactly n unique addresses")
    void generate_batchUniqueIds() {
        List<Address> batch = generator.generate(25);
        List<String> ids = batch.stream().map(Address::getId).distinct().collect(Collectors.toList());

        assertEquals(25, batch.size());
        assertEquals(25, ids.size());
    }
}
