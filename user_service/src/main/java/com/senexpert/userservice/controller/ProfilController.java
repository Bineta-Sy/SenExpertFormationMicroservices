package com.senexpert.userservice.controller;

import com.senexpert.userservice.entity.Profil;
import com.senexpert.userservice.service.ProfilService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/profils")
public class ProfilController {

    @Autowired private ProfilService profilService;

    @PostMapping
    public ResponseEntity<Profil> creer(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(profilService.creer(body.get("nom")));
    }

    @GetMapping
    public ResponseEntity<List<Profil>> lister() {
        return ResponseEntity.ok(profilService.listerTous());
    }

    @PutMapping("/{profilId}/permissions/{permissionId}")
    public ResponseEntity<Profil> ajouterPermission(@PathVariable Long profilId,
                                                      @PathVariable Long permissionId) {
        return ResponseEntity.ok(profilService.ajouterPermission(profilId, permissionId));
    }

    @DeleteMapping("/{profilId}/permissions/{permissionId}")
    public ResponseEntity<Profil> retirerPermission(@PathVariable Long profilId,
                                                     @PathVariable Long permissionId) {
        return ResponseEntity.ok(profilService.retirerPermission(profilId, permissionId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        profilService.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
