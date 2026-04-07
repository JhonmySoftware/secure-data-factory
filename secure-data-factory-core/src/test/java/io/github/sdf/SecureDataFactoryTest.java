package io.github.sdf;

import io.github.sdf.audit.AuditEvent;
import io.github.sdf.crypto.AnonymizationStrategy;
import io.github.sdf.crypto.SecurityLevel;
import io.github.sdf.exception.VerificationException;
import io.github.sdf.generator.DocumentGenerator;
import io.github.sdf.model.Address;
import io.github.sdf.model.BankAccount;
import io.github.sdf.model.Company;
import io.github.sdf.model.DeviceProfile;
import io.github.sdf.model.Person;
import io.github.sdf.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("SecureDataFactory")
class SecureDataFactoryTest {

    private SecureDataFactory factory;

    @BeforeEach
    void setUp() {
        factory = SecureDataFactory.builder()
                .securityLevel(SecurityLevel.MEDIUM)
                .enableAudit(true)
                .build();
    }

    @Test
    @DisplayName("generatePerson() returns non-null SecureData")
    void generatePerson_returnsNonNull() {
        SecureData<Person> result = factory.generatePerson();
        assertNotNull(result);
        assertNotNull(result.getData());
    }

    @Test
    @DisplayName("generatePerson() populates all Person fields")
    void generatePerson_populatesAllFields() {
        Person person = factory.generatePerson().getData();

        assertNotNull(person.getId(), "id must not be null");
        assertNotNull(person.getFirstName(), "firstName must not be null");
        assertNotNull(person.getLastName(), "lastName must not be null");
        assertNotNull(person.getEmail(), "email must not be null");
        assertNotNull(person.getPhone(), "phone must not be null");
        assertNotNull(person.getBirthDate(), "birthDate must not be null");
    }

    @Test
    @DisplayName("generatePerson() uses configured security level")
    void generatePerson_usesConfiguredSecurityLevel() {
        SecureData<Person> result = factory.generatePerson();
        assertEquals(SecurityLevel.MEDIUM, result.getSecurityLevel());
    }

    @Test
    @DisplayName("generatePerson() includes a non-empty checksum")
    void generatePerson_hasChecksum() {
        SecureData<Person> result = factory.generatePerson();
        assertNotNull(result.getChecksum());
        assertFalse(result.getChecksum().isEmpty());
    }

    @Test
    @DisplayName("generatePerson() records audit events when audit is enabled")
    void generatePerson_recordsAuditEvents() {
        factory.generatePerson();
        assertTrue(factory.getAuditLogger().getBufferSize() > 0);
    }

    @Test
    @DisplayName("generatePerson() keeps audit trail scoped to the generated record")
    void generatePerson_auditTrailScopedToRecord() {
        SecureData<Person> first = factory.generatePerson();
        SecureData<Person> second = factory.generatePerson();

        assertEquals(1, first.getAuditTrail().size());
        assertEquals(1, second.getAuditTrail().size());
        assertEquals(AuditEvent.EventType.DATA_GENERATED, first.getAuditTrail().get(0).getEventType());
        assertEquals(AuditEvent.EventType.DATA_GENERATED, second.getAuditTrail().get(0).getEventType());
        assertNotEquals(first.getAuditTrail().get(0).getEventId(), second.getAuditTrail().get(0).getEventId());
    }

    @Test
    @DisplayName("generatePersons(n) returns exactly n records")
    void generatePersonsBatch_returnsExactCount() {
        List<SecureData<Person>> batch = factory.generatePersons(50);
        assertEquals(50, batch.size());
    }

    @Test
    @DisplayName("generatePersons(n) produces unique IDs")
    void generatePersonsBatch_uniqueIds() {
        List<SecureData<Person>> batch = factory.generatePersons(100);
        long uniqueIds = batch.stream()
                .map(sd -> sd.getData().getId())
                .distinct()
                .count();
        assertEquals(100, uniqueIds);
    }

