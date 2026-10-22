package com.senexpert.frontend.bean;

import com.senexpert.frontend.client.FormationServiceClient;
import com.senexpert.frontend.model.*;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Bean dédié à la page de correction des TP / Devoirs / Examens.
 * Charge toutes les soumissions de tous les cours évaluables du formateur.
 */
@Named
@ViewScoped
public class FormateurCorrectionsBean implements Serializable {

    @Inject private FormationServiceClient formationClient;
    @Inject private SessionBean            session;

    /** Cours évaluables avec leurs soumissions. */
    private List<CoursCorrections> groupes = new ArrayList<>();

    /** Filtre actif : "tous", "attente", "corrige" */
    private String filtre = "tous";

    @PostConstruct
    public void init() {
        // charger() est appelé par f:viewAction après injection du filtre via f:viewParam
    }

    public void charger() {
        String email = session.getEmail();
        String token = session.getToken();
        groupes = new ArrayList<>();

        // 1. Récupère toutes les offres et filtre celles du formateur
        List<OffreFormation> mesOffres = formationClient.listerOffresAdmin(session.getToken()).stream()
            .filter(o -> o.getFormateurs() != null && o.getFormateurs().stream()
                .anyMatch(f -> email != null && email.equalsIgnoreCase(f.getEmail())))
            .collect(Collectors.toList());

        // 2. Pour chaque offre, charge les cours évaluables
        for (OffreFormation offre : mesOffres) {
            List<CoursEnLigneFront> cours = formationClient.coursParOffre(offre.getId(), true, token);
            if (cours == null) continue;
            for (CoursEnLigneFront c : cours) {
                if (!c.isTravauxPratiques()) continue;
                c.setOffreId(offre.getId());
                c.setOffreTitre(offre.getTitre());

                // 3. Récupère les soumissions de ce cours
                List<SoumissionFront> soumissions = formationClient.soumissionsParCours(c.getId(), token);
                if (soumissions == null) soumissions = List.of();

                // Pré-initialise les champs de saisie
                soumissions.forEach(s -> {
                    if (s.getNoteSaisie() == 0 && s.getNote() != null) s.setNoteSaisie(s.getNote());
                    if (s.getCommentaireSaisie() == null && s.getCommentaireFormateur() != null)
                        s.setCommentaireSaisie(s.getCommentaireFormateur());
                });

                if (!soumissions.isEmpty()) {
                    groupes.add(new CoursCorrections(c, soumissions));
                }
            }
        }
    }

    /** Formateur : attribue une note à une soumission. */
    public void noter(SoumissionFront s, CoursEnLigneFront cours) {
        SoumissionFront updated = formationClient.noterSoumission(
                s.getId(), s.getNoteSaisie(), s.getCommentaireSaisie(), session.getToken());
        if (updated != null) {
            msg(true, "Note " + s.getNoteSaisie() + "/" + cours.getBaremeOu20()
                    + " attribuée à " + s.getNomApprenant() + ".");
            charger();
        } else {
            msg(false, "Échec de la notation.");
        }
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public List<CoursCorrections> getGroupes() {
        if ("attente".equals(filtre))
            return groupes.stream()
                .map(g -> new CoursCorrections(g.getCours(),
                    g.getSoumissions().stream().filter(SoumissionFront::isEnAttente).collect(Collectors.toList())))
                .filter(g -> !g.getSoumissions().isEmpty())
                .collect(Collectors.toList());
        if ("corrige".equals(filtre))
            return groupes.stream()
                .map(g -> new CoursCorrections(g.getCours(),
                    g.getSoumissions().stream().filter(SoumissionFront::isCorrige).collect(Collectors.toList())))
                .filter(g -> !g.getSoumissions().isEmpty())
                .collect(Collectors.toList());
        return groupes;
    }

    public int getNbTotalEnAttente() {
        return groupes.stream()
            .mapToInt(g -> (int) g.getSoumissions().stream().filter(SoumissionFront::isEnAttente).count())
            .sum();
    }

    public int getNbTotalSoumissions() {
        return groupes.stream().mapToInt(g -> g.getSoumissions().size()).sum();
    }

    public String getFiltre()         { return filtre; }
    public void   setFiltre(String v) { filtre = v; }

    private void msg(boolean ok, String text) {
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(ok ? FacesMessage.SEVERITY_INFO : FacesMessage.SEVERITY_ERROR,
                ok ? "Succès" : "Erreur", text));
    }

    // ─────────────────────────────────────────────────────────────────────────
    /** Groupe : un cours évaluable + ses soumissions. */
    public static class CoursCorrections implements Serializable {
        private final CoursEnLigneFront    cours;
        private final List<SoumissionFront> soumissions;

        public CoursCorrections(CoursEnLigneFront cours, List<SoumissionFront> soumissions) {
            this.cours       = cours;
            this.soumissions = soumissions;
        }

        public CoursEnLigneFront     getCours()       { return cours; }
        public List<SoumissionFront> getSoumissions() { return soumissions; }

        public long getNbEnAttente() {
            return soumissions.stream().filter(SoumissionFront::isEnAttente).count();
        }
        public long getNbCorrige() {
            return soumissions.stream().filter(SoumissionFront::isCorrige).count();
        }
    }
}
