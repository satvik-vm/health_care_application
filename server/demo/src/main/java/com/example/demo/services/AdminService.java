package com.example.demo.services;

import com.example.demo.Entity.Admin;
import com.example.demo.Entity.Hospital;
import com.example.demo.Entity.Supervisor;
import com.example.demo.Entity.User;
import com.example.demo.Repository.AdminRepository;
import com.example.demo.Repository.HospitalRepository;
import com.example.demo.Repository.SupervisorRepository;
import com.example.demo.Repository.UserRepository;
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

    public List<Supervisor> getSupervisors(String district)
    {
        return supervisorRepository.findByDistrict(district);
    }
}
