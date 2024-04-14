package com.example.demo.services;

import com.example.demo.Entity.*;
import com.example.demo.Repository.DoctorRepository;
import com.example.demo.Repository.HospitalRepository;
import com.example.demo.Repository.PatientRepository;
import com.example.demo.models.DoctorCreationRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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

    public Doctor createDoctor(DoctorCreationRequest request, String hospitalEmail)
    {
        try{
            String email = request.getUser().getEmail();
//            Removing this for testing purposes
//            String password = userService.generatePassword();
            String password = "1234";
            String roleName = request.getUser().getRole().getName();
            String speciality = request.getSpeciality();
            String status = request.getStatus();
            Role role = roleService.getOrCreateRole(roleName);
            User user = userService.createUserForDoctor(email, password, role, request.getFirstName(), request.getLastName());
            Hospital hospital = hospitalRepository.findByUser_Email(hospitalEmail);
            Doctor doctor = new Doctor();
            doctor.setUser(user);
            doctor.setHospital(hospital);
            doctor.setStatus(status);
            doctor.setSpeciality(speciality);
//            For testing purposes I am removing the email system
//            sendDoctorCredentials(email, password);
            return doctorRepository.save(doctor);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.out.println("Error Occurring");
            return null;
        }
    }

    public boolean deleteDoctor(int id) {
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

            result.set(hospital.getName(), hospitalNode);
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
