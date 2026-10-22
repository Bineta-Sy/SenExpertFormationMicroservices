package com.senexpert.frontend.bean;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

import java.io.Serializable;

@Named
@ApplicationScoped
public class ParametresBean implements Serializable {

    private String nomPlateforme = "SenExpertFormation";
    private String sousTitre = "Plateforme de Formations";
    private String emailContact = "contact@senexpertformation.sn";
    private String telephoneContact = "+221 33 000 00 00";
    private String adresse = "Dakar, Sénégal";
    private int maxDemandesParUtilisateur = 5;
    private boolean inscriptionOuverte = true;
    private boolean validationAutomatique = false;
    private String messageAccueil = "Bienvenue sur SenExpertFormation, votre plateforme de formations professionnelles.";
    private String couleurPrimaire = "#2563EB";

    public void sauvegarder() {
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_INFO, "Succès", "Paramètres sauvegardés."));
        AuditBean.log("MODIFICATION", "PARAMETRES", "Mise à jour des paramètres plateforme", "Admin", "INFO");
    }

    // Getters / Setters
    public String getNomPlateforme() { return nomPlateforme; }
    public void setNomPlateforme(String v) { this.nomPlateforme = v; }
    public String getSousTitre() { return sousTitre; }
    public void setSousTitre(String v) { this.sousTitre = v; }
    public String getEmailContact() { return emailContact; }
    public void setEmailContact(String v) { this.emailContact = v; }
    public String getTelephoneContact() { return telephoneContact; }
    public void setTelephoneContact(String v) { this.telephoneContact = v; }
    public String getAdresse() { return adresse; }
    public void setAdresse(String v) { this.adresse = v; }
    public int getMaxDemandesParUtilisateur() { return maxDemandesParUtilisateur; }
    public void setMaxDemandesParUtilisateur(int v) { this.maxDemandesParUtilisateur = v; }
    public boolean isInscriptionOuverte() { return inscriptionOuverte; }
    public void setInscriptionOuverte(boolean v) { this.inscriptionOuverte = v; }
    public boolean isValidationAutomatique() { return validationAutomatique; }
    public void setValidationAutomatique(boolean v) { this.validationAutomatique = v; }
    public String getMessageAccueil() { return messageAccueil; }
    public void setMessageAccueil(String v) { this.messageAccueil = v; }
    public String getCouleurPrimaire() { return couleurPrimaire; }
    public void setCouleurPrimaire(String v) { this.couleurPrimaire = v; }
}
