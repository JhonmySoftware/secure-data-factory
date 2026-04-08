# Security Policy

## Supported Versions

| Version | Supported          |
| ------- | ------------------ |
| 1.0.x   | :white_check_mark: |
| < 1.0   | :x:                |

## Reporting a Vulnerability

If you discover a security vulnerability within Secure Data Factory, please send an email to:

**jhonquinonesarboleda@gmail.com**

Please include the following:
- Description of the vulnerability
- Steps to reproduce the issue
- Potential impact
- Any suggested fixes (optional)

We aim to acknowledge and address reported vulnerabilities within **7 days**.

## Security Best Practices

When using Secure Data Factory:

1. **Never hardcode encryption keys** - Use secure key management (Vault, AWS Secrets Manager, etc.)
2. **Use appropriate security levels** - `HIGH` or `CRITICAL` for production-like environments
3. **Enable audit logging** - Track all data generation events
4. **Keep dependencies updated** - We regularly update dependencies to patch vulnerabilities
5. **Isolate test environments** - Never use generated dummy data in production systems

## Dependency Security

This project uses:
- [OWASP Dependency Check](https://owasp.org/www-project-dependency-check/) for vulnerability scanning
- [Dependabot](https://github.com/dependabot) for automatic dependency updates

We monitor security advisories and release patches promptly.

## Compliance

This library generates synthetic dummy data intended for:
- Development environments
- QA testing
- CI/CD pipelines
- Demo environments

It is **not** a production data masking or migration tool.

---
*Last updated: April 2026*