package com.example.demo.controllers;
import com.example.demo.Entity.Admin;
import com.example.demo.Entity.Role;
import com.example.demo.Entity.Supervisor;
import com.example.demo.Entity.User;
import com.example.demo.models.SupervisorCreationRequest;
import com.example.demo.models.SupervisorRemovalRequest;
import com.example.demo.services.AdminService;
import com.example.demo.services.EmailSenderService;
import com.example.demo.services.RoleService;
import com.example.demo.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

}
