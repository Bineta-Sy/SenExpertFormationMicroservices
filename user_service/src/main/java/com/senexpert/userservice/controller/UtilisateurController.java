package com.senexpert.userservice.controller;

import com.senexpert.userservice.dto.RegisterRequest;
import com.senexpert.userservice.entity.Utilisateur;
import com.senexpert.userservice.service.InactiviteScheduler;
import com.senexpert.userservice.service.UtilisateurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/utilisateurs")
public class UtilisateurController {

    @Autowired private UtilisateurService utilisateurService;
    @Autowired private InactiviteScheduler inactiviteScheduler;

    @PostMapping("/register")
    public ResponseEntity<Utilisateur> inscrire(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(utilisateurService.inscrire(request));
    }

    @GetMapping
    public ResponseEntity<List<Utilisateur>> listerTous() {
        return ResponseEntity.ok(utilisateurService.listerTous());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Utilisateur> trouverParId(@PathVariable Long id) {
        return ResponseEntity.ok(utilisateurService.trouverParId(id));
    }

    /**
     * Endpoint interne sans auth — utilisé par formation_service pour
     * vérifier le statut d'un utilisateur sans token de service.
     * Retourne {"statut":"ACTIF"} ou {"statut":"EN_ATTENTE"} etc.
     */
    @GetMapping("/{id}/statut")
    public ResponseEntity<Map<String, String>> statut(@PathVariable Long id) {
        Utilisateur u = utilisateurService.trouverParId(id);
        return ResponseEntity.ok(Map.of("statut", u.getStatut().name()));
    }

    @PutMapping("/valider/{id}")
    public ResponseEntity<Utilisateur> valider(@PathVariable Long id) {
        return ResponseEntity.ok(utilisateurService.valider(id));
    }

    /** Change le rôle d'un utilisateur (Admin) */
    @PutMapping("/role/{id}")
    public ResponseEntity<Utilisateur> changerRole(@PathVariable Long id,
                                                    @RequestParam String role) {
        return ResponseEntity.ok(utilisateurService.changerRole(id, role));
    }

    /** Désactive un compte */
    @PutMapping("/desactiver/{id}")
    public ResponseEntity<Utilisateur> desactiver(@PathVariable Long id) {
        return ResponseEntity.ok(utilisateurService.desactiver(id));
    }

    /** Réactive un compte */
    @PutMapping("/reactiver/{id}")
    public ResponseEntity<Utilisateur> reactiver(@PathVariable Long id) {
        return ResponseEntity.ok(utilisateurService.reactiver(id));
    }

    /** Supprime un compte */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        utilisateurService.supprimer(id);
        return ResponseEntity.noContent().build();
    }

    /** Crée un compte ADMIN (réservé à un admin connecté) */
    @PostMapping("/admin")
    public ResponseEntity<Utilisateur> creerAdmin(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(utilisateurService.creerAdmin(request));
    }

    /** Assigne un profil à un utilisateur */
    @PutMapping("/profil/{id}")
    public ResponseEntity<Utilisateur> assignerProfil(@PathVariable Long id,
                                                       @RequestParam Long profilId) {
        return ResponseEntity.ok(utilisateurService.assignerProfil(id, profilId));
    }

    /** Recherche utilisateurs par nom, prenom ou email */
    @GetMapping("/recherche")
    public ResponseEntity<List<Utilisateur>> rechercher(@RequestParam String q) {
        return ResponseEntity.ok(utilisateurService.rechercher(q));
    }

    // Retourne l'utilisateur connecté (identifié par le token JWT)
    @GetMapping("/me")
    public ResponseEntity<Utilisateur> moi(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(utilisateurService.trouverParEmail(email));
    }

    /**
     * Modifie le profil de l'utilisateur connecté (nom, prénom, téléphone).
     */
    @PutMapping("/modifier-profil/{id}")
    public ResponseEntity<Utilisateur> modifierProfil(
            @PathVariable Long id,
            @RequestParam(required = false) String nom,
            @RequestParam(required = false) String prenom,
            @RequestParam(required = false) String telephone) {
        return ResponseEntity.ok(utilisateurService.modifierProfil(id, nom, prenom, telephone));
    }

