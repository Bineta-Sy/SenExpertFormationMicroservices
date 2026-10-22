package com.senexpert.formationservice.controller;

import com.senexpert.formationservice.entity.SessionFormation;
import com.senexpert.formationservice.service.SessionFormationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sessions")
public class SessionFormationController {

    @Autowired private SessionFormationService sessionService;

    /** Liste toutes les sessions */
    @GetMapping
    public ResponseEntity<List<SessionFormation>> listerToutes() {
        return ResponseEntity.ok(sessionService.listerToutes());
    }

    /** Liste les sessions d'une offre */
    @GetMapping("/offre/{offreId}")
    public ResponseEntity<List<SessionFormation>> parOffre(@PathVariable Long offreId) {
        return ResponseEntity.ok(sessionService.listerParOffre(offreId));
    }

    /** Détail d'une session */
    @GetMapping("/{id}")
    public ResponseEntity<SessionFormation> detail(@PathVariable Long id) {
        return ResponseEntity.ok(sessionService.trouverParId(id));
    }

    /**
     * Crée une session pour une offre.
     * Body JSON : { offreId, dateHeure, dureeMinutes, salle, formateur, nbPlaces }
     */
    @PostMapping
    public ResponseEntity<SessionFormation> creer(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(sessionService.creer(body));
    }

    /** Modifie une session */
    @PutMapping("/{id}")
    public ResponseEntity<SessionFormation> modifier(@PathVariable Long id,
                                                      @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(sessionService.modifier(id, body));
    }

    /** Change le statut d'une session (PLANIFIEE, EN_COURS, TERMINEE, ANNULEE) */
    @PutMapping("/{id}/statut")
    public ResponseEntity<SessionFormation> changerStatut(@PathVariable Long id,
                                                           @RequestParam String statut) {
        return ResponseEntity.ok(sessionService.changerStatut(id, statut));
    }

    /** Supprime une session */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        sessionService.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
