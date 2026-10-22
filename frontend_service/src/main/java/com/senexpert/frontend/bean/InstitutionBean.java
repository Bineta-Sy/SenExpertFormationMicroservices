package com.senexpert.frontend.bean;

import com.senexpert.frontend.client.UserServiceClient;
import com.senexpert.frontend.model.Institution;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;

@Named
@ViewScoped
public class InstitutionBean implements Serializable {

    @Inject
    private UserServiceClient userClient;

    @Inject
    private SessionBean session;

    // Champs du formulaire
    private String nom;
    private String type = "ENTREPRISE";
    private String adresse;
    private String secteur;
    private String ninea;

    public String creer() {
        if (!session.isConnecte()) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Vous devez être connecté."));
            return null;
        }

        Institution inst = userClient.creerInstitution(nom, type, adresse, secteur, ninea, session.getToken());
        if (inst == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur",
                    "Création échouée. Vérifiez que le service est disponible."));
            return null;
        }

        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_INFO, "Succès",
                "Institution \"" + inst.getNom() + "\" créée. En attente de validation par l'administrateur."));

        // Réinitialiser le formulaire
        nom = null;
        adresse = null;
        secteur = null;
        ninea = null;
        type = "ENTREPRISE";
        return null;
    }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }
    public String getSecteur() { return secteur; }
    public void setSecteur(String secteur) { this.secteur = secteur; }
    public String getNinea() { return ninea; }
    public void setNinea(String ninea) { this.ninea = ninea; }
}
