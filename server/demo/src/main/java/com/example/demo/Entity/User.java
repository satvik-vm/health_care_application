package com.example.demo.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.sql.Date;

@Getter
//@Setter
@Entity
@Table(name = "User")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private int uniqueId;

    @Column(name="first_name")
    private String firstName;

    @Column(name="last_name")
    private String lastName;

    @Column(name="adhaar")
    private String adhaar;

    @Column(name="dob")
    private Date dob;

    @Column(name="phone_number")
    private String phoneNumber;

    @Column(name="address")
    private String address;

    @Column(name="emergency_contact")
    private String emergencyContact;

    @Column(name="gender")
    private char gender;

    @Column(name="zone")
    private String zone;

    @Column(name="role")
    private String role;

    void delete_user(){
        firstName = "XXXXXXXXXXXX";
        lastName = "XXXXXXXXXX";
        adhaar = "XXXXXXXXXXXX";
        dob = Date.valueOf(LocalDate.now());
        phoneNumber = "XXXXXXXXXX";
        address = "XXXXXX";
        emergencyContact = "XXXXXXXXXXX";
        gender = 'X';
        zone = "XXXXXXXXXXX";
    }

    void change_address(String new_address){
        this.address = new_address;
    }

    void change_phone_number(String new_phone_number){
        this.phoneNumber = new_phone_number;
    }

}
