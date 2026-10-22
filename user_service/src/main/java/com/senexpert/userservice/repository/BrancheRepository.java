package com.senexpert.userservice.repository;

import com.senexpert.userservice.entity.Branche;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BrancheRepository extends JpaRepository<Branche, Long> {
    List<Branche> findByNomContainingIgnoreCase(String nom);
}
