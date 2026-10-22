package com.senexpert.formationservice.controller;

import com.senexpert.formationservice.dto.*;
import com.senexpert.formationservice.service.CoursEnLigneService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/elearning")
public class CoursEnLigneController {

    private final CoursEnLigneService service;

    @Value("${app.upload.dir:/uploads}")
    private String uploadDir;

    public CoursEnLigneController(CoursEnLigneService service) {
        this.service = service;
    }

    // ── COURS ────────────────────────────────────────────────────────────────

    /** Récupère les cours d'une offre (admin/formateur : tous ; apprenant : publiés). */
    @GetMapping("/cours/offre/{offreId}")
    public ResponseEntity<List<CoursEnLigneDTO>> coursParOffre(
            @PathVariable Long offreId,
            @RequestParam(defaultValue = "false") boolean admin) {
        return ResponseEntity.ok(service.listeParOffre(offreId, admin));
    }

    @PostMapping("/cours")
    public ResponseEntity<CoursEnLigneDTO> creerCours(@RequestBody CoursEnLigneDTO dto) {
        return ResponseEntity.ok(service.creer(dto));
    }

    @PutMapping("/cours/{id}")
    public ResponseEntity<CoursEnLigneDTO> modifierCours(
            @PathVariable Long id, @RequestBody CoursEnLigneDTO dto) {
        return ResponseEntity.ok(service.modifier(id, dto));
    }

    @DeleteMapping("/cours/{id}")
    public ResponseEntity<Void> supprimerCours(@PathVariable Long id) {
        service.supprimer(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/cours/{id}/publier")
    public ResponseEntity<CoursEnLigneDTO> publierCours(
            @PathVariable Long id,
            @RequestParam(defaultValue = "true") boolean publie) {
        return ResponseEntity.ok(service.publier(id, publie));
    }

    // ── QUIZ ─────────────────────────────────────────────────────────────────

    @GetMapping("/quiz/{coursId}/questions")
    public ResponseEntity<List<QuizQuestionDTO>> questions(@PathVariable Long coursId) {
        return ResponseEntity.ok(service.questionsParCours(coursId));
    }

    @PostMapping("/quiz/question")
    public ResponseEntity<QuizQuestionDTO> ajouterQuestion(@RequestBody QuizQuestionDTO dto) {
        return ResponseEntity.ok(service.ajouterQuestion(dto));
    }

    @DeleteMapping("/quiz/question/{id}")
    public ResponseEntity<Void> supprimerQuestion(@PathVariable Long id) {
        service.supprimerQuestion(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/quiz/{coursId}/soumettre")
    public ResponseEntity<ProgressionDTO> soumettreQuiz(
            @PathVariable Long coursId,
            @RequestParam Long utilisateurId,
            @RequestBody List<ReponseQuizDTO> reponses) {
        return ResponseEntity.ok(service.soumettreQuiz(utilisateurId, coursId, reponses));
    }

    // ── PROGRESSION ──────────────────────────────────────────────────────────

    @PostMapping("/progression/{coursId}/acceder")
    public ResponseEntity<ProgressionDTO> acceder(
            @PathVariable Long coursId,
            @RequestParam Long utilisateurId) {
        return ResponseEntity.ok(service.acceder(utilisateurId, coursId));
    }

    @PostMapping("/progression/{coursId}/completer")
    public ResponseEntity<ProgressionDTO> completer(
            @PathVariable Long coursId,
            @RequestParam Long utilisateurId) {
        return ResponseEntity.ok(service.completer(utilisateurId, coursId));
    }

    @GetMapping("/progression/offre/{offreId}")
    public ResponseEntity<List<ProgressionDTO>> progressionParOffre(
            @PathVariable Long offreId,
            @RequestParam Long utilisateurId) {
        return ResponseEntity.ok(service.progressionParOffre(utilisateurId, offreId));
    }

    // ── UPLOAD FICHIER ───────────────────────────────────────────────────────

    /**
     * Upload un fichier (PDF, Word, Excel, vidéo, image…) pour un cours.
     * Stockage local dans ${app.upload.dir}/cours/{coursId}/
     * Types acceptés : pdf, doc, docx, xls, xlsx, ppt, pptx, mp4, mp3, jpg, png, zip
     */
    @PostMapping(value = "/cours/{id}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadFichier(
            @PathVariable Long id,
            @RequestParam("fichier") MultipartFile fichier) {

        if (fichier.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("erreur", "Fichier vide"));
        }

        // Vérification extension
        String nomOriginal = fichier.getOriginalFilename() != null
                ? fichier.getOriginalFilename() : "fichier";
        String ext = nomOriginal.contains(".")
                ? nomOriginal.substring(nomOriginal.lastIndexOf('.')).toLowerCase() : "";
        List<String> extAutorisees = List.of(
            ".pdf",".doc",".docx",".xls",".xlsx",".ppt",".pptx",
            ".mp4",".webm",".mp3",".jpg",".jpeg",".png",".gif",".zip",".txt"
        );
        if (!extAutorisees.contains(ext)) {
            return ResponseEntity.badRequest().body(Map.of("erreur", "Extension non autorisée : " + ext));
        }

        try {
            // Création du répertoire
            Path dossier = Paths.get(uploadDir, "cours", String.valueOf(id));
            Files.createDirectories(dossier);

            // Nom unique pour éviter les collisions
            String nomFichier = UUID.randomUUID().toString().substring(0, 8) + "_" + nomOriginal;
            Path cible = dossier.resolve(nomFichier);
            Files.copy(fichier.getInputStream(), cible, StandardCopyOption.REPLACE_EXISTING);

            // Chemin relatif stocké en base
            String chemin = "cours/" + id + "/" + nomFichier;
            CoursEnLigneDTO dto = service.enregistrerFichier(id, chemin, nomOriginal, fichier.getSize());
            return ResponseEntity.ok(dto);

        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("erreur", "Échec de l'upload : " + e.getMessage()));
        }
    }

    /**
     * Sert un fichier uploadé.
     * GET /api/elearning/fichiers/cours/42/abc123_rapport.pdf
     */
    @GetMapping("/fichiers/**")
    public ResponseEntity<Resource> servirFichier(HttpServletRequest request) {
        try {
            String chemin = request.getRequestURI()
                    .substring(request.getRequestURI().indexOf("/fichiers/") + "/fichiers/".length());
            Path fichierPath = Paths.get(uploadDir).resolve(chemin).normalize();

            // Sécurité : on reste dans uploadDir
            if (!fichierPath.toAbsolutePath().startsWith(Paths.get(uploadDir).toAbsolutePath())) {
                return ResponseEntity.badRequest().build();
            }

            Resource resource = new UrlResource(fichierPath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = Files.probeContentType(fichierPath);
            if (contentType == null) contentType = "application/octet-stream";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ── Santé ────────────────────────────────────────────────────────────────

    @GetMapping("/ping")
    public ResponseEntity<Map<String,String>> ping() {
        return ResponseEntity.ok(Map.of("status", "ok", "module", "elearning"));
    }
}
