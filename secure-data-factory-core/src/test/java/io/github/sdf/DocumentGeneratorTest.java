package io.github.sdf;

import io.github.sdf.generator.DocumentGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("DocumentGenerator")
class DocumentGeneratorTest {

    @ParameterizedTest(name = "generate() prefixes {0} references correctly")
    @EnumSource(DocumentGenerator.DocumentType.class)
    @DisplayName("generate() uses the expected type prefix")
    void generate_usesDocumentPrefix(DocumentGenerator.DocumentType type) {
        DocumentGenerator generator = new DocumentGenerator(type);
        String value = generator.generate();

        assertTrue(value.startsWith(type.getPrefix() + "-"),
                "Expected prefix " + type.getPrefix() + " in: " + value);
    }

    @Test
    @DisplayName("INVOICE references follow the expected pattern")
    void generate_invoicePattern() {
        String value = new DocumentGenerator(DocumentGenerator.DocumentType.INVOICE).generate();
        assertTrue(value.matches("INV-\\d{6}-[A-Z0-9]{6}"));
    }

    @Test
    @DisplayName("TAX_ID references follow the expected pattern")
    void generate_taxIdPattern() {
        String value = new DocumentGenerator(DocumentGenerator.DocumentType.TAX_ID).generate();
        assertTrue(value.matches("TAX-[A-Z]{2}-\\d{8}"));
    }

    @Test
    @DisplayName("generate(n) returns exactly n references")
    void generate_batchCount() {
        DocumentGenerator generator = new DocumentGenerator(DocumentGenerator.DocumentType.ORDER);
        assertEquals(15, generator.generate(15).size());
    }
}
