package com.example.demo.Repository;

import com.example.demo.Entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, String> {
    Answer findByQuestionIdAndPatientId(String questionId, String patientId);
}
