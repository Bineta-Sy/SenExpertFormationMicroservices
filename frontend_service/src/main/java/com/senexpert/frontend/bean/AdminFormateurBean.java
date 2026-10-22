package com.senexpert.frontend.bean;

import com.senexpert.frontend.client.FormationServiceClient;
import com.senexpert.frontend.model.FormateurFront;
import com.senexpert.frontend.model.OffreFormation;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Named
@ViewScoped
public class AdminFormateurBean implements Serializable {

    @Inject private FormationServiceClient formationClient;
    @Inject private SessionBean session;

    // Liste
    private List<FormateurFront> formateurs = new ArrayList<>();

    // Formulaire création
    private String fNom;
    private String fPrenom;
    private String fEmail;
    private String fTelephone;
    private String fSpecialite;
    private String fBio;

    // Assignation
    private List<OffreFormation> offres = new ArrayList<>();
    private Long selectedOffreId;
    private Long selectedFormateurId;
    private List<FormateurFront> formateursOffreSelectionnee = new ArrayList<>();
    private String offreTitreSelectionnee;

    @PostConstruct
    public void init() {
        charger();
    }

    public void charger() {
        formateurs = formationClient.listerFormateurs();
        offres     = formationClient.listerOffresAdmin(session.getToken());
    }

    public void creer() {
        boolean ok = formationClient.creerFormateur(fNom, fPrenom, fEmail, fTelephone, fSpecialite, fBio, session.getToken());
        if (ok) {
            msg(true, "Formateur créé avec succès.");
            reinitForm();
            charger();
        } else {
            msg(false, "Échec de la création du formateur.");
        }
    }

    public void supprimer(Long id) {
        boolean ok = formationClient.supprimerFormateur(id, session.getToken());
        if (ok) {
            msg(true, "Formateur supprimé.");
            charger();
        } else {
            msg(false, "Échec de la suppression.");
        }
    }

    public void voirFormateursOffre(Long offreId, String titre) {
        this.selectedOffreId = offreId;
        this.offreTitreSelectionnee = titre;
        this.formateursOffreSelectionnee = formationClient.formateursParOffre(offreId);
    }

    public void assigner() {
        if (selectedFormateurId == null || selectedOffreId == null) {
            msg(false, "Sélectionnez un formateur et une offre.");
            return;
        }
        boolean ok = formationClient.assignerFormateurAOffre(selectedFormateurId, selectedOffreId, session.getToken());
        if (ok) {
            msg(true, "Formateur assigné à l'offre.");
            formateursOffreSelectionnee = formationClient.formateursParOffre(selectedOffreId);
        } else {
            msg(false, "Échec de l'assignation.");
        }
    }

    public void retirerFormateurOffre(Long formateurId) {
        if (selectedOffreId == null) return;
        boolean ok = formationClient.retirerFormateurDeOffre(formateurId, selectedOffreId, session.getToken());
        if (ok) {
            msg(true, "Formateur retiré de l'offre.");
            formateursOffreSelectionnee = formationClient.formateursParOffre(selectedOffreId);
        } else {
            msg(false, "Échec du retrait.");
        }
    }

    private void reinitForm() {
        fNom = null; fPrenom = null; fEmail = null;
        fTelephone = null; fSpecialite = null; fBio = null;
    }

    private void msg(boolean ok, String text) {
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(ok ? FacesMessage.SEVERITY_INFO : FacesMessage.SEVERITY_ERROR,
                ok ? "Succès" : "Erreur", text));
    }

    // ── Compteurs sans paramètres (évite EL .size()) ──
    public int getNbFormateurs()                  { return formateurs                  == null ? 0 : formateurs.size(); }
    public int getNbOffres()                      { return offres                      == null ? 0 : offres.size(); }
    public int getNbFormateursOffreSelectionnee() { return formateursOffreSelectionnee == null ? 0 : formateursOffreSelectionnee.size(); }

    // ── Getters / Setters ──
    public List<FormateurFront> getFormateurs() { return formateurs; }
    public List<OffreFormation> getOffres()     { return offres; }
    public List<FormateurFront> getFormateursOffreSelectionnee() { return formateursOffreSelectionnee; }
    public String getOffreTitreSelectionnee()  { return offreTitreSelectionnee; }

    public String getFNom()        { return fNom; }
    public void   setFNom(String v){ fNom = v; }
    public String getFPrenom()        { return fPrenom; }
    public void   setFPrenom(String v){ fPrenom = v; }
    public String getFEmail()        { return fEmail; }
    public void   setFEmail(String v){ fEmail = v; }
    public String getFTelephone()        { return fTelephone; }
    public void   setFTelephone(String v){ fTelephone = v; }
    public String getFSpecialite()        { return fSpecialite; }
    public void   setFSpecialite(String v){ fSpecialite = v; }
    public String getFBio()        { return fBio; }
    public void   setFBio(String v){ fBio = v; }

    public Long getSelectedOffreId()         { return selectedOffreId; }
    public void setSelectedOffreId(Long v)   { selectedOffreId = v; }
    public Long getSelectedFormateurId()     { return selectedFormateurId; }
    public void setSelectedFormateurId(Long v){ selectedFormateurId = v; }
}
