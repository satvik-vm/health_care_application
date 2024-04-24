package com.example.demo.services;

import com.example.demo.Entity.*;
import com.example.demo.Repository.FieldWorkerRepository;
import com.example.demo.Repository.FwTeamRepository;
import com.example.demo.Repository.IdMappingRepository;
import com.example.demo.Repository.SupervisorRepository;
import com.example.demo.models.AssignGuidelinesRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;

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

    @Autowired
    private IdMappingRepository idMappingRepository;

    @Autowired
    private FwTeamRepository fwTeamRepository;


    public Supervisor getDetails(String id){
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


    public boolean removeFieldWorker(int fw_id)
    {
        String id = idMappingRepository.findById(fw_id).get().getPrivateId().toString();
        Optional<FieldWorker> fieldWorkerOptional = fieldWorkerRepository.findById(id);
        // Delete the field worker
        if(fieldWorkerOptional.isPresent()) {
            fieldWorkerRepository.deleteById(id);
            return true;
        }
        else
            return false;
    }

    public boolean transferFieldWorker(int id, String area)
    {
        String fw_id = idMappingRepository.findById(id).get().getPrivateId().toString();
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


    public String getSupervisorIdByEmail(String email) {
        Supervisor supervisor = supervisorRepository.findByUser_Email(email);
        if (supervisor != null) {
            return supervisor.getId(); // Assuming getId() returns the supervisor ID
        } else {
            return "-1";
        }

    }

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

    public ResponseEntity<List<Map<String, String>>> findfwByArea(String area) {
        List<FieldWorker> fieldWorkers = fieldWorkerRepository.findByArea(area);
        List<Map<String, String>> response = new ArrayList<>();

        for (FieldWorker fieldWorker : fieldWorkers) {
            int id = idMappingRepository.findByPrivateId(UUID.fromString(fieldWorker.getId())).getPublicId();
            User user = fieldWorker.getUser();
            Map<String, String> fieldWorkerDetails = new HashMap<>();
            fieldWorkerDetails.put("publicId", String.valueOf(id));
            fieldWorkerDetails.put("firstName", user.getFirstName());
            fieldWorkerDetails.put("lastName", user.getLastName());
            fieldWorkerDetails.put("email", user.getEmail());
            response.add(fieldWorkerDetails);
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public String assignGuidelines(AssignGuidelinesRequest request) {
        int id = request.getId();
        String team_id = idMappingRepository.findById(id).get().getPrivateId().toString();
        Optional<FwTeam> fwTeam = fwTeamRepository.findById(team_id);
        if(fwTeam.isPresent())
        {
            FwTeam team = fwTeam.get();
            team.setGuidelines(request.getGuideline());
            fwTeamRepository.save(team);
            return "Guidelines assigned successfully";
        }
        else
            return "Error assigning guidelines";

    }

    public boolean assignTeam(List<Integer> list) {
        try{
            for(int id : list)
            {
                String fw_id = idMappingRepository.findById(id).get().getPrivateId().toString();
                Optional<FieldWorker> fieldWorkerOptional = fieldWorkerRepository.findById(fw_id);
                FwTeam team = createTeam();
                if(fieldWorkerOptional.isPresent())
                {
                    FieldWorker fieldWorker = fieldWorkerOptional.get();
                    fieldWorker.setTeam(team);
                    fieldWorkerRepository.save(fieldWorker);
                }
            }
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    private FwTeam createTeam() {
        FwTeam team = new FwTeam();
        IdMapping idMapping = new IdMapping();
        idMapping.setPrivateId(UUID.fromString(team.getId()));
        return fwTeamRepository.save(team);
    }
}
