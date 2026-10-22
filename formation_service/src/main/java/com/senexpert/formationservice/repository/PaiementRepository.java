package com.senexpert.formationservice.repository;

import com.senexpert.formationservice.entity.Paiement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PaiementRepository extends JpaRepository<Paiement, Long> {

    List<Paiement> findByUtilisateurIdOrderByDatePaiementDesc(Long utilisateurId);

    @Query("SELECT SUM(p.montant) FROM Paiement p WHERE p.utilisateurId = :userId AND p.statut = 'SUCCES'")
    Double totalPaye(Long userId);

    /** Paiements liés à une demande institution. */
    List<Paiement> findByDemandeIdOrderByDatePaiementDesc(Long demandeId);

    /** Vérifie si une demande est déjà payée (SUCCES). */
    boolean existsByDemandeIdAndStatut(Long demandeId, String statut);
}
