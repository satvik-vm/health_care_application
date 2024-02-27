package com.example.demo.controllers;
import com.example.demo.Entity.Admin;
import com.example.demo.Entity.Role;
import com.example.demo.Entity.Supervisor;
import com.example.demo.Entity.User;
import com.example.demo.models.SupervisorCreationRequest;
import com.example.demo.services.AdminService;
import com.example.demo.services.RoleService;
import com.example.demo.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @GetMapping("/get-admins")
    public List<Admin> getAdmin()
    {
        System.out.println("getting Admins");
        return adminService.getAdmin();
    }

    @GetMapping("/current-admin")
    public String getLoggedInAdmin(Principal principal)
    {
        return principal.getName();
    }

    @PostMapping("/regSup")
    public ResponseEntity<String> registerSupervisor(@RequestBody SupervisorCreationRequest request)
    {
//        try {
            System.out.println("mukul");
            // Extract user information from the request
            String email = request.getUser().getEmail();
            String password = request.getUser().getPassword();
            String roleName = request.getUser().getRole().getName();
            String district = request.getDistrict();

            Role role = roleService.getOrCreateRole(roleName);
            User user = userService.createUser(email, password, role);

            // Create a new Supervisor object
            Supervisor supervisor = new Supervisor();
            supervisor.setUser(user);
            supervisor.setDistrict(district);

            // Save the Admin object
            adminService.createSupervisor(supervisor);

            return ResponseEntity.ok("Supervisor created successfully");
//        }
//        catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create Supervisor");
//        }
    }
}
