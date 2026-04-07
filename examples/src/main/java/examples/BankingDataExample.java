package examples;

import io.github.sdf.SecureData;
import io.github.sdf.SecureDataFactory;
import io.github.sdf.crypto.SecurityLevel;
import io.github.sdf.model.Person;
import io.github.sdf.policy.GDPRPolicy;

import java.util.List;

/**
 * Simulates the generation of anonymized banking test data under GDPR constraints.
 */
public class BankingDataExample {

    public static void main(String[] args) {
        SecureDataFactory factory = SecureDataFactory.builder()
                .securityLevel(SecurityLevel.HIGH)
                .enableAudit(true)
                .applyPolicies(true)
                .build();

        System.out.println("====================================================");
        System.out.println("  Banking Test Data - GDPR Compliant");
        System.out.println("====================================================\n");

        List<SecureData<Person>> customers = factory.generatePersons(5);

        for (SecureData<Person> customer : customers) {
            Person person = customer.getData();
            System.out.printf("Customer ID : %s%n", person.getId());
            System.out.printf("Name        : %s %s%n", person.getFirstName(), person.getLastName());
            System.out.printf("Email       : %s%n", person.getEmail());
            System.out.printf("Phone       : %s%n", person.getPhone());
            System.out.printf("National ID : %s%n", person.getNationalId());
            System.out.printf("Location    : %s, %s%n", person.getCity(), person.getCountry());
            System.out.printf("Address     : %s%n", person.getAddress() == null ? "[REMOVED BY GDPR]" : person.getAddress());
            System.out.printf("Checksum    : %s%n", customer.getChecksum());
            System.out.println("----------------------------------------------------");
        }

        System.out.println("\n=== Field-Level Encryption ===");
        String sensitiveField = "ACC-12345678-EUR";
        String encrypted = factory.encrypt(sensitiveField);
        System.out.println("Plain account ref : " + sensitiveField);
        System.out.println("Encrypted ref     : " + encrypted);
        System.out.println("Decrypted ref     : " + factory.decrypt(encrypted));

        System.out.println("\n=== GDPR Compliance Check ===");
        GDPRPolicy gdpr = new GDPRPolicy();
        for (SecureData<Person> customer : customers) {
            boolean compliant = gdpr.isCompliant(customer.getData());
            System.out.printf("Customer %-36s -> compliant: %s%n", customer.getData().getId(), compliant);
        }

        System.out.println("\n=== Audit Trail ===");
        System.out.println("Total audit events: " + factory.getAuditLogger().getBufferSize());
    }
}
