package io.github.sdf;

import io.github.sdf.audit.AuditEvent;
import io.github.sdf.audit.AuditLogger;
import io.github.sdf.crypto.AnonymizationStrategy;
import io.github.sdf.crypto.CryptoManager;
import io.github.sdf.crypto.SecurityLevel;
import io.github.sdf.exception.GenerationException;
import io.github.sdf.exception.VerificationException;
import io.github.sdf.generator.AddressGenerator;
import io.github.sdf.generator.BankAccountGenerator;
import io.github.sdf.generator.CompanyGenerator;
import io.github.sdf.generator.DeviceProfileGenerator;
import io.github.sdf.generator.DocumentGenerator;
import io.github.sdf.generator.PersonGenerator;
import io.github.sdf.generator.ProductGenerator;
import io.github.sdf.model.Address;
import io.github.sdf.model.BankAccount;
import io.github.sdf.model.Company;
import io.github.sdf.model.DeviceProfile;
import io.github.sdf.model.Person;
import io.github.sdf.model.Product;
import io.github.sdf.policy.SecurityPolicy;
import io.github.sdf.policy.SecurityPolicyRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Main entry point for the Secure Data Factory library.
 *
 * <p>Orchestrates data generation, cryptographic protection, policy enforcement,
 * and audit logging in a single fluent API.</p>
 */
public class SecureDataFactory {

    private static final Logger log = LoggerFactory.getLogger(SecureDataFactory.class);

    private final SecurityLevel securityLevel;
    private final CryptoManager cryptoManager;
    private final AuditLogger auditLogger;
    private final SecurityPolicyRegistry policyRegistry;
    private final PersonGenerator personGenerator;
    private final AddressGenerator addressGenerator;
    private final CompanyGenerator companyGenerator;
    private final ProductGenerator productGenerator;
    private final BankAccountGenerator bankAccountGenerator;
    private final DeviceProfileGenerator deviceProfileGenerator;
    private final boolean auditEnabled;
    private final boolean applyPolicies;

    private SecureDataFactory(Builder builder) {
        this.securityLevel = builder.securityLevel;
        this.auditEnabled = builder.enableAudit;
        this.applyPolicies = builder.applyPolicies;

        if (securityLevel.requiresAuditTrail() && !auditEnabled) {
            throw new VerificationException(
                    "Security level " + securityLevel + " requires audit to be enabled",
                    "build");
        }

        this.cryptoManager = new CryptoManager(securityLevel);
        this.auditLogger = new AuditLogger("SecureDataFactory");
        this.policyRegistry = new SecurityPolicyRegistry();
        this.personGenerator = new PersonGenerator(builder.locale);
        this.addressGenerator = new AddressGenerator(builder.locale);
        this.companyGenerator = new CompanyGenerator(builder.locale);
        this.productGenerator = new ProductGenerator(builder.locale);
        this.bankAccountGenerator = new BankAccountGenerator(builder.locale);
        this.deviceProfileGenerator = new DeviceProfileGenerator(builder.locale);

        auditLogger.setEnabled(auditEnabled);
        auditLogger.log(AuditEvent.builder(AuditEvent.EventType.AUDIT_STARTED)
                .description("SecureDataFactory initialized with level=" + securityLevel)
                .build());

        log.info("SecureDataFactory ready. level={} audit={} policies={}",
                securityLevel, auditEnabled, applyPolicies);
    }

    /**
     * Generates a single {@link Person} wrapped in {@link SecureData}.
     *
     * @return secure data wrapper containing the generated person
     */
    public SecureData<Person> generatePerson() {
        if (applyPolicies) {
            policyRegistry.validateFor(securityLevel);
        }

        try {
            return toSecureData(personGenerator.generate());
        } catch (VerificationException e) {
            throw e;
        } catch (Exception e) {
            throw new GenerationException("Failed to generate Person", "PersonGenerator", e);
        }
    }

