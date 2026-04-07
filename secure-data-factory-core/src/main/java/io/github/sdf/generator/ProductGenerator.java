package io.github.sdf.generator;

import io.github.sdf.model.Product;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

/**
 * Generates synthetic product and catalog records for dummy commerce scenarios.
 */
public class ProductGenerator implements DataGenerator<Product> {

    private static final List<String> NAME_PREFIXES = Arrays.asList(
            "Sandbox", "Mock", "Sample", "Test", "Dev", "Pilot"
    );
    private static final List<String> PRODUCT_TERMS = Arrays.asList(
            "Gateway", "Connector", "Bundle", "License", "Starter Kit", "Console",
            "Suite", "Module", "Package", "Subscription"
    );
    private static final List<String> CATEGORIES = Arrays.asList(
            "Software", "Hardware", "Retail", "Finance", "Analytics",
            "Logistics", "Support", "Security", "Testing", "Education"
    );
    private static final List<String> CURRENCIES = Arrays.asList(
            "USD", "EUR", "COP", "MXN", "GBP"
    );
    private static final List<String> MANUFACTURERS = Arrays.asList(
            "Northbridge Dummy Labs", "BlueHarbor Sample Works", "VertexPoint Test Systems",
            "CloudMesa Sandbox Group", "PilotStone Dev Services"
    );

    private final Random random;

    public ProductGenerator() {
        this(Locale.ENGLISH);
    }

    public ProductGenerator(Locale locale) {
        this.random = new Random();
    }

    @Override
    public Product generate() {
        String category = pick(CATEGORIES);
        String currency = pick(CURRENCIES);
        String name = pick(NAME_PREFIXES) + " " + category + " " + pick(PRODUCT_TERMS);

        return Product.builder()
                .id(UUID.randomUUID().toString())
                .sku("SKU-" + abbreviation(category) + "-" + randomAlphaNumeric(6))
                .name(name)
                .category(category)
                .description("Synthetic " + category.toLowerCase() + " item for development and QA workflows.")
                .unitPrice(randomPrice())
                .currency(currency)
                .barcode("BAR-" + randomDigits(12))
                .manufacturer(pick(MANUFACTURERS))
                .build();
    }

    @Override
    public String getGeneratorName() {
        return "ProductGenerator";
    }

    private BigDecimal randomPrice() {
        double baseValue = 5 + (random.nextDouble() * 4995);
        return BigDecimal.valueOf(baseValue).setScale(2, RoundingMode.HALF_UP);
    }

    private String abbreviation(String category) {
        String normalized = category.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
        if (normalized.length() >= 3) {
            return normalized.substring(0, 3);
        }
        return (normalized + "XXX").substring(0, 3);
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
}
