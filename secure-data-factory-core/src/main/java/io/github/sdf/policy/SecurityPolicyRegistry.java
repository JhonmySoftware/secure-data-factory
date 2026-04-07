/*
 * Copyright (c) 2024 Secure Data Factory Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.sdf.policy;

import io.github.sdf.crypto.SecurityLevel;
import io.github.sdf.exception.VerificationException;
import io.github.sdf.model.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Registry for {@link SecurityPolicy} instances.
 *
 * <p>Policies are looked up by name and can be applied in chain to a record.
 * Comes pre-loaded with the built-in {@link GDPRPolicy}.</p>
 */
public class SecurityPolicyRegistry {

    private static final Logger log = LoggerFactory.getLogger(SecurityPolicyRegistry.class);

    private final Map<String, SecurityPolicy> policies;

    public SecurityPolicyRegistry() {
        this.policies = new LinkedHashMap<String, SecurityPolicy>();
        register(new GDPRPolicy());
    }

    /**
     * Registers a policy. Overwrites any existing policy with the same name.
     */
    public void register(SecurityPolicy policy) {
        Objects.requireNonNull(policy, "policy must not be null");
        policies.put(policy.getName(), policy);
        log.info("Security policy registered: {}", policy.getName());
    }

    /**
     * Removes a policy by name.
     *
     * @return the removed policy, or {@code null} if not found
     */
    public SecurityPolicy unregister(String policyName) {
        SecurityPolicy removed = policies.remove(policyName);
        if (removed != null) {
            log.info("Security policy removed: {}", policyName);
        }
        return removed;
    }

    public Optional<SecurityPolicy> getPolicy(String name) {
        return Optional.ofNullable(policies.get(name));
    }

    public boolean hasPolicy(String name) {
        return policies.containsKey(name);
    }

    public Collection<SecurityPolicy> getAllPolicies() {
        return Collections.unmodifiableCollection(policies.values());
    }

    /**
     * Applies all registered policies to the given person in registration order.
     *
     * @param person the record to process
     * @return the policy-compliant record
     */
    public Person applyAll(Person person) {
        Person result = person;
        for (SecurityPolicy policy : policies.values()) {
            result = policy.apply(result);
            log.debug("Applied policy '{}' to person id={}",
                    policy.getName(),
                    result != null ? result.getId() : null);
        }
        return result;
    }

    /**
     * Validates that all registered policies are compatible with the active security level.
     */
    public void validateFor(SecurityLevel activeLevel) {
        Objects.requireNonNull(activeLevel, "activeLevel must not be null");
        for (SecurityPolicy policy : policies.values()) {
            SecurityLevel minimumLevel = policy.minimumSecurityLevel();
            if (!activeLevel.isAtLeast(minimumLevel)) {
                throw new VerificationException(
                        "Policy '" + policy.getName() + "' requires at least " + minimumLevel
                                + " but active level is " + activeLevel,
                        "policy");
            }
        }
    }

    /**
     * Applies all registered policies after validating their security requirements.
     */
    public Person applyAll(Person person, SecurityLevel activeLevel) {
        validateFor(activeLevel);
        return applyAll(person);
    }

    /**
     * Applies a specific named policy to the given person.
     *
     * @param policyName the policy to apply
     * @param person the record to process
     * @return the processed record, or the original if the policy is not found
     */
    public Person apply(String policyName, Person person) {
        return getPolicy(policyName)
                .map(policy -> policy.apply(person))
                .orElseGet(() -> {
                    log.warn("Policy '{}' not found in registry, returning original record", policyName);
                    return person;
                });
    }

    /**
     * Applies a specific named policy after validating its minimum security requirement.
     */
    public Person apply(String policyName, Person person, SecurityLevel activeLevel) {
        Objects.requireNonNull(activeLevel, "activeLevel must not be null");
        return getPolicy(policyName)
                .map(policy -> {
                    SecurityLevel minimumLevel = policy.minimumSecurityLevel();
                    if (!activeLevel.isAtLeast(minimumLevel)) {
                        throw new VerificationException(
                                "Policy '" + policy.getName() + "' requires at least " + minimumLevel
                                        + " but active level is " + activeLevel,
                                "policy");
                    }
                    return policy.apply(person);
                })
                .orElseGet(() -> {
                    log.warn("Policy '{}' not found in registry, returning original record", policyName);
                    return person;
                });
    }

    public int size() {
        return policies.size();
    }
}
