package io.github.sdf.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Objects;

/**
 * Represents a synthetic company record for development and test environments.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Company {

    private String id;
    private String legalName;
    private String tradeName;
    private String industry;
    private String supportEmail;
    private String phone;
    private String website;
    private String taxId;
    private String registrationNumber;
    private Address address;

    public Company() {
    }

    private Company(Builder builder) {
        this.id = builder.id;
        this.legalName = builder.legalName;
        this.tradeName = builder.tradeName;
        this.industry = builder.industry;
        this.supportEmail = builder.supportEmail;
        this.phone = builder.phone;
        this.website = builder.website;
        this.taxId = builder.taxId;
        this.registrationNumber = builder.registrationNumber;
        this.address = builder.address;
    }

    public String getId() {
        return id;
    }

    public String getLegalName() {
        return legalName;
    }

    public String getTradeName() {
        return tradeName;
    }

    public String getIndustry() {
        return industry;
    }

    public String getSupportEmail() {
        return supportEmail;
    }

    public String getPhone() {
        return phone;
    }

    public String getWebsite() {
        return website;
    }

    public String getTaxId() {
        return taxId;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public Address getAddress() {
        return address;
    }

    public String getDisplayName() {
        return tradeName != null && !tradeName.trim().isEmpty() ? tradeName : legalName;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setLegalName(String legalName) {
        this.legalName = legalName;
    }

    public void setTradeName(String tradeName) {
        this.tradeName = tradeName;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public void setSupportEmail(String supportEmail) {
        this.supportEmail = supportEmail;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public void setTaxId(String taxId) {
        this.taxId = taxId;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String id;
        private String legalName;
        private String tradeName;
        private String industry;
        private String supportEmail;
        private String phone;
        private String website;
        private String taxId;
        private String registrationNumber;
        private Address address;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder legalName(String legalName) {
            this.legalName = legalName;
            return this;
        }

        public Builder tradeName(String tradeName) {
            this.tradeName = tradeName;
            return this;
        }

        public Builder industry(String industry) {
            this.industry = industry;
            return this;
        }

        public Builder supportEmail(String supportEmail) {
            this.supportEmail = supportEmail;
            return this;
        }

        public Builder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public Builder website(String website) {
            this.website = website;
            return this;
        }

        public Builder taxId(String taxId) {
            this.taxId = taxId;
            return this;
        }

        public Builder registrationNumber(String registrationNumber) {
            this.registrationNumber = registrationNumber;
            return this;
        }

        public Builder address(Address address) {
            this.address = address;
            return this;
        }

        public Company build() {
            return new Company(this);
        }
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Company)) {
            return false;
        }
        Company company = (Company) other;
        return Objects.equals(id, company.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Company{" +
                "id='" + id + '\'' +
                ", displayName='" + getDisplayName() + '\'' +
                ", industry='" + industry + '\'' +
                ", website='" + website + '\'' +
                '}';
    }
}
