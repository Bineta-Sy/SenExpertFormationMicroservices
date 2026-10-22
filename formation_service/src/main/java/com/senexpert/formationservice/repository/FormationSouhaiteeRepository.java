package com.senexpert.formationservice.repository;

import com.senexpert.formationservice.entity.FormationSouhaitee;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FormationSouhaiteeRepository extends JpaRepository<FormationSouhaitee, Long> {
    List<FormationSouhaitee> findByDemandeId(Long demandeId);
    void deleteByDemandeId(Long demandeId);
}
