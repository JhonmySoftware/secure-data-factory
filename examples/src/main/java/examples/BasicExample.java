package examples;

import io.github.sdf.SecureData;
import io.github.sdf.SecureDataFactory;
import io.github.sdf.crypto.AnonymizationStrategy;
import io.github.sdf.crypto.SecurityLevel;
import io.github.sdf.model.Person;

import java.util.List;

/**
 * Demonstrates the basic usage of SecureDataFactory.
 */
public class BasicExample {

    public static void main(String[] args) {
        SecureDataFactory factory = SecureDataFactory.builder()
                .securityLevel(SecurityLevel.MEDIUM)
                .enableAudit(true)
                .build();

        System.out.println("=== Single Person ===");
        SecureData<Person> secureData = factory.generatePerson();
        Person person = secureData.getData();
        System.out.println("Generated : " + person);
        System.out.println("Checksum  : " + secureData.getChecksum());
        System.out.println("Level     : " + secureData.getSecurityLevel());

        System.out.println("\n=== Encrypt / Decrypt ===");
        String original = person.getNationalId();
        String encrypted = factory.encrypt(original);
        String decrypted = factory.decrypt(encrypted);
        System.out.println("Original  : " + original);
        System.out.println("Encrypted : " + encrypted);
        System.out.println("Decrypted : " + decrypted);
        System.out.println("Match     : " + original.equals(decrypted));

        System.out.println("\n=== Anonymization ===");
        System.out.println("MASKING         : " + factory.anonymize(person.getEmail(), AnonymizationStrategy.MASKING));
        System.out.println("PARTIAL_MASKING : " + factory.anonymize(person.getEmail(), AnonymizationStrategy.PARTIAL_MASKING));
        System.out.println("HASHING         : " + factory.anonymize(person.getEmail(), AnonymizationStrategy.HASHING));
        System.out.println("PSEUDONYMIZATION: " + factory.anonymize(person.getEmail(), AnonymizationStrategy.PSEUDONYMIZATION));

        System.out.println("\n=== Batch (10 persons) ===");
        List<SecureData<Person>> batch = factory.generatePersons(10);
        for (SecureData<Person> item : batch) {
            System.out.printf("  %-36s | %-20s | %s%n",
                    item.getData().getId(),
                    item.getData().getFullName(),
                    item.getData().getEmail());
        }

        System.out.println("\n=== Audit Events ===");
        System.out.println("Total events: " + factory.getAuditLogger().getBufferSize());
        for (io.github.sdf.audit.AuditEvent event : factory.getAuditLogger().getEvents()) {
            System.out.println("  [" + event.getEventType() + "] " + event.getDescription());
        }
    }
}
