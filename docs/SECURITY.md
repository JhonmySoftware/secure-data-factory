# Security

Project ownership and contact information: see [PROJECT_OWNERSHIP.md](PROJECT_OWNERSHIP.md).

## Cryptographic Primitives

| Operation | Algorithm | Notes |
|-----------|-----------|-------|
| Symmetric encryption | AES-128-CBC | Used for `LOW` |
| Symmetric encryption | AES-256-CBC | Used for `MEDIUM` and `HIGH` |
| Symmetric encryption | AES-256-GCM | Used for `CRITICAL`; provides authenticity |
| Hashing | SHA-256 / SHA-512 | Selected by `SecurityLevel` |
| HMAC | HMAC-SHA-256 | Used for integrity verification |
| Key generation | `KeyGenerator/AES` | Uses JVM-provided `SecureRandom` |
| IV / nonce | `SecureRandom` | Fresh random value per encryption call |

## Runtime Guarantees

- `HIGH` and `CRITICAL` cannot be built with audit disabled.
- Policy execution validates each policy's minimum required `SecurityLevel` before generation.
- Record-scoped audit trails are attached to each `SecureData<T>` result when audit is enabled.
- Business dummy records use synthetic addresses, safe references, reserved `.example` domains, and reserved test network ranges where applicable.

## Key Management

- Keys are generated in memory via `KeyGenerator`. They are not persisted automatically.
- To persist and reuse a key, call `CryptoManager.exportKeyAsBase64()` and store the result in a secrets manager such as HashiCorp Vault or AWS Secrets Manager.
- To reload a key, pass the Base64 string to `new CryptoManager(level, base64Key)`.

## GDPR Compliance

`GDPRPolicy` applies the following data minimization rules:

- `nationalId` -> cryptographic hash selected by `SecurityLevel`
- `phone` -> partial masking with the first 25% visible
- `email` -> local part partially masked while keeping the domain
- `address` -> removed entirely, retaining only city and country

## Threat Model

- Scope: synthetic dummy data for development, QA, CI, demos, and staging-like test environments
- Threat: accidental use or storage of real PII in test suites, unsafe fake identifiers, ambiguous dummy records, or weak handling of generated test data
- Mitigation: generated data is synthetic, uses safe dummy conventions, and can be processed according to the configured policy
- Out of scope: key distribution, HSM integration, network security, and masking/exporting live production datasets

See [DUMMY_DATA_SCOPE.md](DUMMY_DATA_SCOPE.md) for the explicit project boundary.

## Dependency Security

Dependencies are scanned weekly with OWASP Dependency-Check. CodeQL static analysis runs on every push to `main`.

## Reporting Vulnerabilities

Report security issues privately via GitHub Security Advisories, not via public issues.