    /**
     * Changement de mot de passe (auto) — vérifie l'ancien mot de passe.
     */
    @PutMapping("/changer-mdp/{id}")
    public ResponseEntity<Map<String, String>> changerMotDePasse(
            @PathVariable Long id,
            @RequestParam String ancienMdp,
            @RequestParam String nouveauMdp) {
        utilisateurService.changerMotDePasse(id, ancienMdp, nouveauMdp);
        return ResponseEntity.ok(Map.of("message", "Mot de passe modifié avec succès."));
    }

    /**
     * Réinitialise le mot de passe d'un utilisateur (Admin).
     * Retourne le nouveau mot de passe temporaire.
     */
    @PutMapping("/reset-password/{id}")
    public ResponseEntity<Map<String, String>> reinitialiserMotDePasse(@PathVariable Long id) {
        String tempPassword = utilisateurService.reinitialiserMotDePasse(id);
        return ResponseEntity.ok(Map.of("motDePasseTemporaire", tempPassword));
    }

    /**
     * Import en masse via fichier CSV (Admin).
     * Format CSV : nom;prenom;email;role (séparateur point-virgule, 1 ligne d'entête)
     */
    @PostMapping("/import-csv")
    public ResponseEntity<Map<String, Object>> importerCSV(@RequestParam("file") MultipartFile file) {
        List<RegisterRequest> requests = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            boolean first = true;
            while ((line = reader.readLine()) != null) {
                if (first) { first = false; continue; } // skip header
                String[] parts = line.split(";");
                if (parts.length < 3) continue;
                RegisterRequest req = new RegisterRequest();
                req.setNom(parts[0].trim());
                req.setPrenom(parts[1].trim());
                req.setEmail(parts[2].trim());
                req.setRole(parts.length >= 4 ? parts[3].trim() : "PARTICULIER");
                req.setMotDePasse(parts.length >= 5 ? parts[4].trim() : "Changeme123!");
                requests.add(req);
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", "Fichier invalide : " + e.getMessage()));
        }
        int count = utilisateurService.importerEnMasse(requests);
        return ResponseEntity.ok(Map.of("crees", count, "total", requests.size()));
    }

    /**
     * Expire manuellement les comptes inactifs depuis N jours (Admin).
     * POST /api/utilisateurs/expirer-inactifs?jours=90
     */
    @PostMapping("/expirer-inactifs")
    public ResponseEntity<Map<String, Object>> expirerInactifs(
            @RequestParam(defaultValue = "90") int jours) {
        int expires = inactiviteScheduler.expirerComptesInactifs(jours);
        return ResponseEntity.ok(Map.of("expires", expires, "jours", jours));
    }

    /** GET /api/utilisateurs/{id}/permissions — liste des IDs de permissions directes */
    @GetMapping("/{id}/permissions")
    public ResponseEntity<List<Long>> getPermissions(@PathVariable Long id) {
        return ResponseEntity.ok(utilisateurService.getPermissionsDirectesIds(id));
    }

    /** GET /api/utilisateurs/{id}/permissions/effectives — codes fusionnés (profil + directes) */
    @GetMapping("/{id}/permissions/effectives")
    public ResponseEntity<List<String>> getPermissionsEffectives(@PathVariable Long id) {
        return ResponseEntity.ok(utilisateurService.getPermissionsEffectivesCodes(id));
    }

    /** PUT /api/utilisateurs/{id}/permissions — remplace les permissions directes */
    @PutMapping("/{id}/permissions")
    public ResponseEntity<Void> setPermissions(@PathVariable Long id,
                                               @RequestBody List<Long> permissionIds) {
        utilisateurService.setPermissionsDirectes(id, permissionIds);
        return ResponseEntity.ok().build();
    }
}