package com.example.demo.controllers;

import com.example.demo.Entity.*;
import com.example.demo.Repository.DistrictRepository;
import com.example.demo.Repository.SupervisorRepository;
import com.example.demo.Repository.UserRepository;
import com.example.demo.models.*;
import com.example.demo.services.EmailSenderService;
import com.example.demo.services.RoleService;
import com.example.demo.services.SupervisorService;
import com.example.demo.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Date;


@RestController
@RequestMapping("/supervisor")
@CrossOrigin(origins = "*")
public class SupervisorController {
    @Autowired
    UserRepository userRepository;

    @Autowired
    SupervisorRepository supervisorRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    EmailSenderService emailSenderService;

    @Autowired
    UserService userService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private SupervisorService supervisorService;

    @Autowired
    private DistrictRepository districtRepository;

    @GetMapping("/hello")
    public String helloWorld()
    {
        return "Hello, Mukul !!";
    }

    @PostMapping("/modifyDetails")
    public ResponseEntity<User> modifyDetails(@RequestBody ModifyUserRequest request)
    {
        try
        {
            User user = userRepository.getUserByUsername(request.getEmail());
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setDob(request.getDob());
            user.setPhone(request.getPhone());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            userRepository.save(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/sendOtp")
    public boolean sendOtp(@RequestBody SendOtpRequest request) {
        String otp = userService.generateOTP();
        String subject = "OTP Verification";
        String body = "Dear Supervisor,\n\n"
                + "Thank you for using our service. To complete your registration/authentication process, please use the following OTP (One-Time Password):\n\n"
                + "OTP: " + otp + "\n\n"
                + "Please enter this OTP on the verification page to confirm your identity.\n\n"
                + "If you did not request this OTP, please ignore this email.\n\n"
                + "Thank you,\n"
                + "Medimate India";
        try {
            userService.createOTP(request.getEmail(), otp);
            emailSenderService.sendEmail(request.getEmail(), subject, body);
            return true; // Email sent successfully
        } catch (Exception e) {
            e.printStackTrace(); // Log the exception for debugging purposes
            return false; // Failed to send email
        }
    }

    @PostMapping("/verifyOtp")
    public boolean verifyOtp(@RequestBody VerifyOtpRequest request)
    {
        String otp_num = request.getOtp_num();
        User user = userRepository.getUserByUsername(request.getEmail());

        // Check if user or OTP is null
        if (user == null || user.getOtp() == null) {
            return false;
        }

        String stored_otp = user.getOtp().getOtp_num();
        Date expDate = user.getOtp().getExpDate();

        // Check if the provided OTP matches the stored OTP
        if (!otp_num.equals(stored_otp)) {
            return false;
        }

        // Check if the current time is before the expiry time
        Date currentTime = new Date();
        if (currentTime.after(expDate)) {
            // OTP has expired
            return false;
        }

        // OTP is valid and not expired
        return true;
    }

    @PostMapping("/regFW")
    public ResponseEntity<FieldWorker> registerFieldWorker(@RequestBody FWCreationRequest request)
    {
        try{
            // Extract user information from the request
            String email = request.getUser().getEmail();
            String password = userService.generatePassword();
            String roleName = request.getUser().getRole().getName();
            String area = request.getArea();
            District district = districtRepository.findByName(request.getDistrict().getName()).get();

            Role role = roleService.getOrCreateRole(roleName);
            User user = userService.createUser(email, password, role);

            // Create a new FW object
            FieldWorker fieldWorker = new FieldWorker();
            fieldWorker.setUser(user);
            fieldWorker.setArea(area);
            fieldWorker.setDistrict(district);

            // Save the Field Worker object
            FieldWorker createdFieldWorker = supervisorService.createFieldWorker(fieldWorker);
            supervisorService.sendFieldWorkerCredentials(email, password, area);

            return ResponseEntity.status(HttpStatus.CREATED).body(createdFieldWorker);
        }

        catch (Exception e) {
            System.out.println("Error Occurring");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/remFW")
    public ResponseEntity<String> removeFieldWorker(@RequestParam int id)
    {
        boolean response = supervisorService.removeFieldWorker(id);
        if(response)
            return ResponseEntity.ok("Field Worker removed successfully");
        else
            return ResponseEntity.ok("Can Not delete Field Worker");
    }

    @GetMapping("/supId")
    public int getSupervisorId(Principal principal) {
        String loggedInUserEmail = principal.getName();
        return supervisorService.getSupervisorIdByEmail(loggedInUserEmail);
    }

    @GetMapping("/dob")
    public Boolean getDob(Principal principal){
        String email = principal.getName();
        if(userService.getDobByEmail(email) == null) {
            return false;
        }
        return true;
    }

    @PostMapping("/transFW")
    public ResponseEntity<String> transferFW(@RequestBody FWTransferRequest request)
    {
        int fw_id = request.getFw_id();
        String area = request.getArea();
        if(supervisorService.transferFieldWorker(fw_id, area))
            return ResponseEntity.ok("Field Worker transferred successfully");
        else
            return ResponseEntity.ok("Could not transfer field worker");
    }
}
