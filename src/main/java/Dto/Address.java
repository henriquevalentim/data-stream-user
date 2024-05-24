package Dto;

import java.util.Objects;

public class Address {
    public String userId;
    public String address;
    public String city;
    public String state;
    public String zipCode;
    public String country;

    public Address() {}

    public Address(String userId, String address, String city, String state, String zipCode, String country) {
        this.userId = userId;
        this.address = address;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
        this.country = country;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Address{");
        sb.append("userId='").append(userId).append('\'');
        sb.append(", address='").append(address).append('\'');
        sb.append(", city='").append(city).append('\'');
        sb.append(", state='").append(state).append('\'');
        sb.append(", zipCode='").append(zipCode).append('\'');
        sb.append(", country='").append(country).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, address, city, state, zipCode, country);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Address address1 = (Address) obj;
        return Objects.equals(userId, address1.userId) &&
                Objects.equals(address, address1.address) &&
                Objects.equals(city, address1.city) &&
                Objects.equals(state, address1.state) &&
                Objects.equals(zipCode, address1.zipCode) &&
                Objects.equals(country, address1.country);
    }
}