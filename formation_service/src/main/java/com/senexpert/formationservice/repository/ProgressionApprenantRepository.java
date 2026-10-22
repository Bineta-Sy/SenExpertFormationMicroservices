package com.senexpert.formationservice.repository;

import com.senexpert.formationservice.entity.ProgressionApprenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProgressionApprenantRepository extends JpaRepository<ProgressionApprenant, Long> {

    Optional<ProgressionApprenant> findByUtilisateurIdAndCoursId(Long utilisateurId, Long coursId);

    List<ProgressionApprenant> findByUtilisateurIdAndCoursOffreId(Long utilisateurId, Long offreId);

    long countByCoursOffreIdAndUtilisateurIdAndCompleteTrue(Long offreId, Long utilisateurId);
}
