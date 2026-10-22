package com.senexpert.frontend.bean;

import com.senexpert.frontend.client.FormationServiceClient;
import com.senexpert.frontend.model.Domaine;
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
public class OffreAdminBean implements Serializable {

    @Inject private FormationServiceClient formationClient;
    @Inject private SessionBean session;

    // Formulaire création/modification offre
    private String titre;
    private String description;
    private String dateDebut;
    private String dateFin;
    private Integer places;
    private String type = "EN_LIGNE";
    private Long domaineId;

    // Documents requis pour cette offre (définis par le prestataire)
    private List<String> documentsRequis = new ArrayList<>();
    private String       nouveauDocumentRequis;

    // Offre en cours de modification
    private Long offreEnCoursId;
    private boolean modeEdition = false;

    // Formulaire domaine
    private String libelleNouveauDomaine;

    private List<Domaine> domaines;
    private List<OffreFormation> offres;

    @PostConstruct
    public void init() {
        domaines = formationClient.listerDomaines();
        offres = formationClient.listerOffresAdmin(session.getToken());
    }

    /** Crée une nouvelle offre */
    public void creerOffre() {
        boolean ok = formationClient.creerOffreComplete(titre, description, dateDebut, dateFin,
                places, type, domaineId, null, null, null, documentsRequis, session.getToken());
        if (ok) {
            msg(true, "Offre créée avec succès.");
            resetOffre();
            offres = formationClient.listerOffresAdmin(session.getToken());
        } else {
            msg(false, "Échec de la création de l'offre.");
        }
    }

    /** Charge une offre pour modification */
    public void chargerPourEdition(OffreFormation o) {
        offreEnCoursId = o.getId();
        titre = o.getTitre();
        description = o.getDescription();
        dateDebut = o.getDateDebut();
        dateFin = o.getDateFin();
        places = o.getPlaces();
        type = o.getType();
        domaineId = o.getDomaine() != null ? o.getDomaine().getId() : null;
        documentsRequis = new ArrayList<>(o.getDocumentsRequis());
        modeEdition = true;
    }

    /** Enregistre les modifications d'une offre */
    public void modifierOffre() {
        boolean ok = formationClient.modifierOffreComplete(offreEnCoursId, titre, description,
                dateDebut, dateFin, places, type, domaineId, null, null, null,
                documentsRequis, session.getToken());
        if (ok) {
            msg(true, "Offre modifiée avec succès.");
            resetOffre();
            offres = formationClient.listerOffresAdmin(session.getToken());
        } else {
            msg(false, "Échec de la modification.");
        }
    }

    /** Annule le mode édition */
    public void annulerEdition() {
        resetOffre();
    }

    /** Ajoute un document requis à la liste */
    public void ajouterDocumentRequis() {
        if (nouveauDocumentRequis != null && !nouveauDocumentRequis.isBlank()) {
            documentsRequis.add(nouveauDocumentRequis.trim());
            nouveauDocumentRequis = null;
        }
    }

    /** Supprime un document requis par index */
    public void supprimerDocumentRequis(int index) {
        if (index >= 0 && index < documentsRequis.size()) {
            documentsRequis.remove(index);
        }
    }

    /** Supprime une offre */
    public void supprimerOffre(Long id) {
        boolean ok = formationClient.supprimerOffre(id, session.getToken());
        if (ok) {
            msg(true, "Offre supprimée.");
            offres = formationClient.listerOffresAdmin(session.getToken());
        } else {
            msg(false, "Échec suppression offre.");
        }
    }

    /** Crée un domaine */
    public void creerDomaine() {
        boolean ok = formationClient.creerDomaine(libelleNouveauDomaine, session.getToken());
        if (ok) {
            msg(true, "Domaine \"" + libelleNouveauDomaine + "\" créé.");
            libelleNouveauDomaine = null;
            domaines = formationClient.listerDomaines();
        } else {
            msg(false, "Échec création domaine.");
        }
    }

    /** Supprime un domaine */
    public void supprimerDomaine(Long id) {
        boolean ok = formationClient.supprimerDomaine(id, session.getToken());
        if (ok) {
            msg(true, "Domaine supprimé.");
            domaines = formationClient.listerDomaines();
        } else {
            msg(false, "Échec suppression domaine (vérifiez qu'aucune offre n'y est liée).");
        }
    }

    private void resetOffre() {
        offreEnCoursId = null; modeEdition = false;
        titre = null; description = null; dateDebut = null;
        dateFin = null; places = null; type = "EN_LIGNE"; domaineId = null;
        documentsRequis = new ArrayList<>(); nouveauDocumentRequis = null;
    }

    private void msg(boolean ok, String text) {
        FacesContext.getCurrentInstance().addMessage(null, ok
            ? new FacesMessage(FacesMessage.SEVERITY_INFO, "Succès", text)
            : new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", text));
    }

    // Getters / Setters
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getDateDebut() { return dateDebut; }
    public void setDateDebut(String dateDebut) { this.dateDebut = dateDebut; }
    public String getDateFin() { return dateFin; }
    public void setDateFin(String dateFin) { this.dateFin = dateFin; }
    public Integer getPlaces() { return places; }
    public void setPlaces(Integer places) { this.places = places; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Long getDomaineId() { return domaineId; }
    public void setDomaineId(Long domaineId) { this.domaineId = domaineId; }
    public String getLibelleNouveauDomaine() { return libelleNouveauDomaine; }
    public void setLibelleNouveauDomaine(String v) { this.libelleNouveauDomaine = v; }
    public List<Domaine> getDomaines() { return domaines; }
    public List<OffreFormation> getOffres() { return offres; }
    public boolean isModeEdition() { return modeEdition; }
    public Long getOffreEnCoursId() { return offreEnCoursId; }
    public List<String> getDocumentsRequis()            { return documentsRequis; }
    public String       getNouveauDocumentRequis()      { return nouveauDocumentRequis; }
    public void         setNouveauDocumentRequis(String v) { this.nouveauDocumentRequis = v; }
    public int          getNbDocumentsRequis()          { return documentsRequis.size(); }
}
