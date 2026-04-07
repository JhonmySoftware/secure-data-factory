# Dummy Data Scope

Project ownership and contact information: see [PROJECT_OWNERSHIP.md](PROJECT_OWNERSHIP.md).

## Purpose

Secure Data Factory exists to generate synthetic dummy data for software development, QA, CI pipelines, demos, and automated test fixtures.

## What the Project Generates

- Synthetic people with safe emails, phone numbers, and non-production identifiers
- Synthetic companies with explicit dummy naming markers and reserved `.example` websites
- Synthetic addresses for development and QA workflows
- Synthetic product/catalog records for commerce, ERP, and inventory testing
- Synthetic financial account records with dummy-only account, routing, and international references
- Synthetic device/client profiles with reserved test network ranges and `.example` hostnames
- Synthetic document and business references such as invoices, orders, tickets, tax IDs, registrations, and account references

## What the Project Does Not Do

- It does not ingest, copy, import, or transform real production customer data.
- It does not claim that generated values correspond to real people, companies, or addresses.
- It is not a data migration utility.
- It is not a production data masking or test data subsetting platform for live datasets.

## Data Safety Position

- Generated records are synthetic by design.
- Reserved domains such as `.example` are used for business websites and support emails where applicable.
- Phone numbers and reference identifiers are generated for dummy/test use, not for real-world contact or legal use.
- Security controls in this project help protect synthetic test data and enforce good engineering practices, but the dataset itself is not real production data.

## Important Clarification

This project applies security best practices to synthetic test data. That does not change the scope of the project: the output is still dummy data, not real data.

If a generated value resembles a real entity, that resemblance is incidental.
