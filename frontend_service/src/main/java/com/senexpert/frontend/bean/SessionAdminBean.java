package com.senexpert.frontend.bean;

import com.senexpert.frontend.client.FormationServiceClient;
import com.senexpert.frontend.model.Offre;
import com.senexpert.frontend.model.SessionFormationFront;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.List;

/**
 * Bean pour la gestion des sessions de formation (admin).
 */
@Named
@ViewScoped
public class SessionAdminBean implements Serializable {

    @Inject private FormationServiceClient formationClient;
    @Inject private SessionBean session;

    private List<SessionFormationFront> sessions;
    private List<Offre> offres;

    // Formulaire création
    private Long sessionOffreId;
    private String sessionDateHeure;   // format ISO : 2025-09-01T09:00
    private Integer sessionDuree = 60;
    private String sessionSalle;
    private String sessionFormateur;
    private Integer sessionNbPlaces;

    // Filtre
    private Long filtreOffreId;

    @PostConstruct
    public void init() {
        chargerTout();
    }

    public void chargerTout() {
        String t = session.getToken();
        sessions = filtreOffreId != null
            ? formationClient.listerSessionsParOffre(filtreOffreId, t)
            : formationClient.listerSessions(t);
        offres = formationClient.listerOffresCompletes();
    }

    public void creerSession() {
        boolean ok = formationClient.creerSession(
            sessionOffreId, sessionDateHeure, sessionDuree,
            sessionSalle, sessionFormateur, sessionNbPlaces,
            session.getToken());
        msg(ok, "Session créée.", "Echec création. Vérifiez les champs.");
        if (ok) {
            resetForm();
            chargerTout();
        }
    }

    public void changerStatut(Long id, String statut) {
        msg(formationClient.changerStatutSession(id, statut, session.getToken()),
            "Statut mis à jour.", "Echec mise à jour statut.");
        chargerTout();
    }

    public void supprimerSession(Long id) {
        msg(formationClient.supprimerSession(id, session.getToken()),
            "Session supprimée.", "Echec suppression.");
        chargerTout();
    }

    public void filtrerParOffre() {
        chargerTout();
    }

    public void reinitialiserFiltre() {
        filtreOffreId = null;
        chargerTout();
    }

    private void resetForm() {
        sessionOffreId = null; sessionDateHeure = null;
        sessionDuree = 60; sessionSalle = null;
        sessionFormateur = null; sessionNbPlaces = null;
    }

    private void msg(boolean ok, String success, String error) {
        FacesContext.getCurrentInstance().addMessage(null,
            ok ? new FacesMessage(FacesMessage.SEVERITY_INFO, "Succès", success)
               : new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", error));
    }

    // Getters / Setters
    public List<SessionFormationFront> getSessions() { return sessions; }
    public List<Offre> getOffres() { return offres; }

    public Long getSessionOffreId() { return sessionOffreId; }
    public void setSessionOffreId(Long v) { this.sessionOffreId = v; }
    public String getSessionDateHeure() { return sessionDateHeure; }
    public void setSessionDateHeure(String v) { this.sessionDateHeure = v; }
    public Integer getSessionDuree() { return sessionDuree; }
    public void setSessionDuree(Integer v) { this.sessionDuree = v; }
    public String getSessionSalle() { return sessionSalle; }
    public void setSessionSalle(String v) { this.sessionSalle = v; }
    public String getSessionFormateur() { return sessionFormateur; }
    public void setSessionFormateur(String v) { this.sessionFormateur = v; }
    public Integer getSessionNbPlaces() { return sessionNbPlaces; }
    public void setSessionNbPlaces(Integer v) { this.sessionNbPlaces = v; }
    public Long getFiltreOffreId() { return filtreOffreId; }
    public void setFiltreOffreId(Long v) { this.filtreOffreId = v; }
}
