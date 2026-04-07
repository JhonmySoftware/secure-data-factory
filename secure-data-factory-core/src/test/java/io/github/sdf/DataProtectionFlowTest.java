package io.github.sdf;

import io.github.sdf.audit.AuditEvent;
import io.github.sdf.crypto.CryptoManager;
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

import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Data Protection Flow")
class DataProtectionFlowTest {

    private SecureDataFactory mediumFactory;

    @BeforeEach
    void setUp() {
        mediumFactory = SecureDataFactory.builder()
                .securityLevel(SecurityLevel.MEDIUM)
                .locale(Locale.ENGLISH)
                .enableAudit(true)
                .build();
    }

    @Test
    @DisplayName("cleartext generation keeps secure envelope metadata across supported record types")
    void cleartextGeneration_hasSecureEnvelopeAcrossSupportedTypes() {
        List<SecureData<?>> records = Arrays.asList(
                mediumFactory.generatePerson(),
                mediumFactory.generateAddress(),
                mediumFactory.generateCompany(),
                mediumFactory.generateProduct(),
                mediumFactory.generateBankAccount(),
                mediumFactory.generateDeviceProfile(),
                mediumFactory.generateDocumentReference(DocumentGenerator.DocumentType.INVOICE)
        );

        for (SecureData<?> record : records) {
            assertSecureEnvelope(record);
        }
    }

    @Test
    @DisplayName("encrypt/decrypt round-trip preserves clear payload snapshots across supported record types")
    void encryptDecrypt_roundTripAcrossSupportedTypes() {
        List<String> payloads = Arrays.asList(
                snapshot(mediumFactory.generatePerson().getData()),
                snapshot(mediumFactory.generateAddress().getData()),
                snapshot(mediumFactory.generateCompany().getData()),
                snapshot(mediumFactory.generateProduct().getData()),
                snapshot(mediumFactory.generateBankAccount().getData()),
                snapshot(mediumFactory.generateDeviceProfile().getData()),
                mediumFactory.generateDocumentReference(DocumentGenerator.DocumentType.ORDER).getData()
        );

        for (String payload : payloads) {
            String encrypted = mediumFactory.encrypt(payload);
            String decrypted = mediumFactory.decrypt(encrypted);

            assertNotNull(encrypted);
            assertFalse(encrypted.isEmpty());
            assertNotEqualsWithMessage(payload, encrypted, "ciphertext must differ from clear payload");
            assertFalse(encrypted.contains(payload), "ciphertext must not embed the full clear payload");
            assertEquals(payload, decrypted, "decrypted payload must match the original clear payload");
        }
    }

    @Test
    @DisplayName("ciphertext does not expose sensitive cleartext fragments from generated records")
    void ciphertext_doesNotExposeSensitiveClearFragments() {
        SecureData<Person> person = mediumFactory.generatePerson();
        SecureData<Company> company = mediumFactory.generateCompany();
        SecureData<BankAccount> bankAccount = mediumFactory.generateBankAccount();
        SecureData<DeviceProfile> deviceProfile = mediumFactory.generateDeviceProfile();

        String personCiphertext = mediumFactory.encrypt(snapshot(person.getData()));
        String companyCiphertext = mediumFactory.encrypt(snapshot(company.getData()));
        String bankCiphertext = mediumFactory.encrypt(snapshot(bankAccount.getData()));
        String deviceCiphertext = mediumFactory.encrypt(snapshot(deviceProfile.getData()));

        assertFalse(personCiphertext.contains(person.getData().getEmail()));
        assertFalse(personCiphertext.contains(person.getData().getNationalId()));
        assertFalse(personCiphertext.contains(person.getData().getPhone()));

        assertFalse(companyCiphertext.contains(company.getData().getSupportEmail()));
        assertFalse(companyCiphertext.contains(company.getData().getWebsite()));
        assertFalse(companyCiphertext.contains(company.getData().getTaxId()));

        assertFalse(bankCiphertext.contains(bankAccount.getData().getAccountNumber()));
        assertFalse(bankCiphertext.contains(bankAccount.getData().getRoutingReference()));
        assertFalse(bankCiphertext.contains(bankAccount.getData().getIbanReference()));

        assertFalse(deviceCiphertext.contains(deviceProfile.getData().getHostname()));
        assertFalse(deviceCiphertext.contains(deviceProfile.getData().getIpAddress()));
        assertFalse(deviceCiphertext.contains(deviceProfile.getData().getMacAddress()));
    }

