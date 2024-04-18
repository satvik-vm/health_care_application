package com.example.demo.services;

import com.example.demo.Entity.FieldWorker;
import com.example.demo.Entity.Supervisor;
import com.example.demo.Repository.FieldWorkerRepository;
import com.example.demo.Repository.SupervisorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Service
public class SupervisorService {
    @Autowired
    private SupervisorRepository supervisorRepository;

    @Autowired
    private FieldWorkerRepository fieldWorkerRepository;

    @Autowired
    private EmailSenderService emailSenderService;

    @Autowired
    private DistrictService districtService;


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

    public boolean transferFieldWorker(int fw_id, String area)
    {
        Optional<FieldWorker> fieldWorkerOptional = fieldWorkerRepository.findById(fw_id);
        if(fieldWorkerOptional.isPresent())
        {
            FieldWorker fieldWorker = fieldWorkerOptional.get();
            fieldWorker.setArea(area);
            fieldWorkerRepository.save(fieldWorker);
            return true;
        }
        else
            return false;

    }


//    public int getSupervisorIdByEmail(String email) {
//        Supervisor supervisor = supervisorRepository.findByUser_Email(email);
//        if (supervisor != null) {
//            return supervisor.getId(); // Assuming getId() returns the supervisor ID
//        } else {
//            return -1;
//        }
//
//    }

    public String getSupState(String email) {
        Supervisor supervisor = supervisorRepository.findByUser_Email(email);
        if (supervisor != null) {
            return supervisor.getState();
        } else {
            return null;
        }
    }

    public String getSupDistrict(String email) {
        Supervisor supervisor = supervisorRepository.findByUser_Email(email);
        if (supervisor != null) {
            return supervisor.getDistrict().getName();
        } else {
            return null;
        }
    }
}
