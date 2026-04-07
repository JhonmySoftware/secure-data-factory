# Secure Data Factory

> A Java 8+ library for generating synthetic dummy data for development, QA, CI, demos, and test fixtures, with optional anonymization and security controls.

[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/java-8%2B-orange.svg)](https://openjdk.org/)

---

## Project Ownership

Secure Data Factory was conceived, designed, and engineered by **Jhon Qui&#241;ones Arboleda**, Software Engineer. AI tools were used only as support for selected development and documentation tasks. The project's authorship, technical direction, architecture, and engineering decisions belong to its author.

- Email: `jhonquinonesarboleda@gmail.com`
- LinkedIn: <https://www.linkedin.com/in/jquinonesa0001/>
- GitHub: <https://github.com/JhonmySoftware>

For the canonical ownership statement, see [docs/PROJECT_OWNERSHIP.md](docs/PROJECT_OWNERSHIP.md).

---

## Scope

Secure Data Factory is designed to generate synthetic, fictitious, dummy-only data for software development and test environments.

- It does not use production records or real customer datasets.
- It is intended for development, QA, CI pipelines, demos, and test fixtures.
- It is not a production data masking, subsetting, or migration tool.
- Generated values may look plausible by design, but they are synthetic and any resemblance to real entities is coincidental.

For the detailed statement, see [docs/DUMMY_DATA_SCOPE.md](docs/DUMMY_DATA_SCOPE.md).

---

## Features

- Synthetic people, companies, addresses, products, bank accounts, device profiles, and document/reference identifiers
- Clearly dummy business records with reserved `.example` domains
- Safe synthetic addresses, phone numbers, emails, financial references, device/network identifiers, and generic references for QA workflows
- Pluggable security levels: `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`
- GDPR-ready anonymization: masking, hashing, pseudonymization, partial masking, deletion
- Security policy engine with built-in policies: GDPR, HIPAA, PCIDSS
- Full audit trail for generation, encryption, and policy application
- High-throughput batch generation with parallel processing
- Thread-safe core services for parallel test execution

---

## Quick Start

### Maven dependency

```xml
<dependency>
    <groupId>io.github.sdf</groupId>
    <artifactId>secure-data-factory-core</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle dependency

```groovy
dependencies {
    implementation 'io.github.sdf:secure-data-factory-core:1.0.0'
}
```

Or for Kotlin DSL:

```kotlin
dependencies {
    implementation("io.github.sdf:secure-data-factory-core:1.0.0")
}
```

### Gradle repository (if using older Gradle versions)

```groovy
repositories {
    mavenCentral()
}
```

### Generate a person

```java
SecureDataFactory factory = SecureDataFactory.builder()
        .securityLevel(SecurityLevel.HIGH)
        .enableAudit(true)
        .build();

SecureData<Person> result = factory.generatePerson();
Person person = result.getData();

System.out.println(person.getFullName());
System.out.println(person.getEmail());
System.out.println(result.getChecksum());
```

### Generate plain data without encryption

```java
SecureData<Person> person = factory.generatePerson();
Person clearPerson = person.getData(); // clear object

System.out.println(clearPerson.getFullName());
System.out.println(clearPerson.getEmail());
```

### Generate business dummy data

```java
SecureData<Company> company = factory.generateCompany();
SecureData<Address> address = factory.generateAddress();
SecureData<Product> product = factory.generateProduct();
SecureData<BankAccount> bankAccount = factory.generateBankAccount();
SecureData<DeviceProfile> device = factory.generateDeviceProfile();
SecureData<String> invoice = factory.generateDocumentReference(DocumentGenerator.DocumentType.INVOICE);

System.out.println(company.getData().getLegalName());
System.out.println(company.getData().getWebsite());   // always .example
System.out.println(address.getData().getFullAddress());
System.out.println(product.getData().getSku());
System.out.println(bankAccount.getData().getAccountNumber());
System.out.println(device.getData().getHostname());   // always .example
System.out.println(invoice.getData());
```

### Build a QA sandbox seed package

```java
// See examples.QaSandboxSeedExample
// Produces examples/target/generated-fixtures/qa-sandbox-seed.json
// with tenant, catalog, customers, devices, references, encrypted fields,
// and audit summary data for a QA or demo environment.
```

### Generate a GDPR-compliant batch

```java
SecureDataFactory factory = SecureDataFactory.builder()
        .securityLevel(SecurityLevel.HIGH)
        .applyPolicies(true)
        .build();

List<SecureData<Person>> customers = factory.generatePersons(1000);
```

`HIGH` and `CRITICAL` require audit to remain enabled. When `applyPolicies(true)` is used, person generation validates that the active `SecurityLevel` satisfies every registered policy's minimum requirement.

### Encrypt and anonymize fields

```java
String encrypted = factory.encrypt(sensitiveValue);
String decrypted = factory.decrypt(encrypted);

String masked = factory.anonymize(email, AnonymizationStrategy.PARTIAL_MASKING);
String hashed = factory.anonymize(nationalId, AnonymizationStrategy.HASHING);
```

---

## Architecture

```text
SecureDataFactory
|-- PersonGenerator        -> synthetic people
|-- AddressGenerator       -> synthetic addresses
|-- CompanyGenerator       -> synthetic business records
|-- ProductGenerator       -> synthetic catalog data
|-- BankAccountGenerator   -> synthetic financial account data
|-- DeviceProfileGenerator -> synthetic client/device data
|-- DocumentGenerator      -> synthetic references and identifiers
|-- CryptoManager          -> encryption, hashing, anonymization
|-- SecurityPolicyRegistry -> policy application (GDPR, HIPAA, PCIDSS)
`-- AuditLogger            -> event capture and listeners

BatchProcessor
|-- Parallel generation using ExecutorService
|-- Comprehensive dataset generation
`-- Throughput measurement
```

See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) for the detailed breakdown.

---

## Project Structure

```text
secure-data-factory/
|-- pom.xml
|-- secure-data-factory-core/
|   `-- src/main/java/io/github/sdf/
|-- examples/
|   `-- src/main/java/examples/
`-- docs/
```

---

## Security Levels

| Level | Cipher | Hash | Use case |
|-------|--------|------|----------|
| `LOW` | AES-128-CBC | SHA-256 | Local development |
| `MEDIUM` | AES-256-CBC | SHA-256 | Staging environments |
| `HIGH` | AES-256-CBC | SHA-512 | Production-like test data |
| `CRITICAL` | AES-256-GCM | SHA-512 | Financial or GDPR-critical datasets |

---

## Relevance

Secure Data Factory matters when a team needs repeatable, safe, non-production data that is still rich enough to exercise real application flows.

- Development teams can populate local databases and service stubs without touching real customer records.
- QA teams can validate forms, APIs, dashboards, workflows, and batch jobs with broader data variety.
- CI pipelines can create test fixtures on demand instead of storing brittle static JSON files.
- Demo and pre-sales environments can show realistic business scenarios without compliance risk from live data.

## What Can Be Done with This Project

With Secure Data Factory, you can:

- Generate diverse synthetic datasets for multiple domains: individuals, businesses, financial accounts, products, addresses, devices, and documents.
- Create high-throughput batches for performance testing and load simulation using parallel processing.
- Apply configurable security levels and anonymization strategies to protect generated data.
- Integrate audit trails and checksums for traceability in test environments.
- Build QA sandboxes with encrypted fields and policy-compliant records.
- Use in parallel test execution thanks to thread-safe services.
- Customize generation with pluggable policies for GDPR, HIPAA, or PCIDSS compliance.
- Export generated data in formats suitable for databases, APIs, or file-based fixtures.

## Why This Project Exists

**Problem**: Most fake data generators create realistic-looking data without security controls, compliance features, or audit trails. This leads to:
- Accidental use of "real-looking" test data in production
- Non-compliance with GDPR, HIPAA, or PCIDSS in test environments
- No traceability of how test data was generated
- Poor performance in CI/CD pipelines requiring large datasets

**Solution**: Secure Data Factory provides:
- Synthetic data with built-in safety (`.example` domains, reserved IPs)
- Security levels from AES-128 to AES-256-GCM
- Pre-built compliance policies (GDPR, HIPAA, PCIDSS)
- Full audit trail and checksums for every generated record
- High-throughput parallel processing for large test datasets

## Licensing

This project is licensed under **Apache License 2.0**.

- You are free to use, modify, and distribute this software for any purpose.
- Commercial use is permitted.
- The license comes with no warranties or liability.
- See the full [LICENSE](LICENSE) file for details.

**Why Apache 2.0?**
- Widely recognized and compatible with both proprietary and open-source projects
- Used by major projects (Apache Commons, Spring, Kubernetes)
- Provides patent protections for contributors
- Allows use in closed-source commercial products

## Advantages

- **Security-First Approach**: Unlike basic faker libraries, it includes encryption, anonymization, and policy enforcement to ensure safe handling of even synthetic data.
- **Comprehensive Coverage**: Supports a wide range of data types needed for full-stack testing, from personal data to business records.
- **Auditability**: Full audit trails help in debugging and compliance in test environments.
- **Performance**: High-throughput generation with parallel processing and thread-safety enable efficient CI/CD integration.
- **Compliance-Ready**: Built-in policies (GDPR, HIPAA, PCIDSS) and anonymization make it suitable for regulated industries.
- **Dummy-Safe Conventions**: Uses reserved domains and identifiers to prevent accidental use in production.
- **Flexibility**: Configurable security levels allow tailoring to different environments (dev, staging, prod-like tests).
- **Open Source**: Apache 2.0 license allows free use and contribution.

## Recognition & Comparison

Secure Data Factory stands out from other fake data generators:

| Feature | Secure Data Factory | JavaFaker | MockNeat |
|---------|---------------------|-----------|----------|
| Built-in encryption | AES-128 to AES-256-GCM | No | No |
| Compliance policies | GDPR, HIPAA, PCIDSS | No | No |
| Audit trail | Full logging | No | No |
| Parallel processing | Yes (BatchProcessor) | No | No |
| Dummy-safe (.example) | Yes | No | No |
| Checksums | SHA-256/512 | No | No |

For a detailed comparison, see [docs/COMPARISON.md](docs/COMPARISON.md).

## Working Securely with Dummy Data

Even though Secure Data Factory generates synthetic and random data, it's crucial to handle it securely to maintain best practices and avoid potential risks:

- **Use Appropriate Security Levels**: Choose `LOW` for local dev, `MEDIUM` for staging, `HIGH` for production-like tests, and `CRITICAL` for sensitive scenarios. Higher levels provide stronger encryption and hashing.
- **Enable Audit Trails**: Always enable auditing for traceability, especially in shared environments. This logs generation events, policy applications, and checksums.
- **Apply Policies**: Use `applyPolicies(true)` to enforce GDPR or custom policies, ensuring data minimization and anonymization.
- **Encrypt Sensitive Fields**: Even for dummy data, encrypt fields like emails, phones, or IDs when storing or transmitting.
- **Avoid Production Use**: Clearly mark generated data as dummy (e.g., via `.example` domains) and never use it in production systems.
- **Key Management**: Store encryption keys securely using tools like HashiCorp Vault or AWS Secrets Manager; never hardcode them.
- **Regular Updates**: Keep dependencies updated and scan for vulnerabilities to ensure the library itself is secure.
- **Compliance Awareness**: Understand that while data is synthetic, applying security controls helps in building secure habits and preparing for real data handling.
- **Isolate Environments**: Use generated data only in designated test, dev, or demo environments to prevent leakage.

By following these practices, you ensure that even dummy data is handled with the same care as real data, promoting secure development workflows.

---

## Build & Development

### Prerequisites

- Java 8+
- Maven 3.6+

### Compile

```bash
mvn compile
```

### Run tests

```bash
mvn test
```

### Build JAR

```bash
mvn package
```

### Run examples

```bash
mvn -pl examples exec:java -Dexec.mainClass="examples.BasicExample"
```

Available examples:
- `examples.BasicExample` - Basic usage
- `examples.BusinessDummyDataExample` - Business data generation
- `examples.PlainDataGenerationExample` - Plain (unencrypted) data
- `examples.QaSandboxSeedExample` - QA sandbox seed package
- `examples.MassiveGenerationExample` - High-throughput batch generation
- `examples.BankingDataExample` - Financial data generation
- `examples.LatamColombiaExample` - Regional data (Latam/Colombia)

### Install locally

```bash
mvn install
```

---

## Documentation

- [API Reference](docs/API.md)
- [Architecture](docs/ARCHITECTURE.md)
- [Dummy Data Scope](docs/DUMMY_DATA_SCOPE.md)
- [Examples](docs/EXAMPLES.md)
- [Project Positioning](docs/PROJECT_POSITIONING.md)
- [Security](docs/SECURITY.md)
- [Project Ownership](docs/PROJECT_OWNERSHIP.md)

---

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).

---

## License

Apache License 2.0. See [LICENSE](LICENSE).

## Versions & Updates

Check for new releases on GitHub: https://github.com/JhonmySoftware/secure-data-factory/releases

To update to a newer version, simply change the version number in your dependency:

```xml
<!-- Maven -->
<version>1.1.0</version>
```

```groovy
// Gradle
implementation 'io.github.sdf:secure-data-factory-core:1.1.0'
```

To see all available versions, visit Maven Central: https://search.maven.org/artifact/io.github.sdf/secure-data-factory-core