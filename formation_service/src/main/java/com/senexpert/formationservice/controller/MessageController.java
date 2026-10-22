package com.senexpert.formationservice.controller;

import com.senexpert.formationservice.dto.MessageDTO;
import com.senexpert.formationservice.entity.Message;
import com.senexpert.formationservice.repository.MessageRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageRepository repo;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public MessageController(MessageRepository repo) {
        this.repo = repo;
    }

    /** Fil de conversation : tous les messages d'une offre impliquant userId. */
    @GetMapping("/offre/{offreId}/utilisateur/{userId}")
    public ResponseEntity<List<MessageDTO>> conversation(
            @PathVariable Long offreId,
            @PathVariable Long userId) {
        List<MessageDTO> dtos = repo.findConversation(offreId, userId)
                .stream().map(this::toDTO).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /** Envoyer un message. */
    @PostMapping
    public ResponseEntity<MessageDTO> envoyer(@RequestBody MessageDTO dto) {
        Message m = new Message();
        m.setOffreId(dto.getOffreId());
        m.setDemandeId(dto.getDemandeId());
        m.setExpediteurId(dto.getExpediteurId());
        m.setExpediteurRole(dto.getExpediteurRole());
        m.setExpediteurNom(dto.getExpediteurNom() != null ? dto.getExpediteurNom() : "Utilisateur");
        m.setDestinataireId(dto.getDestinataireId());
        m.setContenu(dto.getContenu());
        return ResponseEntity.ok(toDTO(repo.save(m)));
    }

    // ── Endpoints messagerie agent institution ↔ admin ────────────────────

    /** Fil de conversation lié à une demande. */
    @GetMapping("/demande/{demandeId}/utilisateur/{userId}")
    public ResponseEntity<List<MessageDTO>> conversationDemande(
            @PathVariable Long demandeId,
            @PathVariable Long userId) {
        return ResponseEntity.ok(
            repo.findConversationDemande(demandeId, userId)
                .stream().map(this::toDTO).collect(Collectors.toList()));
    }

    /** Marquer tous les messages d'une demande reçus par userId comme lus. */
    @PutMapping("/demande/{demandeId}/utilisateur/{userId}/lu-tout")
    public ResponseEntity<Map<String, Long>> marquerToutLuDemande(
            @PathVariable Long demandeId,
            @PathVariable Long userId) {
        List<Message> nonLus = repo.findConversationDemande(demandeId, userId).stream()
                .filter(m -> !m.isLu() && m.getDestinataireId().equals(userId))
                .collect(Collectors.toList());
        nonLus.forEach(m -> m.setLu(true));
        repo.saveAll(nonLus);
        return ResponseEntity.ok(Map.of("marques", (long) nonLus.size()));
    }

    /** Non lus pour une demande et un destinataire. */
    @GetMapping("/demande/{demandeId}/non-lus/{userId}")
    public ResponseEntity<Map<String, Long>> nonLusDemande(
            @PathVariable Long demandeId,
            @PathVariable Long userId) {
        return ResponseEntity.ok(Map.of("count",
            repo.countByDemandeIdAndDestinataireIdAndLuFalse(demandeId, userId)));
    }

    /** Marquer comme lu. */
    @PutMapping("/{id}/lu")
    public ResponseEntity<Void> marquerLu(@PathVariable Long id) {
        repo.findById(id).ifPresent(m -> { m.setLu(true); repo.save(m); });
        return ResponseEntity.ok().build();
    }

    /** Marquer tous les messages d'une offre reçus par userId comme lus. */
    @PutMapping("/offre/{offreId}/utilisateur/{userId}/lu-tout")
    public ResponseEntity<Map<String, Long>> marquerToutLu(
            @PathVariable Long offreId,
            @PathVariable Long userId) {
        List<Message> nonLus = repo.findConversation(offreId, userId).stream()
                .filter(m -> !m.isLu() && m.getDestinataireId().equals(userId))
                .collect(Collectors.toList());
        nonLus.forEach(m -> m.setLu(true));
        repo.saveAll(nonLus);
        return ResponseEntity.ok(Map.of("marques", (long) nonLus.size()));
    }

    /** Nombre total de messages non lus pour userId. */
    @GetMapping("/non-lus/{userId}")
    public ResponseEntity<Map<String, Long>> nonLus(@PathVariable Long userId) {
        return ResponseEntity.ok(Map.of("count", repo.countByDestinataireIdAndLuFalse(userId)));
    }

    private MessageDTO toDTO(Message m) {
        MessageDTO d = new MessageDTO();
        d.setId(m.getId());
        d.setOffreId(m.getOffreId());
        d.setDemandeId(m.getDemandeId());
        d.setExpediteurId(m.getExpediteurId());
        d.setExpediteurRole(m.getExpediteurRole());
        d.setExpediteurNom(m.getExpediteurNom());
        d.setDestinataireId(m.getDestinataireId());
        d.setContenu(m.getContenu());
        d.setDateEnvoi(m.getDateEnvoi() != null ? m.getDateEnvoi().format(FMT) : "");
        d.setLu(m.isLu());
        return d;
    }
}
