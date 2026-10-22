package com.senexpert.formationservice.controller;

import com.senexpert.formationservice.dto.SeanceDTO;
import com.senexpert.formationservice.entity.Seance;
import com.senexpert.formationservice.service.SeanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/seances")
public class SeanceController {

    @Autowired private SeanceService service;

    /** Séances d'un module */
    @GetMapping("/module/{moduleId}")
    public ResponseEntity<List<Seance>> parModule(@PathVariable Long moduleId) {
        return ResponseEntity.ok(service.parModule(moduleId));
    }

    /** Toutes les séances d'une offre (emploi du temps global) */
    @GetMapping("/offre/{offreId}")
    public ResponseEntity<List<Seance>> parOffre(@PathVariable Long offreId) {
        return ResponseEntity.ok(service.parOffre(offreId));
    }

    /** Créer une séance */
    @PostMapping
    public ResponseEntity<Seance> creer(@RequestBody SeanceDTO dto) {
        return ResponseEntity.ok(service.creer(dto));
    }

    /** Modifier une séance */
    @PutMapping("/{id}")
    public ResponseEntity<Seance> modifier(@PathVariable Long id, @RequestBody SeanceDTO dto) {
        return ResponseEntity.ok(service.modifier(id, dto));
    }

    /** Supprimer une séance */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        service.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
