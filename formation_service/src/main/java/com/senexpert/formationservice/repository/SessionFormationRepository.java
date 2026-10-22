package com.senexpert.formationservice.repository;

import com.senexpert.formationservice.entity.SessionFormation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SessionFormationRepository extends JpaRepository<SessionFormation, Long> {
    List<SessionFormation> findByOffreId(Long offreId);
    List<SessionFormation> findByStatut(com.senexpert.formationservice.entity.StatutSession statut);
}
