package com.example.demo.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.Entity.Supervisor;

import java.util.List;

public interface SupervisorRepository extends JpaRepository<Supervisor, Integer> {
    List<Supervisor> findByDistrict(String district);

    Supervisor findByUser_Email(String email);
}
