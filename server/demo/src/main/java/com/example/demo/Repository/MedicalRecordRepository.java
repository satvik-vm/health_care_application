package com.example.demo.Repository;

import com.example.demo.Entity.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, String> {

    MedicalRecord findByPatient_Id(String patientId);
}
