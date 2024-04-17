package com.example.demo.controllers;
import com.example.demo.Entity.*;
import com.example.demo.Repository.IdMappingRepository;
import com.example.demo.models.*;
import com.example.demo.services.*;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

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
    public boolean registerSupervisor(@RequestBody SupervisorCreationRequest request)
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
            supervisor.setUser(user);

            // Save the Supervisor object
            Supervisor createdSupervisor = adminService.createSupervisor(supervisor);
            adminService.sendSupervisorCredentials(email, password, district.getName());
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    @DeleteMapping("/remSup")
    public boolean removeSupervisor(@RequestParam("sup_id") int sup_id)
    {

        return adminService.removeSupervisor(sup_id);

    }

    @GetMapping("/getSup")
    public int getSupervisor(@RequestParam String district)
    {
        return adminService.getSupervisor(district);
    }

//  Transfer Supervisor API
    @PostMapping("/transSup")
    public boolean transferSupervisor(@RequestBody SupervisorTransferRequest request)
    {
        int id = request.getSup_id();
        String district = request.getDistrict();
        return adminService.transferSupervisor(id, district);
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

            // Create a new IdMapping object and set its privateId to the Hospital's UUID
            IdMapping idMapping = new IdMapping();
            idMapping.setPrivateId(UUID.fromString(hospital.getId()));

            // Save the IdMapping object to the database
            idMappingRepository.save(idMapping);// Create a new IdMapping object and set its privateId to the generated UUID

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

    @PostMapping("/setQn")
    public boolean setQuestionnaire(@RequestParam String name)
    {
        return adminService.createQuestionnaire(name);
    }

    @PostMapping("/setQ")
    public boolean setQuestion(@RequestBody QuestionRequest request)
    {
        System.out.println("mukul");
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
    public List<String> getAllQuestions(@RequestParam String name) {
        List<Question> questions = adminService.getAllQuestionByQnName(name);
        return questions.stream().map(Question::getQuestion).collect(Collectors.toList());
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
