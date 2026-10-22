package com.senexpert.frontend.bean;

import com.senexpert.frontend.client.FormationServiceClient;
import com.senexpert.frontend.model.EvaluationFront;
import com.senexpert.frontend.model.PaiementFront;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.*;

/**
 * Gestion des évaluations et de l'historique de paiements du particulier.
 */
@Named
@ViewScoped
public class EvaluationBean implements Serializable {

    @Inject private FormationServiceClient client;
    @Inject private SessionBean            session;
    @Inject private ParticularDashboardBean dashboard;

    // ── Évaluation ──────────────────────────────────────────────────────────
    /** Cache : offreId → évaluation de cet apprenant */
    private Map<Long, EvaluationFront> evaluations = new HashMap<>();

    /** Offre en cours d'évaluation (formulaire) */
    private Long   offreEnCoursId;
    private int    noteEnCours   = 5;
    private String commentaireEnCours;

    // ── Paiements ────────────────────────────────────────────────────────────
    private List<PaiementFront> paiements        = new ArrayList<>();
    private Double              totalPaye        = 0.0;
    private boolean             paiementsCharges = false;

    @PostConstruct
    public void init() { }

    /** Charge les évaluations pour toutes les formations actives. */
    public void chargerEvaluations() {
        if (session.getUtilisateurId() == null) return;
        evaluations = new HashMap<>();
        for (var f : dashboard.getFormations()) {
            if (f.getDemande().getOffre() == null) continue;
            Long oid = f.getDemande().getOffre().getId();
            try {
                EvaluationFront ev = client.monEvaluation(
                    session.getUtilisateurId(), oid, session.getToken());
                if (ev != null) evaluations.put(oid, ev);
            } catch (Exception ignored) {}
        }
    }

    /** Charge l'historique des paiements (idempotent). */
    public void chargerPaiements() {
        if (paiementsCharges || session.getUtilisateurId() == null) return;
        paiementsCharges = true;
        paiements = new ArrayList<>(
            client.mesPaiements(session.getUtilisateurId(), session.getToken()));
        totalPaye = paiements.stream()
            .filter(p -> "SUCCES".equals(p.getStatut()) && p.getMontant() != null)
            .mapToDouble(PaiementFront::getMontant).sum();
    }

    /** Ouvre le formulaire d'évaluation pour une offre. */
    public void ouvrirEvaluation(Long offreId) {
        this.offreEnCoursId = offreId;
        EvaluationFront existing = evaluations.get(offreId);
        if (existing != null) {
            noteEnCours        = existing.getNote();
            commentaireEnCours = existing.getCommentaire();
        } else {
            noteEnCours        = 5;
            commentaireEnCours = "";
        }
    }

    /** Sélectionne l'offre ET soumet en une seule action (utilisé depuis dashboard). */
    public void soumettrePour(Long offreId) {
        ouvrirEvaluation(offreId);
        soumettre();
    }

    /** Soumet l'évaluation. */
    public void soumettre() {
        if (offreEnCoursId == null) return;
        if (noteEnCours < 1 || noteEnCours > 5) {
            addMsg(false, "La note doit être entre 1 et 5.");
            return;
        }
        EvaluationFront ev = new EvaluationFront();
        ev.setApprenantId(session.getUtilisateurId());
        ev.setOffreId(offreEnCoursId);
        ev.setNote(noteEnCours);
        ev.setCommentaire(commentaireEnCours);

        EvaluationFront saved = client.evaluerFormation(ev, session.getToken());
        if (saved != null) {
            evaluations.put(offreEnCoursId, saved);
            addMsg(true, "Évaluation enregistrée. Merci !");
            offreEnCoursId = null;
        } else {
            addMsg(false, "Impossible d'enregistrer l'évaluation. Réessayez.");
        }
    }

    public EvaluationFront getEvaluationPour(Long offreId)    { return evaluations.get(offreId); }
    public boolean         aEvalue(Long offreId)             { return evaluations.containsKey(offreId); }
    /** Accès Map direct depuis la vue (value expression). */
    public Map<Long, EvaluationFront> getEvaluations()       { return evaluations; }

    private void addMsg(boolean ok, String text) {
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(ok ? FacesMessage.SEVERITY_INFO : FacesMessage.SEVERITY_ERROR,
                ok ? "OK" : "Erreur", text));
    }

    // ── Getters / Setters ────────────────────────────────────────────────────
    public Long   getOffreEnCoursId()             { return offreEnCoursId; }
    public void   setOffreEnCoursId(Long v)       { offreEnCoursId = v; }
    public int    getNoteEnCours()                { return noteEnCours; }
    public void   setNoteEnCours(int v)           { noteEnCours = v; }
    public String getCommentaireEnCours()         { return commentaireEnCours; }
    public void   setCommentaireEnCours(String v) { commentaireEnCours = v; }
    public List<PaiementFront> getPaiements()     { return paiements; }
    public Double getTotalPaye()                  { return totalPaye; }
    public int    getNbPaiements()                { return paiements.size(); }

    public String getTotalPayeFormate() {
        if (totalPaye <= 0) return "0 FCFA";
        return String.format("%,.0f FCFA", totalPaye).replace(",", " ");
    }
}
