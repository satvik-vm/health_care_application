package com.example.demo.controllers;


import com.example.demo.Entity.Patient;
import com.example.demo.dto.PatientDTO;
import com.example.demo.models.PrescriptionRequest;
import com.example.demo.services.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.security.GeneralSecurityException;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/doctor")
@CrossOrigin(origins = "*")
public class DoctorController {
    @Autowired
    private DoctorService doctorService;
    @GetMapping("/viewPatients")
    public List<PatientDTO> viewPatients(Principal principal)
    {
        return doctorService.viewPatients(principal.getName());
    }

    @GetMapping("/seeReport")
    public String seeReport(@RequestParam("id") int id, Principal principal) throws IOException, GeneralSecurityException {
        String email = principal.getName();
        return doctorService.seeReport(id, email);
    }

    @PostMapping("/prescription")
    public String givePrescription(@RequestBody PrescriptionRequest request) throws IOException, GeneralSecurityException {
        System.out.println("mukul");
        return doctorService.givePrescription(request.getId(), request.getPrescription());
    }



}
