package examples;

import io.github.sdf.SecureData;
import io.github.sdf.SecureDataFactory;
import io.github.sdf.crypto.SecurityLevel;
import io.github.sdf.generator.DocumentGenerator;
import io.github.sdf.model.Address;
import io.github.sdf.model.BankAccount;
import io.github.sdf.model.Company;
import io.github.sdf.model.DeviceProfile;
import io.github.sdf.model.Person;
import io.github.sdf.model.Product;

import java.util.Locale;

/**
 * Demonstrates plain-text generation only.
 *
 * <p>The generated records are returned in {@link SecureData}, but the domain
 * object inside {@code getData()} remains in clear text unless the caller
 * explicitly encrypts a field.</p>
 */
public class PlainDataGenerationExample {

    public static void main(String[] args) {
        SecureDataFactory factory = SecureDataFactory.builder()
                .securityLevel(SecurityLevel.MEDIUM)
                .locale(Locale.forLanguageTag("es"))
                .enableAudit(true)
                .build();

        SecureData<Person> person = factory.generatePerson();
        SecureData<Address> address = factory.generateAddress();
        SecureData<Company> company = factory.generateCompany();
        SecureData<Product> product = factory.generateProduct();
        SecureData<BankAccount> bankAccount = factory.generateBankAccount();
        SecureData<DeviceProfile> deviceProfile = factory.generateDeviceProfile();
        SecureData<String> orderReference = factory.generateDocumentReference(DocumentGenerator.DocumentType.ORDER);

        System.out.println("====================================================");
        System.out.println("  Plain Data Generation Example");
        System.out.println("====================================================");
        System.out.println("All values below are generated in clear text.");
        System.out.println("Nothing is encrypted here unless factory.encrypt(...) is called.\n");

        System.out.println("=== Person ===");
        System.out.println(person.getData());

        System.out.println("\n=== Address ===");
        System.out.println(address.getData().getFullAddress());

        System.out.println("\n=== Company ===");
        System.out.println(company.getData());

        System.out.println("\n=== Product ===");
        System.out.println(product.getData());

        System.out.println("\n=== Bank Account ===");
        System.out.println(bankAccount.getData());

        System.out.println("\n=== Device Profile ===");
        System.out.println(deviceProfile.getData());

        System.out.println("\n=== Order Reference ===");
        System.out.println(orderReference.getData());

        System.out.println("\n=== Accessing Clear Objects ===");
        System.out.println("Person email      : " + person.getData().getEmail());
        System.out.println("Company website   : " + company.getData().getWebsite());
        System.out.println("Product SKU       : " + product.getData().getSku());
        System.out.println("Account number    : " + bankAccount.getData().getAccountNumber());
        System.out.println("Device hostname   : " + deviceProfile.getData().getHostname());
        System.out.println("Reference         : " + orderReference.getData());
    }
}
