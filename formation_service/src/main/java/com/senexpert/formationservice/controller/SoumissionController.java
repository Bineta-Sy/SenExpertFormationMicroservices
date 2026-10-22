package com.senexpert.formationservice.controller;

import com.senexpert.formationservice.dto.SoumissionDTO;
import com.senexpert.formationservice.service.SoumissionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/elearning")
public class SoumissionController {

    private final SoumissionService service;

    @Value("${app.upload.dir:/uploads}")
    private String uploadDir;

    private static final List<String> EXT_AUTORISEES = List.of(
        ".pdf",".doc",".docx",".xls",".xlsx",".ppt",".pptx",
        ".mp4",".webm",".mp3",".jpg",".jpeg",".png",".gif",".zip",".txt"
    );

    public SoumissionController(SoumissionService service) {
        this.service = service;
    }

    // ── APPRENANT : soumettre (texte + lien, sans fichier) ──────────────────

    @PostMapping("/cours/{coursId}/soumettre")
    public ResponseEntity<?> soumettre(
            @PathVariable Long coursId,
            @RequestParam Long apprenantId,
            @RequestParam(defaultValue = "") String nomApprenant,
            @RequestParam(required = false) String contenuTexte,
            @RequestParam(required = false) String lienUrl) {
        SoumissionDTO dto = service.soumettre(coursId, apprenantId, nomApprenant,
                contenuTexte, lienUrl, null, null, null);
        return ResponseEntity.ok(dto);
    }

    // ── APPRENANT : soumettre avec fichier (multipart) ───────────────────────

    @PostMapping(value = "/cours/{coursId}/soumettre/fichier",
                 consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> soumettreFichier(
            @PathVariable Long coursId,
            @RequestParam Long apprenantId,
            @RequestParam(defaultValue = "") String nomApprenant,
            @RequestParam(required = false) String contenuTexte,
            @RequestParam(required = false) String lienUrl,
            @RequestParam("fichier") MultipartFile fichier) {

        if (fichier.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("erreur", "Fichier vide"));
        }

        String nomOriginal = fichier.getOriginalFilename() != null
                ? fichier.getOriginalFilename() : "fichier";
        String ext = nomOriginal.contains(".")
                ? nomOriginal.substring(nomOriginal.lastIndexOf('.')).toLowerCase() : "";
        if (!EXT_AUTORISEES.contains(ext)) {
            return ResponseEntity.badRequest().body(Map.of("erreur", "Extension non autorisée : " + ext));
        }

        try {
            Path dossier = Paths.get(uploadDir, "soumissions", String.valueOf(coursId));
            Files.createDirectories(dossier);

            String nomFichier = UUID.randomUUID().toString().substring(0, 8) + "_" + nomOriginal;
            Path cible = dossier.resolve(nomFichier);
            Files.copy(fichier.getInputStream(), cible, StandardCopyOption.REPLACE_EXISTING);

            String chemin = "soumissions/" + coursId + "/" + nomFichier;
            SoumissionDTO dto = service.soumettre(coursId, apprenantId, nomApprenant,
                    contenuTexte, lienUrl, chemin, nomOriginal, fichier.getSize());
            return ResponseEntity.ok(dto);

        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("erreur", "Échec de l'upload : " + e.getMessage()));
        }
    }

    // ── APPRENANT : démarrer un examen QCM (enregistre l'heure de début) ────

    @PostMapping("/cours/{coursId}/demarrer-examen")
    public ResponseEntity<SoumissionDTO> demarrerExamen(
            @PathVariable Long coursId,
            @RequestParam Long apprenantId,
            @RequestParam(defaultValue = "") String nomApprenant) {
        return ResponseEntity.ok(service.demarrerExamen(coursId, apprenantId, nomApprenant));
    }

    // ── APPRENANT : voir sa propre soumission ────────────────────────────────

    @GetMapping("/cours/{coursId}/ma-soumission")
    public ResponseEntity<SoumissionDTO> maSoumission(
            @PathVariable Long coursId,
            @RequestParam Long apprenantId) {
        SoumissionDTO dto = service.maSoumission(coursId, apprenantId);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    // ── FORMATEUR : lister toutes les soumissions d'un cours ────────────────

    @GetMapping("/cours/{coursId}/soumissions")
    public ResponseEntity<List<SoumissionDTO>> soumissionsParCours(
            @PathVariable Long coursId) {
        return ResponseEntity.ok(service.listerParCours(coursId));
    }

    // ── FORMATEUR : nombre de soumissions en attente de correction ───────────

    @GetMapping("/cours/{coursId}/soumissions/count")
    public ResponseEntity<Map<String, Long>> countEnAttente(@PathVariable Long coursId) {
        return ResponseEntity.ok(Map.of("enAttente", service.countEnAttente(coursId)));
    }

    // ── FORMATEUR : noter une soumission ────────────────────────────────────

    @PutMapping("/soumissions/{id}/noter")
    public ResponseEntity<SoumissionDTO> noter(
            @PathVariable Long id,
            @RequestParam Double note,
            @RequestParam(required = false, defaultValue = "") String commentaire) {
        return ResponseEntity.ok(service.noter(id, note, commentaire));
    }
}
