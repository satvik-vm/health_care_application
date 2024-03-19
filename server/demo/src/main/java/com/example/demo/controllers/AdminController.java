package com.example.demo.controllers;
import com.example.demo.Entity.*;
import com.example.demo.models.QuestionRequest;
import com.example.demo.models.SupervisorCreationRequest;
import com.example.demo.models.HospitalCreationRequest;
import com.example.demo.models.SupervisorRemovalRequest;
import com.example.demo.services.AdminService;
import com.example.demo.services.EmailSenderService;
import com.example.demo.services.RoleService;
import com.example.demo.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    private EmailSenderService emailSenderService;

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
            String district = request.getDistrict();
            System.out.println(district);

            Role role = roleService.getOrCreateRole(roleName);
            User user = userService.createUser(email, password, role);

            // Create a new Supervisor object
            Supervisor supervisor = new Supervisor();
            supervisor.setUser(user);
            supervisor.setDistrict(district);

            // Save the Supervisor object
            Supervisor createdSupervisor = adminService.createSupervisor(supervisor);
            adminService.sendSupervisorCredentials(email, password, district);

            return ResponseEntity.status(HttpStatus.CREATED).body(createdSupervisor);
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    @PostMapping("/regHospital")
    public ResponseEntity<Hospital> registerHospital(@RequestBody HospitalCreationRequest request)
    {
        try {
            String email = request.getUser().getEmail();
            String password = userService.generatePassword();
            String roleName = request.getUser().getRole().getName();
            String district = request.getDistrict();
            String subDivision = request.getSubDivision();

            Role role = roleService.getOrCreateRole(roleName);
            User user = userService.createUser(email, password, role);

            // Create a new Hospital object
            Hospital hospital = new Hospital();
            hospital.setUser(user);
            hospital.setDistrict(district);
            hospital.setSubDivision(subDivision);
            System.out.println(hospital.getSubDivision());
            // Save the hospital object
            Hospital createdHospital = adminService.createHospital(hospital);
            System.out.println("Hewooo");
            adminService.sendHospitalCredentials(email, password, district, subDivision);

            return ResponseEntity.status(HttpStatus.CREATED).body(createdHospital);
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
    public ResponseEntity<List<Supervisor>> getSupervisors(@RequestParam String district)
    {
        List<Supervisor> supervisors = adminService.getSupervisors(district);
        return ResponseEntity.ok().body(supervisors);
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

    @PostMapping("/setQ")
    public boolean setQuestionnaire(@RequestBody QuestionRequest request)
    {
        return adminService.createQuestion(request);
    }
    @PostMapping("/setQn")
    public boolean setQuestionnaire(@RequestParam String name)
    {
        return adminService.createQuestionnaire(name);
    }

    @GetMapping("/getQ")
    public Optional<Question> getQuestionnaire(@RequestParam int id)
    {
        return adminService.getQuestionById(id);
    }

    @GetMapping("/getAllQ")
    public List<Question> getAllQuestionnaire()
    {
        return adminService.getAllQuestionById();
    }
}
