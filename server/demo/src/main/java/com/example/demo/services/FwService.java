package com.example.demo.services;

import com.example.demo.Entity.*;
import com.example.demo.Repository.*;
import com.example.demo.models.AnswerResponse;
import com.example.demo.models.DriveResponse;
import com.example.demo.models.PatientCreationRequest;
import com.example.demo.models.QuestionnaireResponseRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Optional;

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

    public Patient createPatient(PatientCreationRequest request) {
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
        if(!request.getPhone().isEmpty())
        {
            user.setPhone(request.getPhone());
        }
        if(request.isAssist())
        {
            user.setEmail(request.getAabha());
            user.setPassword(passwordEncoder.encode("1234"));
        }
        else {
            String email = request.getEmail();
            String password = userService.generatePassword();
            user.setEmail(email);
            String subject = "Added as a member of Medimate India";

            String body = "Dear "+ firstName + " " + lastName+",\n\n" +
                    "You have been officially registered in the medimate India.\n\n" +
                    "Your credentials:\n" +
                    "Email: "+email+"\n" +
                    "Password: "+password+"\n\n" +
                    "Best regards,\n" +
                    "Medimate India";
            emailSenderService.sendEmail(email, subject, body);
            user.setPassword(passwordEncoder.encode(password));
        }
        User createdUser = userRepository.save(user);
        Patient patient = new Patient();
        patient.setUser(user);
        patient.setAabhaId(request.getAabha());
        patient.setFwAssistance(request.isAssist());
        return patientRepository.save(patient);
    }

    public String getCategorizedClass(QuestionnaireResponseRequest request) throws IOException, GeneralSecurityException {
        int n1 = 0;
        int n2 = 0;
        int score = 0;
        int pid = request.getPid();
        Optional<Patient> patient = patientRepository.findById(pid);
        List<AnswerResponse> answers = request.getAnswers();
        for(AnswerResponse answer : answers)
        {
            Optional<Question> question =  questionRepository.findById(answer.getQid());
            Answer ans = new Answer();
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
        if(score > n1*80 || score > n2*70)
        {
            if(patient.isPresent())
            {
                patient.get().setHealthStatus("RED");
                patientRepository.save(patient.get());
            }
            return "RED";
        }
        else if(score > n1*31 && score > n2*31)
        {
            if(patient.isPresent())
            {
                patient.get().setHealthStatus("YELLOW");
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

    public DriveResponse uploadDescriptiveMsg(File tempFile, int qid, int pid) throws GeneralSecurityException, IOException {
        Answer answer = new Answer();
        Optional<Patient> patient = patientRepository.findById(pid);
        Optional<Question> question = questionRepository.findById(qid);
        if(question.isPresent())
        {
            DriveResponse res = googleDriveService.uploadDescriptiveMsgToDrive(tempFile);
            answer.setQuestion(question.get());
            answer.setSubjAns(res.getUrl());
            if(patient.isPresent())
                answer.setPatient(patient.get());
            answerRepository.save(answer);
            return res;
        }
        return null;
    }
}
