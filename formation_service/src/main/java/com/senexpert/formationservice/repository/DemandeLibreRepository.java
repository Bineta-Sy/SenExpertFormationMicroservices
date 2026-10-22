package com.senexpert.formationservice.repository;

import com.senexpert.formationservice.entity.DemandeLibre;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DemandeLibreRepository extends JpaRepository<DemandeLibre, Long> {
    List<DemandeLibre> findByUtilisateurId(Long utilisateurId);
}
