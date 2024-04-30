package com.example.demo.Repository;

import com.example.demo.Entity.District;
import com.example.demo.Entity.FieldWorker;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FieldWorkerRepository extends JpaRepository<FieldWorker, String> {
    List<FieldWorker> findByArea(String area);
    FieldWorker findByUser_Email(String email);
    List<FieldWorker> findByTeam_Id(String teamId);
    List<FieldWorker> findByDistrict(District district);

}
