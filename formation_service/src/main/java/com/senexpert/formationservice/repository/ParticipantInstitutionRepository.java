package com.senexpert.formationservice.repository;

import com.senexpert.formationservice.entity.ParticipantInstitution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParticipantInstitutionRepository extends JpaRepository<ParticipantInstitution, Long> {
    List<ParticipantInstitution> findByDemandeIdOrderByNomAsc(Long demandeId);
    long countByDemandeId(Long demandeId);
}
