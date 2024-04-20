package com.example.demo.controllers;

import com.example.demo.Entity.Doctor;
import com.example.demo.models.DoctorCreationRequest;
import com.example.demo.services.GeneralService;
import com.example.demo.services.HospitalService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/hospital")
@CrossOrigin(origins = "*")
public class HospitalController {

    @Autowired
    HospitalService hospitalService;
    @Autowired
    GeneralService generalService;

    @PostMapping("/regDoctor")
    public String registerDoctor(@RequestBody List<DoctorCreationRequest> request, Principal principal)
    {
        String hospitalEmail = principal.getName();
        System.out.println(hospitalEmail);
        return hospitalService.createDoctors(request, hospitalEmail);
    }

    @DeleteMapping("/remDoctor")
    public boolean removeDoctor(@RequestParam String id)
    {
        return hospitalService.deleteDoctor(id);
    }

    @GetMapping("/details")
    public JsonNode getHospitalDetails(Principal principal)
    {
        String email = principal.getName();
        return hospitalService.getDetails(email);
    }

    @GetMapping("/doctors")
    public JsonNode getDoctors(Principal principal)
    {
        String email = principal.getName();
        return hospitalService.getDoctors(email);
    }
}
