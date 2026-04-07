package examples;

import io.github.sdf.SecureData;
import io.github.sdf.SecureDataFactory;
import io.github.sdf.crypto.SecurityLevel;
import io.github.sdf.model.Address;
import io.github.sdf.model.BankAccount;
import io.github.sdf.model.Company;
import io.github.sdf.model.Person;
import io.github.sdf.model.Product;

import java.util.Locale;

/**
 * Latin America & Colombia-specific data generation example.
 * 
 * Demonstrates how to generate synthetic test data tailored for
 * Colombian and LATAM markets with proper localization and compliance.
 */
public class LatamColombiaExample {

    private static final String LINE = "======================================================================";
    private static final String DASH = "----------------------------------------------------------------------";

    public static void main(String[] args) {
        System.out.println(LINE);
        System.out.println("SECURE DATA FACTORY - LATAM & COLOMBIA EXAMPLE");
        System.out.println(LINE);

        Locale colombianLocale = Locale.forLanguageTag("es-CO");
        
        SecureDataFactory factory = SecureDataFactory.builder()
                .securityLevel(SecurityLevel.HIGH)
                .locale(colombianLocale)
                .enableAudit(true)
                .build();

        System.out.println("\n[Datos generados para Colombia y Latinoamerica]");
        System.out.println("- Locale: " + colombianLocale.getDisplayName());
        System.out.println("- Nivel de Seguridad: " + SecurityLevel.HIGH);
        System.out.println("- Cumplimiento: GDPR, HIPAA, PCIDSS, Ley 1581 Colombia\n");

        System.out.println(DASH);
        System.out.println("1. PERSONAS COLOMBIANAS");
        System.out.println(DASH);
        generateColombianPersons(factory, 3);

        System.out.println("\n" + DASH);
        System.out.println("2. EMPRESAS LATAM");
        System.out.println(DASH);
        generateLatamCompanies(factory, 3);

        System.out.println("\n" + DASH);
        System.out.println("3. PRODUCTOS PARA E-COMMERCE LATAM");
        System.out.println(DASH);
        generateLatamProducts(factory, 3);

        System.out.println("\n" + DASH);
        System.out.println("4. CUENTAS BANCARIAS (Formatos IBAN LATAM)");
        System.out.println(DASH);
        generateLatamBankAccounts(factory, 3);

        System.out.println("\n" + DASH);
        System.out.println("5. DIRECCIONES COLOMBIANAS");
        System.out.println(DASH);
        generateColombianAddresses(factory, 3);

        System.out.println("\n" + DASH);
        System.out.println("6. DATOS JSON COMPATIBLES CON SISTEMAS COLOMBIANOS");
        System.out.println(DASH);
        generateJsonCompatibleData(factory);

        System.out.println("\n" + LINE);
        System.out.println("TOTAL EVENTOS DE AUDITORIA: " + factory.getAuditLogger().getBufferSize());
        System.out.println(LINE);
    }

    private static void generateColombianPersons(SecureDataFactory factory, int count) {
        for (int i = 0; i < count; i++) {
            SecureData<Person> personData = factory.generatePerson();
            Person person = personData.getData();
            
            System.out.println("  [" + person.getId().substring(0, 8) + "] " + 
                    person.getFirstName() + " " + person.getLastName());
            System.out.println("    Email: " + person.getEmail());
            System.out.println("    Tel: " + person.getPhone() + " | CC: " + person.getNationalId());
            System.out.println("    Ciudad: " + person.getCity() + ", " + person.getCountry());
            System.out.println("    Checksum: " + personData.getChecksum().substring(0, 16) + "...");
            System.out.println();
        }
    }

