package com.example.demo.Repository;

import com.example.demo.Entity.Doctor;
import com.example.demo.Entity.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Integer> {
}
