package com.example.demo.Repository;

import com.example.demo.Entity.Patient;
import com.example.demo.Entity.Questionnaire;
import com.example.demo.Entity.Supervisor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QuestionnaireRepository extends JpaRepository<Questionnaire, String> {
    Optional<Questionnaire> findByName(String name);
}
