# Contributing

Thank you for your interest in contributing to Secure Data Factory.

## Maintainer and Project Ownership

Secure Data Factory was conceived, designed, and engineered by **Jhon Qui&#241;ones Arboleda**, Software Engineer. AI tools may be used as implementation or documentation support, but the project's authorship and technical direction remain with its author.

- Email: `jhonquinonesarboleda@gmail.com`
- LinkedIn: <https://www.linkedin.com/in/jquinonesa0001/>
- GitHub: <https://github.com/JhonmySoftware>

For the canonical ownership statement, see [docs/PROJECT_OWNERSHIP.md](docs/PROJECT_OWNERSHIP.md).

## Getting Started

1. Fork the repository and clone your fork.
2. Make sure you have **JDK 8+** and **Maven 3.9+** installed.
3. Run the test suite to verify your setup:

```bash
mvn clean verify
```

## Development Workflow

1. Create a branch from `develop`:

```bash
git checkout -b feature/my-improvement develop
```

2. Make your changes, including tests for new behavior.
3. Ensure all tests pass:

```bash
mvn clean verify
```

4. Open a Pull Request targeting the `develop` branch.

## Code Style

- Follow standard Java conventions: 4-space indentation and no wildcard imports.
- Every public class and method must have a Javadoc comment.
- New generators must implement `DataGenerator<T>` and include at least 5 unit tests.
- New security policies must implement `SecurityPolicy` and should be registered in `SecurityPolicyRegistry`.

## Commit Messages

Use the Conventional Commits format:

```text
feat: add HIPAA security policy
fix: correct partial masking for single-char inputs
test: add edge case for empty email in GDPRPolicy
docs: update API reference for SecurityLevel.CRITICAL
```

## Security Issues

Do **not** open public issues for security vulnerabilities. Use GitHub Security Advisories instead.

## License

By contributing, you agree that your contributions will be licensed under the Apache 2.0 License.
