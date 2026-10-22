package com.senexpert.formationservice.controller;

import com.senexpert.formationservice.dto.PropositionFormationDTO;
import com.senexpert.formationservice.entity.PropositionFormation;
import com.senexpert.formationservice.service.PropositionFormationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/propositions")
public class PropositionFormationController {

    @Autowired
    private PropositionFormationService service;

    /** Admin : créer une proposition pour une demande validée */
    @PostMapping
    public ResponseEntity<PropositionFormation> creer(@RequestBody PropositionFormationDTO dto) {
        return ResponseEntity.ok(service.creer(dto));
    }

    /** Toutes les propositions (admin) */
    @GetMapping
    public ResponseEntity<List<PropositionFormation>> listerToutes() {
        return ResponseEntity.ok(service.listerToutes());
    }

    /** Propositions d'une demande à la carte */
    @GetMapping("/demande/{demandeLibreId}")
    public ResponseEntity<List<PropositionFormation>> parDemande(@PathVariable Long demandeLibreId) {
        return ResponseEntity.ok(service.listerParDemande(demandeLibreId));
    }

    /** Dernière proposition d'une demande (la plus récente) */
    @GetMapping("/demande/{demandeLibreId}/derniere")
    public ResponseEntity<PropositionFormation> derniere(@PathVariable Long demandeLibreId) {
        return service.derniereparDemande(demandeLibreId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** Agent : accepter une proposition */
    @PutMapping("/{id}/accepter")
    public ResponseEntity<PropositionFormation> accepter(@PathVariable Long id) {
        return ResponseEntity.ok(service.accepter(id));
    }

    /** Agent : refuser une proposition */
    @PutMapping("/{id}/refuser")
    public ResponseEntity<PropositionFormation> refuser(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        String motif = body != null ? body.get("motif") : null;
        return ResponseEntity.ok(service.refuser(id, motif));
    }
}
