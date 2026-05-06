package com.restassured.models.response.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthenticatedUser {
    private int id;
    private String firstName;
    private String lastName;
    private String gender;
    private String email;
    private String username;
    private String image;
    private String role;
    private Hair hair;
    private Address address;
    private Company company;
    private Bank bank;
    private Crypto crypto;

    public int getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getGender() {
        return gender;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getImage() {
        return image;
    }

    public String getRole() {
        return role;
    }

    public Hair getHair() {
        return hair;
    }

    public Address getAddress() {
        return address;
    }

    public Company getCompany() {
        return company;
    }

    public Bank getBank() {
        return bank;
    }

    public Crypto getCrypto() {
        return crypto;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Hair {
        private String color;
        private String type;

        public String getColor() {
            return color;
        }

        public String getType() {
            return type;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Address {
        private String address;
        private String city;
        private String state;
        private String stateCode;
        private String postalCode;
        private String country;

        public String getAddress() {
            return address;
        }

        public String getCity() {
            return city;
        }

        public String getState() {
            return state;
        }

        public String getStateCode() {
            return stateCode;
        }

        public String getPostalCode() {
            return postalCode;
        }

        public String getCountry() {
            return country;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Company {
        private String department;
        private String name;
        private String title;
        private Address address;

        public String getDepartment() {
            return department;
        }

        public String getName() {
            return name;
        }

        public String getTitle() {
            return title;
        }

        public Address getAddress() {
            return address;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Bank {
        private String cardType;
        private String currency;
        private String iban;

        public String getCardType() {
            return cardType;
        }

        public String getCurrency() {
            return currency;
        }

        public String getIban() {
            return iban;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Crypto {
        private String coin;
        private String wallet;
        private String network;

        public String getCoin() {
            return coin;
        }

        public String getWallet() {
            return wallet;
        }

        public String getNetwork() {
            return network;
        }
    }
}
