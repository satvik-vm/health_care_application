package com.example.demo.services;

import com.example.demo.Entity.*;
import com.example.demo.Repository.*;
import com.example.demo.models.QuestionRequest;
import com.example.demo.models.SupervisorTransferRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AdminService {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private SupervisorRepository supervisorRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailSenderService emailSenderService;

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuestionnaireRepository questionnaireRepository;

    @Autowired
    private DistrictService districtService;

    public List<Admin> getAdmin()
    {
        return adminRepository.findAll();
    }

    public Admin createAdmin(Admin admin)
    {
        return adminRepository.save(admin);
    }

    public Supervisor createSupervisor(Supervisor supervisor)
    {
        return supervisorRepository.save(supervisor);
    }

    public Hospital createHospital(Hospital hospital)
    {
        System.out.println("mukul");
        try{
            return hospitalRepository.save(hospital);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    @Transactional
    public boolean removeSupervisor(int id) {
        System.out.println(id);
        Optional<Supervisor> supervisorOptional = supervisorRepository.findById(id);
        System.out.println(supervisorOptional);
        if (supervisorOptional.isPresent()) {
            Supervisor supervisor = supervisorOptional.get();

            // Delete the supervisor
            supervisorRepository.deleteById(id);
            return true;
        }
        else {
            return false;
        }
    }



    public void sendSupervisorCredentials(String email, String password, String district)
    {
        String subject = "Appointment as Supervisor";

        String body = "Dear Supervisor,\n\n" +
                "Congratulations! You have been appointed as a supervisor for district " + district+".\n\n" +
                "Your credentials:\n" +
                "Email: "+email+"\n" +
                "Password: "+password+"\n\n" +
                "Best regards,\n" +
                "Medimate India";
        emailSenderService.sendEmail(email, subject, body);
    }

    public void sendHospitalCredentials(String email, String password, String district, String subDivision)
    {
        String subject = "Appointment as Hospital";

        String body = "Dear Hospital admin,\n\n" +
                "Congratulations! You have been officially registered in the medimate India for district " + district+ ", for subDivision"+ subDivision + ".\n\n" +
                "Your credentials:\n" +
                "Email: "+email+"\n" +
                "Password: "+password+"\n\n" +
                "Best regards,\n" +
                "Medimate India";
        emailSenderService.sendEmail(email, subject, body);
    }

    public Supervisor getSupervisors(String district)
    {
        return supervisorRepository.findByDistrict(district);
    }

    @Transactional
    public boolean transferSupervisor(int sup_id, String district)
    {

        // Get the current district of the supervisor with the given ID
        Optional<Supervisor> supervisorOptional = supervisorRepository.findById(sup_id);
        Supervisor otherSupervisor = supervisorRepository.findByDistrict(district);

        if (supervisorOptional.isPresent()) {

            Supervisor supervisor = supervisorOptional.get();
            // Save the current district
            District currentDistrict = supervisor.getDistrict();

            // Update the district of the supervisor with the given ID to the new district
            supervisor.setDistrict(districtService.getOrCreateDistrict(district));
            supervisorRepository.save(supervisor);

            // Update the district of the supervisor with the specified district to the current district
            if (otherSupervisor != null) {
                otherSupervisor.setDistrict(currentDistrict);
                supervisorRepository.save(otherSupervisor);
            }
            return true;
        }
        else
            return false;
    }

    public boolean createQuestion(QuestionRequest req) {
        String type = req.getType();
        Question qs = new Question();

        qs.setQuestion(req.getQuestion());
        qs.setType(req.getType());
        Questionnaire qn = questionnaireRepository.findById(req.getQnId()).get();
        qs.setQn(qn);
        if(type.equals("mcq"))
        {
            qs.setOptionA(req.getOptA());
            qs.setOptionB(req.getOptB());
            qs.setOptionC(req.getOptC());
            qs.setOptionD(req.getOptD());
        }
        try {
            questionRepository.save(qs);
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public Optional<Question> getQuestionById(int id) {
        return questionRepository.findById(id);
    }

    public List<Question> getAllQuestionById() {
        return questionRepository.findAll();
    }

    public boolean createQuestionnaire(String name) {
        Questionnaire qn = new Questionnaire();
        qn.setName(name);
        try {
            questionnaireRepository.save(qn);
            return true;
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
}
