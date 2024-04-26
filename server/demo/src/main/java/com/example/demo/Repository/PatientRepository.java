package com.example.demo.Repository;

import com.example.demo.Entity.Patient;
import com.example.demo.Entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PatientRepository extends JpaRepository<Patient, String> {
    int countByHospitalId(String hospitalId);
    int countByDoctorId(String doctorId);

    List<Patient> findByDoctorId(String doctorId);

    Patient findByAabhaId(String aabha);

    List<Patient> findByDoctor_User_Email(String email);
}
