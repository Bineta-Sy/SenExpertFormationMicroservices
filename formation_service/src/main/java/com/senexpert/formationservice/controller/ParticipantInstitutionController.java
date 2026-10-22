package com.senexpert.formationservice.controller;

import com.senexpert.formationservice.dto.ParticipantInstitutionDTO;
import com.senexpert.formationservice.entity.ParticipantInstitution;
import com.senexpert.formationservice.repository.ParticipantInstitutionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class ParticipantInstitutionController {

    private final ParticipantInstitutionRepository repo;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public ParticipantInstitutionController(ParticipantInstitutionRepository repo) {
        this.repo = repo;
    }

    /** Ajoute un participant manuellement à une demande. */
    @PostMapping("/demandes/{demandeId}/participants")
    public ResponseEntity<ParticipantInstitutionDTO> ajouter(
            @PathVariable Long demandeId,
            @RequestBody ParticipantInstitutionDTO dto) {

        ParticipantInstitution p = new ParticipantInstitution();
        p.setDemandeId(demandeId);
        p.setNom(dto.getNom());
        p.setPrenom(dto.getPrenom());
        p.setEmail(dto.getEmail());
        p.setTelephone(dto.getTelephone());
        if (dto.getStatut() != null) p.setStatut(dto.getStatut());
        return ResponseEntity.ok(toDTO(repo.save(p)));
    }

    /** Liste tous les participants d'une demande. */
    @GetMapping("/demandes/{demandeId}/participants")
    public ResponseEntity<List<ParticipantInstitutionDTO>> lister(
            @PathVariable Long demandeId) {

        return ResponseEntity.ok(
            repo.findByDemandeIdOrderByNomAsc(demandeId)
                .stream().map(this::toDTO).collect(Collectors.toList()));
    }

    /** Nombre de participants pour une demande. */
    @GetMapping("/demandes/{demandeId}/participants/count")
    public ResponseEntity<Map<String, Long>> count(@PathVariable Long demandeId) {
        return ResponseEntity.ok(Map.of("count", repo.countByDemandeId(demandeId)));
    }

    /** Met à jour le statut d'un participant. */
    @PutMapping("/participants/{id}/statut")
    public ResponseEntity<ParticipantInstitutionDTO> updateStatut(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        return repo.findById(id).map(p -> {
            p.setStatut(body.getOrDefault("statut", p.getStatut()));
            return ResponseEntity.ok(toDTO(repo.save(p)));
        }).orElse(ResponseEntity.notFound().build());
    }

    /** Supprime un participant. */
    @DeleteMapping("/participants/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private ParticipantInstitutionDTO toDTO(ParticipantInstitution p) {
        ParticipantInstitutionDTO d = new ParticipantInstitutionDTO();
        d.setId(p.getId());
        d.setDemandeId(p.getDemandeId());
        d.setNom(p.getNom());
        d.setPrenom(p.getPrenom());
        d.setEmail(p.getEmail());
        d.setTelephone(p.getTelephone());
        d.setStatut(p.getStatut());
        d.setDateInscription(p.getDateInscription() != null ? p.getDateInscription().format(FMT) : "");
        return d;
    }
}
