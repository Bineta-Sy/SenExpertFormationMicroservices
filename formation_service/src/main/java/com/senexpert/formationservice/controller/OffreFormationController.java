package com.senexpert.formationservice.controller;

import com.senexpert.formationservice.dto.OffreFormationDTO;
import com.senexpert.formationservice.entity.OffreFormation;
import com.senexpert.formationservice.service.OffreFormationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/offres")
public class OffreFormationController {

    @Autowired
    private OffreFormationService offreFormationService;

    @PostMapping
    public ResponseEntity<OffreFormation> creer(@RequestBody OffreFormationDTO dto) {
        return ResponseEntity.ok(offreFormationService.creer(dto));
    }

    /** Endpoint public : offres DISPONIBLE uniquement. */
    @GetMapping
    public ResponseEntity<List<OffreFormation>> listerToutes() {
        return ResponseEntity.ok(offreFormationService.listerToutes());
    }

    /** Endpoint protégé (admin/formateur) : tous les statuts. */
    @GetMapping("/admin")
    public ResponseEntity<List<OffreFormation>> listerToutesAdmin() {
        return ResponseEntity.ok(offreFormationService.listerToutesAdmin());
    }

    @GetMapping("/domaine/{domaineId}")
    public ResponseEntity<List<OffreFormation>> listerParDomaine(@PathVariable Long domaineId) {
        return ResponseEntity.ok(offreFormationService.listerParDomaine(domaineId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OffreFormation> trouverParId(@PathVariable Long id) {
        return ResponseEntity.ok(offreFormationService.trouverParId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OffreFormation> modifier(@PathVariable Long id,
                                                    @RequestBody OffreFormationDTO dto) {
        return ResponseEntity.ok(offreFormationService.modifier(id, dto));
    }

    @PutMapping("/publier/{id}")
    public ResponseEntity<OffreFormation> publier(@PathVariable Long id) {
        return ResponseEntity.ok(offreFormationService.publier(id));
    }

    @PutMapping("/fermer/{id}")
    public ResponseEntity<OffreFormation> fermer(@PathVariable Long id) {
        return ResponseEntity.ok(offreFormationService.fermer(id));
    }

    /** Modifie la date limite de candidature (null = supprime la limite) */
    @PutMapping("/{id}/date-limite")
    public ResponseEntity<OffreFormation> majDateLimite(
            @PathVariable Long id,
            @RequestParam(required = false) String date) {
        LocalDate parsed = (date != null && !date.isBlank()) ? LocalDate.parse(date) : null;
        return ResponseEntity.ok(offreFormationService.majDateLimiteCandidature(id, parsed));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        offreFormationService.supprimer(id);
        return ResponseEntity.noContent().build();
    }

    // ── Prestataire ──────────────────────────────────────────────────────────

    /** Formations d'un prestataire */
    @GetMapping("/prestataire/{prestataireId}")
    public ResponseEntity<List<OffreFormation>> mesFormations(@PathVariable Long prestataireId) {
        return ResponseEntity.ok(offreFormationService.mesFormations(prestataireId));
    }

    /** Soumet une formation à l'admin (prestataire) */
    @PutMapping("/soumettre/{id}")
    public ResponseEntity<OffreFormation> soumettre(@PathVariable Long id,
                                                     @RequestParam Long prestataireId) {
        return ResponseEntity.ok(offreFormationService.soumettre(id, prestataireId));
    }

    /** Annule la soumission (reprend en brouillon) */
    @PutMapping("/annuler-soumission/{id}")
    public ResponseEntity<OffreFormation> annulerSoumission(@PathVariable Long id,
                                                              @RequestParam Long prestataireId) {
        return ResponseEntity.ok(offreFormationService.annulerSoumission(id, prestataireId));
    }

    /** Admin : formations soumises en attente */
    @GetMapping("/soumises")
    public ResponseEntity<List<OffreFormation>> soumises() {
        return ResponseEntity.ok(offreFormationService.listerSoumises());
    }

    /** Admin : rejette une formation soumise, avec motif optionnel */
    @PutMapping("/rejeter/{id}")
    public ResponseEntity<OffreFormation> rejeter(@PathVariable Long id,
                                                   @RequestParam(required = false) String motif) {
        return ResponseEntity.ok(offreFormationService.rejeter(id, motif));
    }
}