package com.example.demo.Repository;

import com.example.demo.Entity.FieldWorker;
import com.example.demo.Entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, String> {
}
