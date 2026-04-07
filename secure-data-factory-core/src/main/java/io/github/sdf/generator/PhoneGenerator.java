package io.github.sdf.generator;

import com.github.javafaker.Faker;

import java.util.Locale;
import java.util.Random;

/**
 * Generates synthetic phone numbers in E.164 format.
 *
 * <p>Uses country prefixes that exist but generates subscriber numbers
 * that fall outside real allocations (prefixed with 555 for US, or
 * with leading zeros for international formats).</p>
 */
public class PhoneGenerator implements DataGenerator<String> {

    /**
     * Supported country presets for phone generation.
     */
    public enum Country {
        US("+1",   "555", 7),
        UK("+44",  "7700", 6),
        DE("+49",  "1511", 7),
        ES("+34",  "600",  6),
        FR("+33",  "601",  6),
        CO("+57",  "310",  7),
        MX("+52",  "55",   8);

        final String prefix;
        final String safeInfix;
        final int    subscriberDigits;

        Country(String prefix, String safeInfix, int subscriberDigits) {
            this.prefix          = prefix;
            this.safeInfix       = safeInfix;
            this.subscriberDigits = subscriberDigits;
        }
    }

    private final Country country;
    private final Faker   faker;
    private final Random  random;

    public PhoneGenerator() {
        this(Country.US);
    }

    public PhoneGenerator(Country country) {
        this.country = country;
        this.faker   = new Faker(Locale.ENGLISH);
        this.random  = new Random();
    }

    @Override
    public String generate() {
        StringBuilder sb = new StringBuilder(country.prefix)
                .append(country.safeInfix);

        for (int i = 0; i < country.subscriberDigits; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    @Override
    public String getGeneratorName() { return "PhoneGenerator"; }

    public Country getCountry() { return country; }
}
