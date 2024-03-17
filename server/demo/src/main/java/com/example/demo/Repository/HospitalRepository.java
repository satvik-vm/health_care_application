package com.example.demo.Repository;

import com.example.demo.Entity.Admin;
import com.example.demo.Entity.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HospitalRepository extends JpaRepository<Hospital, Integer> {
}
