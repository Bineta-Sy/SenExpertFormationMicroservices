package com.senexpert.formationservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.senexpert.formationservice.client.UserServiceClient;
import com.senexpert.formationservice.dto.DemandeFormationDTO;
import com.senexpert.formationservice.entity.DemandeFormation;
import com.senexpert.formationservice.entity.OffreFormation;
import com.senexpert.formationservice.entity.StatutDemande;
import com.senexpert.formationservice.exception.BusinessException;
import com.senexpert.formationservice.exception.ResourceNotFoundException;
import com.senexpert.formationservice.repository.DemandeFormationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class DemandeFormationService {

    @Autowired
    private DemandeFormationRepository demandeFormationRepository;

    @Autowired
    private OffreFormationService offreFormationService;

    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private EmailService emailService;

    @Value("${app.upload.dir:/uploads}")
    private String uploadDir;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public DemandeFormation creer(DemandeFormationDTO dto) {
        // Vérification que l'utilisateur est ACTIF dans le user_service
        try {
            userServiceClient.verifierUtilisateurActif(dto.getUtilisateurId());
        } catch (RuntimeException e) {
            throw new BusinessException(e.getMessage());
        }

        // Vérification doublon
        if (demandeFormationRepository.existsByUtilisateurIdAndOffreId(dto.getUtilisateurId(), dto.getOffreId())) {
            throw new BusinessException("Vous avez déjà soumis une demande pour cette offre");
        }

        OffreFormation offre = offreFormationService.trouverParId(dto.getOffreId());

        // Vérification de la date limite de candidature
        if (offre.getDateLimiteCandidature() != null && LocalDate.now().isAfter(offre.getDateLimiteCandidature())) {
            throw new BusinessException("La date limite de candidature pour cette offre est dépassée ("
                + offre.getDateLimiteCandidature() + ")");
        }

        // Vérification des places disponibles (EN_ATTENTE + VALIDEE comptent comme prises)
        long demandesActives = demandeFormationRepository.countByOffreIdAndStatutIn(
            offre.getId(), List.of(StatutDemande.EN_ATTENTE, StatutDemande.VALIDEE));
        if (offre.getPlaces() != null && demandesActives >= offre.getPlaces()) {
            throw new BusinessException("Plus de places disponibles pour cette offre de formation");
        }

        DemandeFormation demande = new DemandeFormation();
        demande.setUtilisateurId(dto.getUtilisateurId());
        demande.setDateDemande(LocalDate.now());
        demande.setStatut(StatutDemande.EN_ATTENTE);
        demande.setOffre(offre);

        // Sauvegarde des chemins de documents
        if (dto.getDocuments() != null && !dto.getDocuments().isEmpty()) {
            try {
                demande.setDocumentsJson(objectMapper.writeValueAsString(dto.getDocuments()));
            } catch (Exception e) {
                demande.setDocumentsJson("[]");
            }
        }

        return demandeFormationRepository.save(demande);
    }

    public DemandeFormation trouverParId(Long id) {
        return demandeFormationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Demande non trouvée : id=" + id));
    }

    public List<DemandeFormation> listerToutes() {
        return demandeFormationRepository.findAll();
    }

    public List<DemandeFormation> listerParUtilisateur(Long utilisateurId) {
        return demandeFormationRepository.findByUtilisateurId(utilisateurId);
    }

    public List<DemandeFormation> listerParOffre(Long offreId) {
        return demandeFormationRepository.findByOffreId(offreId);
    }

    /** Retourne la liste des chemins de fichiers joints à une demande. */
    public List<String> getDocuments(Long id) {
        DemandeFormation demande = demandeFormationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Demande non trouvée : id=" + id));
        String json = demande.getDocumentsJson();
        if (json == null || json.isBlank()) return Collections.emptyList();
        try {
            return objectMapper.readValue(json,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public DemandeFormation valider(Long id) {
        DemandeFormation demande = demandeFormationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Demande non trouvée : id=" + id));
        demande.setStatut(StatutDemande.VALIDEE);
        DemandeFormation saved = demandeFormationRepository.save(demande);

        // Notification email asynchrone
        String email      = userServiceClient.getEmailUtilisateur(demande.getUtilisateurId());
        String nomComplet = userServiceClient.getNomCompletUtilisateur(demande.getUtilisateurId());
        String titre      = demande.getOffre() != null ? demande.getOffre().getTitre() : "Formation";
        emailService.envoyerDemandeValidee(email, nomComplet != null ? nomComplet : "Candidat", titre);

        return saved;
    }

    public DemandeFormation rejeter(Long id) {
        DemandeFormation demande = demandeFormationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Demande non trouvee : id=" + id));
        demande.setStatut(StatutDemande.REJETEE);
        return demandeFormationRepository.save(demande);
    }

    public void annuler(Long id) {
        DemandeFormation demande = demandeFormationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Demande non trouvée : id=" + id));
        if (demande.getStatut() != StatutDemande.EN_ATTENTE) {
            throw new BusinessException("Seules les demandes en attente peuvent être annulées.");
        }
        demandeFormationRepository.deleteById(id);
    }

    // ── Fichier liste des participants ────────────────────────────────────────

    public DemandeFormation sauvegarderFichierParticipants(Long demandeId, MultipartFile fichier) throws IOException {
        DemandeFormation demande = demandeFormationRepository.findById(demandeId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande non trouvee : id=" + demandeId));

        // Supprimer l'ancien fichier s'il existe
        if (demande.getFichierParticipantsPath() != null) {
            try { java.nio.file.Files.deleteIfExists(Paths.get(demande.getFichierParticipantsPath())); }
            catch (IOException ignored) {}
        }

        // Sauvegarder le nouveau fichier
        Path dir = Paths.get(uploadDir, "participants");
        Files.createDirectories(dir);

        String ext = "";
        String originalName = fichier.getOriginalFilename();
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf('.'));
        }
        String nomStocke = "demande_" + demandeId + "_" + UUID.randomUUID() + ext;
        Path dest = dir.resolve(nomStocke);
        Files.copy(fichier.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);

        demande.setFichierParticipantsPath(dest.toString());
        demande.setFichierParticipantsNom(originalName != null ? originalName : nomStocke);
        demande.setFichierParticipantsType(fichier.getContentType());

        return demandeFormationRepository.save(demande);
    }

    public byte[] getFichierParticipants(Long demandeId) throws IOException {
        DemandeFormation demande = demandeFormationRepository.findById(demandeId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande non trouvee : id=" + demandeId));
        if (demande.getFichierParticipantsPath() == null) {
            throw new ResourceNotFoundException("Aucun fichier participants pour la demande " + demandeId);
        }
        return Files.readAllBytes(Paths.get(demande.getFichierParticipantsPath()));
    }
}
