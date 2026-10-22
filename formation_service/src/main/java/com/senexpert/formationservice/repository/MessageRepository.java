package com.senexpert.formationservice.repository;

import com.senexpert.formationservice.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    /** Conversation d'une offre entre deux utilisateurs (fil bilatéral). */
    @Query("SELECT m FROM Message m WHERE m.offreId = :offreId " +
           "AND ((m.expediteurId = :userId) OR (m.destinataireId = :userId)) " +
           "ORDER BY m.dateEnvoi ASC")
    List<Message> findConversation(Long offreId, Long userId);

    /** Messages non lus destinés à userId dans toute une offre. */
    long countByOffreIdAndDestinataireIdAndLuFalse(Long offreId, Long destinataireId);

    /** Total non lus pour un utilisateur (toutes offres). */
    long countByDestinataireIdAndLuFalse(Long destinataireId);

    /** Conversation liée à une demande (agent institution ↔ admin). */
    @Query("SELECT m FROM Message m WHERE m.demandeId = :demandeId " +
           "AND ((m.expediteurId = :userId) OR (m.destinataireId = :userId)) " +
           "ORDER BY m.dateEnvoi ASC")
    List<Message> findConversationDemande(Long demandeId, Long userId);

    /** Non lus pour une demande et un destinataire. */
    long countByDemandeIdAndDestinataireIdAndLuFalse(Long demandeId, Long destinataireId);
}