    /**
     * Generates {@code count} persons as a batch.
     *
     * @param count number of persons to generate (must be &gt; 0)
     * @return list of secure data wrappers
     */
    public List<SecureData<Person>> generatePersons(int count) {
        validatePositiveCount(count);
        if (applyPolicies) {
            policyRegistry.validateFor(securityLevel);
        }

        log.info("Starting batch generation of {} persons", count);
        long start = System.currentTimeMillis();

        try {
            List<SecureData<Person>> generated = personGenerator.generate(count).stream()
                    .map(this::toSecureData)
                    .collect(Collectors.toList());

            long elapsed = System.currentTimeMillis() - start;

            auditLogger.log(AuditEvent.builder(AuditEvent.EventType.DATA_GENERATED)
                    .description("Batch generated " + count + " persons in " + elapsed + "ms")
                    .metadata("count", count)
                    .metadata("elapsed", elapsed)
                    .build());

            log.info("Batch complete: {} persons generated in {}ms", count, elapsed);
            return Collections.unmodifiableList(generated);
        } catch (VerificationException e) {
            throw e;
        } catch (Exception e) {
            throw new GenerationException("Failed to generate Person batch", "PersonGenerator", e);
        }
    }

    /**
     * Generates a single synthetic {@link Address} wrapped in {@link SecureData}.
     *
     * @return secure data wrapper containing the generated address
     */
    public SecureData<Address> generateAddress() {
        try {
            return toSecureData(addressGenerator.generate());
        } catch (VerificationException e) {
            throw e;
        } catch (Exception e) {
            throw new GenerationException("Failed to generate Address", "AddressGenerator", e);
        }
    }

    /**
     * Generates {@code count} synthetic addresses as a batch.
     *
     * @param count number of addresses to generate (must be &gt; 0)
     * @return list of secure data wrappers
     */
    public List<SecureData<Address>> generateAddresses(int count) {
        validatePositiveCount(count);

        log.info("Starting batch generation of {} addresses", count);
        long start = System.currentTimeMillis();

        try {
            List<SecureData<Address>> generated = addressGenerator.generate(count).stream()
                    .map(this::toSecureData)
                    .collect(Collectors.toList());

            recordBatchGeneration("addresses", count, start);
            return Collections.unmodifiableList(generated);
        } catch (VerificationException e) {
            throw e;
        } catch (Exception e) {
            throw new GenerationException("Failed to generate Address batch", "AddressGenerator", e);
        }
    }

    /**
     * Generates a single synthetic {@link Company} wrapped in {@link SecureData}.
     *
     * @return secure data wrapper containing the generated company
     */
    public SecureData<Company> generateCompany() {
        try {
            return toSecureData(companyGenerator.generate());
        } catch (VerificationException e) {
            throw e;
        } catch (Exception e) {
            throw new GenerationException("Failed to generate Company", "CompanyGenerator", e);
        }
    }

    /**
     * Generates {@code count} synthetic companies as a batch.
     *
     * @param count number of companies to generate (must be &gt; 0)
     * @return list of secure data wrappers
     */
    public List<SecureData<Company>> generateCompanies(int count) {
        validatePositiveCount(count);

        log.info("Starting batch generation of {} companies", count);
        long start = System.currentTimeMillis();

        try {
            List<SecureData<Company>> generated = companyGenerator.generate(count).stream()
                    .map(this::toSecureData)
                    .collect(Collectors.toList());

            recordBatchGeneration("companies", count, start);
            return Collections.unmodifiableList(generated);
        } catch (VerificationException e) {
            throw e;
        } catch (Exception e) {
            throw new GenerationException("Failed to generate Company batch", "CompanyGenerator", e);
        }
    }

    /**
     * Generates a single synthetic {@link Product} wrapped in {@link SecureData}.
     *
     * @return secure data wrapper containing the generated product
     */
    public SecureData<Product> generateProduct() {
        try {
            return toSecureData(productGenerator.generate());
        } catch (VerificationException e) {
            throw e;
        } catch (Exception e) {
            throw new GenerationException("Failed to generate Product", "ProductGenerator", e);
        }
    }

