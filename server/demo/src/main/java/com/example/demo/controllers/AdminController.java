package com.example.demo.controllers;
import com.example.demo.Entity.*;
import com.example.demo.Repository.IdMappingRepository;
import com.example.demo.models.*;
import com.example.demo.services.*;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.util.*;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private DistrictService districtService;

    @Autowired
    private EmailSenderService emailSenderService;

    @Autowired
    private GeneralService generalService;

    @Autowired
    private IdMappingRepository idMappingRepository;

    @GetMapping("/get-admins")
    public List<Admin> getAdmin()
    {
        System.out.println("getting Admins");
        return adminService.getAdmin();
    }

    @GetMapping("/current-admin")
    public ResponseEntity<Object> getLoggedInAdmin(Principal principal)
    {
        Map<String, String>data = new HashMap<>();
        data.put("email", principal.getName());
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @PostMapping("/regSup")
    public ResponseEntity<Supervisor> registerSupervisor(@RequestBody SupervisorCreationRequest request)
    {
        try {
            System.out.println("mukul......");
            // Extract user information from the request
            String email = request.getUser().getEmail();
            System.out.println(email);
            String password = userService.generatePassword();
            System.out.println(password);
            String roleName = request.getUser().getRole().getName();
            System.out.println(roleName);
            Role role = roleService.getOrCreateRole(roleName);
            User user = userService.createUser(email, password, role);

            // Create a new Supervisor object
            Supervisor supervisor = new Supervisor();
            // Create a new IdMapping object and set its privateId to the Supervisor's UUID
            IdMapping idMapping = new IdMapping();
            idMapping.setPrivateId(UUID.fromString(supervisor.getId()));

            // Save the IdMapping object to the database
            idMappingRepository.save(idMapping);// Create a new IdMapping object and set its privateId to the generated UUID

            District district = districtService.getOrCreateDistrict(request.getDistrict().getName());
            supervisor.setDistrict(district);
            supervisor.setState(request.getState());

            // Save the Supervisor object
            Supervisor createdSupervisor = adminService.createSupervisor(supervisor);
            adminService.sendSupervisorCredentials(email, password, district.getName());

            return ResponseEntity.status(HttpStatus.CREATED).body(createdSupervisor);
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    @PostMapping("/remSup")
    public ResponseEntity<String> removeSupervisor(@RequestBody SupervisorRemovalRequest request)
    {
        int sup_id = request.getSup_id();
        boolean response = adminService.removeSupervisor(sup_id);
        if(response)
        {
            return ResponseEntity.ok("Supervisor removed successfully");
        }
        else {
            return ResponseEntity.ok("Can Not delete supervisor");
        }
    }

    @GetMapping("/getSup")
    public ResponseEntity<Supervisor> getSupervisors(@RequestParam String district)
    {
        Supervisor supervisors = adminService.getSupervisors(district);
        return ResponseEntity.ok().body(supervisors);
    }

//  Transfer Supervisor API
    @PostMapping("/transSup")
    public ResponseEntity<String> transferSupervisor(@RequestBody SupervisorTransferRequest request)
    {
        int sup_id = request.getSup_id();
        String district = request.getDistrict();
        boolean response = adminService.transferSupervisor(sup_id, district);
        if(response)
            return ResponseEntity.ok("Supervisor transferred successfully");
        else
            return ResponseEntity.ok("Could not transfer supervisor");
    }

    @PostMapping("/regHospital")
    public ResponseEntity<Hospital> registerHospital(@RequestBody HospitalCreationRequest request)
    {
        try {
            String email = request.getUser().getEmail();
//            Removing this for testing purposes
//            String password = userService.generatePassword();

            String password = "1234";
            String roleName = request.getUser().getRole().getName();
            String district = request.getDistrict();
            String subDivision = request.getSubDivision();
            String state= request.getState();

            Role role = roleService.getOrCreateRole(roleName);
            User user = userService.createUser(email, password, role);

            // Create a new Hospital object
            Hospital hospital = new Hospital();
            hospital.setUser(user);
            hospital.setDistrict(district);
            hospital.setSubDivision(subDivision);
            hospital.setName(request.getName());
            hospital.setState(request.getState());
            System.out.println(hospital.getSubDivision());
            // Save the hospital object
            Hospital createdHospital = adminService.createHospital(hospital);
            System.out.println("Hewooo");
            System.out.println(password);
//            For testing purposes I am removing the email system
            adminService.sendHospitalCredentials(email, password, district, subDivision, state);

            return ResponseEntity.status(HttpStatus.CREATED).body(createdHospital);
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file) {
        // Implement file upload logic here
        if (!file.isEmpty()) {
            try {
                String fileName = file.getOriginalFilename();
                String filePath = "/path/to/save/" + fileName; // Change this to your desired file path
                File dest = new File(filePath);
                file.transferTo(dest);
                return "File uploaded successfully!";
            } catch (IOException e) {
                e.printStackTrace();
                return "Failed to upload file!";
            }
        } else {
            return "File is empty!";
        }
    }

    @PostMapping("/setQn")
    public Questionnaire setQuestionnaire(@RequestBody String name)
    {
        return adminService.createQuestionnaire(name);
    }

    @PostMapping("/setQ")
    public boolean setQuestion(@RequestBody QuestionRequest request)
    {
        return adminService.createQuestion(request);
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/getQn")
    public int getQuestionnaire(@RequestParam("name") String name)
    {
        System.out.println(name);
        return adminService.getQuestionnaireByName(name);
    }

    @GetMapping("/getAllQ")
    public List<Question> getAllQuestions(@RequestParam String name)
    {
        return adminService.getAllQuestionByQnName(name);
    }

    @GetMapping("/getState")
    public List<String> getStates() throws IOException {
        return generalService.getStates();
    }

    @GetMapping("/getDistrict")
    public List<String> getDistrictByState(@RequestParam("state") String state) throws IOException {
        return generalService.getDistrictsByState(state);
    }

    @GetMapping("/getSubDistrict")
    public List<String> getSubDistricts(@RequestParam("state") String state,
                                        @RequestParam("district") String district) throws IOException {
        return generalService.getSubdistrictsByStateAndDistrict(state, district);
    }

    @GetMapping("/getLoc")
    public JsonNode getLocation() throws IOException {
        return generalService.getLocation();
    }

    @GetMapping("/getHospitals")
    public List<JsonNode> getHospitals(@RequestParam("state") String state,
                                       @RequestParam("district") String district,
                                       @RequestParam("subDistrict") String subDistrict) throws IOException {
        return generalService.getHospitals(state, district, subDistrict);
    }

}
