package com.example.demo.services;

import com.example.demo.Entity.*;
import com.example.demo.Repository.*;
import com.example.demo.models.AnswerResponse;
import com.example.demo.models.DriveResponse;
import com.example.demo.models.PatientCreationRequest;
import com.example.demo.models.QuestionnaireResponseRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class FwService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    RoleService roleService;
    @Autowired
    UserService userService;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    EmailSenderService emailSenderService;
    @Autowired
    PatientRepository patientRepository;
    @Autowired
    AnswerRepository answerRepository;

    @Autowired
    GoogleDriveService googleDriveService;

    @Autowired
    QuestionRepository questionRepository;
    @Autowired
    HospitalService hospitalService;
    @Autowired
    DoctorService doctorService;
    @Autowired
    DoctorRepository doctorRepository;
    @Autowired
    AdminService adminService;
    @Autowired
    GeneralService generalService;
    @Autowired
    IdMappingRepository idMappingRepository;
    @Autowired
    FieldWorkerRepository fieldWorkerRepository;

    public Boolean createPatient(PatientCreationRequest request) {
        try {
            String roleName = request.getRole().getName();
            Role role = roleService.getOrCreateRole(roleName);
            User user = new User();
            String firstName = request.getFirstName();
            String lastName = request.getLastName();
            user.setDob(request.getDob());
            user.setLastName(lastName);
            user.setFirstName(firstName);
            user.setAddress(request.getAddress());
            user.setGender(request.getGender());
            user.setRole(role);
            if (!request.getPhone().isEmpty()) {
                user.setPhone(request.getPhone());
            }
            if (request.isAssist()) {
                user.setEmail(request.getAabha());
                user.setPassword(passwordEncoder.encode("1234"));
            } else {
                String email = request.getEmail();
                String password = userService.generatePassword();
                user.setEmail(email);
                String subject = "Added as a member of Medimate India";

                String body = "Dear " + firstName + " " + lastName + ",\n\n" +
                        "You have been officially registered in the medimate India.\n\n" +
                        "Your credentials:\n" +
                        "Email: " + email + "\n" +
                        "Password: " + password + "\n\n" +
                        "Best regards,\n" +
                        "Medimate India";
                emailSenderService.sendEmail(email, subject, body);
                user.setPassword(passwordEncoder.encode(password));
            }
            User createdUser = userRepository.save(user);
            Patient patient = new Patient();

            // Create a new IdMapping object and set its privateId to the Patient's UUID
            IdMapping idMapping = new IdMapping();
            idMapping.setPrivateId(UUID.fromString(patient.getId()));

            // Save the IdMapping object to the database
            idMappingRepository.save(idMapping);// Create a new IdMapping object and set its privateId to the generated UUID

            patient.setUser(user);
            patient.setAabhaId(request.getAabha());
            patient.setFwAssistance(request.isAssist());
            patient.setDistrict(request.getDistrict());
            patient.setSubDivision(request.getSubDivision());
            patientRepository.save(patient);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getCategorizedClass(QuestionnaireResponseRequest request) throws IOException, GeneralSecurityException {
        int n1 = 0;
        int n2 = 0;
        int score = 0;
        int id = request.getPid();
        String pid = idMappingRepository.findById(id).get().getPrivateId().toString();
        Optional<Patient> patient = patientRepository.findById(pid);
        List<AnswerResponse> answers = request.getAnswers();
        for(AnswerResponse answer : answers)
        {
//            System.out.println("mukul");
            Optional<Question> question =  questionRepository.findById(idMappingRepository.findById(answer.getQid()).get().getPrivateId().toString());
            Answer ans = new Answer();

            // Create a new IdMapping object and set its privateId to the Answer's UUID
            IdMapping idMapping = new IdMapping();
            idMapping.setPrivateId(UUID.fromString(ans.getAnsId()));

            // Save the IdMapping object to the database
            idMappingRepository.save(idMapping);// Create a new IdMapping object and set its privateId to the generated UUID

            if(patient.isPresent())
            {
                ans.setPatient(patient.get());
            }
            if(question.isPresent())
            {
                ans.setQuestion(question.get());
                if(question.get().getType().equals("mcq"))
                {
                    ans.setMcqAns(answer.getMcqAns());
                    if(answer.getMcqAns().equals("A"))
                        score+=0;
                    else if(answer.getMcqAns().equals("B"))
                        score+=10;
                    else if(answer.getMcqAns().equals("C"))
                        score+=50;
                    else if(answer.getMcqAns().equals("D"))
                        score+=100;
                    n1++;
                }
                else if(question.get().getType().equals("range"))
                {
                    ans.setRangeAns(answer.getRangeAns());
                    score+= (10- answer.getRangeAns())*10;
                    n2++;
                }
                answerRepository.save(ans);
            }
        }
        System.out.println(score);
        if(score > n1*80 || score > n2*70)
        {
            if(patient.isPresent())
            {
                patient.get().setHealthStatus("RED");
                Hospital hospital = hospitalService.allocateHospital(patient.get().getSubDivision(), patient.get().getDistrict());
                Doctor doctor = doctorService.allocateDoctor(hospital);
                patient.get().setHospital(hospital);
                patient.get().setDoctor(doctor);
                patientRepository.save(patient.get());
            }
            return "RED";
        }
        else if(score > n1*31 || score > n2*31)
        {
            if(patient.isPresent())
            {
                patient.get().setHealthStatus("YELLOW");
                Hospital hospital = hospitalService.allocateHospital(patient.get().getSubDivision(), patient.get().getDistrict());
                Doctor doctor = doctorService.allocateDoctor(hospital);
                patient.get().setHospital(hospital);
                patient.get().setDoctor(doctor);
                patientRepository.save(patient.get());
            }
            return "YELLOW";
        }
        else
        {
            if(patient.isPresent())
            {
                patient.get().setHealthStatus("GREEN");
                patientRepository.save(patient.get());
            }
            return "GREEN";
        }
    }

    public DriveResponse uploadDescriptiveMsg(File tempFile, String qid, String pid) throws GeneralSecurityException, IOException {
        Answer answer = new Answer();

        // Create a new IdMapping object and set its privateId to the Answer's UUID
        IdMapping idMapping = new IdMapping();
        idMapping.setPrivateId(UUID.fromString(answer.getAnsId()));

        // Save the IdMapping object to the database
        idMappingRepository.save(idMapping);// Create a new IdMapping object and set its privateId to the generated UUID

        Optional<Patient> patient = patientRepository.findById(pid);
        Optional<Question> question = questionRepository.findById(qid);
        if(question.isPresent())
        {
            DriveResponse res = googleDriveService.uploadDescriptiveMsgToDrive(tempFile);
            answer.setQuestion(question.get());
            answer.setSubjAns(generalService.encrypt(res.getUrl()));
            if(patient.isPresent())
                answer.setPatient(patient.get());
            answerRepository.save(answer);
            return res;
        }
        return null;
    }


    public String submitFile(String questionnaireName, String patientId, String doctorId) throws IOException, GeneralSecurityException {
        // Fetch the questions related to the questionnaire
        List<Question> questions = adminService.getAllQuestionByQnName(questionnaireName);
        Patient patient = patientRepository.findById(patientId).orElseThrow(()->new RuntimeException("Patient not found"));
        Doctor doctor = doctorRepository.findById(doctorId).orElseThrow(()->new RuntimeException("Doctor not found"));

        // Create a map to hold the question-answer pairs
        Map<String, String> questionAnswers = new HashMap<>();

        // For each question, fetch the corresponding answer given by the patient
        for (Question question : questions) {
            Answer answer = answerRepository.findByQuestionIdAndPatientId(question.getId(), patientId);
            if(answer.getMcqAns() != null)
                questionAnswers.put(question.getQuestion(), answer.getMcqAns());
            else if(answer.getRangeAns() != 0)
            {
                int rangeAns = answer.getRangeAns();
                String rangeAnsAsString = String.valueOf(rangeAns);
                questionAnswers.put(question.getQuestion(), rangeAnsAsString);
            }
            else if(answer.getSubjAns() != null)
                questionAnswers.put(question.getQuestion(), answer.getSubjAns());
        }

        // Continue with the existing logic...
        // Create a new map to hold the final JSON structure
        Map<String, Object> finalJsonMap = new HashMap<>();

        // Add timestamp, type, and questionnaire name to the map
        finalJsonMap.put("timestamp", LocalDateTime.now().toString());
        finalJsonMap.put("type", "Questionnaire");
        finalJsonMap.put(questionnaireName, questionAnswers);

        // Convert the map to a JSON string
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(finalJsonMap);

        // Write this JSON object to a file
        Path tempFile = Files.createTempFile(generalService.encrypt(patient.getUser().getEmail()), ".json");
        Files.write(tempFile, json.getBytes());

        // Upload the file to Google Drive
        GoogleDriveService googleDriveService = new GoogleDriveService();
        DriveResponse driveResponse = googleDriveService.uploadMedicalFileToDrive(tempFile.toFile());

        // Delete the temporary file
        Files.delete(tempFile);

        String url = generalService.encrypt(driveResponse.getUrl());
        MedicalRecord mr = new MedicalRecord();

        // Create a new IdMapping object and set its privateId to the MedicalRecord's UUID
        IdMapping idMapping = new IdMapping();
        idMapping.setPrivateId(UUID.fromString(mr.getUniqueId()));

        // Save the IdMapping object to the database
        idMappingRepository.save(idMapping);// Create a new IdMapping object and set its privateId to the generated UUID

        mr.setPatient(patient);
        mr.setDoctor(doctor);
        mr.setRecord(url);
        return "File uploaded successfully";

    }

    public String getFwState(String email) {
        FieldWorker fw = fieldWorkerRepository.findByUser_Email(email);
        return fw.getState();

    }

    public String getFwDistrict(String email) {
        FieldWorker fw = fieldWorkerRepository.findByUser_Email(email);
        return fw.getDistrict().getName();
    }


    public String getSubDistrict(String email) {
        FieldWorker fw = fieldWorkerRepository.findByUser_Email(email);
        return fw.getArea();
    }

    public int getPatientPublicKey(String aabha) {
        Patient patient = patientRepository.findByAabhaId(aabha);
        return idMappingRepository.findByPrivateId(UUID.fromString(patient.getId())).getPublicId();
    }
}
