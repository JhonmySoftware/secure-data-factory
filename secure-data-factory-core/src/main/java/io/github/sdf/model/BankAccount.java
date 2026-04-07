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
package io.github.sdf.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Objects;

/**
 * Represents a synthetic financial account for development and QA.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BankAccount {

    private String id;
    private String bankName;
    private String accountHolderName;
    private String accountType;
    private String currency;
    private String accountNumber;
    private String routingReference;
    private String ibanReference;
    private String status;
    private String countryCode;

    public BankAccount() {
    }

    private BankAccount(Builder builder) {
        this.id = builder.id;
        this.bankName = builder.bankName;
        this.accountHolderName = builder.accountHolderName;
        this.accountType = builder.accountType;
        this.currency = builder.currency;
        this.accountNumber = builder.accountNumber;
        this.routingReference = builder.routingReference;
        this.ibanReference = builder.ibanReference;
        this.status = builder.status;
        this.countryCode = builder.countryCode;
    }

    public String getId() {
        return id;
    }

    public String getBankName() {
        return bankName;
    }

    public String getAccountHolderName() {
        return accountHolderName;
    }

    public String getAccountType() {
        return accountType;
    }

    public String getCurrency() {
        return currency;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getRoutingReference() {
        return routingReference;
    }

    public String getIbanReference() {
        return ibanReference;
    }

    public String getStatus() {
        return status;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public void setAccountHolderName(String accountHolderName) {
        this.accountHolderName = accountHolderName;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public void setRoutingReference(String routingReference) {
        this.routingReference = routingReference;
    }

    public void setIbanReference(String ibanReference) {
        this.ibanReference = ibanReference;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String id;
        private String bankName;
        private String accountHolderName;
        private String accountType;
        private String currency;
        private String accountNumber;
        private String routingReference;
        private String ibanReference;
        private String status;
        private String countryCode;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder bankName(String bankName) {
            this.bankName = bankName;
            return this;
        }

        public Builder accountHolderName(String accountHolderName) {
            this.accountHolderName = accountHolderName;
            return this;
        }

        public Builder accountType(String accountType) {
            this.accountType = accountType;
            return this;
        }

        public Builder currency(String currency) {
            this.currency = currency;
            return this;
        }

        public Builder accountNumber(String accountNumber) {
            this.accountNumber = accountNumber;
            return this;
        }

        public Builder routingReference(String routingReference) {
            this.routingReference = routingReference;
            return this;
        }

        public Builder ibanReference(String ibanReference) {
            this.ibanReference = ibanReference;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder countryCode(String countryCode) {
            this.countryCode = countryCode;
            return this;
        }

        public BankAccount build() {
            return new BankAccount(this);
        }
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof BankAccount)) {
            return false;
        }
        BankAccount bankAccount = (BankAccount) other;
        return Objects.equals(id, bankAccount.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "BankAccount{" +
                "id='" + id + '\'' +
                ", bankName='" + bankName + '\'' +
                ", accountType='" + accountType + '\'' +
                ", currency='" + currency + '\'' +
                ", accountNumber='" + accountNumber + '\'' +
                '}';
    }
}
