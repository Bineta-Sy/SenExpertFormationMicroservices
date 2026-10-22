package com.senexpert.formationservice.repository;

import com.senexpert.formationservice.entity.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {

    List<QuizQuestion> findByCoursIdOrderByOrdreAsc(Long coursId);

    void deleteByCoursId(Long coursId);
}