    @Test
    @DisplayName("generatePersons(n) stores per-record audit trails")
    void generatePersonsBatch_recordsPerItemAuditTrail() {
        List<SecureData<Person>> batch = factory.generatePersons(3);

        for (SecureData<Person> item : batch) {
            assertTrue(item.hasAuditTrail());
            assertEquals(1, item.getAuditTrail().size());
            assertEquals(AuditEvent.EventType.DATA_GENERATED, item.getAuditTrail().get(0).getEventType());
        }
    }

    @Test
    @DisplayName("generatePersons(0) throws IllegalArgumentException")
    void generatePersonsBatch_zeroCountThrows() {
        assertThrows(IllegalArgumentException.class, () -> factory.generatePersons(0));
    }

    @Test
    @DisplayName("generatePersons(-1) throws IllegalArgumentException")
    void generatePersonsBatch_negativeCountThrows() {
        assertThrows(IllegalArgumentException.class, () -> factory.generatePersons(-1));
    }

    @Test
    @DisplayName("generateAddress() returns secure synthetic address data")
    void generateAddress_returnsSecureSyntheticData() {
        SecureData<Address> result = factory.generateAddress();

        assertNotNull(result);
        assertNotNull(result.getData());
        assertNotNull(result.getData().getLine1());
        assertNotNull(result.getData().getCity());
        assertNotNull(result.getChecksum());
        assertEquals(1, result.getAuditTrail().size());
        assertEquals(AuditEvent.EventType.DATA_GENERATED, result.getAuditTrail().get(0).getEventType());
    }

    @Test
    @DisplayName("generateAddresses(n) returns exactly n secure addresses")
    void generateAddressesBatch_returnsExactCount() {
        List<SecureData<Address>> batch = factory.generateAddresses(12);

        assertEquals(12, batch.size());
        assertTrue(batch.stream().allMatch(SecureData::hasAuditTrail));
    }

    @Test
    @DisplayName("generateCompany() returns secure synthetic company data")
    void generateCompany_returnsSecureSyntheticData() {
        SecureData<Company> result = factory.generateCompany();
        Company company = result.getData();

        assertNotNull(result);
        assertNotNull(company);
        assertTrue(company.getLegalName().matches(".*(Dummy|Sample|Mock|Sandbox|Test|Dev).*"));
        assertTrue(company.getSupportEmail().endsWith(".example"));
        assertTrue(company.getWebsite().endsWith(".example"));
        assertNotNull(result.getChecksum());
        assertEquals(1, result.getAuditTrail().size());
    }

    @Test
    @DisplayName("generateCompanies(n) returns exactly n secure companies")
    void generateCompaniesBatch_returnsExactCount() {
        List<SecureData<Company>> batch = factory.generateCompanies(8);

        assertEquals(8, batch.size());
        assertTrue(batch.stream().allMatch(item -> item.getData().getAddress() != null));
    }

    @Test
    @DisplayName("generateProduct() returns secure synthetic product data")
    void generateProduct_returnsSecureSyntheticData() {
        SecureData<Product> result = factory.generateProduct();
        Product product = result.getData();

        assertNotNull(result);
        assertNotNull(product);
        assertTrue(product.getSku().startsWith("SKU-"));
        assertTrue(product.getBarcode().startsWith("BAR-"));
        assertNotNull(result.getChecksum());
        assertEquals(1, result.getAuditTrail().size());
    }

    @Test
    @DisplayName("generateProducts(n) returns exactly n secure products")
    void generateProductsBatch_returnsExactCount() {
        List<SecureData<Product>> batch = factory.generateProducts(7);

        assertEquals(7, batch.size());
        assertTrue(batch.stream().allMatch(item -> item.getData().getUnitPrice().signum() > 0));
    }

    @Test
    @DisplayName("generateBankAccount() returns secure synthetic bank account data")
    void generateBankAccount_returnsSecureSyntheticData() {
        SecureData<BankAccount> result = factory.generateBankAccount();
        BankAccount bankAccount = result.getData();

        assertNotNull(result);
        assertNotNull(bankAccount);
        assertTrue(bankAccount.getAccountNumber().startsWith("ACC-"));
        assertTrue(bankAccount.getIbanReference().startsWith("IBAN-TEST-"));
        assertNotNull(result.getChecksum());
        assertEquals(1, result.getAuditTrail().size());
    }

