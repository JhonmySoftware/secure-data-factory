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

import java.time.LocalDate;
import java.util.Objects;

/**
 * Represents a person entity with personally identifiable information (PII).
 * All fields follow data minimization principles.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Person {

    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private LocalDate birthDate;
    private String address;
    private String city;
    private String country;
    private String nationalId;

    public Person() {}

    private Person(Builder builder) {
        this.id = builder.id;
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
        this.email = builder.email;
        this.phone = builder.phone;
        this.birthDate = builder.birthDate;
        this.address = builder.address;
        this.city = builder.city;
        this.country = builder.country;
        this.nationalId = builder.nationalId;
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public String getId()          { return id; }
    public String getFirstName()   { return firstName; }
    public String getLastName()    { return lastName; }
    public String getEmail()       { return email; }
    public String getPhone()       { return phone; }
    public LocalDate getBirthDate(){ return birthDate; }
    public String getAddress()     { return address; }
    public String getCity()        { return city; }
    public String getCountry()     { return country; }
    public String getNationalId()  { return nationalId; }

    /** Returns the full name (firstName + lastName). */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    // ── Setters (for deserialization) ────────────────────────────────────────

    public void setId(String id)                  { this.id = id; }
    public void setFirstName(String firstName)     { this.firstName = firstName; }
    public void setLastName(String lastName)       { this.lastName = lastName; }
    public void setEmail(String email)             { this.email = email; }
    public void setPhone(String phone)             { this.phone = phone; }
    public void setBirthDate(LocalDate birthDate)  { this.birthDate = birthDate; }
    public void setAddress(String address)         { this.address = address; }
    public void setCity(String city)               { this.city = city; }
    public void setCountry(String country)         { this.country = country; }
    public void setNationalId(String nationalId)   { this.nationalId = nationalId; }

    // ── Builder ───────────────────────────────────────────────────────────────

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String id;
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
        private LocalDate birthDate;
        private String address;
        private String city;
        private String country;
        private String nationalId;

        public Builder id(String id)                  { this.id = id; return this; }
        public Builder firstName(String firstName)     { this.firstName = firstName; return this; }
        public Builder lastName(String lastName)       { this.lastName = lastName; return this; }
        public Builder email(String email)             { this.email = email; return this; }
        public Builder phone(String phone)             { this.phone = phone; return this; }
        public Builder birthDate(LocalDate birthDate)  { this.birthDate = birthDate; return this; }
        public Builder address(String address)         { this.address = address; return this; }
        public Builder city(String city)               { this.city = city; return this; }
        public Builder country(String country)         { this.country = country; return this; }
        public Builder nationalId(String nationalId)   { this.nationalId = nationalId; return this; }

        public Person build() {
            return new Person(this);
        }
    }

    // ── Object overrides ──────────────────────────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Person)) return false;
        Person person = (Person) o;
        return Objects.equals(id, person.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Person{" +
                "id='" + id + '\'' +
                ", fullName='" + getFullName() + '\'' +
                ", email='" + email + '\'' +
                ", country='" + country + '\'' +
                '}';
    }
}