    /**
     * Generates {@code count} synthetic products as a batch.
     *
     * @param count number of products to generate (must be &gt; 0)
     * @return list of secure data wrappers
     */
    public List<SecureData<Product>> generateProducts(int count) {
        validatePositiveCount(count);

        log.info("Starting batch generation of {} products", count);
        long start = System.currentTimeMillis();

        try {
            List<SecureData<Product>> generated = productGenerator.generate(count).stream()
                    .map(this::toSecureData)
                    .collect(Collectors.toList());

            recordBatchGeneration("products", count, start);
            return Collections.unmodifiableList(generated);
        } catch (VerificationException e) {
            throw e;
        } catch (Exception e) {
            throw new GenerationException("Failed to generate Product batch", "ProductGenerator", e);
        }
    }

    /**
     * Generates a single synthetic {@link BankAccount} wrapped in {@link SecureData}.
     *
     * @return secure data wrapper containing the generated bank account
     */
    public SecureData<BankAccount> generateBankAccount() {
        try {
            return toSecureData(bankAccountGenerator.generate());
        } catch (VerificationException e) {
            throw e;
        } catch (Exception e) {
            throw new GenerationException("Failed to generate BankAccount", "BankAccountGenerator", e);
        }
    }

    /**
     * Generates {@code count} synthetic bank accounts as a batch.
     *
     * @param count number of bank accounts to generate (must be &gt; 0)
     * @return list of secure data wrappers
     */
    public List<SecureData<BankAccount>> generateBankAccounts(int count) {
        validatePositiveCount(count);

        log.info("Starting batch generation of {} bank accounts", count);
        long start = System.currentTimeMillis();

        try {
            List<SecureData<BankAccount>> generated = bankAccountGenerator.generate(count).stream()
                    .map(this::toSecureData)
                    .collect(Collectors.toList());

            recordBatchGeneration("bank accounts", count, start);
            return Collections.unmodifiableList(generated);
        } catch (VerificationException e) {
            throw e;
        } catch (Exception e) {
            throw new GenerationException("Failed to generate BankAccount batch", "BankAccountGenerator", e);
        }
    }

    /**
     * Generates a single synthetic {@link DeviceProfile} wrapped in {@link SecureData}.
     *
     * @return secure data wrapper containing the generated device profile
     */
    public SecureData<DeviceProfile> generateDeviceProfile() {
        try {
            return toSecureData(deviceProfileGenerator.generate());
        } catch (VerificationException e) {
            throw e;
        } catch (Exception e) {
            throw new GenerationException("Failed to generate DeviceProfile", "DeviceProfileGenerator", e);
        }
    }

    /**
     * Generates {@code count} synthetic device profiles as a batch.
     *
     * @param count number of device profiles to generate (must be &gt; 0)
     * @return list of secure data wrappers
     */
    public List<SecureData<DeviceProfile>> generateDeviceProfiles(int count) {
        validatePositiveCount(count);

        log.info("Starting batch generation of {} device profiles", count);
        long start = System.currentTimeMillis();

        try {
            List<SecureData<DeviceProfile>> generated = deviceProfileGenerator.generate(count).stream()
                    .map(this::toSecureData)
                    .collect(Collectors.toList());

            recordBatchGeneration("device profiles", count, start);
            return Collections.unmodifiableList(generated);
        } catch (VerificationException e) {
            throw e;
        } catch (Exception e) {
            throw new GenerationException("Failed to generate DeviceProfile batch", "DeviceProfileGenerator", e);
        }
    }

    /**
     * Generates a synthetic document or reference identifier wrapped in {@link SecureData}.
     *
     * @param documentType the requested synthetic reference type
     * @return secure data wrapper containing the generated reference string
     */
    public SecureData<String> generateDocumentReference(DocumentGenerator.DocumentType documentType) {
        Objects.requireNonNull(documentType, "documentType must not be null");

        try {
            DocumentGenerator generator = new DocumentGenerator(documentType);
            return toSecureDocumentReference(generator.generate(), documentType);
        } catch (VerificationException e) {
            throw e;
        } catch (Exception e) {
            throw new GenerationException("Failed to generate document reference",
                    "DocumentGenerator[" + documentType + "]", e);
        }
    }

