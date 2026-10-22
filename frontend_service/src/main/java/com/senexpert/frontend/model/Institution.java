package com.senexpert.frontend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Institution {
    private Long   id;
    private String nom;
    private String type;
    private String adresse;
    private String secteur;
    private String ninea;
    private String domainesInteret;
    private String dateValidation;   // LocalDate sérialisé en String (yyyy-MM-dd)
    private String statut;

    public Long   getId()             { return id; }
    public void   setId(Long id)      { this.id = id; }
    public String getNom()            { return nom; }
    public void   setNom(String v)    { this.nom = v; }
    public String getType()           { return type; }
    public void   setType(String v)   { this.type = v; }
    public String getAdresse()        { return adresse; }
    public void   setAdresse(String v){ this.adresse = v; }
    public String getSecteur()        { return secteur; }
    public void   setSecteur(String v){ this.secteur = v; }
    public String getNinea()          { return ninea; }
    public void   setNinea(String v)  { this.ninea = v; }
    public String getDomainesInteret()       { return domainesInteret; }
    public void   setDomainesInteret(String v){ this.domainesInteret = v; }
    public String getDateValidation()        { return dateValidation; }
    public void   setDateValidation(String v){ this.dateValidation = v; }
    public String getStatut()         { return statut; }
    public void   setStatut(String v) { this.statut = v; }

    /** Libellé lisible du type */
    public String getTypeLibelle() {
        if (type == null) return "";
        switch (type) {
            case "ENTREPRISE":  return "Entreprise privée";
            case "ONG":         return "ONG";
            case "MINISTERE":   return "Ministère";
            case "DIRECTION":   return "Direction";
            case "PARTICULIER": return "Particulier";
            default:            return type;
        }
    }
}
