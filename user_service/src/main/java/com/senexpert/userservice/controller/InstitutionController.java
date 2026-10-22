package com.senexpert.userservice.controller;

import com.senexpert.userservice.entity.Institution;
import com.senexpert.userservice.service.InstitutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/institutions")
public class InstitutionController {

    @Autowired
    private InstitutionService institutionService;

    @PostMapping
    public ResponseEntity<Institution> creer(@RequestBody Institution institution) {
        return ResponseEntity.ok(institutionService.creer(institution));
    }

    @GetMapping
    public ResponseEntity<List<Institution>> listerToutes() {
        return ResponseEntity.ok(institutionService.listerToutes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Institution> trouverParId(@PathVariable Long id) {
        return ResponseEntity.ok(institutionService.trouverParId(id));
    }

    @PutMapping("/valider/{id}")
    public ResponseEntity<Institution> valider(@PathVariable Long id) {
        return ResponseEntity.ok(institutionService.valider(id));
    }

    /** Désactive une institution (Admin) */
    @PutMapping("/desactiver/{id}")
    public ResponseEntity<Institution> desactiver(@PathVariable Long id) {
        return ResponseEntity.ok(institutionService.desactiver(id));
    }

    /** Réactive une institution (Admin) */
    @PutMapping("/reactiver/{id}")
    public ResponseEntity<Institution> reactiver(@PathVariable Long id) {
        return ResponseEntity.ok(institutionService.reactiver(id));
    }

    /** Supprime une institution (Admin) */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        institutionService.supprimer(id);
        return ResponseEntity.noContent().build();
    }

    /** Modifie les informations d'une institution (agent connecté) */
    @PutMapping("/{id}")
    public ResponseEntity<Institution> modifier(
            @PathVariable Long id,
            @RequestParam(required = false) String nom,
            @RequestParam(required = false) String adresse,
            @RequestParam(required = false) String secteur,
            @RequestParam(required = false) String ninea,
            @RequestParam(required = false) String domainesInteret) {
        return ResponseEntity.ok(institutionService.modifier(id, nom, adresse, secteur, ninea, domainesInteret));
    }
}
