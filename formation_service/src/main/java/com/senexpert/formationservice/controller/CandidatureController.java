package com.senexpert.formationservice.controller;

import com.senexpert.formationservice.entity.Candidature;
import com.senexpert.formationservice.service.CandidatureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/candidatures")
public class CandidatureController {

    @Autowired private CandidatureService candidatureService;

    @PostMapping
    public ResponseEntity<Candidature> soumettre(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(candidatureService.soumettre(
            Long.valueOf(body.get("utilisateurId").toString()),
            (String) body.get("nomCandidat"),
            Long.valueOf(body.get("offreId").toString())
        ));
    }

    @GetMapping
    public ResponseEntity<List<Candidature>> lister() {
        return ResponseEntity.ok(candidatureService.listerTous());
    }

    @GetMapping("/utilisateur/{id}")
    public ResponseEntity<List<Candidature>> parUtilisateur(@PathVariable Long id) {
        return ResponseEntity.ok(candidatureService.parUtilisateur(id));
    }

    /** Candidatures d'une offre spécifique (pour le prestataire propriétaire) */
    @GetMapping("/offre/{offreId}")
    public ResponseEntity<List<Candidature>> parOffre(@PathVariable Long offreId) {
        return ResponseEntity.ok(candidatureService.parOffre(offreId));
    }

    @PutMapping("/accepter/{id}")
    public ResponseEntity<Candidature> accepter(@PathVariable Long id) {
        return ResponseEntity.ok(candidatureService.accepter(id));
    }

    @PutMapping("/refuser/{id}")
    public ResponseEntity<Candidature> refuser(@PathVariable Long id) {
        return ResponseEntity.ok(candidatureService.refuser(id));
    }

    /**
     * Upload du fichier liste des participants pour une candidature d'institution.
     * Formats acceptés : .docx, .xlsx, .pdf, .csv, .odt, .ods, etc.
     */
    @PostMapping("/{id}/upload-participants")
    public ResponseEntity<?> uploadParticipants(
            @PathVariable Long id,
            @RequestParam("fichier") MultipartFile fichier) {
        try {
            Candidature c = candidatureService.sauvegarderFichierParticipants(id, fichier);
            return ResponseEntity.ok(Map.of(
                "message", "Fichier uploadé avec succès",
                "nomFichier", c.getFichierParticipantsNom(),
                "type", c.getFichierParticipantsType() != null ? c.getFichierParticipantsType() : ""
            ));
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("erreur", "Échec de l'upload : " + e.getMessage()));
        }
    }

    /**
     * Téléchargement du fichier liste des participants d'une candidature.
     */
    @GetMapping("/{id}/participants-file")
    public ResponseEntity<byte[]> downloadParticipants(@PathVariable Long id) {
        try {
            Candidature c = candidatureService.trouverParId(id);
            byte[] data = candidatureService.getFichierParticipants(id);
            String nom = c.getFichierParticipantsNom() != null ? c.getFichierParticipantsNom() : "participants.bin";
            String type = c.getFichierParticipantsType() != null ? c.getFichierParticipantsType() : "application/octet-stream";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nom + "\"")
                    .contentType(MediaType.parseMediaType(type))
                    .body(data);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Export CSV de toutes les candidatures (Admin).
     * Filtre optionnel par statut : ?statut=ACCEPTEE
     */
    @GetMapping("/export-csv")
    public ResponseEntity<byte[]> exporterCSV(@RequestParam(required = false) String statut) {
        List<Candidature> liste = candidatureService.listerTous();
        StringBuilder csv = new StringBuilder();
        csv.append("ID;Candidat;UtilisateurId;Offre;DateCreation;Statut;Commentaire\n");
        for (Candidature c : liste) {
            if (statut != null && !statut.isBlank() && !statut.equals(c.getStatut().name())) continue;
            csv.append(c.getId()).append(";")
               .append(c.getNomCandidat() != null ? c.getNomCandidat() : "").append(";")
               .append(c.getUtilisateurId()).append(";")
               .append(c.getOffre() != null ? c.getOffre().getTitre() : "").append(";")
               .append(c.getDateCreation()).append(";")
               .append(c.getStatut()).append(";")
               .append(c.getCommentaire() != null ? c.getCommentaire() : "").append("\n");
        }
        byte[] bytes = csv.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"candidatures.csv\"")
            .contentType(MediaType.parseMediaType("text/csv"))
            .body(bytes);
    }

    /**
     * Export CSV du rapport d'activité global (Admin).
     * Retourne stats groupées par offre.
     */
    @GetMapping("/rapport-csv")
    public ResponseEntity<byte[]> exporterRapport() {
        List<Candidature> toutes = candidatureService.listerTous();
        // grouper par offre
        java.util.Map<String, long[]> stats = new java.util.LinkedHashMap<>();
        for (Candidature c : toutes) {
            String offre = c.getOffre() != null ? c.getOffre().getTitre() : "Inconnue";
            stats.putIfAbsent(offre, new long[]{0, 0, 0, 0});
            long[] s = stats.get(offre);
            s[0]++; // total
            if ("ACCEPTEE".equals(c.getStatut().name())) s[1]++;
            else if ("REFUSEE".equals(c.getStatut().name())) s[2]++;
            else s[3]++;
        }
        StringBuilder csv = new StringBuilder();
        csv.append("Offre;Total;Acceptées;Refusées;En attente\n");
        stats.forEach((offre, s) ->
            csv.append(offre).append(";").append(s[0]).append(";")
               .append(s[1]).append(";").append(s[2]).append(";").append(s[3]).append("\n"));
        byte[] bytes = csv.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"rapport-activite.csv\"")
            .contentType(MediaType.parseMediaType("text/csv"))
            .body(bytes);
    }
}
