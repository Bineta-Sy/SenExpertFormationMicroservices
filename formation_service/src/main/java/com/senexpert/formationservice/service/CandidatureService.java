package com.senexpert.formationservice.service;

import com.senexpert.formationservice.client.UserServiceClient;
import com.senexpert.formationservice.entity.Candidature;
import com.senexpert.formationservice.entity.StatutCandidature;
import com.senexpert.formationservice.exception.ResourceNotFoundException;
import com.senexpert.formationservice.repository.CandidatureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class CandidatureService {

    @Autowired private CandidatureRepository candidatureRepository;
    @Autowired private OffreFormationService offreService;
    @Autowired private UserServiceClient userServiceClient;
    @Autowired private EmailService emailService;

    @Value("${app.upload.dir:/uploads}")
    private String uploadDir;

    public Candidature soumettre(Long utilisateurId, String nomCandidat, Long offreId) {
        Candidature c = new Candidature();
        c.setUtilisateurId(utilisateurId);
        c.setNomCandidat(nomCandidat);
        c.setOffre(offreService.trouverParId(offreId));
        return candidatureRepository.save(c);
    }

    public List<Candidature> listerTous() {
        return candidatureRepository.findAll();
    }

    public List<Candidature> parUtilisateur(Long utilisateurId) {
        return candidatureRepository.findByUtilisateurId(utilisateurId);
    }

    public List<Candidature> parOffre(Long offreId) {
        return candidatureRepository.findByOffre_Id(offreId);
    }

    public Candidature trouverParId(Long id) {
        return candidatureRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Candidature non trouvée : id=" + id));
    }

    public Candidature accepter(Long id) {
        Candidature c = trouverParId(id);
        c.setStatut(StatutCandidature.ACCEPTEE);
        Candidature saved = candidatureRepository.save(c);

        // Notification email asynchrone
        String email     = userServiceClient.getEmailUtilisateur(c.getUtilisateurId());
        String nomComplet = c.getNomCandidat() != null ? c.getNomCandidat()
                : userServiceClient.getNomCompletUtilisateur(c.getUtilisateurId());
        String titreOffre = c.getOffre() != null ? c.getOffre().getTitre() : "Formation";
        emailService.envoyerCandidatureAcceptee(email, nomComplet, titreOffre);

        return saved;
    }

    // ── Fichier liste des participants ────────────────────────────────────────

    /**
     * Sauvegarde le fichier participants sur le filesystem et met à jour la candidature.
     * Formats acceptés : Word (.doc/.docx), Excel (.xls/.xlsx), PDF, CSV, images, etc.
     */
    public Candidature sauvegarderFichierParticipants(Long candidatureId, MultipartFile fichier) throws IOException {
        Candidature c = trouverParId(candidatureId);

        // Créer le répertoire si nécessaire
        Path dir = Paths.get(uploadDir, "participants");
        Files.createDirectories(dir);

        // Nom unique pour éviter les collisions
        String extension = "";
        String nomOriginal = fichier.getOriginalFilename() != null ? fichier.getOriginalFilename() : "fichier";
        int lastDot = nomOriginal.lastIndexOf('.');
        if (lastDot >= 0) extension = nomOriginal.substring(lastDot);
        String nomFichier = "cand_" + candidatureId + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;

        // Supprimer l'ancien fichier s'il existe
        if (c.getFichierParticipantsPath() != null) {
            try { Files.deleteIfExists(Paths.get(c.getFichierParticipantsPath())); } catch (Exception ignored) {}
        }

        // Sauvegarder le nouveau fichier
        Path destination = dir.resolve(nomFichier);
        Files.copy(fichier.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

        c.setFichierParticipantsPath(destination.toString());
        c.setFichierParticipantsNom(nomOriginal);
        c.setFichierParticipantsType(fichier.getContentType());
        return candidatureRepository.save(c);
    }

    /**
     * Retourne les bytes du fichier participants d'une candidature.
     */
    public byte[] getFichierParticipants(Long candidatureId) throws IOException {
        Candidature c = trouverParId(candidatureId);
        if (c.getFichierParticipantsPath() == null) {
            throw new ResourceNotFoundException("Aucun fichier participants pour la candidature " + candidatureId);
        }
        return Files.readAllBytes(Paths.get(c.getFichierParticipantsPath()));
    }

    public Candidature refuser(Long id) {
        Candidature c = trouverParId(id);
        c.setStatut(StatutCandidature.REFUSEE);
        Candidature saved = candidatureRepository.save(c);

        // Notification email asynchrone
        String email      = userServiceClient.getEmailUtilisateur(c.getUtilisateurId());
        String nomComplet = c.getNomCandidat() != null ? c.getNomCandidat()
                : userServiceClient.getNomCompletUtilisateur(c.getUtilisateurId());
        String titreOffre = c.getOffre() != null ? c.getOffre().getTitre() : "Formation";
        emailService.envoyerCandidatureRefusee(email, nomComplet, titreOffre);

        return saved;
    }
}
