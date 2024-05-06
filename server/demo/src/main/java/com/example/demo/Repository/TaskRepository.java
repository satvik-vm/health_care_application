package com.example.demo.Repository;

import com.example.demo.Entity.FieldWorker;
import com.example.demo.Entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, String> {
    List<Task> findByFieldWorkerAndDate(FieldWorker fieldWorker, String date);
    List<Task> findByFieldWorkerAndStatus(FieldWorker fieldWorker, boolean status);
    List<Task> findByFieldWorkerAndDateAndStatus(FieldWorker fieldWorker, String date, boolean status);
}
