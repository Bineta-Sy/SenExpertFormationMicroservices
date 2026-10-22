package com.senexpert.formationservice.service;

import com.senexpert.formationservice.dto.SoumissionDTO;
import com.senexpert.formationservice.entity.*;
import com.senexpert.formationservice.repository.CoursEnLigneRepository;
import com.senexpert.formationservice.repository.SoumissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SoumissionService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final SoumissionRepository    soumissionRepo;
    private final CoursEnLigneRepository  coursRepo;

    public SoumissionService(SoumissionRepository soumissionRepo,
                             CoursEnLigneRepository coursRepo) {
        this.soumissionRepo = soumissionRepo;
        this.coursRepo      = coursRepo;
    }

    /** Crée ou met à jour la soumission d'un apprenant pour un cours. */
    public SoumissionDTO soumettre(Long coursId, Long apprenantId, String nomApprenant,
                                   String contenuTexte, String lienUrl,
                                   String cheminFichier, String nomFichierOriginal,
                                   Long tailleFichier) {
        CoursEnLigne cours = coursRepo.findById(coursId)
            .orElseThrow(() -> new RuntimeException("Cours introuvable : " + coursId));

        // Récupère ou crée la soumission
        Soumission s = soumissionRepo
            .findByCoursIdAndApprenantId(coursId, apprenantId)
            .orElseGet(() -> {
                Soumission ns = new Soumission();
                ns.setCoursId(coursId);
                ns.setApprenantId(apprenantId);
                return ns;
            });

        s.setNomApprenant(nomApprenant);
        s.setContenuTexte(contenuTexte);
        s.setLienUrl(lienUrl);
        if (cheminFichier != null) {
            s.setCheminFichier(cheminFichier);
            s.setNomFichierOriginal(nomFichierOriginal);
            s.setTailleFichier(tailleFichier);
        }
        s.setDateSoumission(LocalDateTime.now());

        // ── Vérification de la fenêtre de soumission ─────────────────────────
        LocalDateTime now = LocalDateTime.now();

        // Avant la date d'ouverture → refus
        if (cours.getDateDebut() != null && now.isBefore(cours.getDateDebut())) {
            throw new RuntimeException(
                "SOUMISSION_AVANT_OUVERTURE:" + cours.getDateDebut().format(FMT));
        }

        // Après la date limite → refus (le formateur doit prolonger la date)
        if (cours.getDateLimite() != null && now.isAfter(cours.getDateLimite())) {
            throw new RuntimeException(
                "SOUMISSION_APRES_LIMITE:" + cours.getDateLimite().format(FMT));
        }

        s.setEnRetard(false);
        if (s.getStatut() != StatutSoumission.CORRIGE) {
            s.setStatut(StatutSoumission.EN_ATTENTE);
        }
        return toDTO(soumissionRepo.save(s));
    }

    /** Démarre un examen QCM pour un apprenant (enregistre l'heure de début). */
    public SoumissionDTO demarrerExamen(Long coursId, Long apprenantId, String nomApprenant) {
        Soumission s = soumissionRepo
            .findByCoursIdAndApprenantId(coursId, apprenantId)
            .orElseGet(() -> {
                Soumission ns = new Soumission();
                ns.setCoursId(coursId);
                ns.setApprenantId(apprenantId);
                ns.setDateSoumission(LocalDateTime.now());
                return ns;
            });
        if (s.getNomApprenant() == null) s.setNomApprenant(nomApprenant);
        if (s.getDebutExamen() == null) {
            s.setDebutExamen(LocalDateTime.now());
        }
        return toDTO(soumissionRepo.save(s));
    }

    /** Retourne toutes les soumissions d'un cours (vue formateur). */
    public List<SoumissionDTO> listerParCours(Long coursId) {
        return soumissionRepo.findByCoursIdOrderByDateSoumissionDesc(coursId)
            .stream().map(this::toDTO).collect(Collectors.toList());
    }

    /** Retourne la soumission d'un apprenant pour un cours. */
    public SoumissionDTO maSoumission(Long coursId, Long apprenantId) {
        return soumissionRepo
            .findByCoursIdAndApprenantId(coursId, apprenantId)
            .map(this::toDTO)
            .orElse(null);
    }

    /** Formateur attribue une note et un commentaire. */
    public SoumissionDTO noter(Long soumissionId, Double note, String commentaire) {
        Soumission s = soumissionRepo.findById(soumissionId)
            .orElseThrow(() -> new RuntimeException("Soumission introuvable : " + soumissionId));
        s.setNote(note);
        s.setCommentaireFormateur(commentaire);
        s.setStatut(StatutSoumission.CORRIGE);
        s.setDateCorrection(LocalDateTime.now());
        return toDTO(soumissionRepo.save(s));
    }

    /** Enregistre le chemin du fichier soumis (appelé après l'upload multipart). */
    public SoumissionDTO enregistrerFichierSoumission(Long soumissionId, String chemin,
                                                       String nomOriginal, long taille) {
        Soumission s = soumissionRepo.findById(soumissionId)
            .orElseThrow(() -> new RuntimeException("Soumission introuvable"));
        s.setCheminFichier(chemin);
        s.setNomFichierOriginal(nomOriginal);
        s.setTailleFichier(taille);
        return toDTO(soumissionRepo.save(s));
    }

    public long countEnAttente(Long coursId) {
        return soumissionRepo.countByCoursIdAndStatut(coursId, StatutSoumission.EN_ATTENTE)
             + soumissionRepo.countByCoursIdAndStatut(coursId, StatutSoumission.RENDU_TARD);
    }

    // ── Mapper ───────────────────────────────────────────────────────────────

    SoumissionDTO toDTO(Soumission s) {
        SoumissionDTO dto = new SoumissionDTO();
        dto.setId(s.getId());
        dto.setCoursId(s.getCoursId());
        dto.setApprenantId(s.getApprenantId());
        dto.setNomApprenant(s.getNomApprenant());
        dto.setCheminFichier(s.getCheminFichier());
        dto.setNomFichierOriginal(s.getNomFichierOriginal());
        dto.setTailleFichier(s.getTailleFichier());
        if (s.getCheminFichier() != null) {
            String nom = s.getCheminFichier().substring(s.getCheminFichier().lastIndexOf('/') + 1);
            dto.setUrlFichier("/api/elearning/fichiers/" + s.getCheminFichier());
        }
        dto.setContenuTexte(s.getContenuTexte());
        dto.setLienUrl(s.getLienUrl());
        if (s.getDateSoumission() != null) dto.setDateSoumission(s.getDateSoumission().format(FMT));
        if (s.getDebutExamen()    != null) dto.setDebutExamen(s.getDebutExamen().format(FMT));
        dto.setEnRetard(s.isEnRetard());
        dto.setNote(s.getNote());
        dto.setCommentaireFormateur(s.getCommentaireFormateur());
        if (s.getDateCorrection() != null) dto.setDateCorrection(s.getDateCorrection().format(FMT));
        dto.setStatut(s.getStatut() != null ? s.getStatut().name() : null);
        return dto;
    }
}
