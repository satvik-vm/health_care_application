package com.example.demo.controllers;

import com.example.demo.Entity.*;
import com.example.demo.Repository.DistrictRepository;
import com.example.demo.Repository.IdMappingRepository;
import com.example.demo.Repository.SupervisorRepository;
import com.example.demo.Repository.UserRepository;
import com.example.demo.models.*;
import com.example.demo.services.*;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.Principal;
import java.util.*;


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
    private GeneralService generalService;

    @Autowired
    private DistrictRepository districtRepository;
    @Autowired
    private IdMappingRepository idMappingRepository;

    @GetMapping("/hello")
    public String helloWorld()
    {
        return "Hello, Mukul !!";
    }

    @PostMapping("/modifyDetails")
    public boolean modifyDetails(@RequestBody ModifyUserRequest request)
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
            return true;
        }
        catch (Exception e) {
            return false;
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
    public boolean registerFieldWorker(@RequestBody FWCreationRequest request, Principal principal)
    {
        try{
            // Extract user information from the request
            String supEmail = principal.getName();
            Supervisor supervisor = supervisorRepository.findByUser_Email(supEmail);
            String email = request.getUser().getEmail();
            String password = userService.generatePassword();
            String roleName = request.getUser().getRole().getName();
            String area = request.getArea();
            District district = supervisor.getDistrict();
            String state = request.getState();

            Role role = roleService.getOrCreateRole(roleName);
            User user = userService.createUser(email, password, role);

            // Create a new FW object
            FieldWorker fieldWorker = new FieldWorker();
            // Create a new IdMapping object and set its privateId to the Supervisor's UUID
            IdMapping idMapping = new IdMapping();
            idMapping.setPrivateId(UUID.fromString(fieldWorker.getId()));

            // Save the IdMapping object to the database
            idMappingRepository.save(idMapping);
            fieldWorker.setUser(user);
            fieldWorker.setArea(area);
            fieldWorker.setDistrict(district);
            fieldWorker.setState(state);

            // Save the Field Worker object
            FieldWorker createdFieldWorker = supervisorService.createFieldWorker(fieldWorker);
            supervisorService.sendFieldWorkerCredentials(email, password, area);

            return true;
        }

        catch (Exception e) {
            System.out.println("Error Occurring");
            e.printStackTrace();
            return false;
        }
    }

    @DeleteMapping("/remFW")
    public boolean removeFieldWorker(@RequestParam int id)
    {
        boolean response = supervisorService.removeFieldWorker(id);
        if(response)
            return true;
        else
            return false;
    }

    @GetMapping("/viewFw")
    public ResponseEntity<List<Map<String, String>>> getFieldWorkersInArea(@RequestParam String area) {
        return supervisorService.findfwByArea(area);
    }

    @PostMapping("/transFW")
    public ResponseEntity<String> transferFW(@RequestBody FWTransferRequest request)
    {
        String fw_id = request.getFw_id();
        String area = request.getArea();
        if(supervisorService.transferFieldWorker(fw_id, area))
            return ResponseEntity.ok("Field Worker transferred successfully");
        else
            return ResponseEntity.ok("Could not transfer field worker");
    }

    @GetMapping("/supId")
    public String getSupervisorId(Principal principal) {
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

    @GetMapping("/getLoc")
    public JsonNode getLocation() throws IOException {
        return generalService.getLocation();
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

    @GetMapping("/getSupState")
    public ResponseEntity<Object> getSupState(Principal principal) {
        String email = principal.getName();
        Map<String, String> data = new HashMap<>();
        data.put("state", supervisorService.getSupState(email));
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @GetMapping("/getSupDistrict")
    public ResponseEntity<Object> getSupDistrict(Principal principal) {
        String email = principal.getName();
        Map<String, String> data = new HashMap<>();
        data.put("state", supervisorService.getSupDistrict(email));
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

}
