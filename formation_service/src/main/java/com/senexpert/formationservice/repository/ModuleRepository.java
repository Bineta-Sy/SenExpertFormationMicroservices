package com.senexpert.formationservice.repository;

import com.senexpert.formationservice.entity.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ModuleRepository extends JpaRepository<Module, Long> {
    List<Module> findByOffreIdOrderByOrdreAsc(Long offreId);
    void deleteByOffreId(Long offreId);
    long countByOffreId(Long offreId);
}
