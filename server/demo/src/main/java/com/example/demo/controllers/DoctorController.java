package com.example.demo.controllers;


import com.example.demo.Entity.Patient;
import com.example.demo.dto.PatientDTO;
import com.example.demo.models.FollowUpRequest;
import com.example.demo.models.PrescriptionRequest;
import com.example.demo.models.QuestionRequest;
import com.example.demo.services.AdminService;
import com.example.demo.services.DoctorService;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.security.GeneralSecurityException;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/doctor")
@CrossOrigin(origins = "*")
public class DoctorController {
    @Autowired
    private DoctorService doctorService;
    @Autowired
    private AdminService adminService;
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

    @PostMapping("/followup")
    public String giveFollowup(@RequestBody FollowUpRequest request, Principal principal) throws IOException, GeneralSecurityException {
        return doctorService.giveFollowUp(request, principal.getName());
    }

    @GetMapping("/getDocName")
    public String getDocNameByEmail(Principal principal){
        return doctorService.getDocNameByEmail(principal.getName());
    }

    @GetMapping("/viewActivePatient")
    public List<PatientDTO> viewActivePatients(Principal principal){
        List<String> statusList = Arrays.asList("YELLOW", "RED");

        return doctorService.viewActivePatients(principal.getName(), statusList);
    }

    @PutMapping("/patientDone")
    public Boolean updatePatientStatus(@RequestParam("id") int publicId){
        return doctorService.updatePatientStatus(publicId);
    }

    @GetMapping("/isLoggedIn")
    public boolean isLoggedIn(Principal principal) {
        return (principal != null);
    }

//    @PostMapping("/setQn")
//    public boolean setQuestionnaire(@RequestParam int publicId)
//    {
//        String name = "patient_" + publicId;
//        return adminService.createQuestionnaire(name);
//    }

//    @GetMapping("/getQn")
//    public int getQuestionnaire(@RequestParam int publicId)
//    {
//
//        return adminService.getQuestionnaireByName(name);
//    }
//
//    // API to set follow up question
//    @PostMapping("/setFollowUpQ")
//    public boolean setFollowUpQuestion(@RequestBody QuestionRequest request)
//    {
//        int questionnaireId = adminService.getQuestionnaireByName(name);
//        request.setQnId();
//        return adminService.createQuestion(request);
//    }


//    @GetMapping("/viewHealthyPatient")
//    public List<Patient> viewHealthyPatients(Principal principal){
//        List<String> statusList = List.of("GREEN");
//        return doctorService.viewActivePatients(principal.getName(), statusList);
//    }
}
