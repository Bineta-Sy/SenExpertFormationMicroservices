package com.senexpert.formationservice.controller;

import com.senexpert.formationservice.dto.DemandeFormationDTO;
import com.senexpert.formationservice.entity.DemandeFormation;
import com.senexpert.formationservice.service.DemandeFormationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/demandes")
public class DemandeFormationController {

    @Autowired
    private DemandeFormationService demandeFormationService;

    @Value("${app.upload.dir:uploads/demandes}")
    private String uploadDir;

    @PostMapping
    public ResponseEntity<DemandeFormation> creer(@RequestBody DemandeFormationDTO dto) {
        return ResponseEntity.ok(demandeFormationService.creer(dto));
    }

    /**
     * Upload de fichiers (CV, diplôme, lettre…) avant soumission de la demande.
     * Retourne la liste des chemins enregistrés côté serveur.
     */
    @PostMapping("/upload")
    public ResponseEntity<List<String>> uploadDocuments(
            @RequestParam("utilisateurId") Long utilisateurId,
            @RequestParam("fichiers") List<MultipartFile> fichiers) throws IOException {

        Path dir = Paths.get(uploadDir, utilisateurId.toString());
        Files.createDirectories(dir);

        List<String> chemins = new ArrayList<>();
        for (MultipartFile fichier : fichiers) {
            if (fichier.isEmpty()) continue;
            String nomFichier = UUID.randomUUID() + "_" + fichier.getOriginalFilename();
            Path dest = dir.resolve(nomFichier);
            fichier.transferTo(dest);
            chemins.add(utilisateurId + "/" + nomFichier);
        }
        return ResponseEntity.ok(chemins);
    }

    /** Documents joints à une demande (pour l'admin). */
    @GetMapping("/{id}/documents")
    public ResponseEntity<List<String>> getDocuments(@PathVariable Long id) {
        return ResponseEntity.ok(demandeFormationService.getDocuments(id));
    }

    @GetMapping
    public ResponseEntity<List<DemandeFormation>> listerToutes() {
        return ResponseEntity.ok(demandeFormationService.listerToutes());
    }

    @GetMapping("/utilisateur/{utilisateurId}")
    public ResponseEntity<List<DemandeFormation>> listerParUtilisateur(@PathVariable Long utilisateurId) {
        return ResponseEntity.ok(demandeFormationService.listerParUtilisateur(utilisateurId));
    }

    /** Candidats d'une offre (Admin) */
    @GetMapping("/offre/{offreId}")
    public ResponseEntity<List<DemandeFormation>> listerParOffre(@PathVariable Long offreId) {
        return ResponseEntity.ok(demandeFormationService.listerParOffre(offreId));
    }

    @PutMapping("/valider/{id}")
    public ResponseEntity<DemandeFormation> valider(@PathVariable Long id) {
        return ResponseEntity.ok(demandeFormationService.valider(id));
    }

    @PutMapping("/rejeter/{id}")
    public ResponseEntity<DemandeFormation> rejeter(@PathVariable Long id) {
        return ResponseEntity.ok(demandeFormationService.rejeter(id));
    }

    /** Annuler une demande EN_ATTENTE */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> annuler(@PathVariable Long id) {
        demandeFormationService.annuler(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Upload du fichier liste des participants pour une demande d'institution.
     * Formats acceptes : .docx, .xlsx, .pdf, .csv, .odt, .ods, etc.
     */
    @PostMapping("/{id}/upload-participants")
    public ResponseEntity<?> uploadParticipants(
            @PathVariable Long id,
            @RequestParam("fichier") MultipartFile fichier) {
        try {
            DemandeFormation d = demandeFormationService.sauvegarderFichierParticipants(id, fichier);
            return ResponseEntity.ok(Map.of(
                "message", "Fichier uploade avec succes",
                "nomFichier", d.getFichierParticipantsNom(),
                "type", d.getFichierParticipantsType() != null ? d.getFichierParticipantsType() : ""
            ));
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("erreur", "Echec de l'upload : " + e.getMessage()));
        }
    }

    /**
     * Telechargement du fichier liste des participants d'une demande.
     */
    @GetMapping("/{id}/participants-file")
    public ResponseEntity<byte[]> downloadParticipants(@PathVariable Long id) {
        try {
            DemandeFormation d    = demandeFormationService.trouverParId(id);
            byte[]           data = demandeFormationService.getFichierParticipants(id);
            String nom  = d.getFichierParticipantsNom()  != null ? d.getFichierParticipantsNom()  : "participants.bin";
            String type = d.getFichierParticipantsType() != null ? d.getFichierParticipantsType() : "application/octet-stream";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nom + "\"")
                    .contentType(MediaType.parseMediaType(type))
                    .body(data);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