    /**
     * Generates a batch of synthetic document or reference identifiers.
     *
     * @param documentType the requested synthetic reference type
     * @param count number of references to generate (must be &gt; 0)
     * @return list of secure data wrappers
     */
    public List<SecureData<String>> generateDocumentReferences(
            DocumentGenerator.DocumentType documentType,
            int count
    ) {
        Objects.requireNonNull(documentType, "documentType must not be null");
        validatePositiveCount(count);

        log.info("Starting batch generation of {} document references for type={}", count, documentType);
        long start = System.currentTimeMillis();

        try {
            DocumentGenerator generator = new DocumentGenerator(documentType);
            List<SecureData<String>> generated = generator.generate(count).stream()
                    .map(value -> toSecureDocumentReference(value, documentType))
                    .collect(Collectors.toList());

            recordBatchGeneration(documentType + " references", count, start);
            return Collections.unmodifiableList(generated);
        } catch (VerificationException e) {
            throw e;
        } catch (Exception e) {
            throw new GenerationException("Failed to generate document reference batch",
                    "DocumentGenerator[" + documentType + "]", e);
        }
    }

    /**
     * Encrypts a value using the configured security level.
     *
     * @param value plaintext to encrypt
     * @return Base64-encoded ciphertext
     */
    public String encrypt(String value) {
        String encrypted = cryptoManager.encrypt(value);
        auditLogger.log(AuditEvent.EventType.DATA_ENCRYPTED, "Value encrypted");
        return encrypted;
    }

    /**
     * Decrypts a previously encrypted value.
     *
     * @param ciphertext Base64-encoded ciphertext
     * @return original plaintext
     */
    public String decrypt(String ciphertext) {
        String decrypted = cryptoManager.decrypt(ciphertext);
        auditLogger.log(AuditEvent.EventType.DATA_DECRYPTED, "Value decrypted");
        return decrypted;
    }

    /**
     * Anonymizes a value using the given strategy.
     *
     * @param value the PII value to anonymize
     * @param strategy the anonymization strategy
     * @return the anonymized result
     */
    public String anonymize(String value, AnonymizationStrategy strategy) {
        String result = cryptoManager.anonymize(value, strategy);
        auditLogger.log(AuditEvent.EventType.DATA_ANONYMIZED,
                "Value anonymized with strategy=" + strategy);
        return result;
    }

    public SecurityLevel getSecurityLevel() {
        return securityLevel;
    }

    public AuditLogger getAuditLogger() {
        return auditLogger;
    }

    public SecurityPolicyRegistry getPolicyRegistry() {
        return policyRegistry;
    }

    public CryptoManager getCryptoManager() {
        return cryptoManager;
    }

    private SecureData<Person> toSecureData(Person person) {
        List<AuditEvent> auditTrail = new ArrayList<AuditEvent>();
        recordDataGenerated(person, auditTrail);

        Person protectedPerson = applyPolicies ? applyPolicies(person, auditTrail) : person;
        String checksum = cryptoManager.hash(protectedPerson.getId() + protectedPerson.getEmail());

        return SecureData.<Person>builder(protectedPerson)
                .securityLevel(securityLevel)
                .auditTrail(auditTrail)
                .checksum(checksum)
                .build();
    }

    private SecureData<Address> toSecureData(Address address) {
        List<AuditEvent> auditTrail = new ArrayList<AuditEvent>();
        recordAddressGenerated(address, auditTrail);

        String checksum = cryptoManager.hash(
                safe(address.getId()) +
                        safe(address.getFullAddress()) +
                        safe(address.getCountryCode()));

        return buildSecureData(address, auditTrail, checksum);
    }

