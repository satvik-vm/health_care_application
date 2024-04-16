package com.example.demo.Repository;

import com.example.demo.Entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Integer> {
    Answer findByQuestionIdAndPatientId(int questionId, int patientId);
}
