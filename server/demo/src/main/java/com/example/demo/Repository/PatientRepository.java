package com.example.demo.Repository;

import com.example.demo.Entity.Patient;
import com.example.demo.Entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Integer> {
    int countByHospitalId(int hospitalId);
    int countByDoctorId(int doctorId);
}
