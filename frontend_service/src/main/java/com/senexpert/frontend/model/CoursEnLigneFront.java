package com.senexpert.frontend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CoursEnLigneFront {

    private Long    id;
    private String  titre;
    private String  description;
    private String  instructionsDetaillees;
    private String  type;         // VIDEO | DOCUMENT | QUIZ | LIEN | LIVE | TP | DEVOIR | EXAMEN
    private String  urlContenu;
    private Integer ordre;
    private Integer dureeMinutes;
    private boolean publie;

    // TP / DEVOIR / EXAMEN
    private String  dateDebut;    // ISO — ouverture des soumissions
    private String  dateLimite;   // ISO — fermeture des soumissions
    private Integer baremeNote;
    private Integer dureeExamen;  // minutes pour examen QCM
    private String  typeExamen;   // "QCM" ou "FICHIER"

    // LIVE
    private String  lienVisio;
    private String  plateforme;
    private String  dateHeureLive;

    // Relation
    private Long    offreId;
    private String  offreTitre;

    // Formateur spécifique (optionnel)
    private Long    formateurId;
    private String  formateurNom;

    // Fichier uploadé localement (pour VIDEO/DOCUMENT)
    private String  cheminFichier;
    private String  nomFichierOriginal;
    private Long    tailleFichier;
    private String  urlFichier;

    // Quiz
    private List<QuizQuestionFront> questions;
    private int     nbQuestions;

    // ── Getters / Setters ──────────────────────────────────────────────────
    public Long    getId()                        { return id; }
    public void    setId(Long v)                  { id = v; }
    public String  getTitre()                     { return titre; }
    public void    setTitre(String v)             { titre = v; }
    public String  getDescription()               { return description; }
    public void    setDescription(String v)       { description = v; }
    public String  getInstructionsDetaillees()    { return instructionsDetaillees; }
    public void    setInstructionsDetaillees(String v){ instructionsDetaillees = v; }
    public String  getType()                      { return type; }
    public void    setType(String v)              { type = v; }
    public String  getUrlContenu()                { return urlContenu; }
    public void    setUrlContenu(String v)        { urlContenu = v; }
    public Integer getOrdre()                     { return ordre; }
    public void    setOrdre(Integer v)            { ordre = v; }
    public Integer getDureeMinutes()              { return dureeMinutes; }
    public void    setDureeMinutes(Integer v)     { dureeMinutes = v; }
    public boolean isPublie()                     { return publie; }
    public void    setPublie(boolean v)           { publie = v; }
    public String  getDateDebut()                 { return dateDebut; }
    public void    setDateDebut(String v)         { dateDebut = v; }
    public String  getDateLimite()                { return dateLimite; }
    public void    setDateLimite(String v)        { dateLimite = v; }
    public Integer getBaremeNote()                { return baremeNote; }
    public void    setBaremeNote(Integer v)       { baremeNote = v; }
    public Integer getDureeExamen()               { return dureeExamen; }
    public void    setDureeExamen(Integer v)      { dureeExamen = v; }
    public String  getTypeExamen()                { return typeExamen; }
    public void    setTypeExamen(String v)        { typeExamen = v; }
    public String  getLienVisio()                 { return lienVisio; }
    public void    setLienVisio(String v)         { lienVisio = v; }
    public String  getPlateforme()                { return plateforme; }
    public void    setPlateforme(String v)        { plateforme = v; }
    public String  getDateHeureLive()             { return dateHeureLive; }
    public void    setDateHeureLive(String v)     { dateHeureLive = v; }
    public Long    getOffreId()                   { return offreId; }
    public void    setOffreId(Long v)             { offreId = v; }
    public String  getOffreTitre()                { return offreTitre; }
    public void    setOffreTitre(String v)        { offreTitre = v; }
    public Long    getFormateurId()               { return formateurId; }
    public void    setFormateurId(Long v)         { formateurId = v; }
    public String  getFormateurNom()              { return formateurNom; }
    public void    setFormateurNom(String v)      { formateurNom = v; }
    public String  getCheminFichier()             { return cheminFichier; }
    public void    setCheminFichier(String v)     { cheminFichier = v; }
    public String  getNomFichierOriginal()        { return nomFichierOriginal; }
    public void    setNomFichierOriginal(String v){ nomFichierOriginal = v; }
    public Long    getTailleFichier()             { return tailleFichier; }
    public void    setTailleFichier(Long v)       { tailleFichier = v; }
    public String  getUrlFichier()                { return urlFichier; }
    public void    setUrlFichier(String v)        { urlFichier = v; }
    public boolean isAFichierLocal()              { return cheminFichier != null && !cheminFichier.isBlank(); }
    public String  getTailleFormatee() {
        if (tailleFichier == null) return "";
        if (tailleFichier < 1024)       return tailleFichier + " o";
        if (tailleFichier < 1024*1024)  return String.format("%.1f Ko", tailleFichier / 1024.0);
        return String.format("%.1f Mo", tailleFichier / (1024.0 * 1024));
    }
    public List<QuizQuestionFront> getQuestions()          { return questions; }
    public void    setQuestions(List<QuizQuestionFront> v) { questions = v; }
    public int     getNbQuestions()               { return nbQuestions; }
    public void    setNbQuestions(int v)          { nbQuestions = v; }

    // ── Helpers type ────────────────────────────────────────────────────────
    public boolean isLive()      { return "LIVE".equals(type); }
    public boolean isTP()        { return "TP".equals(type); }
    public boolean isDevoir()    { return "DEVOIR".equals(type); }
    public boolean isExamen()    { return "EXAMEN".equals(type); }
    public boolean isExamenQCM() { return "EXAMEN".equals(type) && "QCM".equals(typeExamen); }
    public boolean isExamenFichier() { return "EXAMEN".equals(type) && "FICHIER".equals(typeExamen); }
    public boolean isTravauxPratiques() { return isTP() || isDevoir() || isExamen(); }

    public String getTypeLabel() {
        if (type == null) return "";
        return switch (type) {
            case "VIDEO"    -> "Vidéo";
            case "DOCUMENT" -> "Document";
            case "QUIZ"     -> "Quiz";
            case "LIEN"     -> "Lien";
            case "LIVE"     -> "Session live";
            case "TP"       -> "TP";
            case "DEVOIR"   -> "Devoir";
            case "EXAMEN"   -> "Examen" + ("QCM".equals(typeExamen) ? " QCM" : " fichier");
            default -> type;
        };
    }

    public String getTypeIcon() {
        if (type == null) return "ti-file";
        return switch (type) {
            case "VIDEO"    -> "ti-player-play";
            case "DOCUMENT" -> "ti-file-text";
            case "QUIZ"     -> "ti-checklist";
            case "LIEN"     -> "ti-external-link";
            case "LIVE"     -> "ti-video";
            case "TP"       -> "ti-tools";
            case "DEVOIR"   -> "ti-clipboard-text";
            case "EXAMEN"   -> "ti-school";
            default -> "ti-file";
        };
    }

    public String getTypeCouleur() {
        if (type == null) return "#64748B";
        return switch (type) {
            case "VIDEO"    -> "#DC2626";
            case "DOCUMENT" -> "#2563EB";
            case "QUIZ"     -> "#7C3AED";
            case "LIEN"     -> "#059669";
            case "LIVE"     -> "#D97706";
            case "TP"       -> "#0891B2";  // cyan
            case "DEVOIR"   -> "#7C3AED";  // violet
            case "EXAMEN"   -> "#DC2626";  // rouge
            default -> "#64748B";
        };
    }

    /** Retourne le bareme ou 20 par défaut. */
    public int getBaremeOu20() { return baremeNote != null ? baremeNote : 20; }

    // ── Statut (publié / brouillon / à venir) ─────────────────────────────
    public String getStatutLabel() {
        if ("LIVE".equals(type)) return "À venir";
        return publie ? "Publié" : "Brouillon";
    }
    public String getStatutBg() {
        return switch (getStatutLabel()) {
            case "Publié"  -> "#D1FAE5";
            case "À venir" -> "#FEF3C7";
            default        -> "#F1F5F9";
        };
    }
    public String getStatutColor() {
        return switch (getStatutLabel()) {
            case "Publié"  -> "#065F46";
            case "À venir" -> "#92400E";
            default        -> "#64748B";
        };
    }

    /** Retourne les 10 premiers caractères de dateDebut (ex: "2025-09-01"). */
    public String getDateDebutCourte() {
        if (dateDebut == null || dateDebut.length() < 10) return dateDebut;
        return dateDebut.substring(0, 10);
    }

    /** Retourne les 10 premiers caractères de dateLimite (ex: "2025-09-30"). */
    public String getDateLimiteCourte() {
        if (dateLimite == null || dateLimite.length() < 10) return dateLimite;
        return dateLimite.substring(0, 10);
    }
}
