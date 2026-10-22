package com.senexpert.formationservice.repository;

import com.senexpert.formationservice.entity.Domaine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DomaineRepository extends JpaRepository<Domaine, Long> {

}