package com.senexpert.formationservice.repository;

import com.senexpert.formationservice.entity.DemandeFormation;
import com.senexpert.formationservice.entity.StatutDemande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DemandeFormationRepository extends JpaRepository<DemandeFormation, Long> {

    List<DemandeFormation> findByUtilisateurId(Long utilisateurId);

    List<DemandeFormation> findByOffreId(Long offreId);

    boolean existsByUtilisateurIdAndOffreId(Long utilisateurId, Long offreId);

    long countByOffreIdAndStatut(Long offreId, StatutDemande statut);

    long countByOffreIdAndStatutIn(Long offreId, List<StatutDemande> statuts);
}
