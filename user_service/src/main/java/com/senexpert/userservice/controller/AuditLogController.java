package com.senexpert.userservice.controller;

import com.senexpert.userservice.entity.AuditLog;
import com.senexpert.userservice.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/audit")
public class AuditLogController {

    @Autowired
    private AuditLogService auditLogService;

    /**
     * Enregistre une entrée d'audit.
     * Body JSON : { action, entite, detail, adminEmail, niveau }
     */
    @PostMapping
    public ResponseEntity<AuditLog> enregistrer(@RequestBody Map<String, String> body) {
        AuditLog log = auditLogService.enregistrer(
            body.get("action"),
            body.get("entite"),
            body.get("detail"),
            body.get("adminEmail"),
            body.getOrDefault("niveau", "INFO")
        );
        return ResponseEntity.ok(log);
    }

    /** Liste les N entrées les plus récentes (?limit=200) */
    @GetMapping
    public ResponseEntity<List<AuditLog>> lister(
            @RequestParam(defaultValue = "200") int limit) {
        return ResponseEntity.ok(auditLogService.listerRecents(limit));
    }

    /** Filtre par admin */
    @GetMapping("/admin/{email}")
    public ResponseEntity<List<AuditLog>> parAdmin(@PathVariable String email) {
        return ResponseEntity.ok(auditLogService.listerParAdmin(email));
    }

    /** Filtre par niveau */
    @GetMapping("/niveau/{niveau}")
    public ResponseEntity<List<AuditLog>> parNiveau(@PathVariable String niveau) {
        return ResponseEntity.ok(auditLogService.listerParNiveau(niveau));
    }

    /** Nombre total d'entrées */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> compter() {
        return ResponseEntity.ok(Map.of("total", auditLogService.compter()));
    }
}
