package com.example.demo.services;

import com.example.demo.Entity.*;
import com.example.demo.Repository.DoctorRepository;
import com.example.demo.Repository.HospitalRepository;
import com.example.demo.Repository.IdMappingRepository;
import com.example.demo.Repository.PatientRepository;
import com.example.demo.models.DoctorCreationRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class HospitalService {
    @Autowired
    RoleService roleService;

    @Autowired
    UserService userService;

    @Autowired
    HospitalRepository hospitalRepository;

    @Autowired
    DoctorRepository doctorRepository;

    @Autowired
    PatientRepository patientRepository;
    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    private IdMappingRepository idMappingRepository;

    @Transactional
    public String createDoctors(List<DoctorCreationRequest> requests, String hospitalEmail)
    {
        try{
            for(DoctorCreationRequest request : requests)
            {
                User user = new User();
                user.setEmail(request.getUser().getEmail());
                user.setPassword(passwordEncoder.encode("1234"));
                user.setFirstName(request.getFullName());
                Role role = roleService.getOrCreateRole("DOCTOR");
                user.setRole(role);
                Doctor doctor = new Doctor();

                // Create a new IdMapping object and set its privateId to the Doctor's UUID
                IdMapping idMapping = new IdMapping();
                idMapping.setPrivateId(UUID.fromString(doctor.getId()));

                // Save the IdMapping object to the database
                idMappingRepository.save(idMapping);// Create a new IdMapping object and set its privateId to the generated UUID

                doctor.setUser(user);
                doctor.setRegId(request.getRegNo());
                doctor.setHospital(hospitalRepository.findByUser_Email(hospitalEmail));
                doctorRepository.save(doctor);
            }
            return "Doctors Successfully added to the hospital";
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.out.println("Error Occurring");
            return "Doctors not added to the hospital";
        }
    }

    public boolean deleteDoctor(String id) {
        Optional<Doctor> doctorOptional = doctorRepository.findById(id);

        // Delete the doctor
        if(doctorOptional.isPresent()) {
            doctorRepository.deleteById(id);
            return true;
        }
        else
            return false;
    }

    public Hospital allocateHospital(String subDivision, String district) {
        List<Hospital> hospitalList = hospitalRepository.findBySubDivision(subDivision);
        if(hospitalList.isEmpty())
        {
            hospitalList = hospitalRepository.findByDistrict(district);
        }
        Hospital allocatedHospital = null;
        int mini = Integer.MAX_VALUE;
        for(Hospital hospital : hospitalList)
        {
            int doctorCount = doctorRepository.countByHospitalId(hospital.getId());
            int patientCount = patientRepository.countByHospitalId(hospital.getId());
            if(doctorCount == 0)
                continue;
            else
            {
                if(mini > patientCount/doctorCount)
                {
                    mini = patientCount/doctorCount;
                    allocatedHospital = hospital;
                }
            }
        }
        return allocatedHospital;
    }

    public JsonNode getDetails(String email) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode result = mapper.createObjectNode();

        Hospital hospital = hospitalRepository.findByUser_Email(email);

        if (hospital != null) {
            ObjectNode hospitalNode = mapper.createObjectNode();
            hospitalNode.put("name", hospital.getName());
            hospitalNode.put("state", hospital.getState());
            hospitalNode.put("district", hospital.getDistrict());
            hospitalNode.put("subdivision", hospital.getSubDivision());
            hospitalNode.put("email", email); // Add email to the details

            result.set("hospital", hospitalNode);
        } else {
            result.put("error", "Hospital not found with email: " + email);
        }

        return result;
    }

    public List<Doctor> getDoctors(String email)
    {
        return doctorRepository.findByHospital_User_Email(email);
    }
}
