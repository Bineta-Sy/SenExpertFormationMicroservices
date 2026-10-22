package com.senexpert.formationservice.controller;

import com.senexpert.formationservice.dto.DemandeLibreDTO;
import com.senexpert.formationservice.dto.FormationSouhaiteeDTO;
import com.senexpert.formationservice.entity.DemandeLibre;
import com.senexpert.formationservice.entity.FormationSouhaitee;
import com.senexpert.formationservice.service.DemandeLibreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/demandes-libre")
public class DemandeLibreController {

    @Autowired
    private DemandeLibreService service;

    @PostMapping
    public ResponseEntity<DemandeLibre> creer(@RequestBody DemandeLibreDTO dto) {
        return ResponseEntity.ok(service.creer(dto));
    }

    @GetMapping("/utilisateur/{utilisateurId}")
    public ResponseEntity<List<DemandeLibre>> listerParUtilisateur(@PathVariable Long utilisateurId) {
        return ResponseEntity.ok(service.listerParUtilisateur(utilisateurId));
    }

    @GetMapping
    public ResponseEntity<List<DemandeLibre>> listerToutes() {
        return ResponseEntity.ok(service.listerToutes());
    }

    @PutMapping("/valider/{id}")
    public ResponseEntity<DemandeLibre> valider(@PathVariable Long id) {
        return ResponseEntity.ok(service.valider(id));
    }

    @PutMapping("/rejeter/{id}")
    public ResponseEntity<DemandeLibre> rejeter(@PathVariable Long id) {
        return ResponseEntity.ok(service.rejeter(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> annuler(@PathVariable Long id) {
        service.annuler(id);
        return ResponseEntity.noContent().build();
    }

    /** Migration : cree les offres manquantes pour les demandes VALIDEE */
    @PostMapping("/migrer-offres")
    public ResponseEntity<String> migrerOffres() {
        int nb = service.creerOffresManquantes();
        return ResponseEntity.ok(nb + " offre(s) creee(s).");
    }

    @PostMapping("/{id}/formations")
    public ResponseEntity<FormationSouhaitee> ajouterFormation(
            @PathVariable Long id,
            @RequestBody FormationSouhaiteeDTO dto) {
        return ResponseEntity.ok(service.ajouterFormation(id, dto));
    }

    @GetMapping("/{id}/formations")
    public ResponseEntity<List<FormationSouhaitee>> listerFormations(@PathVariable Long id) {
        return ResponseEntity.ok(service.listerFormations(id));
    }

    @DeleteMapping("/{id}/formations/{formationId}")
    public ResponseEntity<Void> supprimerFormation(
            @PathVariable Long id,
            @PathVariable Long formationId) {
        service.supprimerFormation(id, formationId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/participants")
    public ResponseEntity<DemandeLibre> mettreAJourParticipants(
            @PathVariable Long id,
            @RequestBody List<Long> participantIds) {
        return ResponseEntity.ok(service.mettreAJourParticipants(id, participantIds));
    }
}
