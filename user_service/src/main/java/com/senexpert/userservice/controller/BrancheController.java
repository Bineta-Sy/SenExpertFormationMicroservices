package com.senexpert.userservice.controller;

import com.senexpert.userservice.entity.Branche;
import com.senexpert.userservice.service.BrancheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/branches")
public class BrancheController {

    @Autowired private BrancheService brancheService;

    @PostMapping
    public ResponseEntity<Branche> creer(@RequestBody Branche branche) {
        return ResponseEntity.ok(brancheService.creer(branche));
    }

    @GetMapping
    public ResponseEntity<List<Branche>> lister() {
        return ResponseEntity.ok(brancheService.listerTous());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Branche> modifier(@PathVariable Long id, @RequestBody Branche body) {
        return ResponseEntity.ok(brancheService.modifier(id, body.getNom(), body.getCode()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        brancheService.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
