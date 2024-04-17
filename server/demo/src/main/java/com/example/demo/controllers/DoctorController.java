package com.example.demo.controllers;


import com.example.demo.Entity.Patient;
import com.example.demo.services.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.security.GeneralSecurityException;
import java.util.List;

@RestController
@RequestMapping("/doctor")
@CrossOrigin(origins = "*")
public class DoctorController {
    @Autowired
    private DoctorService doctorService;
    @GetMapping("/viewPatients")
    public List<Patient> viewPatients(@RequestParam("id") String id)
    {
        return doctorService.viewPatients(id);
    }

    @PostMapping("/prescription")
    public String givePrescription(@RequestParam("patientId") String id,
                                   @RequestParam("prescription") String prescription) throws IOException, GeneralSecurityException {
        return doctorService.givePrescription(id, prescription);
    }


}