    @Test
    @DisplayName("exported key can decrypt encrypted generated payloads in a different crypto instance")
    void exportedKey_canDecryptGeneratedPayloads() {
        String payload = snapshot(mediumFactory.generateCompany().getData());
        String ciphertext = mediumFactory.encrypt(payload);

        CryptoManager imported = new CryptoManager(
                SecurityLevel.MEDIUM,
                mediumFactory.getCryptoManager().exportKeyAsBase64());

        assertEquals(payload, imported.decrypt(ciphertext));
    }

    @Test
    @DisplayName("CRITICAL mode rejects tampered ciphertext for generated payloads")
    void criticalMode_rejectsTamperedGeneratedPayload() {
        SecureDataFactory criticalFactory = SecureDataFactory.builder()
                .securityLevel(SecurityLevel.CRITICAL)
                .enableAudit(true)
                .build();

        String payload = snapshot(criticalFactory.generateBankAccount().getData());
        String ciphertext = criticalFactory.encrypt(payload);
        String tamperedCiphertext = tamper(ciphertext);

        assertThrows(VerificationException.class, () -> criticalFactory.decrypt(tamperedCiphertext));
    }

    private void assertSecureEnvelope(SecureData<?> record) {
        assertNotNull(record);
        assertNotNull(record.getData());
        assertNotNull(record.getCreatedAt());
        assertEquals(SecurityLevel.MEDIUM, record.getSecurityLevel());
        assertNotNull(record.getChecksum());
        assertFalse(record.getChecksum().trim().isEmpty());
        assertTrue(record.hasAuditTrail());
        assertEquals(1, record.getAuditTrail().size());
        assertEquals(AuditEvent.EventType.DATA_GENERATED, record.getAuditTrail().get(0).getEventType());
    }

    private String snapshot(Person person) {
        return join(
                person.getId(),
                person.getFullName(),
                person.getEmail(),
                person.getPhone(),
                String.valueOf(person.getBirthDate()),
                person.getAddress(),
                person.getCity(),
                person.getCountry(),
                person.getNationalId());
    }

    private String snapshot(Address address) {
        return join(
                address.getId(),
                address.getLine1(),
                address.getLine2(),
                address.getCity(),
                address.getRegion(),
                address.getPostalCode(),
                address.getCountryCode(),
                address.getCountry());
    }

    private String snapshot(Company company) {
        return join(
                company.getId(),
                company.getLegalName(),
                company.getTradeName(),
                company.getIndustry(),
                company.getSupportEmail(),
                company.getPhone(),
                company.getWebsite(),
                company.getTaxId(),
                company.getRegistrationNumber(),
                company.getAddress() != null ? company.getAddress().getFullAddress() : null);
    }

    private String snapshot(Product product) {
        return join(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getCategory(),
                product.getDescription(),
                product.getUnitPrice() != null ? product.getUnitPrice().toPlainString() : null,
                product.getCurrency(),
                product.getBarcode(),
                product.getManufacturer());
    }

    private String snapshot(BankAccount bankAccount) {
        return join(
                bankAccount.getId(),
                bankAccount.getBankName(),
                bankAccount.getAccountHolderName(),
                bankAccount.getAccountType(),
                bankAccount.getCurrency(),
                bankAccount.getAccountNumber(),
                bankAccount.getRoutingReference(),
                bankAccount.getIbanReference(),
                bankAccount.getStatus(),
                bankAccount.getCountryCode());
    }

    private String snapshot(DeviceProfile deviceProfile) {
        return join(
                deviceProfile.getId(),
                deviceProfile.getDeviceId(),
                deviceProfile.getPlatform(),
                deviceProfile.getModel(),
                deviceProfile.getOsVersion(),
                deviceProfile.getAppVersion(),
                deviceProfile.getHostname(),
                deviceProfile.getIpAddress(),
                deviceProfile.getMacAddress(),
                deviceProfile.getLocale(),
                deviceProfile.getEnvironment());
    }

    private String join(String... values) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                builder.append('|');
            }
            builder.append(values[i] == null ? "<null>" : values[i]);
        }
        return builder.toString();
    }

    private String tamper(String ciphertext) {
        byte[] decoded = Base64.getDecoder().decode(ciphertext);
        decoded[decoded.length - 1] = (byte) (decoded[decoded.length - 1] ^ 0x01);
        return Base64.getEncoder().encodeToString(decoded);
    }

    private void assertNotEqualsWithMessage(String first, String second, String message) {
        assertFalse(first.equals(second), message);
    }
}
