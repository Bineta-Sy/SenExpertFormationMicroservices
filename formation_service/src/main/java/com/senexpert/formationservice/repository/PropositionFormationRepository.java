package com.senexpert.formationservice.repository;

import com.senexpert.formationservice.entity.PropositionFormation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PropositionFormationRepository extends JpaRepository<PropositionFormation, Long> {
    List<PropositionFormation> findByDemandeLibreId(Long demandeLibreId);
    Optional<PropositionFormation> findFirstByDemandeLibreIdOrderByDatePropositionDesc(Long demandeLibreId);
}
