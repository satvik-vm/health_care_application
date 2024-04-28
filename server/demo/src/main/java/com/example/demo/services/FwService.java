package com.example.demo.services;

import com.example.demo.Entity.*;
import com.example.demo.Repository.*;
import com.example.demo.dto.PatientDTO;
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
    @Autowired
    MedicalRecordRepository medicalRecordRepository;

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

    public String getCategorizedClass(QuestionnaireResponseRequest request, String email) throws IOException, GeneralSecurityException {
        int n1 = 0;
        int n2 = 0;
        int score = 0;
        int id = request.getPid();
        String pid = idMappingRepository.findById(id).get().getPrivateId().toString();
        Optional<Patient> patient = patientRepository.findById(pid);
        List<AnswerResponse> answers = request.getAnswers();
        for(AnswerResponse answer : answers)
        {
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
                FieldWorker fieldWorker = assignFieldWorker(email);
                patient.get().setHospital(hospital);
                patient.get().setDoctor(doctor);
                patient.get().setFieldWorker(fieldWorker);
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
                FieldWorker fieldWorker = assignFieldWorker(email);
                patient.get().setHospital(hospital);
                patient.get().setDoctor(doctor);
                patient.get().setFieldWorker(fieldWorker);
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


    public String submitFile(String questionnaireName, int id) throws IOException, GeneralSecurityException {
        String patientId = idMappingRepository.findById(id).get().getPrivateId().toString();
        List<Question> questions = adminService.getAllQuestionByQnName(questionnaireName);
        Patient patient = patientRepository.findById(patientId).orElseThrow(()->new RuntimeException("Patient not found"));
        String doctorId = patient.getDoctor().getId();
        Doctor doctor = doctorRepository.findById(doctorId).orElseThrow(()->new RuntimeException("Doctor not found"));

        List<Map<String, Object>> questionAnswersList = new ArrayList<>();

        for (Question question : questions) {
            Answer answer = answerRepository.findByQuestionIdAndPatientId(question.getId(), patientId);
            Map<String, Object> questionAnswers = new HashMap<>();
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
            questionAnswersList.add(questionAnswers);
        }

        Map<String, Object> finalJsonMap = new HashMap<>();
        finalJsonMap.put("timestamp", LocalDateTime.now().toString());
        finalJsonMap.put("type", "Questionnaire");
        finalJsonMap.put(questionnaireName, questionAnswersList);

        List<Map<String, Object>> finalJsonList = new ArrayList<>();
        finalJsonList.add(finalJsonMap);

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(finalJsonList);

        Path tempFile = Files.createTempFile(generalService.encrypt(patient.getUser().getEmail()), ".json");
        Files.write(tempFile, json.getBytes());

        DriveResponse driveResponse = googleDriveService.uploadMedicalFileToDrive(tempFile.toFile());

        if (Files.exists(tempFile)) {
            Files.delete(tempFile);
            System.out.println("File deleted successfully: " + tempFile.toAbsolutePath());
        } else {
            System.out.println("File does not exist: " + tempFile.toAbsolutePath());
        }

        System.out.println(driveResponse.getUrl());

        String url = generalService.encrypt(driveResponse.getUrl());
        MedicalRecord mr = new MedicalRecord();
        IdMapping idMapping = new IdMapping();
        idMapping.setPrivateId(UUID.fromString(mr.getUniqueId()));
        idMappingRepository.save(idMapping);
        mr.setPatient(patient);
        mr.setDoctor(doctor);
        mr.setRecord(url);
        medicalRecordRepository.save(mr);

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

    public FieldWorker assignFieldWorker(String email) {
        FieldWorker fw = fieldWorkerRepository.findByUser_Email(email);
        FwTeam team = fw.getTeam();
        if(team != null)
        {
            List<FieldWorker> fieldWorkers = fieldWorkerRepository.findByTeam_Id(team.getId());
            int mini = Integer.MAX_VALUE;
            FieldWorker assignedfFieldWorker = fw;
            for(FieldWorker worker : fieldWorkers)
            {
                int n = getNumberOfPatientsAssignedToFieldWorker(worker.getUser().getEmail());
                if(n < mini)
                {
                    mini = n;
                    assignedfFieldWorker = worker;
                }
            }
            return assignedfFieldWorker;
        }
        else
        {
            return fw;
        }
    }
    public int getNumberOfPatientsAssignedToFieldWorker(String email) {
        FieldWorker fieldWorker = fieldWorkerRepository.findByUser_Email(email);
        List<Patient> patients = patientRepository.findByFieldWorker(fieldWorker);
        return patients.size();
    }

    public PatientDTO patientLogIn(String aabhaId, String name) {
        Patient patient = patientRepository.findByAabhaId(aabhaId);
        if(patient.getFieldWorker().getUser().getEmail().equals(name))
        {
            PatientDTO patientDTO = new PatientDTO();
            int publicId = idMappingRepository.findByPrivateId(UUID.fromString(patient.getId())).getPublicId();
            patientDTO.setPublicId(publicId);
            patientDTO.setAabhaId(patient.getAabhaId());
            patientDTO.setFirstName(patient.getUser().getFirstName());
            patientDTO.setLastName(patient.getUser().getLastName());
            patientDTO.setStatus(patient.getHealthStatus());
            return patientDTO;
        }
        return null;
    }
}
