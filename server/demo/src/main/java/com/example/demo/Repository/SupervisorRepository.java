package com.example.demo.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.Entity.Supervisor;

public interface SupervisorRepository extends JpaRepository<Supervisor, Integer> {
}
