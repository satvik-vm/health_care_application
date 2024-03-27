package com.example.demo.services;

import com.example.demo.Entity.*;
import com.example.demo.Repository.DoctorRepository;
import com.example.demo.Repository.HospitalRepository;
import com.example.demo.models.DoctorCreationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}
