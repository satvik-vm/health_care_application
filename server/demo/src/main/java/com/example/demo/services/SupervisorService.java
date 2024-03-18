package com.example.demo.services;

import com.example.demo.Entity.FieldWorker;
import com.example.demo.Entity.Supervisor;
import com.example.demo.Repository.FieldWorkerRepository;
import com.example.demo.Repository.SupervisorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SupervisorService {
    @Autowired
    private SupervisorRepository supervisorRepository;

    @Autowired
    private FieldWorkerRepository fieldWorkerRepository;

    @Autowired
    private EmailSenderService emailSenderService;
    public Supervisor getDetails(int id){
        Optional<Supervisor> supervisorOptional = supervisorRepository.findById(id);
        return supervisorOptional.orElse(null);
    }

    public FieldWorker createFieldWorker(FieldWorker fw)
    {
        return fieldWorkerRepository.save(fw);
    }

    public void sendFieldWorkerCredentials(String email, String password, String area)
    {
        String subject = "Appointment as Field Worker";

        String body = "Dear Field Worker,\n\n" +
                "Congratulations! You have been appointed as a field worker for area " + area+".\n\n" +
                "Your credentials:\n" +
                "Email: "+email+"\n" +
                "Password: "+password+"\n\n" +
                "Best regards,\n" +
                "Medimate India";
        emailSenderService.sendEmail(email, subject, body);
    }


    public boolean removeFieldWorker(int id)
    {
        Optional<FieldWorker> fieldWorkerOptional = fieldWorkerRepository.findById(id);

        // Delete the field worker
        if(fieldWorkerOptional.isPresent()) {
            fieldWorkerRepository.deleteById(id);
            return true;
        }
        else
            return false;
    }


}
