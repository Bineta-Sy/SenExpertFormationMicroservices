package com.senexpert.formationservice.controller;

import com.senexpert.formationservice.dto.FormateurDTO;
import com.senexpert.formationservice.entity.Formateur;
import com.senexpert.formationservice.entity.OffreFormation;
import com.senexpert.formationservice.service.FormateurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/formateurs")
public class FormateurController {

    @Autowired
    private FormateurService service;

    /** Lister tous les formateurs */
    @GetMapping
    public ResponseEntity<List<Formateur>> listerTous() {
        return ResponseEntity.ok(service.listerTous());
    }

    /** Détail d'un formateur */
    @GetMapping("/{id}")
    public ResponseEntity<Formateur> trouver(@PathVariable Long id) {
        return ResponseEntity.ok(service.trouverParId(id));
    }

    /** Formateurs d'une offre */
    @GetMapping("/offre/{offreId}")
    public ResponseEntity<List<Formateur>> parOffre(@PathVariable Long offreId) {
        return ResponseEntity.ok(service.formateursParOffre(offreId));
    }

    /** Créer un formateur (ADMIN) */
    @PostMapping
    public ResponseEntity<Formateur> creer(@RequestBody FormateurDTO dto) {
        return ResponseEntity.ok(service.creer(dto));
    }

    /** Modifier un formateur (ADMIN) */
    @PutMapping("/{id}")
    public ResponseEntity<Formateur> modifier(@PathVariable Long id, @RequestBody FormateurDTO dto) {
        return ResponseEntity.ok(service.modifier(id, dto));
    }

    /** Supprimer un formateur (ADMIN) */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        service.supprimer(id);
        return ResponseEntity.noContent().build();
    }

    /** Assigner un formateur à une offre (ADMIN) */
    @PostMapping("/{formateurId}/offre/{offreId}")
    public ResponseEntity<OffreFormation> assigner(@PathVariable Long formateurId, @PathVariable Long offreId) {
        return ResponseEntity.ok(service.assignerAOffre(formateurId, offreId));
    }

    /** Retirer un formateur d'une offre (ADMIN) */
    @DeleteMapping("/{formateurId}/offre/{offreId}")
    public ResponseEntity<OffreFormation> retirer(@PathVariable Long formateurId, @PathVariable Long offreId) {
        return ResponseEntity.ok(service.retirerDeOffre(formateurId, offreId));
    }
}
