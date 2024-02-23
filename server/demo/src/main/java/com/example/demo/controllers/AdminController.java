package com.example.demo.controllers;


import com.example.demo.Entity.Admin;
import com.example.demo.services.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @GetMapping("/get-admins")
    public List<Admin> getAdmin()
    {
        System.out.println("getting Admins");
        return adminService.getAdmin();
    }

    @GetMapping("/current-admin")
    public String getLoggedInAdmin(Principal principal)
    {
        return principal.getName();
    }
}
