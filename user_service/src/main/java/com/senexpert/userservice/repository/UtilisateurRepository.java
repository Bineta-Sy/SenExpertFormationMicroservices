package com.senexpert.userservice.repository;

import com.senexpert.userservice.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    Optional<Utilisateur> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM Utilisateur u WHERE LOWER(u.nom) LIKE LOWER(CONCAT('%',:q,'%')) " +
           "OR LOWER(u.prenom) LIKE LOWER(CONCAT('%',:q,'%')) " +
           "OR LOWER(u.email) LIKE LOWER(CONCAT('%',:q,'%'))")
    List<Utilisateur> rechercher(@Param("q") String q);
    
}