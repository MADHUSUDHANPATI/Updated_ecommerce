package com.ecommerce.project.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "addresses")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long addressId;

    @NotBlank
    @Size(min = 5, message = "Street name should be at least 5 characters")
    private String street;

    @NotBlank
    @Size(min = 5, message = "building Name name should be at least 5 characters")
    private String buildingName;

    @NotBlank
    @Size(min = 2, message = "city name should be at least 2 characters")
    private String city;

    @NotBlank
    @Size(min = 5, message = "state name should be at least 5 characters")
    private String state;

    @NotBlank
    @Size(min = 4, message = "country name should be at least 4 characters")
    private String country;

    @NotBlank
    @Size(min = 6, message = "pin code name should be at least 6 numbers")  // video it is string
    private String pincode;

    public Address(String street, String buildingName, String city, String pincode, String country, String state) {
        this.street = street;
        this.buildingName = buildingName;
        this.city = city;
        this.pincode = pincode;
        this.country = country;
        this.state = state;
    }

    @ManyToOne    // (mappedBy = "addresses") // this means addresses table is not the owner of the relationship;
    @JoinColumn(name = "user_id")
    private User user;
}
