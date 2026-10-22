package com.senexpert.formationservice.repository;

import com.senexpert.formationservice.entity.EvaluationFormation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface EvaluationFormationRepository extends JpaRepository<EvaluationFormation, Long> {

    Optional<EvaluationFormation> findByApprenantIdAndOffreId(Long apprenantId, Long offreId);

    List<EvaluationFormation> findByOffreId(Long offreId);

    List<EvaluationFormation> findByApprenantId(Long apprenantId);

    @Query("SELECT AVG(e.note) FROM EvaluationFormation e WHERE e.offreId = :offreId")
    Double moyenneParOffre(Long offreId);

    @Query("SELECT COUNT(e) FROM EvaluationFormation e WHERE e.offreId = :offreId")
    long nombreParOffre(Long offreId);
}
