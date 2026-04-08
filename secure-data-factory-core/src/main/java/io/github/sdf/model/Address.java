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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a synthetic address intended for development and test scenarios.
 *
 * @author Jhon Quiñones Arboleda
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Address {

    private String id;
    private String line1;
    private String line2;
    private String city;
    private String region;
    private String postalCode;
    private String countryCode;
    private String country;

    public Address() {
    }

    private Address(Builder builder) {
        this.id = builder.id;
        this.line1 = builder.line1;
        this.line2 = builder.line2;
        this.city = builder.city;
        this.region = builder.region;
        this.postalCode = builder.postalCode;
        this.countryCode = builder.countryCode;
        this.country = builder.country;
    }

    public String getId() {
        return id;
    }

    public String getLine1() {
        return line1;
    }

    public String getLine2() {
        return line2;
    }

    public String getCity() {
        return city;
    }

    public String getRegion() {
        return region;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public String getCountry() {
        return country;
    }

    public String getFullAddress() {
        List<String> parts = new ArrayList<String>();
        addPart(parts, line1);
        addPart(parts, line2);
        addPart(parts, city);
        addPart(parts, region);
        addPart(parts, postalCode);
        addPart(parts, country);
        return String.join(", ", parts);
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setLine1(String line1) {
        this.line1 = line1;
    }

    public void setLine2(String line2) {
        this.line2 = line2;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String id;
        private String line1;
        private String line2;
        private String city;
        private String region;
        private String postalCode;
        private String countryCode;
        private String country;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder line1(String line1) {
            this.line1 = line1;
            return this;
        }

        public Builder line2(String line2) {
            this.line2 = line2;
            return this;
        }

        public Builder city(String city) {
            this.city = city;
            return this;
        }

        public Builder region(String region) {
            this.region = region;
            return this;
        }

        public Builder postalCode(String postalCode) {
            this.postalCode = postalCode;
            return this;
        }

        public Builder countryCode(String countryCode) {
            this.countryCode = countryCode;
            return this;
        }

        public Builder country(String country) {
            this.country = country;
            return this;
        }

        public Address build() {
            return new Address(this);
        }
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Address)) {
            return false;
        }
        Address address = (Address) other;
        return Objects.equals(id, address.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Address{" +
                "id='" + id + '\'' +
                ", line1='" + line1 + '\'' +
                ", city='" + city + '\'' +
                ", region='" + region + '\'' +
                ", countryCode='" + countryCode + '\'' +
                '}';
    }

    private static void addPart(List<String> parts, String value) {
        if (value != null && !value.trim().isEmpty()) {
            parts.add(value);
        }
    }
}
