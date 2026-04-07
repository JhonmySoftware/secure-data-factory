package io.github.sdf.policy;

import io.github.sdf.model.Person;

/**
 * Defines a security policy that can validate and transform generated data.
 *
 * <p>Implement this interface to enforce organization-specific or regulatory
 * constraints (GDPR, HIPAA, PCI-DSS, etc.) on generated records.</p>
 */
public interface SecurityPolicy {

    /**
     * Returns the unique name of this policy (e.g., "GDPR", "HIPAA").
     */
    String getName();

    /**
     * Returns a human-readable description of what this policy enforces.
     */
    String getDescription();

    /**
     * Checks whether the given {@link Person} record complies with this policy.
     *
     * @param person the record to validate
     * @return {@code true} if the record is compliant, {@code false} otherwise
     */
    boolean isCompliant(Person person);

    /**
     * Applies this policy to the given record, transforming or masking fields
     * as required. The returned person may be a new instance.
     *
     * @param person the original record
     * @return a policy-compliant version of the record
     */
    Person apply(Person person);

    /**
     * Returns the minimum {@link io.github.sdf.crypto.SecurityLevel} required
     * by this policy. Default is {@code null} (no requirement).
     */
    default io.github.sdf.crypto.SecurityLevel minimumSecurityLevel() {
        return null;
    }
}
