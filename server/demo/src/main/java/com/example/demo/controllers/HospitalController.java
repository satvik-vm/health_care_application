package com.example.demo.controllers;

import com.example.demo.Entity.Doctor;
import com.example.demo.models.DoctorCreationRequest;
import com.example.demo.services.HospitalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/hospital")
@CrossOrigin(origins = "*")
public class HospitalController {

    @Autowired
    HospitalService hospitalService;

    @PostMapping("/regDoctor")
    public Doctor registerDoctor(@RequestBody DoctorCreationRequest request, Principal principal)
    {
        String hospitalEmail = principal.getName();
        System.out.println(hospitalEmail);
        return hospitalService.createDoctor(request, hospitalEmail);
    }

    @DeleteMapping("remDoctor")
    public boolean removeDoctor(@RequestParam int id)
    {
        return hospitalService.deleteDoctor(id);
    }

}
