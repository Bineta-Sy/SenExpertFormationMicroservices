package com.senexpert.formationservice.repository;

import com.senexpert.formationservice.entity.Candidature;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CandidatureRepository extends JpaRepository<Candidature, Long> {
    List<Candidature> findByUtilisateurId(Long utilisateurId);
    List<Candidature> findByOffre_Id(Long offreId);
}
