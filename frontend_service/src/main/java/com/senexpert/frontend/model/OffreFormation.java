package com.senexpert.frontend.model;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OffreFormation implements Serializable {
    private Long id;
    private String titre;
    private String description;
    private String dateDebut;
    private String dateFin;
    private Integer places;
    private Integer placesDisponibles;
    private String type;
    private Double cout;
    private String niveau;
    private String duree;
    private Domaine domaine;
    private String statut;
    private String lieu;
    private List<FormateurFront> formateurs = new ArrayList<>();
    /** Documents à fournir pour postuler (définis par le prestataire). */
    private List<String> documentsRequis = new ArrayList<>();
    /** Date limite de candidature (ISO date string). */
    private String dateLimiteCandidature;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public Integer getPlacesDisponibles() { return placesDisponibles != null ? placesDisponibles : places; }
    public void setPlacesDisponibles(Integer v) { this.placesDisponibles = v; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Double getCout() { return cout; }
    public void setCout(Double cout) { this.cout = cout; }
    public String getNiveau() { return niveau; }
    public void setNiveau(String niveau) { this.niveau = niveau; }
    public String getDuree() { return duree; }
    public void setDuree(String duree) { this.duree = duree; }
    public Domaine getDomaine() { return domaine; }
    public void setDomaine(Domaine domaine) { this.domaine = domaine; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public String getLieu() { return lieu; }
    public void setLieu(String lieu) { this.lieu = lieu; }
    public List<FormateurFront> getFormateurs() { return formateurs; }
    public void setFormateurs(List<FormateurFront> f) { this.formateurs = f; }
    public List<String> getDocumentsRequis() { return documentsRequis != null ? documentsRequis : Collections.emptyList(); }
    public void setDocumentsRequis(List<String> d) { this.documentsRequis = d; }
    public String getDateLimiteCandidature() { return dateLimiteCandidature; }
    public void setDateLimiteCandidature(String v) { this.dateLimiteCandidature = v; }

    /** Nombre de documents requis — utilisé en EL value expression (évite .size()) */
    public int getNbDocumentsRequis() {
        return documentsRequis != null ? documentsRequis.size() : 0;
    }

    /** Vrai si la formation est payante */
    public boolean isPayante() { return cout != null && cout > 0; }

    /** Prix formaté en FCFA */
    public String getCoutFormate() {
        if (cout == null || cout <= 0) return "Gratuit";
        return String.format("%,.0f FCFA", cout).replace(",", " ");
    }
}
