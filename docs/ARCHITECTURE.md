# Architecture

Project ownership and contact information: see [PROJECT_OWNERSHIP.md](PROJECT_OWNERSHIP.md).

## Overview

Secure Data Factory follows a layered architecture:

```text
+-----------------------------+
|      SecureDataFactory      |  <- Public API
| orchestrates all subsystems |
+--------------+--------------+
               |
   +-----------+-----------+-----------+
   |           |           |           |
Generator   Crypto      Policy      Audit
 Layer      Layer       Layer       Layer
```

## Layers

### 1. Public API - `SecureDataFactory`

Single entry point. Provides the fluent builder and returns `SecureData<T>` wrappers.

### 2. Generator Layer - `generator/`

Implements `DataGenerator<T>`. The generator layer combines Java Faker with curated safe dummy conventions:

- `PersonGenerator` composes synthetic name generation with safe email, phone, and address generators
- `AddressGenerator` produces clearly synthetic addresses for QA and development
- `CompanyGenerator` builds dummy business records with reserved `.example` domains
- `ProductGenerator` produces synthetic catalog and inventory records
- `BankAccountGenerator` produces dummy-only financial account references
- `DeviceProfileGenerator` produces synthetic client/device records with reserved network identifiers
- `DocumentGenerator` creates synthetic business and document references

### 3. Crypto Layer - `crypto/`

`CryptoManager` handles AES encryption and decryption, SHA hashing, HMAC, and anonymization. `SecurityLevel` controls which algorithms are used.

### 4. Policy Layer - `policy/`

`SecurityPolicyRegistry` stores named `SecurityPolicy` implementations. Policies transform `Person` records to enforce regulatory or business rules. The library includes three built-in policies:

- `GDPRPolicy`: EU General Data Protection Regulation — hashes nationalId, masks email/phone, removes addresses. Requires `HIGH` security level.
- `HIPAAPolicy`: US Health Insurance Portability and Accountability Act — pseudonymizes names, masks emails, shifts birth year. Requires `HIGH` security level.
- `PCIDSSPolicy`: Payment Card Industry Data Security Standard — masks PAN-related data, protects cardholder information. Requires `CRITICAL` security level.

### 5. Audit Layer - `audit/`

`AuditLogger` maintains an in-memory buffer of `AuditEvent` objects. `AuditListener` implementations can be registered for real-time monitoring.

## Data Flow

```text
generateCompany()
  |
  +-> CompanyGenerator.generate()        -> synthetic Company record
  +-> CryptoManager.hash()               -> integrity checksum
  +-> AuditLogger.log()                  -> record-scoped audit event
  `-> SecureData.builder().build()       -> returned to caller
```

For `generatePerson()`, the flow optionally inserts `SecurityPolicyRegistry.applyAll()` before checksum creation when `applyPolicies(true)` is enabled.

## Thread Safety

- `CryptoManager` uses `SecureRandom` and creates a fresh `Cipher` per call.
- `AuditLogger` uses concurrent collections.
- Generators that rely on Faker or mutable `Random` instances are not guaranteed to be thread-safe. Use one factory/generator instance per thread or synchronize externally for concurrent use.

## High-Throughput Processing - `processor/`

`BatchProcessor` provides parallel data generation using Java's `ExecutorService`:

- `generatePersonsParallel(int)`: Parallel batch generation using configurable thread pool
- `generateComprehensiveDataset(DatasetConfig)`: Generate full test datasets (persons, companies, products, addresses, bank accounts, devices, references)
- `measureThroughput(int, Supplier)`: Benchmark generation throughput
- `shutdown()` / `shutdownNow()`: Graceful or forced thread pool shutdown

Example:

```java
BatchProcessor processor = new BatchProcessor(factory, Runtime.getRuntime().availableProcessors());
BatchProcessor.BatchGenerationReport report = processor.generateComprehensiveDataset(
    BatchProcessor.DatasetConfig.defaults().persons(1000).companies(100));
processor.shutdown();
```
