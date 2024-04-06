package com.example.demo.services;

import com.example.demo.Entity.Doctor;
import com.example.demo.Entity.Hospital;
import com.example.demo.Repository.DoctorRepository;
import com.example.demo.Repository.HospitalRepository;
import com.example.demo.Repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DoctorService {
    @Autowired
    PatientRepository patientRepository;
    @Autowired
    HospitalRepository hospitalRepository;
    @Autowired
    DoctorRepository doctorRepository;
    public Doctor allocateDoctor(Hospital hospital)
    {
        List<Doctor> doctors = doctorRepository.findByHospitalId(hospital.getId());
        int mini = Integer.MAX_VALUE;
        Doctor allocatedDoctor = null;
        for(Doctor doctor : doctors)
        {
            int patientCount = patientRepository.countByDoctorId(doctor.getId());
            if(mini > patientCount)
            {
                mini = patientCount;
                allocatedDoctor = doctor;
            }
        }
        return allocatedDoctor;
    }
}
