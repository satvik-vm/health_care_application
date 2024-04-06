package com.example.demo.Repository;

import com.example.demo.Entity.Admin;
import com.example.demo.Entity.Hospital;
import com.example.demo.Entity.Supervisor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HospitalRepository extends JpaRepository<Hospital, Integer> {
    Hospital findByUser_Email(String email);
    List<Hospital> findBySubDivision(String subDivision);

    List<Hospital> findByDistrict(String district);
}
