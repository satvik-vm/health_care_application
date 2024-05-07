package com.example.demo.Repository;

import com.example.demo.Entity.UpdateForPatient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface UpdateForPatientRepository extends JpaRepository<UpdateForPatient, String> {
}
