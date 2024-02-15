package com.example.demo.services;

import com.example.demo.Entity.Admin;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdminService {
    private List<Admin> store = new ArrayList<>();

    public AdminService() {
        // Initialize dummy admins
        initializeDummyAdmins();
    }

    public List<Admin> getAdmins()
    {
        return this.store;
    }

    private void initializeDummyAdmins() {
        Admin admin1 = new Admin();
        admin1.setEmail("admin1@example.com");
        admin1.setPassword("password1");
        admin1.setDateOfJoining(LocalDate.parse("2024-01-01"));
        admin1.setDistrict("District1");

        Admin admin2 = new Admin();
        admin2.setEmail("admin2@example.com");
        admin2.setPassword("password2");
        admin2.setDateOfJoining(LocalDate.parse("2024-01-02"));
        admin2.setDistrict("District2");

        Admin admin3 = new Admin();
        admin3.setEmail("admin3@example.com");
        admin3.setPassword("password3");
        admin3.setDateOfJoining(LocalDate.parse("2024-01-03"));
        admin3.setDistrict("District3");

        // Add dummy admins to the store list
        store.add(admin1);
        store.add(admin2);
        store.add(admin3);
    }
    // Other methods of the AdminService class
}