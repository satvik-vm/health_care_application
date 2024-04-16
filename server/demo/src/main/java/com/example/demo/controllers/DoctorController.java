package com.example.demo.controllers;


import com.example.demo.Entity.Patient;
import com.example.demo.services.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/doctor")
@CrossOrigin(origins = "*")
public class DoctorController {
    @Autowired
    private DoctorService doctorService;
    @GetMapping("/viewPatients")
    public List<Patient> viewPatients(@RequestParam("id") int id)
    {
        return doctorService.viewPatients(id);
    }



}
