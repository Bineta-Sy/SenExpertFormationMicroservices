package com.senexpert.formationservice.repository;

import com.senexpert.formationservice.entity.Soumission;
import com.senexpert.formationservice.entity.StatutSoumission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SoumissionRepository extends JpaRepository<Soumission, Long> {
    List<Soumission> findByCoursIdOrderByDateSoumissionDesc(Long coursId);
    Optional<Soumission> findByCoursIdAndApprenantId(Long coursId, Long apprenantId);
    List<Soumission> findByCoursIdAndStatut(Long coursId, StatutSoumission statut);
    long countByCoursIdAndStatut(Long coursId, StatutSoumission statut);
}