    private SecureData<Company> toSecureData(Company company) {
        List<AuditEvent> auditTrail = new ArrayList<AuditEvent>();
        recordCompanyGenerated(company, auditTrail);

        String checksum = cryptoManager.hash(
                safe(company.getId()) +
                        safe(company.getWebsite()) +
                        safe(company.getTaxId()));

        return buildSecureData(company, auditTrail, checksum);
    }

    private SecureData<Product> toSecureData(Product product) {
        List<AuditEvent> auditTrail = new ArrayList<AuditEvent>();
        recordProductGenerated(product, auditTrail);

        String checksum = cryptoManager.hash(
                safe(product.getId()) +
                        safe(product.getSku()) +
                        safe(product.getCurrency()) +
                        (product.getUnitPrice() != null ? product.getUnitPrice().toPlainString() : ""));

        return buildSecureData(product, auditTrail, checksum);
    }

    private SecureData<BankAccount> toSecureData(BankAccount bankAccount) {
        List<AuditEvent> auditTrail = new ArrayList<AuditEvent>();
        recordBankAccountGenerated(bankAccount, auditTrail);

        String checksum = cryptoManager.hash(
                safe(bankAccount.getId()) +
                        safe(bankAccount.getAccountNumber()) +
                        safe(bankAccount.getCurrency()) +
                        safe(bankAccount.getCountryCode()));

        return buildSecureData(bankAccount, auditTrail, checksum);
    }

    private SecureData<DeviceProfile> toSecureData(DeviceProfile deviceProfile) {
        List<AuditEvent> auditTrail = new ArrayList<AuditEvent>();
        recordDeviceProfileGenerated(deviceProfile, auditTrail);

        String checksum = cryptoManager.hash(
                safe(deviceProfile.getId()) +
                        safe(deviceProfile.getDeviceId()) +
                        safe(deviceProfile.getHostname()) +
                        safe(deviceProfile.getIpAddress()));

        return buildSecureData(deviceProfile, auditTrail, checksum);
    }

    private SecureData<String> toSecureDocumentReference(
            String documentReference,
            DocumentGenerator.DocumentType documentType
    ) {
        List<AuditEvent> auditTrail = new ArrayList<AuditEvent>();
        recordDocumentGenerated(documentReference, documentType, auditTrail);

        String checksum = cryptoManager.hash(documentType.name() + safe(documentReference));
        return buildSecureData(documentReference, auditTrail, checksum);
    }

    private <T> SecureData<T> buildSecureData(T data, List<AuditEvent> auditTrail, String checksum) {
        return SecureData.<T>builder(data)
                .securityLevel(securityLevel)
                .auditTrail(auditTrail)
                .checksum(checksum)
                .build();
    }

    private Person applyPolicies(Person person, List<AuditEvent> auditTrail) {
        Person protectedPerson = policyRegistry.applyAll(person, securityLevel);
        List<String> appliedPolicyNames = policyRegistry.getAllPolicies().stream()
                .map(SecurityPolicy::getName)
                .collect(Collectors.toList());

        if (!appliedPolicyNames.isEmpty()) {
            recordAuditEvent(auditTrail, AuditEvent.builder(AuditEvent.EventType.POLICY_APPLIED)
                    .description("Applied policies " + appliedPolicyNames + " to Person id=" + protectedPerson.getId())
                    .metadata("personId", protectedPerson.getId())
                    .metadata("policies", appliedPolicyNames)
                    .build());
        }

        return protectedPerson;
    }

    private void recordDataGenerated(Person person, List<AuditEvent> auditTrail) {
        recordAuditEvent(auditTrail, AuditEvent.builder(AuditEvent.EventType.DATA_GENERATED)
                .description("Generated Person id=" + person.getId())
                .metadata("personId", person.getId())
                .metadata("country", person.getCountry())
                .build());
    }

    private void recordAddressGenerated(Address address, List<AuditEvent> auditTrail) {
        recordAuditEvent(auditTrail, AuditEvent.builder(AuditEvent.EventType.DATA_GENERATED)
                .description("Generated Address id=" + address.getId())
                .metadata("addressId", address.getId())
                .metadata("country", address.getCountry())
                .metadata("countryCode", address.getCountryCode())
                .build());
    }

