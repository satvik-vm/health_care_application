package com.example.demo.Repository;

import org.springframework.data.repository.CrudRepository;
import com.example.demo.Entity.Supervisor;

public interface SupervisorRepository extends CrudRepository<Supervisor, Integer> {
}
