package com.senexpert.formationservice.dto;

public class SoumissionDTO {

    private Long   id;
    private Long   coursId;
    private Long   apprenantId;
    private String nomApprenant;

    // Contenu
    private String cheminFichier;
    private String nomFichierOriginal;
    private Long   tailleFichier;
    private String urlFichier;
    private String contenuTexte;
    private String lienUrl;

    // Horodatage
    private String  dateSoumission;
    private String  debutExamen;
    private boolean enRetard;

    // Correction
    private Double note;
    private String commentaireFormateur;
    private String dateCorrection;
    private String statut; // EN_ATTENTE, CORRIGE, RENDU_TARD

    public SoumissionDTO() {}

    // ── Getters / Setters ─────────────────────────────────────────────────
    public Long   getId()                         { return id; }
    public void   setId(Long v)                   { id = v; }
    public Long   getCoursId()                    { return coursId; }
    public void   setCoursId(Long v)              { coursId = v; }
    public Long   getApprenantId()                { return apprenantId; }
    public void   setApprenantId(Long v)          { apprenantId = v; }
    public String getNomApprenant()               { return nomApprenant; }
    public void   setNomApprenant(String v)       { nomApprenant = v; }
    public String getCheminFichier()              { return cheminFichier; }
    public void   setCheminFichier(String v)      { cheminFichier = v; }
    public String getNomFichierOriginal()         { return nomFichierOriginal; }
    public void   setNomFichierOriginal(String v) { nomFichierOriginal = v; }
    public Long   getTailleFichier()              { return tailleFichier; }
    public void   setTailleFichier(Long v)        { tailleFichier = v; }
    public String getUrlFichier()                 { return urlFichier; }
    public void   setUrlFichier(String v)         { urlFichier = v; }
    public String getContenuTexte()               { return contenuTexte; }
    public void   setContenuTexte(String v)       { contenuTexte = v; }
    public String getLienUrl()                    { return lienUrl; }
    public void   setLienUrl(String v)            { lienUrl = v; }
    public String getDateSoumission()             { return dateSoumission; }
    public void   setDateSoumission(String v)     { dateSoumission = v; }
    public String getDebutExamen()                { return debutExamen; }
    public void   setDebutExamen(String v)        { debutExamen = v; }
    public boolean isEnRetard()                   { return enRetard; }
    public void   setEnRetard(boolean v)          { enRetard = v; }
    public Double  getNote()                      { return note; }
    public void   setNote(Double v)               { note = v; }
    public String getCommentaireFormateur()       { return commentaireFormateur; }
    public void   setCommentaireFormateur(String v){ commentaireFormateur = v; }
    public String getDateCorrection()             { return dateCorrection; }
    public void   setDateCorrection(String v)     { dateCorrection = v; }
    public String getStatut()                     { return statut; }
    public void   setStatut(String v)             { statut = v; }
}
