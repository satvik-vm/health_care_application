package com.example.demo.Repository;

import com.example.demo.Entity.FieldWorker;
import com.example.demo.Entity.Supervisor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FieldWorkerRepository extends JpaRepository<FieldWorker, Integer> {
}
