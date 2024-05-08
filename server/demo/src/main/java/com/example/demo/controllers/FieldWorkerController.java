package com.example.demo.controllers;


import com.example.demo.Entity.Answer;
import com.example.demo.Entity.Patient;
import com.example.demo.Entity.Question;
import com.example.demo.Entity.User;
import com.example.demo.Repository.IdMappingRepository;
import com.example.demo.Repository.UserRepository;
import com.example.demo.dto.*;
import com.example.demo.models.*;
import com.example.demo.services.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/fw")
@CrossOrigin(origins = "*")
public class FieldWorkerController {
    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    UserService userService;

    @Autowired
    EmailSenderService emailSenderService;

    @Autowired
    FwService fwService;
    @Autowired
    AdminService adminService;

    @Autowired
    GoogleDriveService googleDriveService;
    @Autowired
    IdMappingRepository idmappingRepository;

    @GetMapping("/name")
    public String getName(Principal principal)
    {
        return userService.getName(principal.getName());
    }
    @PostMapping("/modifyDetails")
    public Boolean modifyDetails(@RequestBody ModifyUserRequest request)
    {
        try
        {
            User user = userRepository.getUserByUsername(request.getEmail());
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setDob(request.getDob());
            user.setPhone(request.getPhone());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            System.out.println(request.getPassword());
            userRepository.save(user);
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    @PostMapping("/sendOtp")
    public boolean sendOtp(@RequestBody SendOtpRequest request) {
        System.out.println("Hello World");
        String otp = userService.generateOTP();
        System.out.println(otp);
        String subject = "OTP Verification";
        String body = "Dear Field Worker,\n\n"
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
        }
        catch (Exception e) {
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

    @GetMapping("/dob")
    public Boolean getDob(Principal principal){
        String email = principal.getName();
        if(userService.getDobByEmail(email) == null) {
            return false;
        }
        return true;
    }

    @PostMapping("/regPatient")
    public PatientDTO regPatient(@RequestBody PatientCreationRequest request)
    {
        return fwService.createPatient(request);
    }

    @GetMapping("/getPatient")
    public int getPatient(@RequestParam("aabha") String aabha)
    {
        return fwService.getPatientPublicKey(aabha);
    }

    @PostMapping("/qLogic")
    public String getCategorization(@RequestBody QuestionnaireResponseRequest request, Principal principal) throws GeneralSecurityException, IOException {
        return fwService.getCategorizedClass(request, principal.getName());
    }

    @PostMapping("/uploadDescMsg")
    public Object handleFileUpload(@RequestParam("audio") MultipartFile audio,
                                   @RequestParam("qid") String qid,
                                   @RequestParam("pid") String pid) throws IOException, GeneralSecurityException {
        if (audio.isEmpty()) {
            return "File is empty";
        }
        File tempFile = File.createTempFile("temp", null);
        audio.transferTo(tempFile);

        DriveResponse res = fwService.uploadDescriptiveMsg(tempFile, qid, pid);
        System.out.println(res);
        return res;
    }
    @GetMapping("/getAllQ")
    public List<QuestionDTO> getAllQuestions(@RequestParam("name") String name) {
        List<Question> questions = adminService.getAllQuestionByQnName(name);
        return questions.stream().sorted((q1, q2) -> {
            if (q1.getType().equals("mcq") && !q2.getType().equals("mcq")) {
                return -1;
            } else if (!q1.getType().equals("mcq") && q2.getType().equals("mcq")) {
                return 1;
            } else if (q1.getType().equals("range") && q2.getType().equals("descriptive")) {
                return -1;
            } else if (q1.getType().equals("descriptive") && q2.getType().equals("range")) {
                return 1;
            } else {
                return 0;
            }
        }).map(question -> {
            QuestionDTO dto = new QuestionDTO();
            dto.setPublicId(idmappingRepository.findByPrivateId(UUID.fromString(question.getId())).getPublicId());
            dto.setQuestion(question.getQuestion());
            dto.setType(question.getType());
            dto.setOption1(question.getOptionA());
            dto.setOption2(question.getOptionB());
            dto.setOption3(question.getOptionC());
            dto.setOption4(question.getOptionD());
            return dto;
        }).collect(Collectors.toList());
    }

//    @GetMapping("/submitFile")
//    public String submitFile(@RequestParam("qnName") String qnName,
//                             @RequestParam("patientId") int patientId) throws IOException, GeneralSecurityException {
//
//        return fwService.submitFile(qnName, patientId);
//    }

    @GetMapping("/getFwState")
    public ResponseEntity<Object> getSupState(Principal principal) {
        String email = principal.getName();
        Map<String, String> data = new HashMap<>();
        data.put("state", fwService.getFwState(email));
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @GetMapping("/getFwDistrict")
    public ResponseEntity<Object> getFwDistrict(Principal principal) {
        String email = principal.getName();
        Map<String, String> data = new HashMap<>();
        data.put("district", fwService.getFwDistrict(email));
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @GetMapping("/getFwSubDistrict")
    public ResponseEntity<Object> getFwSubDistrict(Principal principal) {
        String email = principal.getName();
        Map<String, String> data = new HashMap<>();
        data.put("subdist", fwService.getSubDistrict(email));
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @GetMapping("/isLoggedIn")
    public boolean isLoggedIn(Principal principal) {
        return (principal != null);
    }

    @PostMapping("/patientLogIn")
    public PatientDTO patientLogIn(@RequestBody String aabhaId, Principal principal) {
        return fwService.patientLogIn(aabhaId, principal.getName());
    }

    @GetMapping("/getProfiles")
    public List<ProfileDTO> getProfiles(Principal principal) {
        return fwService.getProfiles(principal.getName());
    }

    @GetMapping("/getChats")
    public Map<String, List<ChatDTO>> getChats(@RequestParam("id") String id, Principal principal) {
        return fwService.getAllChats(id, principal.getName());
    }

    @GetMapping("/getSupervisor")
    public SupervisorDTO getSupervisor(Principal principal) {
        return fwService.getSupervisor(principal.getName());
    }

    @GetMapping("/viewAllTasks")
    public List<TaskDTO> viewAllTasks(Principal principal) {
        return fwService.viewAllTasks(principal.getName());
    }

    @GetMapping("/viewAllTasksBytDate")
    public List<TaskDTO> viewAllTasksByDate(@RequestParam("date") String date, Principal principal) {
        return fwService.viewAllTasksByDate(principal.getName(), date);
    }

    @GetMapping("/checkFollowUp")
    public Map<String, Object> checkFollowUp(@RequestParam("id") int id, Principal principal) throws IOException {
        return fwService.checkFollowUp(id, principal.getName());
    }

    @PostMapping("/completeTask")
    public boolean completeTask(@RequestBody PatientFollowUpResponse request, Principal principal) throws IOException, GeneralSecurityException {
        return fwService.doTask(request, principal.getName());
    }


}
