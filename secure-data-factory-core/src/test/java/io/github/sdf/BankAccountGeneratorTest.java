package io.github.sdf;

import io.github.sdf.generator.BankAccountGenerator;
import io.github.sdf.model.BankAccount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("BankAccountGenerator")
class BankAccountGeneratorTest {

    private BankAccountGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new BankAccountGenerator();
    }

    @Test
    @DisplayName("generate() returns a non-null BankAccount")
    void generate_nonNull() {
        assertNotNull(generator.generate());
    }

    @Test
    @DisplayName("generate() populates synthetic financial references")
    void generate_populatesFields() {
        BankAccount bankAccount = generator.generate();

        assertNotNull(bankAccount.getId());
        assertNotNull(bankAccount.getBankName());
        assertNotNull(bankAccount.getAccountHolderName());
        assertNotNull(bankAccount.getAccountType());
        assertNotNull(bankAccount.getCurrency());
        assertNotNull(bankAccount.getAccountNumber());
        assertNotNull(bankAccount.getRoutingReference());
        assertNotNull(bankAccount.getIbanReference());
        assertNotNull(bankAccount.getStatus());
        assertNotNull(bankAccount.getCountryCode());
        assertTrue(bankAccount.getAccountNumber().startsWith("ACC-"));
        assertTrue(bankAccount.getRoutingReference().startsWith("ROUTE-"));
        assertTrue(bankAccount.getIbanReference().startsWith("IBAN-TEST-"));
    }

    @Test
    @DisplayName("generate() uses explicit dummy bank naming markers")
    void generate_usesSyntheticMarkers() {
        BankAccount bankAccount = generator.generate();

        assertTrue(bankAccount.getBankName().matches(".*(Dummy|Sample|Mock|Sandbox|Test|Dev).*"),
                "Expected a synthetic marker in bank name, got: " + bankAccount.getBankName());
    }

    @Test
    @DisplayName("generate(n) returns exactly n unique bank accounts")
    void generate_batchUniqueIds() {
        List<BankAccount> batch = generator.generate(15);
        List<String> ids = batch.stream().map(BankAccount::getId).distinct().collect(Collectors.toList());

        assertEquals(15, batch.size());
        assertEquals(15, ids.size());
    }
}
