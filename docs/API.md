# API Reference

Project ownership and contact information: see [PROJECT_OWNERSHIP.md](PROJECT_OWNERSHIP.md).

## SecureDataFactory

Main entry point. Create instances via the fluent builder.

```java
SecureDataFactory factory = SecureDataFactory.builder()
    .securityLevel(SecurityLevel.HIGH)   // default: MEDIUM
    .locale(Locale.forLanguageTag("es")) // default: ENGLISH
    .enableAudit(true)                   // default: true
    .applyPolicies(true)                 // default: false
    .build();
```

Notes:

- `HIGH` and `CRITICAL` require audit to stay enabled.
- `applyPolicies(true)` validates that the active `SecurityLevel` satisfies every registered policy before person generation proceeds.
- Non-person generators (`Address`, `Company`, `DocumentReference`) stay synthetic/dummy-only but do not run `SecurityPolicy` transformations.

### Methods

| Method | Returns | Description |
|--------|---------|-------------|
| `generatePerson()` | `SecureData<Person>` | Generates a single person, enforcing policy minimum levels when enabled |
| `generatePersons(int)` | `List<SecureData<Person>>` | Batch generation with per-record audit trails when audit is enabled |
| `generateAddress()` | `SecureData<Address>` | Generates a single synthetic address for development and QA |
| `generateAddresses(int)` | `List<SecureData<Address>>` | Batch generation of secure synthetic addresses |
| `generateCompany()` | `SecureData<Company>` | Generates a single synthetic company record using reserved `.example` domains |
| `generateCompanies(int)` | `List<SecureData<Company>>` | Batch generation of secure synthetic company records |
| `generateProduct()` | `SecureData<Product>` | Generates a single synthetic product or catalog item |
| `generateProducts(int)` | `List<SecureData<Product>>` | Batch generation of secure synthetic products |
| `generateBankAccount()` | `SecureData<BankAccount>` | Generates a single synthetic financial account |
| `generateBankAccounts(int)` | `List<SecureData<BankAccount>>` | Batch generation of secure synthetic financial accounts |
| `generateDeviceProfile()` | `SecureData<DeviceProfile>` | Generates a single synthetic client/device profile |
| `generateDeviceProfiles(int)` | `List<SecureData<DeviceProfile>>` | Batch generation of secure synthetic device profiles |
| `generateDocumentReference(DocumentType)` | `SecureData<String>` | Generates a secure synthetic document or business reference |
| `generateDocumentReferences(DocumentType, int)` | `List<SecureData<String>>` | Batch generation of secure synthetic references |
| `encrypt(String)` | `String` | Encrypts a value as Base64 |
| `decrypt(String)` | `String` | Decrypts a value |
| `anonymize(String, AnonymizationStrategy)` | `String` | Applies an anonymization strategy |
| `getAuditLogger()` | `AuditLogger` | Returns the audit logger |
| `getPolicyRegistry()` | `SecurityPolicyRegistry` | Returns the policy registry |
| `getCryptoManager()` | `CryptoManager` | Returns the crypto manager |

---

## SecureData<T>

Wrapper returned by factory generation methods such as `generatePerson()`, `generateAddress()`, `generateCompany()`, and `generateDocumentReference()`.

```java
SecureData<Person> sd = factory.generatePerson();
sd.getData();           // the Person record
sd.getSecurityLevel();  // SecurityLevel.HIGH
sd.getChecksum();       // SHA hash of id+email
sd.getCreatedAt();      // Instant
sd.getAuditTrail();     // record-scoped List<AuditEvent>
sd.hasAuditTrail();     // boolean
```

The same wrapper contract applies to `SecureData<Address>`, `SecureData<Company>`, `SecureData<Product>`, `SecureData<BankAccount>`, `SecureData<DeviceProfile>`, and `SecureData<String>` when generating document references.

---

## Address

Synthetic address model intended for development and QA.

```java
SecureData<Address> address = factory.generateAddress();
address.getData().getLine1();
address.getData().getCity();
address.getData().getRegion();
address.getData().getPostalCode();
address.getData().getCountryCode();
address.getData().getCountry();
address.getData().getFullAddress();
```

---

## Company

Synthetic company model intended for dummy business workflows.

```java
SecureData<Company> company = factory.generateCompany();
company.getData().getLegalName();
company.getData().getTradeName();
company.getData().getIndustry();
company.getData().getSupportEmail();  // reserved .example domain
company.getData().getPhone();
company.getData().getWebsite();       // reserved .example domain
company.getData().getTaxId();
company.getData().getRegistrationNumber();
company.getData().getAddress();
company.getData().getDisplayName();
```

---

## Product

Synthetic catalog/product model for e-commerce, ERP, and inventory flows.

```java
SecureData<Product> product = factory.generateProduct();
product.getData().getSku();
product.getData().getName();
product.getData().getCategory();
product.getData().getDescription();
product.getData().getUnitPrice();
product.getData().getCurrency();
product.getData().getBarcode();
product.getData().getManufacturer();
```

---

## BankAccount

Synthetic financial account model for dummy payment and settlement flows.

```java
SecureData<BankAccount> account = factory.generateBankAccount();
account.getData().getBankName();
account.getData().getAccountHolderName();
account.getData().getAccountType();
account.getData().getCurrency();
account.getData().getAccountNumber();     // ACC-...
account.getData().getRoutingReference();  // ROUTE-...
account.getData().getIbanReference();     // IBAN-TEST-...
account.getData().getStatus();
account.getData().getCountryCode();
```

---

## DeviceProfile

Synthetic client/device model for login, telemetry, and environment testing.