    private static void generateLatamCompanies(SecureDataFactory factory, int count) {
        for (int i = 0; i < count; i++) {
            SecureData<Company> companyData = factory.generateCompany();
            Company company = companyData.getData();
            
            System.out.println("  [" + (company.getIndustry() != null ? company.getIndustry() : "General") + 
                    "] " + company.getLegalName());
            System.out.println("    NIT: " + company.getTaxId() + " | Reg: " + company.getRegistrationNumber());
            System.out.println("    Web: " + company.getWebsite() + " | Email: " + company.getSupportEmail());
            System.out.println("    Ciudad: " + (company.getAddress() != null ? company.getAddress().getCity() : "N/A"));
            System.out.println();
        }
    }

    private static void generateLatamProducts(SecureDataFactory factory, int count) {
        for (int i = 0; i < count; i++) {
            SecureData<Product> productData = factory.generateProduct();
            Product product = productData.getData();
            
            System.out.println("  [" + product.getSku() + "] " + product.getName());
            System.out.println("    Precio: " + product.getCurrency() + " " + product.getUnitPrice());
            System.out.println("    Categoria: " + product.getCategory());
            System.out.println("    Barcode: " + product.getBarcode());
            System.out.println();
        }
    }

    private static void generateLatamBankAccounts(SecureDataFactory factory, int count) {
        for (int i = 0; i < count; i++) {
            SecureData<BankAccount> accountData = factory.generateBankAccount();
            BankAccount account = accountData.getData();
            
            System.out.println("  [" + account.getBankName() + "] " + account.getAccountType());
            System.out.println("    Cuenta: " + account.getAccountNumber());
            System.out.println("    IBAN Ref: " + account.getIbanReference());
            System.out.println("    Moneda: " + account.getCurrency() + " | Pais: " + account.getCountryCode());
            System.out.println();
        }
    }

    private static void generateColombianAddresses(SecureDataFactory factory, int count) {
        for (int i = 0; i < count; i++) {
            SecureData<Address> addressData = factory.generateAddress();
            Address address = addressData.getData();
            
            System.out.println("  " + address.getFullAddress());
            System.out.println("    Ciudad: " + address.getCity() + " | Depto: " + address.getRegion());
            System.out.println("    Codigo Postal: " + address.getPostalCode() + " | Pais: " + address.getCountryCode());
            System.out.println();
        }
    }

    private static void generateJsonCompatibleData(SecureDataFactory factory) {
        SecureData<Person> personData = factory.generatePerson();
        Person person = personData.getData();
        
        System.out.println("  // JSON compatible output for API integrations:");
        System.out.println("  {");
        System.out.printf("    \"id\": \"%s\",%n", person.getId());
        System.out.println("    \"tipoIdentificacion\": \"CC\",");
        System.out.printf("    \"numeroIdentificacion\": \"%s\",%n", person.getNationalId());
        System.out.printf("    \"primerNombre\": \"%s\",%n", person.getFirstName());
        System.out.println("    \"segundoNombre\": null,");
        System.out.printf("    \"primerApellido\": \"%s\",%n", person.getLastName());
        System.out.println("    \"segundoApellido\": null,");
        System.out.printf("    \"correoElectronico\": \"%s\",%n", person.getEmail());
        System.out.printf("    \"telefonoCelular\": \"%s\",%n", person.getPhone());
        System.out.printf("    \"fechaNacimiento\": \"%s\",%n", person.getBirthDate());
        System.out.println("    \"genero\": \"M\",");
        System.out.printf("    \"direccion\": \"%s\",%n", person.getAddress());
        System.out.printf("    \"ciudad\": \"%s\",%n", person.getCity());
        System.out.printf("    \"departamento\": \"%s\",%n", person.getCountry());
        System.out.println("    \"pais\": \"CO\",");
        System.out.println("    \"codigoPostal\": \"11001\",");
        System.out.println("    \"metadata\": {");
        System.out.printf("      \"securityLevel\": \"%s\",%n", personData.getSecurityLevel());
        System.out.printf("      \"checksum\": \"%s\",%n", personData.getChecksum().substring(0, 32));
        System.out.printf("      \"auditTrailEvents\": %d%n", personData.getAuditTrail().size());
        System.out.println("    }");
        System.out.println("  }");
    }
}
