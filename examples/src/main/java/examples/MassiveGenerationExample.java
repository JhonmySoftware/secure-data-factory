package examples;

import io.github.sdf.SecureData;
import io.github.sdf.SecureDataFactory;
import io.github.sdf.crypto.SecurityLevel;
import io.github.sdf.model.Person;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Demonstrates high-volume data generation and basic statistics.
 */
public class MassiveGenerationExample {

    private static final int BATCH_SIZE = 10000;

    public static void main(String[] args) {
        SecureDataFactory factory = SecureDataFactory.builder()
                .securityLevel(SecurityLevel.MEDIUM)
                .enableAudit(false)
                .build();

        System.out.println("====================================================");
        System.out.printf("  Massive Generation - %,d persons%n", Integer.valueOf(BATCH_SIZE));
        System.out.println("====================================================\n");

        long start = System.currentTimeMillis();
        List<SecureData<Person>> batch = factory.generatePersons(BATCH_SIZE);
        long elapsed = System.currentTimeMillis() - start;

        System.out.printf("Generated %,d records in %d ms (%.0f records/sec)%n",
                Integer.valueOf(batch.size()),
                Long.valueOf(elapsed),
                Double.valueOf(batch.size() / (elapsed / 1000.0)));

        Map<String, Long> byCountry = batch.stream()
                .collect(Collectors.groupingBy(
                        item -> item.getData().getCountry(),
                        Collectors.counting()));

        System.out.println("\nTop 10 countries:");
        byCountry.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .forEach(entry -> System.out.printf("  %-30s %,d%n", entry.getKey(), entry.getValue()));

        long uniqueEmails = batch.stream()
                .map(item -> item.getData().getEmail())
                .distinct()
                .count();
        System.out.printf("%nUnique emails : %,d / %,d%n", Long.valueOf(uniqueEmails), Integer.valueOf(BATCH_SIZE));

        long withChecksum = batch.stream()
                .filter(item -> item.getChecksum() != null)
                .count();
        System.out.printf("With checksum : %,d / %,d%n", Long.valueOf(withChecksum), Integer.valueOf(BATCH_SIZE));

        System.out.println("\nBirth decade distribution:");
        batch.stream()
                .collect(Collectors.groupingBy(
                        item -> Integer.valueOf((item.getData().getBirthDate().getYear() / 10) * 10),
                        Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> System.out.printf("  %ds: %,d%n", entry.getKey(), entry.getValue()));

        System.out.println("\nDone.");
    }
}
