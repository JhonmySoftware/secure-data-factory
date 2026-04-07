# Project Positioning

Project ownership and contact information: see [PROJECT_OWNERSHIP.md](PROJECT_OWNERSHIP.md).

## Why This Project Exists

Many teams need test data, but the usual options leave gaps:

- static fixtures become brittle and repetitive
- generic fake-data libraries generate values but do not define security posture or project scope
- production masking tools are heavier, riskier, and solve a different problem

Secure Data Factory exists to generate synthetic dummy data that is broad enough for real software workflows while staying explicit about non-production usage.

## Relevance

This project is relevant when teams need:

- safe sample data for local development
- richer records for QA and regression testing
- fixture generation for CI pipelines
- demo environments without compliance exposure to live customer data
- domain-oriented dummy records such as people, companies, products, addresses, accounts, devices, and document references

## Core Difference

Secure Data Factory is not just a random-data library and not a production masking platform.

Its position is in the middle:

- broader and more structured than one-off fake-value helpers
- safer and more explicit than ad-hoc fixture files
- lighter and more development-oriented than enterprise masking/subsetting solutions

## Comparison by Category

### Versus Generic Faker Libraries

- Those libraries mainly generate values.
- Secure Data Factory also wraps records in `SecureData<T>`, attaches checksums, exposes audit trails, and supports security levels and policy enforcement.
- The project deliberately uses dummy-safe conventions such as reserved `.example` domains, synthetic financial references, and reserved test network ranges.

### Versus Static Fixtures

- Static fixtures are easy to start with but hard to scale and diversify.
- Secure Data Factory generates fresh batches on demand and covers multiple data domains with one API.
- It reduces duplication in test suites and helps avoid stale fixture drift.

### Versus Production Masking or Subsetting Tools

- Those tools are designed to transform or extract live datasets.
- Secure Data Factory does not connect to real production data and should not be positioned as a masking or migration product.
- Its job is to produce synthetic dummy data from scratch.

## Strategic Message

The value of this project is the combination of three things:

- synthetic data breadth for real development and QA workflows
- explicit security-aware handling of generated records
- a clear non-production boundary that reduces misuse and documentation ambiguity

## Advantages

Secure Data Factory offers several key advantages over alternative approaches:

- **Enhanced Security**: Incorporates encryption, anonymization, and policy enforcement, making it more robust than simple faker libraries.
- **Broad Applicability**: Covers multiple data domains essential for comprehensive testing, unlike narrow-scope tools.
- **Built-in Compliance**: Ready-to-use GDPR policies and anonymization strategies for regulated environments.
- **Scalability**: High-throughput batch generation and thread-safety support large-scale CI/CD pipelines.
- **Traceability**: Audit trails and checksums provide visibility into data generation and usage.
- **Safety by Design**: Dummy-safe conventions (e.g., .example domains) prevent production misuse.
- **Configurability**: Adjustable security levels adapt to different risk profiles and environments.
- **Community-Driven**: Open-source nature encourages contributions and community support.

## Working Securely with Dummy Data

To maximize security when using synthetic data:

- Select security levels based on environment: LOW for development, CRITICAL for high-risk tests.
- Enable auditing to track data lifecycle and policy applications.
- Apply anonymization strategies like hashing or masking for sensitive fields.
- Manage encryption keys securely and avoid embedding them in code.
- Use generated data exclusively in non-production contexts.
- Regularly update the library and dependencies to address vulnerabilities.
- Educate teams on the synthetic nature of data while treating it with production-level caution.

This approach ensures secure practices are ingrained from the start of development.