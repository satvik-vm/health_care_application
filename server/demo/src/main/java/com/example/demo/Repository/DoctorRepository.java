package com.example.demo.Repository;

import com.example.demo.Entity.Doctor;
import com.example.demo.Entity.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Integer> {
    int countByHospitalId(int hospitalId);
    List<Doctor> findByHospitalId(int id);
    List<Doctor> findByHospital_User_Email(String email);
}
