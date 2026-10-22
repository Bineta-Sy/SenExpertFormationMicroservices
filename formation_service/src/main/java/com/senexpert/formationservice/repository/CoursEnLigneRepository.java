package com.senexpert.formationservice.repository;

import com.senexpert.formationservice.entity.CoursEnLigne;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CoursEnLigneRepository extends JpaRepository<CoursEnLigne, Long> {

    List<CoursEnLigne> findByOffreIdOrderByOrdreAsc(Long offreId);

    List<CoursEnLigne> findByOffreIdAndPublieOrderByOrdreAsc(Long offreId, boolean publie);

    long countByOffreId(Long offreId);
}
