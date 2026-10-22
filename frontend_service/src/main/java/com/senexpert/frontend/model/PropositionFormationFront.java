package com.senexpert.frontend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PropositionFormationFront {
    private Long   id;
    private Long   demandeLibreId;
    private String formateurNom;
    private String dateDebut;
    private String dateFin;
    private String lieu;
    private Double montant;
    private String programme;
    private String noteAdmin;
    private String dateProposition;
    private String statut;        // EN_ATTENTE | ACCEPTEE | REFUSEE
    private String mode;
    private java.util.List<String> modulesProposition = new java.util.ArrayList<>();
    private String motifRefus;

    public Long   getId()                     { return id; }
    public void   setId(Long v)               { this.id = v; }
    public Long   getDemandeLibreId()         { return demandeLibreId; }
    public void   setDemandeLibreId(Long v)   { this.demandeLibreId = v; }
    public String getFormateurNom()           { return formateurNom; }
    public void   setFormateurNom(String v)   { this.formateurNom = v; }
    public String getDateDebut()              { return dateDebut; }
    public void   setDateDebut(String v)      { this.dateDebut = v; }
    public String getDateFin()                { return dateFin; }
    public void   setDateFin(String v)        { this.dateFin = v; }
    public String getLieu()                   { return lieu; }
    public void   setLieu(String v)           { this.lieu = v; }
    public Double getMontant()                { return montant; }
    public void   setMontant(Double v)        { this.montant = v; }
    public String getProgramme()              { return programme; }
    public void   setProgramme(String v)      { this.programme = v; }
    public String getNoteAdmin()              { return noteAdmin; }
    public void   setNoteAdmin(String v)      { this.noteAdmin = v; }
    public String getDateProposition()        { return dateProposition; }
    public void   setDateProposition(String v){ this.dateProposition = v; }
    public String getStatut()                 { return statut; }
    public void   setStatut(String v)         { this.statut = v; }
    public String getMotifRefus()             { return motifRefus; }
    public void   setMotifRefus(String v)     { this.motifRefus = v; }
    public String getMode()           { return mode; }
    public void   setMode(String v)   { this.mode = v; }
    public java.util.List<String> getModulesProposition()         { return modulesProposition; }
    public void   setModulesProposition(java.util.List<String> v) { this.modulesProposition = v; }

}
