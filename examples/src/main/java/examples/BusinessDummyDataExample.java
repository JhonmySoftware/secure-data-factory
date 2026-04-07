package examples;

import io.github.sdf.SecureData;
import io.github.sdf.SecureDataFactory;
import io.github.sdf.crypto.SecurityLevel;
import io.github.sdf.generator.DocumentGenerator;
import io.github.sdf.model.Address;
import io.github.sdf.model.BankAccount;
import io.github.sdf.model.Company;
import io.github.sdf.model.DeviceProfile;
import io.github.sdf.model.Product;

import java.util.List;
import java.util.Locale;

/**
 * Demonstrates synthetic business dummy data for development and QA.
 */
public class BusinessDummyDataExample {

    public static void main(String[] args) {
        SecureDataFactory factory = SecureDataFactory.builder()
                .securityLevel(SecurityLevel.MEDIUM)
                .locale(Locale.forLanguageTag("es"))
                .enableAudit(true)
                .build();

        System.out.println("====================================================");
        System.out.println("  Business Dummy Data Example");
        System.out.println("====================================================");
        System.out.println("Scope: synthetic data for development, QA, CI, demos, and test fixtures.");
        System.out.println("No record below is sourced from real production data.\n");

        SecureData<Company> company = factory.generateCompany();
        SecureData<Address> address = factory.generateAddress();
        SecureData<Product> product = factory.generateProduct();
        SecureData<BankAccount> bankAccount = factory.generateBankAccount();
        SecureData<DeviceProfile> deviceProfile = factory.generateDeviceProfile();
        SecureData<String> invoice = factory.generateDocumentReference(DocumentGenerator.DocumentType.INVOICE);
        SecureData<String> accountRef = factory.generateDocumentReference(
                DocumentGenerator.DocumentType.ACCOUNT_REFERENCE);

        System.out.println("=== Single Synthetic Company ===");
        System.out.println("Legal name   : " + company.getData().getLegalName());
        System.out.println("Trade name   : " + company.getData().getTradeName());
        System.out.println("Industry     : " + company.getData().getIndustry());
        System.out.println("Email        : " + company.getData().getSupportEmail());
        System.out.println("Phone        : " + company.getData().getPhone());
        System.out.println("Website      : " + company.getData().getWebsite());
        System.out.println("Tax ID       : " + company.getData().getTaxId());
        System.out.println("Registration : " + company.getData().getRegistrationNumber());
        System.out.println("Address      : " + company.getData().getAddress().getFullAddress());
        System.out.println("Checksum     : " + company.getChecksum());

        System.out.println("\n=== Standalone Synthetic Address ===");
        System.out.println("Address      : " + address.getData().getFullAddress());
        System.out.println("Checksum     : " + address.getChecksum());

        System.out.println("\n=== Synthetic Product ===");
        System.out.println("SKU          : " + product.getData().getSku());
        System.out.println("Name         : " + product.getData().getName());
        System.out.println("Category     : " + product.getData().getCategory());
        System.out.println("Price        : " + product.getData().getUnitPrice() + " " + product.getData().getCurrency());
        System.out.println("Barcode      : " + product.getData().getBarcode());

        System.out.println("\n=== Synthetic Financial Account ===");
        System.out.println("Bank         : " + bankAccount.getData().getBankName());
        System.out.println("Holder       : " + bankAccount.getData().getAccountHolderName());
        System.out.println("Account      : " + bankAccount.getData().getAccountNumber());
        System.out.println("Route        : " + bankAccount.getData().getRoutingReference());
        System.out.println("IBAN Ref     : " + bankAccount.getData().getIbanReference());

        System.out.println("\n=== Synthetic Device Profile ===");
        System.out.println("Device ID    : " + deviceProfile.getData().getDeviceId());
        System.out.println("Platform     : " + deviceProfile.getData().getPlatform());
        System.out.println("Model        : " + deviceProfile.getData().getModel());
        System.out.println("Hostname     : " + deviceProfile.getData().getHostname());
        System.out.println("IP           : " + deviceProfile.getData().getIpAddress());
        System.out.println("Environment  : " + deviceProfile.getData().getEnvironment());

        System.out.println("\n=== Synthetic References ===");
        System.out.println("Invoice      : " + invoice.getData());
        System.out.println("Account ref  : " + accountRef.getData());

        System.out.println("\n=== Batch (3 synthetic companies) ===");
        List<SecureData<Company>> companies = factory.generateCompanies(3);
        for (SecureData<Company> item : companies) {
            System.out.printf("  %-36s | %-32s | %s%n",
                    item.getData().getId(),
                    item.getData().getDisplayName(),
                    item.getData().getWebsite());
        }

        System.out.println("\n=== Batch (5 order references) ===");
        List<SecureData<String>> orders = factory.generateDocumentReferences(
                DocumentGenerator.DocumentType.ORDER,
                5);
        for (SecureData<String> item : orders) {
            System.out.println("  " + item.getData());
        }

        System.out.println("\n=== Audit Events ===");
        System.out.println("Total events: " + factory.getAuditLogger().getBufferSize());
    }
}
