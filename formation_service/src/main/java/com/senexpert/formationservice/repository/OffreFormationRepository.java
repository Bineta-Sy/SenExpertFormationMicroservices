package com.senexpert.formationservice.repository;

import com.senexpert.formationservice.entity.OffreFormation;
import com.senexpert.formationservice.entity.StatutOffre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OffreFormationRepository extends JpaRepository<OffreFormation, Long> {

    List<OffreFormation> findByDomaineId(Long domaineId);

    /** Offres publiques uniquement (DISPONIBLE) */
    List<OffreFormation> findByStatut(StatutOffre statut);

    /** Offres publiques filtrées par domaine */
    List<OffreFormation> findByDomaineIdAndStatut(Long domaineId, StatutOffre statut);

    /** Offres d'un prestataire (tous statuts) */
    List<OffreFormation> findByPrestataireIdOrderByIdDesc(Long prestataireId);

    /** Offres soumises à l'admin en attente de validation */
    List<OffreFormation> findBySoumisTrue();

    /** Trouver l'offre créée à partir d'une demande à la carte */
    java.util.Optional<OffreFormation> findByDemandeLibreId(Long demandeLibreId);
}