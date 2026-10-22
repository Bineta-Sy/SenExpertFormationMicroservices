package com.senexpert.formationservice.controller;

import com.senexpert.formationservice.dto.ModuleDTO;
import com.senexpert.formationservice.entity.Module;
import com.senexpert.formationservice.service.ModuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/modules")
public class ModuleController {

    @Autowired
    private ModuleService service;

    /** Modules d'une offre triés par ordre */
    @GetMapping("/offre/{offreId}")
    public ResponseEntity<List<Module>> parOffre(@PathVariable Long offreId) {
        return ResponseEntity.ok(service.listerParOffre(offreId));
    }

    /** Créer un module (prestataire ou admin) */
    @PostMapping
    public ResponseEntity<Module> creer(@RequestBody ModuleDTO dto) {
        return ResponseEntity.ok(service.creer(dto));
    }

    /** Modifier un module */
    @PutMapping("/{id}")
    public ResponseEntity<Module> modifier(@PathVariable Long id, @RequestBody ModuleDTO dto) {
        return ResponseEntity.ok(service.modifier(id, dto));
    }

    /** Assigner un formateur à un module */
    @PutMapping("/{moduleId}/formateur/{formateurId}")
    public ResponseEntity<Module> assignerFormateur(@PathVariable Long moduleId,
                                                     @PathVariable Long formateurId) {
        return ResponseEntity.ok(service.assignerFormateur(moduleId, formateurId));
    }

    /** Retirer le formateur d'un module */
    @DeleteMapping("/{moduleId}/formateur")
    public ResponseEntity<Module> retirerFormateur(@PathVariable Long moduleId) {
        return ResponseEntity.ok(service.retirerFormateur(moduleId));
    }

    /** Supprimer un module */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        service.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
