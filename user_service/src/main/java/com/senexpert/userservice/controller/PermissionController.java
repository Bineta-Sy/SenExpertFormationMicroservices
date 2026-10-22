package com.senexpert.userservice.controller;

import com.senexpert.userservice.entity.Permission;
import com.senexpert.userservice.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/permissions")
public class PermissionController {

    @Autowired private PermissionService permissionService;

    @PostMapping
    public ResponseEntity<Permission> creer(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(permissionService.creer(
            (String) body.get("libelle"),
            (String) body.get("code"),
            (String) body.get("niveau"),
            Long.valueOf(body.get("brancheId").toString())
        ));
    }

    @GetMapping
    public ResponseEntity<List<Permission>> lister() {
        return ResponseEntity.ok(permissionService.listerTous());
    }

    @GetMapping("/recherche")
    public ResponseEntity<List<Permission>> rechercher(@RequestParam String q) {
        return ResponseEntity.ok(permissionService.rechercher(q));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        permissionService.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
