package com.example.demo.services;

import com.example.demo.Entity.*;
import com.example.demo.Repository.FieldWorkerRepository;
import com.example.demo.Repository.PatientRepository;
import com.example.demo.Repository.UserRepository;
import com.example.demo.models.PatientCreationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

    public String getCategorizedClass(List<Answer> answers) {
        int n1 = 0;
        int n2 = 0;
        int score = 0;
        for(Answer answer : answers)
        {
            if(answer.getQuestion().getType().equals("mcq"))
            {
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
            else if(answer.getQuestion().getType().equals("range"))
            {
                score+= (10- answer.getRangeAns())*10;
                n2++;
            }
        }
        if(score > n1*80 || score > n2*70)
            return "RED";
        else if(score > n1*31 && score > n2*31)
            return "YELLOW";
        else
            return "GREEN";
    }
}