    private void recordCompanyGenerated(Company company, List<AuditEvent> auditTrail) {
        recordAuditEvent(auditTrail, AuditEvent.builder(AuditEvent.EventType.DATA_GENERATED)
                .description("Generated Company id=" + company.getId())
                .metadata("companyId", company.getId())
                .metadata("industry", company.getIndustry())
                .metadata("countryCode", company.getAddress() != null ? company.getAddress().getCountryCode() : null)
                .build());
    }

    private void recordProductGenerated(Product product, List<AuditEvent> auditTrail) {
        recordAuditEvent(auditTrail, AuditEvent.builder(AuditEvent.EventType.DATA_GENERATED)
                .description("Generated Product id=" + product.getId())
                .metadata("productId", product.getId())
                .metadata("sku", product.getSku())
                .metadata("category", product.getCategory())
                .build());
    }

    private void recordBankAccountGenerated(BankAccount bankAccount, List<AuditEvent> auditTrail) {
        recordAuditEvent(auditTrail, AuditEvent.builder(AuditEvent.EventType.DATA_GENERATED)
                .description("Generated BankAccount id=" + bankAccount.getId())
                .metadata("bankAccountId", bankAccount.getId())
                .metadata("currency", bankAccount.getCurrency())
                .metadata("countryCode", bankAccount.getCountryCode())
                .build());
    }

    private void recordDeviceProfileGenerated(DeviceProfile deviceProfile, List<AuditEvent> auditTrail) {
        recordAuditEvent(auditTrail, AuditEvent.builder(AuditEvent.EventType.DATA_GENERATED)
                .description("Generated DeviceProfile id=" + deviceProfile.getId())
                .metadata("deviceProfileId", deviceProfile.getId())
                .metadata("platform", deviceProfile.getPlatform())
                .metadata("environment", deviceProfile.getEnvironment())
                .build());
    }

    private void recordDocumentGenerated(
            String documentReference,
            DocumentGenerator.DocumentType documentType,
            List<AuditEvent> auditTrail
    ) {
        recordAuditEvent(auditTrail, AuditEvent.builder(AuditEvent.EventType.DATA_GENERATED)
                .description("Generated " + documentType + " reference=" + documentReference)
                .metadata("documentType", documentType.name())
                .metadata("reference", documentReference)
                .build());
    }

    private void recordBatchGeneration(String recordType, int count, long startMillis) {
        long elapsed = System.currentTimeMillis() - startMillis;

        auditLogger.log(AuditEvent.builder(AuditEvent.EventType.DATA_GENERATED)
                .description("Batch generated " + count + " " + recordType + " in " + elapsed + "ms")
                .metadata("count", count)
                .metadata("elapsed", elapsed)
                .metadata("recordType", recordType)
                .build());

        log.info("Batch complete: {} {} generated in {}ms", count, recordType, elapsed);
    }

    private void recordAuditEvent(List<AuditEvent> auditTrail, AuditEvent event) {
        auditLogger.log(event);
        if (auditEnabled && auditTrail != null) {
            auditTrail.add(event);
        }
    }

    private void validatePositiveCount(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("count must be > 0");
        }
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private SecurityLevel securityLevel = SecurityLevel.MEDIUM;
        private Locale locale = Locale.ENGLISH;
        private boolean enableAudit = true;
        private boolean applyPolicies = false;

        public Builder securityLevel(SecurityLevel level) {
            this.securityLevel = Objects.requireNonNull(level);
            return this;
        }

        public Builder locale(Locale locale) {
            this.locale = Objects.requireNonNull(locale);
            return this;
        }

        public Builder enableAudit(boolean enable) {
            this.enableAudit = enable;
            return this;
        }

        /** When true, all registered security policies are applied to each generated record. */
        public Builder applyPolicies(boolean apply) {
            this.applyPolicies = apply;
            return this;
        }

        public SecureDataFactory build() {
            return new SecureDataFactory(this);
        }
    }
}
