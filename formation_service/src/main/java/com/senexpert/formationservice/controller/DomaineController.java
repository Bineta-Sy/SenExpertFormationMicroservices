package com.senexpert.formationservice.controller;

import com.senexpert.formationservice.entity.Domaine;
import com.senexpert.formationservice.service.DomaineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/domaines")
public class DomaineController {

    @Autowired
    private DomaineService domaineService;

    @PostMapping
    public ResponseEntity<Domaine> creer(@RequestBody Domaine domaine) {
        return ResponseEntity.ok(domaineService.creer(domaine));
    }

    @GetMapping
    public ResponseEntity<List<Domaine>> listerTous() {
        return ResponseEntity.ok(domaineService.listerTous());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Domaine> trouverParId(@PathVariable Long id) {
        return ResponseEntity.ok(domaineService.trouverParId(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        domaineService.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}