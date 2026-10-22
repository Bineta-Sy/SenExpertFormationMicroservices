package com.senexpert.frontend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SessionFormationFront implements Serializable {

    private Long id;
    private OffreFormation offre;
    private String dateHeure;
    private Integer dureeMinutes;
    private String salle;
    private String formateur;
    private Integer nbPlaces;
    private String statut;

    // ─── Getters / Setters ────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public OffreFormation getOffre() { return offre; }
    public void setOffre(OffreFormation offre) { this.offre = offre; }

    public String getDateHeure() { return dateHeure; }
    public void setDateHeure(String dateHeure) { this.dateHeure = dateHeure; }

    public Integer getDureeMinutes() { return dureeMinutes; }
    public void setDureeMinutes(Integer dureeMinutes) { this.dureeMinutes = dureeMinutes; }

    public String getSalle() { return salle; }
    public void setSalle(String salle) { this.salle = salle; }

    public String getFormateur() { return formateur; }
    public void setFormateur(String formateur) { this.formateur = formateur; }

    public Integer getNbPlaces() { return nbPlaces; }
    public void setNbPlaces(Integer nbPlaces) { this.nbPlaces = nbPlaces; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    /** "16" */
    public String getJour() {
        if (dateHeure == null || dateHeure.length() < 10) return "—";
        try { return String.valueOf(Integer.parseInt(dateHeure.substring(8, 10))); }
        catch (Exception e) { return "—"; }
    }

    /** "JUN", "JAN", etc. */
    public String getMoisAbr() {
        if (dateHeure == null || dateHeure.length() < 8) return "—";
        try {
            int m = Integer.parseInt(dateHeure.substring(5, 7));
            String[] mois = {"JAN","FÉV","MAR","AVR","MAI","JUN","JUL","AOÛ","SEP","OCT","NOV","DÉC"};
            return mois[m - 1];
        } catch (Exception e) { return "—"; }
    }

    /** "14h00" */
    public String getHeureFormatee() {
        if (dateHeure == null || dateHeure.length() < 16) return "—";
        try { return dateHeure.substring(11, 16).replace(":", "h"); }
        catch (Exception e) { return "—"; }
    }

    /** Durée lisible : "90 min" */
    public String getDureeLabel() {
        if (dureeMinutes == null) return "";
        return dureeMinutes + " min";
    }
}
