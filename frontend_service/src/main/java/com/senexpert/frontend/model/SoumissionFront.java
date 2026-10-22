package com.senexpert.frontend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SoumissionFront {

    private Long    id;
    private Long    coursId;
    private Long    apprenantId;
    private String  nomApprenant;

    // Contenu soumis
    private String  cheminFichier;
    private String  nomFichierOriginal;
    private Long    tailleFichier;
    private String  urlFichier;
    private String  contenuTexte;
    private String  lienUrl;

    // Horodatage
    private String  dateSoumission;
    private String  debutExamen;
    private boolean enRetard;

    // Correction
    private Double  note;
    private String  commentaireFormateur;
    private String  dateCorrection;
    private String  statut; // EN_ATTENTE, CORRIGE, RENDU_TARD

    // UI transient
    private double  noteSaisie;     // champ formulaire correction
    private String  commentaireSaisie;

    // ── Getters / Setters ─────────────────────────────────────────────────
    public Long    getId()                         { return id; }
    public void    setId(Long v)                   { id = v; }
    public Long    getCoursId()                    { return coursId; }
    public void    setCoursId(Long v)              { coursId = v; }
    public Long    getApprenantId()                { return apprenantId; }
    public void    setApprenantId(Long v)          { apprenantId = v; }
    public String  getNomApprenant()               { return nomApprenant; }
    public void    setNomApprenant(String v)       { nomApprenant = v; }
    public String  getCheminFichier()              { return cheminFichier; }
    public void    setCheminFichier(String v)      { cheminFichier = v; }
    public String  getNomFichierOriginal()         { return nomFichierOriginal; }
    public void    setNomFichierOriginal(String v) { nomFichierOriginal = v; }
    public Long    getTailleFichier()              { return tailleFichier; }
    public void    setTailleFichier(Long v)        { tailleFichier = v; }
    public String  getUrlFichier()                 { return urlFichier; }
    public void    setUrlFichier(String v)         { urlFichier = v; }
    public String  getContenuTexte()               { return contenuTexte; }
    public void    setContenuTexte(String v)       { contenuTexte = v; }
    public String  getLienUrl()                    { return lienUrl; }
    public void    setLienUrl(String v)            { lienUrl = v; }
    public String  getDateSoumission()             { return dateSoumission; }
    public void    setDateSoumission(String v)     { dateSoumission = v; }
    public String  getDebutExamen()                { return debutExamen; }
    public void    setDebutExamen(String v)        { debutExamen = v; }
    public boolean isEnRetard()                    { return enRetard; }
    public void    setEnRetard(boolean v)          { enRetard = v; }
    public Double  getNote()                       { return note; }
    public void    setNote(Double v)               { note = v; }
    public String  getCommentaireFormateur()       { return commentaireFormateur; }
    public void    setCommentaireFormateur(String v){ commentaireFormateur = v; }
    public String  getDateCorrection()             { return dateCorrection; }
    public void    setDateCorrection(String v)     { dateCorrection = v; }
    public String  getStatut()                     { return statut; }
    public void    setStatut(String v)             { statut = v; }
    public double  getNoteSaisie()                 { return noteSaisie; }
    public void    setNoteSaisie(double v)         { noteSaisie = v; }
    public String  getCommentaireSaisie()          { return commentaireSaisie; }
    public void    setCommentaireSaisie(String v)  { commentaireSaisie = v; }

    // ── Helpers ────────────────────────────────────────────────────────────
    public boolean isCorrige()     { return "CORRIGE".equals(statut); }
    public boolean isEnAttente()   { return "EN_ATTENTE".equals(statut) || "RENDU_TARD".equals(statut); }
    public boolean isAFichier()    { return cheminFichier != null && !cheminFichier.isBlank(); }
    public boolean isATexte()      { return contenuTexte != null && !contenuTexte.isBlank(); }
    public boolean isALien()       { return lienUrl != null && !lienUrl.isBlank(); }

    public String getStatutLabel() {
        if (statut == null) return "—";
        return switch (statut) {
            case "EN_ATTENTE"  -> "En attente";
            case "CORRIGE"     -> "Corrigé";
            case "RENDU_TARD"  -> "En retard";
            default -> statut;
        };
    }

    public String getTailleFormatee() {
        if (tailleFichier == null) return "";
        if (tailleFichier < 1024)       return tailleFichier + " o";
        if (tailleFichier < 1024*1024)  return String.format("%.1f Ko", tailleFichier / 1024.0);
        return String.format("%.1f Mo", tailleFichier / (1024.0 * 1024));
    }

    /** Date soumission courte (juste la partie date). */
    public String getDateCourteSoumission() {
        if (dateSoumission == null) return "—";
        return dateSoumission.length() >= 10 ? dateSoumission.substring(0, 10) : dateSoumission;
    }
}
