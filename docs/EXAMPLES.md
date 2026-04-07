# Examples

Project ownership and contact information: see [PROJECT_OWNERSHIP.md](PROJECT_OWNERSHIP.md).

All runnable examples live in `examples/src/main/java/examples`.

These examples generate synthetic dummy data only. They are meant for development, QA, CI, demos, and test fixtures, not for production datasets.

Build the project from the repository root before running examples:

```bash
mvn clean compile
```

Recommended execution path:

- IntelliJ IDEA: use the committed run configurations `BasicExample`, `BankingDataExample`, `MassiveGenerationExample`, `BusinessDummyDataExample`, `PlainDataGenerationExample`, and `QaSandboxSeedExample`.
- Source view: open the corresponding example class and run its `main()` method from the editor.

---

## 1 - Basic Usage (`BasicExample.java`)

Covers: single person generation, encrypt/decrypt round-trip, all five anonymization strategies, a batch of 10, and the audit trail dump.

**Sample output**

```text
=== Single Person ===
Generated : Person{id='3f2a...', fullName='Alice Johnson', email='alice.j42@example.com', country='United States'}
Checksum  : a3f8c1d2...
Level     : MEDIUM

=== Encrypt / Decrypt ===
Original  : AB123456CD
Encrypted : eyJpdiI6...  (Base64, changes every run)
Decrypted : AB123456CD
Match     : true

=== Anonymization ===
MASKING         : ********
PARTIAL_MASKING : al******@example.com
HASHING         : hashed_3b2c1a
PSEUDONYMIZATION: ali****XXXX@example.com
```

---

## 2 - Banking / GDPR (`BankingDataExample.java`)

Covers: `HIGH` security level, `applyPolicies(true)`, GDPR compliance checks, and field-level encryption of an account reference.

```text
Customer ID : 7a1b2c3d-...
Name        : Maria G***
Email       : ma**@example.com
Phone       : +131055****
National ID : hashed_4a9f1e
Location    : Madrid, Spain
Address     : [REMOVED BY GDPR]
```

---

## 3 - Massive Generation (`MassiveGenerationExample.java`)

Covers: 10,000 records, throughput metrics, country distribution, unique email count, and birth-decade histograms.

```text
Generated 10,000 records in 1,842 ms (5,429 records/sec)

Top 10 countries:
  United States                  312
  United Kingdom                 289
  Germany                        251
  ...

Unique emails : 10,000 / 10,000
With checksum : 10,000 / 10,000

Birth decade distribution:
  1940s: 842
  1950s: 1,231
  1960s: 1,388
  1970s: 1,502
  1980s: 1,619
  1990s: 1,844
  2000s: 1,574
```

---

## 4 - Business Dummy Data (`BusinessDummyDataExample.java`)

Covers: synthetic companies, standalone synthetic addresses, products, financial accounts, device profiles, invoice/account references, company batches, and explicit dummy-only scope messaging.

```text
Scope: synthetic data for development, QA, CI, demos, and test fixtures.
No record below is sourced from real production data.

=== Single Synthetic Company ===
Legal name   : Northbridge Dummy Systems LLC
Email        : support@northbridge-dummy-systems.example
Website      : https://www.northbridge-dummy-systems.example
Tax ID       : TAX-CO-12345678
Registration : REG-CO-1234567

=== Synthetic Product ===
SKU          : SKU-SOF-A1B2C3
Name         : Sandbox Software Gateway
Price        : 249.90 USD

=== Synthetic Financial Account ===
Account      : ACC-CO-1234567890
Route        : ROUTE-CO-123456
IBAN Ref     : IBAN-TEST-CO-AB12CD34EF56GH

=== Synthetic Device Profile ===
Hostname     : qa-web-1042.example
IP           : 198.51.100.25

=== Synthetic References ===
Invoice      : INV-202604-AB12CD
Account ref  : ACC-12345678-USD
```

---

## 5 - QA Sandbox Seed (`QaSandboxSeedExample.java`)

Covers: a realistic project usage flow where the library assembles a JSON seed package for QA or demo environments, including tenant data, catalog, customers, devices, document references, encrypted operational fields, and audit summary output.

```text
Use case : populate a QA or demo environment with a ready-to-consume JSON seed package.
Output   : .../examples/target/generated-fixtures/qa-sandbox-seed.json
Tenant   : SilverOak Sample Foods
Catalog  : 6 products
Users    : 4 policy-protected customer records
Devices  : 4 device profiles
Orders   : 4 synthetic order references
Audit    : 20 events recorded
```

This example is intentionally closer to a real software workflow than the console-only examples: it exports a fixture package that another service, script, or UI test suite could consume directly.

---

## 6 - Plain Data Generation (`PlainDataGenerationExample.java`)

Covers: clear-text generation only. This is the simplest entry point if you want to verify the generated classes and inspect the objects returned by `getData()` without involving field encryption.

```text
All values below are generated in clear text.
Nothing is encrypted here unless factory.encrypt(...) is called.

=== Person ===
Person{id='...', fullName='...', email='...', country='...'}

=== Company ===
Company{id='...', displayName='...', industry='...', website='...'}
```

---

## Writing Your Own Generator

```java
public class CreditCardGenerator implements DataGenerator<String> {

    private final Faker faker = new Faker();

    @Override
    public String generate() {
        return "4111" + faker.number().digits(12);
    }

    @Override
    public String getGeneratorName() {
        return "CreditCardGenerator";
    }
}

CreditCardGenerator ccGen = new CreditCardGenerator();
String card = ccGen.generate();
List<String> cards = ccGen.generate(100);
```

---

## Adding a Custom Security Policy

```java
public class HIPAAPolicy implements SecurityPolicy {

    @Override public String getName()        { return "HIPAA"; }
    @Override public String getDescription() { return "US health data rules"; }

    @Override
    public SecurityLevel minimumSecurityLevel() { return SecurityLevel.HIGH; }

    @Override
    public boolean isCompliant(Person p) {
        return p.getNationalId() == null
            || p.getNationalId().startsWith("hashed_");
    }

    @Override
    public Person apply(Person p) {
        return Person.builder()
            .id(p.getId())
            .firstName(AnonymizationStrategy.PSEUDONYMIZATION.apply(p.getFirstName()))
            .lastName(AnonymizationStrategy.PSEUDONYMIZATION.apply(p.getLastName()))
            .email(AnonymizationStrategy.PARTIAL_MASKING.apply(p.getEmail()))
            .phone(AnonymizationStrategy.MASKING.apply(p.getPhone()))
            .birthDate(p.getBirthDate())
            .city(p.getCity())
            .country(p.getCountry())
            .nationalId(AnonymizationStrategy.HASHING.apply(p.getNationalId()))
            .build();
    }
}

SecureDataFactory factory = SecureDataFactory.builder()
    .securityLevel(SecurityLevel.HIGH)
    .applyPolicies(true)
    .build();

factory.getPolicyRegistry().register(new HIPAAPolicy());
SecureData<Person> result = factory.generatePerson();
```

---

## Real-Time Audit Listener

```java
SecureDataFactory factory = SecureDataFactory.builder().build();

factory.getAuditLogger().addListener(event -> {
    System.out.printf("[%s] %s - %s%n",
        event.getTimestamp(),
        event.getEventType(),
        event.getDescription());
});

factory.generatePersons(50);
```
