package io.github.sdf;

import io.github.sdf.generator.ProductGenerator;
import io.github.sdf.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ProductGenerator")
class ProductGeneratorTest {

    private ProductGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new ProductGenerator();
    }

    @Test
    @DisplayName("generate() returns a non-null Product")
    void generate_nonNull() {
        assertNotNull(generator.generate());
    }

    @Test
    @DisplayName("generate() populates synthetic catalog fields")
    void generate_populatesFields() {
        Product product = generator.generate();

        assertNotNull(product.getId());
        assertNotNull(product.getSku());
        assertNotNull(product.getName());
        assertNotNull(product.getCategory());
        assertNotNull(product.getDescription());
        assertNotNull(product.getUnitPrice());
        assertNotNull(product.getCurrency());
        assertNotNull(product.getBarcode());
        assertNotNull(product.getManufacturer());
        assertTrue(product.getSku().startsWith("SKU-"));
        assertTrue(product.getBarcode().startsWith("BAR-"));
        assertTrue(product.getUnitPrice().signum() > 0);
    }

    @Test
    @DisplayName("generate() keeps explicit synthetic product markers")
    void generate_usesSyntheticMarkers() {
        Product product = generator.generate();

        assertTrue(product.getName().matches(".*(Sandbox|Mock|Sample|Test|Dev|Pilot).*"),
                "Expected a synthetic marker in product name, got: " + product.getName());
    }

    @Test
    @DisplayName("generate(n) returns exactly n unique products")
    void generate_batchUniqueIds() {
        List<Product> batch = generator.generate(20);
        List<String> ids = batch.stream().map(Product::getId).distinct().collect(Collectors.toList());

        assertEquals(20, batch.size());
        assertEquals(20, ids.size());
    }
}
