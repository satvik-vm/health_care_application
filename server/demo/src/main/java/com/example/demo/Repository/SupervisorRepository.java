package com.example.demo.Repository;

import com.example.demo.Entity.District;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.Entity.Supervisor;

import java.util.List;

public interface SupervisorRepository extends JpaRepository<Supervisor, String> {
    Supervisor findByDistrict_Name(String district);

    Supervisor findByUser_Email(String email);

    Supervisor findByDistrict(District district);

}