```java
SecureData<DeviceProfile> device = factory.generateDeviceProfile();
device.getData().getDeviceId();
device.getData().getPlatform();
device.getData().getModel();
device.getData().getOsVersion();
device.getData().getAppVersion();
device.getData().getHostname();   // .example
device.getData().getIpAddress();  // reserved test-net ranges
device.getData().getMacAddress(); // locally administered
device.getData().getLocale();
device.getData().getEnvironment();
```

---

## DocumentGenerator.DocumentType

Supported synthetic reference categories:

- `INVOICE`
- `ORDER`
- `TICKET`
- `CONTRACT`
- `CUSTOMER`
- `POLICY`
- `SHIPMENT`
- `TAX_ID`
- `REGISTRATION`
- `ACCOUNT_REFERENCE`
- `PAYMENT_REFERENCE`
- `GENERIC_ID`

Example:

```java
SecureData<String> invoice = factory.generateDocumentReference(DocumentGenerator.DocumentType.INVOICE);
String value = invoice.getData();
```

---

## SecurityLevel

```java
SecurityLevel.LOW      // AES-128-CBC + SHA-256
SecurityLevel.MEDIUM   // AES-256-CBC + SHA-256
SecurityLevel.HIGH     // AES-256-CBC + SHA-512 + audit required
SecurityLevel.CRITICAL // AES-256-GCM + SHA-512 + audit required
```

---

## AnonymizationStrategy

| Strategy | Effect |
|----------|--------|
| `MASKING` | Replaces all characters with `*` |
| `HASHING` | One-way SHA hash |
| `PSEUDONYMIZATION` | Prefix plus `XXXX` placeholder |
| `DELETION` | Returns `null` |
| `PARTIAL_MASKING` | First 25% visible, remainder masked |

---

## AuditLogger

```java
AuditLogger log = factory.getAuditLogger();

log.addListener(event -> System.out.println(event));
log.getEvents();                              // all buffered events
log.getEventsByType(EventType.DATA_GENERATED);
log.getBufferSize();
log.clearBuffer();
log.setEnabled(false);                        // suppress logging
```

---

## Custom SecurityPolicy

```java
public class HIPAAPolicy implements SecurityPolicy {

    @Override public String getName() { return "HIPAA"; }
    @Override public String getDescription() { return "US Health data rules"; }

    @Override
    public boolean isCompliant(Person p) {
        return p.getNationalId() == null || p.getNationalId().startsWith("hashed_");
    }

    @Override
    public Person apply(Person p) {
        return Person.builder()
                .id(p.getId())
                .firstName(AnonymizationStrategy.PSEUDONYMIZATION.apply(p.getFirstName()))
                .build();
    }
}

factory.getPolicyRegistry().register(new HIPAAPolicy());
```

---

## BatchProcessor

High-throughput parallel data generation for CI/CD pipelines.

```java
SecureDataFactory factory = SecureDataFactory.builder()
        .securityLevel(SecurityLevel.HIGH)
        .build();

BatchProcessor processor = new BatchProcessor(factory, 4); // 4 threads

// Parallel person generation
List<SecureData<Person>> persons = processor.generatePersonsParallel(1000);

// Comprehensive dataset generation
BatchProcessor.BatchGenerationReport report = processor.generateComprehensiveDataset(
    BatchProcessor.DatasetConfig.defaults()
        .persons(500)
        .companies(100)
        .products(1000)
);

// Measure throughput
BatchProcessor.BatchMetrics metrics = processor.measureThroughput(1000,
    () -> factory.generatePersons(1000));

processor.shutdown();
```

### Methods

| Method | Returns | Description |
|--------|---------|-------------|
| `generatePersonsParallel(int)` | `List<SecureData<Person>>` | Parallel batch generation using thread pool |
| `generatePersonsParallelWithProgress(int, Consumer<Integer>)` | `List<SecureData<Person>>` | Parallel generation with progress callback |
| `generateComprehensiveDataset(DatasetConfig)` | `BatchGenerationReport` | Generate full test dataset (persons, companies, products, etc.) |
| `measureThroughput(int, Supplier)` | `BatchMetrics` | Benchmark generation throughput |
| `shutdown()` | `void` | Gracefully shutdown thread pool |
| `shutdownNow()` | `void` | Force shutdown thread pool |

### DatasetConfig

Builder pattern for configuring comprehensive dataset generation:

```java
BatchProcessor.DatasetConfig config = BatchProcessor.DatasetConfig.defaults()
    .persons(100)           // number of person records
    .companies(50)          // number of company records
    .products(200)           // number of product records
    .addresses(100)         // number of address records
    .bankAccounts(50)       // number of bank account records
    .devices(100)           // number of device profile records
    .invoices(100)          // number of invoice references
    .orders(100)            // number of order references
    .withInvoiceReferences(true)
    .withOrderReferences(true);
```

### BatchGenerationReport

Results from comprehensive dataset generation:

```java
BatchProcessor.BatchGenerationReport report = processor.generateComprehensiveDataset(config);

report.persons.size();           // number of persons generated
report.companies.size();         // number of companies generated
report.products.size();          // number of products generated
report.totalRecords;             // total records across all types
report.elapsedMillis;           // time taken in milliseconds
report.getRecordsPerSecond();   // throughput calculation
```

### BatchMetrics

Throughput measurement results:

```java
BatchProcessor.BatchMetrics metrics = processor.measureThroughput(1000, 
    () -> factory.generatePersons(1000));

metrics.recordCount;           // number of records generated
metrics.elapsedMillis;         // time taken
metrics.recordsPerSecond;      // throughput
metrics.threadCount;           // threads used
```