    @Test
    @DisplayName("generateBankAccounts(n) returns exactly n secure bank accounts")
    void generateBankAccountsBatch_returnsExactCount() {
        List<SecureData<BankAccount>> batch = factory.generateBankAccounts(9);

        assertEquals(9, batch.size());
        assertTrue(batch.stream().allMatch(item -> item.getData().getRoutingReference().startsWith("ROUTE-")));
    }

    @Test
    @DisplayName("generateDeviceProfile() returns secure synthetic device data")
    void generateDeviceProfile_returnsSecureSyntheticData() {
        SecureData<DeviceProfile> result = factory.generateDeviceProfile();
        DeviceProfile deviceProfile = result.getData();

        assertNotNull(result);
        assertNotNull(deviceProfile);
        assertTrue(deviceProfile.getHostname().endsWith(".example"));
        assertTrue(deviceProfile.getMacAddress().startsWith("02:"));
        assertNotNull(result.getChecksum());
        assertEquals(1, result.getAuditTrail().size());
    }

    @Test
    @DisplayName("generateDeviceProfiles(n) returns exactly n secure device profiles")
    void generateDeviceProfilesBatch_returnsExactCount() {
        List<SecureData<DeviceProfile>> batch = factory.generateDeviceProfiles(11);

        assertEquals(11, batch.size());
        assertTrue(batch.stream().allMatch(item -> item.getData().getIpAddress() != null));
    }

    @Test
    @DisplayName("generateDocumentReference() wraps references with checksum and audit trail")
    void generateDocumentReference_wrapsReference() {
        SecureData<String> result = factory.generateDocumentReference(DocumentGenerator.DocumentType.INVOICE);

        assertNotNull(result);
        assertTrue(result.getData().startsWith("INV-"));
        assertNotNull(result.getChecksum());
        assertEquals(1, result.getAuditTrail().size());
    }

    @Test
    @DisplayName("generateDocumentReferences(n) returns exactly n secure references")
    void generateDocumentReferencesBatch_returnsExactCount() {
        List<SecureData<String>> batch = factory.generateDocumentReferences(
                DocumentGenerator.DocumentType.ORDER,
                6);

        assertEquals(6, batch.size());
        assertTrue(batch.stream().allMatch(item -> item.getData().startsWith("ORD-")));
    }

    @Test
    @DisplayName("encrypt/decrypt round-trip preserves plaintext")
    void encryptDecrypt_roundTrip() {
        String original = "sensitive-data-12345";
        String encrypted = factory.encrypt(original);
        String decrypted = factory.decrypt(encrypted);

        assertNotEquals(original, encrypted, "ciphertext must differ from plaintext");
        assertEquals(original, decrypted, "decrypted must equal original");
    }

    @Test
    @DisplayName("encrypt(null) returns null")
    void encrypt_nullReturnsNull() {
        assertNull(factory.encrypt(null));
    }

    @Test
    @DisplayName("anonymize with MASKING replaces all chars with asterisks")
    void anonymize_maskingStrategy() {
        String result = factory.anonymize("SensitiveValue", AnonymizationStrategy.MASKING);
        assertNotNull(result);
        assertTrue(result.chars().allMatch(c -> c == '*'));
        assertEquals("SensitiveValue".length(), result.length());
    }

    @Test
    @DisplayName("anonymize with DELETION returns null")
    void anonymize_deletionStrategy() {
        assertNull(factory.anonymize("anything", AnonymizationStrategy.DELETION));
    }

    @Test
    @DisplayName("anonymize with PARTIAL_MASKING keeps first character visible")
    void anonymize_partialMaskingKeepsPrefix() {
        String result = factory.anonymize("Hello", AnonymizationStrategy.PARTIAL_MASKING);
        assertNotNull(result);
        assertEquals('H', result.charAt(0));
    }

