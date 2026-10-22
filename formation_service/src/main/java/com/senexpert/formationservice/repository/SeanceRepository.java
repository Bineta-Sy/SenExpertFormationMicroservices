package com.senexpert.formationservice.repository;

import com.senexpert.formationservice.entity.Seance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface SeanceRepository extends JpaRepository<Seance, Long> {
    List<Seance> findByModuleIdOrderByDateAscHeureDebutAsc(Long moduleId);

    @Query("SELECT s FROM Seance s WHERE s.module.offre.id = :offreId ORDER BY s.date ASC, s.heureDebut ASC")
    List<Seance> findByOffreId(@Param("offreId") Long offreId);
}
