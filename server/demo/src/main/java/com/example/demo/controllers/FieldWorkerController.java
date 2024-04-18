package com.example.demo.controllers;


import com.example.demo.Entity.Answer;
import com.example.demo.Entity.Patient;
import com.example.demo.Entity.Question;
import com.example.demo.Entity.User;
import com.example.demo.Repository.UserRepository;
import com.example.demo.dto.QuestionDTO;
import com.example.demo.models.*;
import com.example.demo.services.*;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public Boolean regPatient(@RequestBody PatientCreationRequest request)
    {
        return true;
    }

    @PostMapping("/qLogic")
    public String getCategorization(@RequestBody QuestionnaireResponseRequest request) throws GeneralSecurityException, IOException {
        return fwService.getCategorizedClass(request);
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
        return questions.stream().map(question -> {
            QuestionDTO dto = new QuestionDTO();
            dto.setQuestion(question.getQuestion());
            dto.setType(question.getType());
            dto.setOption1(question.getOptionA());
            dto.setOption2(question.getOptionB());
            dto.setOption3(question.getOptionC());
            dto.setOption4(question.getOptionD());
            return dto;
        }).collect(Collectors.toList());
    }

    @GetMapping("/submitFile")
    public String submitFile(@RequestParam("qnName") String questionnaireName,
                             @RequestParam("patientId") String patientId,
                             @RequestParam("doctorId") String doctorId) throws IOException, GeneralSecurityException {

        return fwService.submitFile(questionnaireName, patientId, doctorId);
    }

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
        data.put("state", fwService.getFwDistrict(email));
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @GetMapping("/getFwSubDistrict")
    public ResponseEntity<Object> getFwSubDistrict(Principal principal) {
        String email = principal.getName();
        Map<String, String> data = new HashMap<>();
        data.put("state", fwService.getSubDistrict(email));
        return new ResponseEntity<>(data, HttpStatus.OK);
    }
}