    @Test
    @DisplayName("factory with GDPR policy anonymizes nationalId")
    void gdprPolicy_anonymizesNationalId() {
        SecureDataFactory gdprFactory = SecureDataFactory.builder()
                .securityLevel(SecurityLevel.HIGH)
                .applyPolicies(true)
                .build();

        Person person = gdprFactory.generatePerson().getData();
        assertTrue(
                person.getNationalId() == null || person.getNationalId().startsWith("hashed_"),
                "GDPR policy must hash the nationalId"
        );
    }

    @Test
    @DisplayName("factory with GDPR policy records a POLICY_APPLIED audit event")
    void gdprPolicy_recordsPolicyAppliedEvent() {
        SecureDataFactory gdprFactory = SecureDataFactory.builder()
                .securityLevel(SecurityLevel.HIGH)
                .applyPolicies(true)
                .build();

        SecureData<Person> result = gdprFactory.generatePerson();
        List<AuditEvent.EventType> eventTypes = result.getAuditTrail().stream()
                .map(AuditEvent::getEventType)
                .collect(Collectors.toList());

        assertEquals(2, eventTypes.size());
        assertEquals(AuditEvent.EventType.DATA_GENERATED, eventTypes.get(0));
        assertEquals(AuditEvent.EventType.POLICY_APPLIED, eventTypes.get(1));
    }

    @Test
    @DisplayName("applyPolicies(true) fails at generation when security level is below policy minimum")
    void applyPoliciesBelowMinimumLevelFails() {
        SecureDataFactory weakFactory = SecureDataFactory.builder()
                .securityLevel(SecurityLevel.MEDIUM)
                .applyPolicies(true)
                .build();

        assertThrows(VerificationException.class, weakFactory::generatePerson);
    }

    @Test
    @DisplayName("applyPolicies(true) does not block non-person dummy generators")
    void applyPoliciesStillAllowsNonPersonDummyGenerators() {
        SecureDataFactory weakFactory = SecureDataFactory.builder()
                .securityLevel(SecurityLevel.MEDIUM)
                .applyPolicies(true)
                .build();

        assertDoesNotThrow(weakFactory::generateAddress);
        assertDoesNotThrow(weakFactory::generateCompany);
        assertDoesNotThrow(weakFactory::generateProduct);
        assertDoesNotThrow(weakFactory::generateBankAccount);
        assertDoesNotThrow(weakFactory::generateDeviceProfile);
        assertDoesNotThrow(() -> weakFactory.generateDocumentReference(DocumentGenerator.DocumentType.GENERIC_ID));
    }

    @Test
    @DisplayName("HIGH security level requires audit to be enabled")
    void highSecurityRequiresAudit() {
        assertThrows(VerificationException.class, () -> SecureDataFactory.builder()
                .securityLevel(SecurityLevel.HIGH)
                .enableAudit(false)
                .build());
    }

    @Test
    @DisplayName("CRITICAL security level requires audit to be enabled")
    void criticalSecurityRequiresAudit() {
        assertThrows(VerificationException.class, () -> SecureDataFactory.builder()
                .securityLevel(SecurityLevel.CRITICAL)
                .enableAudit(false)
                .build());
    }

    @Test
    @DisplayName("audit-disabled factories return empty per-record audit trails")
    void auditDisabledReturnsEmptyTrail() {
        SecureDataFactory silentFactory = SecureDataFactory.builder()
                .securityLevel(SecurityLevel.LOW)
                .enableAudit(false)
                .build();

        SecureData<Person> result = silentFactory.generatePerson();

        assertFalse(result.hasAuditTrail());
        assertTrue(result.getAuditTrail().isEmpty());
        assertEquals(0, silentFactory.getAuditLogger().getBufferSize());
    }

    @Test
    @DisplayName("default builder produces MEDIUM security level")
    void builder_defaultSecurityLevel() {
        SecureDataFactory builtFactory = SecureDataFactory.builder().build();
        assertEquals(SecurityLevel.MEDIUM, builtFactory.getSecurityLevel());
    }
}
