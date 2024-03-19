package com.example.demo.Repository;

import com.example.demo.Entity.Patient;
import com.example.demo.Entity.Questionnaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionnaireRepository extends JpaRepository<Questionnaire, Integer> {

}
