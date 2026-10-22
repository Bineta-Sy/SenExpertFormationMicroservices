package com.senexpert.frontend.bean;

import com.senexpert.frontend.client.FormationServiceClient;
import com.senexpert.frontend.model.*;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Bean pour le dashboard apprenant (particulier).
 * Agrège les formations validées avec progression et résultats.
 */
@Named
@ViewScoped
public class ParticularDashboardBean implements Serializable {

    @Inject private FormationServiceClient formationClient;
    @Inject private SessionBean session;
    @Inject private OffreBean   offreBean;

    private List<FormationResume> formations = new ArrayList<>();
    private boolean chargee = false;

    @PostConstruct
    public void init() {
        // Chargement différé : appelé explicitement par la page
    }

    /** Charge les données (idempotent). */
    public void charger() {
        if (chargee || session.getUtilisateurId() == null) return;
        chargee = true;
        formations = new ArrayList<>();

        for (DemandeFormation d : getDemandesValidees()) {
            if (d.getOffre() == null) continue;
            Long offreId = d.getOffre().getId();
            try {
                List<CoursEnLigneFront> cours = formationClient.coursParOffre(offreId, false, session.getToken());
                List<ProgressionFront> progs  = formationClient.progressionParOffre(offreId,
                        session.getUtilisateurId(), session.getToken());
                formations.add(new FormationResume(d, cours != null ? cours : List.of(),
                                                       progs  != null ? progs  : List.of()));
            } catch (Exception ignored) {
                formations.add(new FormationResume(d, List.of(), List.of()));
            }
        }
    }

    /** Demandes VALIDÉE ou CONFIRMEE (paiement effectué) du particulier connecté. */
    public List<DemandeFormation> getDemandesValidees() {
        if (offreBean.getMesDemandes() == null) return List.of();
        return offreBean.getMesDemandes().stream()
                .filter(d -> "VALIDEE".equals(d.getStatut()) || "CONFIRMEE".equals(d.getStatut()))
                .collect(Collectors.toList());
    }

    public List<FormationResume>  getFormations()         { return formations; }
    public int                    getNbFormations()        { return formations.size(); }
    public int                    getNbDemandesValidees()  { return getDemandesValidees().size(); }

    /** Nombre total de cours en cours (au moins un accès mais pas terminé). */
    public int getNbEnCours() {
        return (int) formations.stream()
                .filter(f -> f.getPourcentage() > 0 && f.getPourcentage() < 100)
                .count();
    }

    /** Nombre de cours complétés sur l'ensemble des formations. */
    public int getNbCoursTermines() {
        return formations.stream().mapToInt(FormationResume::getNbTermines).sum();
    }

    /** Nombre total de soumissions corrigées (avec note). */
    public int getNbResultatsRecus() {
        return formations.stream().mapToInt(FormationResume::getNbResultatsRecus).sum();
    }

    // ────────────────────────────────────────────────────────────────────────
    /** DTO interne : formation + cours + progressions. */
    public static class FormationResume implements Serializable {

        private final DemandeFormation          demande;
        private final List<CoursEnLigneFront>   cours;
        private final List<ProgressionFront>    progressions;
        private final Map<Long, ProgressionFront> progressionMap;

        public FormationResume(DemandeFormation demande,
                               List<CoursEnLigneFront> cours,
                               List<ProgressionFront>  progressions) {
            this.demande       = demande;
            this.cours         = cours;
            this.progressions  = progressions;
            this.progressionMap = progressions.stream()
                    .filter(p -> p.getCoursId() != null)
                    .collect(Collectors.toMap(ProgressionFront::getCoursId, p -> p, (a, b) -> a));
        }

        public DemandeFormation          getDemande()      { return demande; }
        public List<CoursEnLigneFront>   getCours()        { return cours; }
        public List<ProgressionFront>    getProgressions() { return progressions; }

        public int getNbCours()    { return cours.size(); }
        public int getNbTermines() {
            return (int) cours.stream()
                    .filter(c -> {
                        ProgressionFront p = progressionMap.get(c.getId());
                        return p != null && p.isComplete();
                    }).count();
        }

        public int getPourcentage() {
            if (cours.isEmpty()) return 0;
            return (int) Math.round(getNbTermines() * 100.0 / cours.size());
        }

        /** Nombre de travaux (TP/Devoir/Examen) avec un score reçu. */
        public int getNbResultatsRecus() {
            return (int) progressions.stream()
                    .filter(p -> p.getScoreQuiz() != null)
                    .count();
        }

        /** Cours TP/Devoir/Examen visibles dans la formation. */
        public List<CoursEnLigneFront> getTravauxPratiques() {
            return cours.stream()
                    .filter(CoursEnLigneFront::isTravauxPratiques)
                    .collect(Collectors.toList());
        }

        public ProgressionFront progressionPour(Long coursId) {
            return progressionMap.get(coursId);
        }

        /** Première lettre du titre de l'offre. */
        public String getInitiale() {
            String t = demande.getOffre() != null ? demande.getOffre().getTitre() : "F";
            return t != null && !t.isBlank() ? t.substring(0, 1).toUpperCase() : "F";
        }
    }
}
