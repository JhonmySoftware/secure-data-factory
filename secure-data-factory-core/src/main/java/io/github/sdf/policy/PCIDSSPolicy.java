package io.github.sdf.policy;

import io.github.sdf.crypto.AnonymizationStrategy;
import io.github.sdf.crypto.SecurityLevel;
import io.github.sdf.model.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PCIDSSPolicy implements SecurityPolicy {

    private static final Logger log = LoggerFactory.getLogger(PCIDSSPolicy.class);

    private static final String NAME = "PCIDSS";
    private static final String DESCRIPTION =
            "Payment Card Industry Data Security Standard — protects cardholder data by masking " +
            "sensitive authentication data and ensuring PAN (Primary Account Number) is never stored in plain text.";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public SecurityLevel minimumSecurityLevel() {
        return SecurityLevel.CRITICAL;
    }

    @Override
    public boolean isCompliant(Person person) {
        if (person == null) return false;
        if (person.getNationalId() != null && !person.getNationalId().startsWith("hashed_")) {
            log.warn("PCIDSS violation: plain-text nationalId (may contain SSN/ITIN) detected for person id={}", person.getId());
            return false;
        }
        return true;
    }

    @Override
    public Person apply(Person person) {
        if (person == null) return null;

        log.debug("Applying PCIDSS policy to person id={}", person.getId());

        return Person.builder()
                .id(person.getId())
                .firstName(AnonymizationStrategy.PARTIAL_MASKING.apply(person.getFirstName()))
                .lastName(AnonymizationStrategy.PARTIAL_MASKING.apply(person.getLastName()))
                .email(maskPanFromEmail(person.getEmail()))
                .phone(AnonymizationStrategy.MASKING.apply(person.getPhone()) + phoneLastFour(person.getPhone()))
                .birthDate(shiftYearOfBirth(person.getBirthDate()))
                .address(null)
                .city(AnonymizationStrategy.PARTIAL_MASKING.apply(person.getCity()))
                .country(person.getCountry())
                .nationalId(AnonymizationStrategy.HASHING.apply(person.getNationalId()))
                .build();
    }

    private String maskPanFromEmail(String email) {
        if (email == null) return null;
        int atIndex = email.indexOf('@');
        if (atIndex <= 2) return "**@*.example";
        return email.substring(0, 2) + AnonymizationStrategy.MASKING.apply(email.substring(2, atIndex)) + "@*.example";
    }

    private String phoneLastFour(String phone) {
        if (phone == null || phone.length() < 4) return "****";
        return "****" + phone.substring(phone.length() - 4);
    }

    private java.time.LocalDate shiftYearOfBirth(java.time.LocalDate birthDate) {
        if (birthDate == null) return null;
        return birthDate.withYear(1995);
    }
}
