package com.senexpert.formationservice.dto;

import java.util.List;

public class CoursEnLigneDTO {

    private Long id;
    private String titre;
    private String description;
    private String instructionsDetaillees;
    private String type;
    private String urlContenu;
    private Integer ordre;
    private Integer dureeMinutes;
    private boolean publie;

    // TP / DEVOIR / EXAMEN
    private String  dateDebut;    // ISO string — ouverture des soumissions
    private String  dateLimite;   // ISO string — fermeture des soumissions
    private Integer baremeNote;
    private Integer dureeExamen;
    private String  typeExamen;   // "QCM" ou "FICHIER"

    // LIVE
    private String lienVisio;
    private String plateforme;
    private String dateHeureLive;

    // Relation offre
    private Long   offreId;
    private String offreTitre;

    // Formateur
    private Long   formateurId;
    private String formateurNom;

    // Fichier uploadé
    private String cheminFichier;
    private String nomFichierOriginal;
    private Long   tailleFichier;
    private String urlFichier;

    // Quiz
    private List<QuizQuestionDTO> questions;
    private int nbQuestions;

    public CoursEnLigneDTO() {}

    // ── Getters / Setters ─────────────────────────────────────────────────────
    public Long   getId()                        { return id; }
    public void   setId(Long v)                  { id = v; }
    public String getTitre()                     { return titre; }
    public void   setTitre(String v)             { titre = v; }
    public String getDescription()               { return description; }
    public void   setDescription(String v)       { description = v; }
    public String getInstructionsDetaillees()    { return instructionsDetaillees; }
    public void   setInstructionsDetaillees(String v) { instructionsDetaillees = v; }
    public String getType()                      { return type; }
    public void   setType(String v)              { type = v; }
    public String getUrlContenu()                { return urlContenu; }
    public void   setUrlContenu(String v)        { urlContenu = v; }
    public Integer getOrdre()                    { return ordre; }
    public void   setOrdre(Integer v)            { ordre = v; }
    public Integer getDureeMinutes()             { return dureeMinutes; }
    public void   setDureeMinutes(Integer v)     { dureeMinutes = v; }
    public boolean isPublie()                    { return publie; }
    public void   setPublie(boolean v)           { publie = v; }
    public String getDateDebut()                 { return dateDebut; }
    public void   setDateDebut(String v)         { dateDebut = v; }
    public String getDateLimite()                { return dateLimite; }
    public void   setDateLimite(String v)        { dateLimite = v; }
    public Integer getBaremeNote()               { return baremeNote; }
    public void   setBaremeNote(Integer v)       { baremeNote = v; }
    public Integer getDureeExamen()              { return dureeExamen; }
    public void   setDureeExamen(Integer v)      { dureeExamen = v; }
    public String getTypeExamen()                { return typeExamen; }
    public void   setTypeExamen(String v)        { typeExamen = v; }
    public String getLienVisio()                 { return lienVisio; }
    public void   setLienVisio(String v)         { lienVisio = v; }
    public String getPlateforme()                { return plateforme; }
    public void   setPlateforme(String v)        { plateforme = v; }
    public String getDateHeureLive()             { return dateHeureLive; }
    public void   setDateHeureLive(String v)     { dateHeureLive = v; }
    public Long   getOffreId()                   { return offreId; }
    public void   setOffreId(Long v)             { offreId = v; }
    public String getOffreTitre()                { return offreTitre; }
    public void   setOffreTitre(String v)        { offreTitre = v; }
    public Long   getFormateurId()               { return formateurId; }
    public void   setFormateurId(Long v)         { formateurId = v; }
    public String getFormateurNom()              { return formateurNom; }
    public void   setFormateurNom(String v)      { formateurNom = v; }
    public String getCheminFichier()             { return cheminFichier; }
    public void   setCheminFichier(String v)     { cheminFichier = v; }
    public String getNomFichierOriginal()        { return nomFichierOriginal; }
    public void   setNomFichierOriginal(String v){ nomFichierOriginal = v; }
    public Long   getTailleFichier()             { return tailleFichier; }
    public void   setTailleFichier(Long v)       { tailleFichier = v; }
    public String getUrlFichier()                { return urlFichier; }
    public void   setUrlFichier(String v)        { urlFichier = v; }
    public List<QuizQuestionDTO> getQuestions()        { return questions; }
    public void setQuestions(List<QuizQuestionDTO> v)  { questions = v; }
    public int  getNbQuestions()                 { return nbQuestions; }
    public void setNbQuestions(int v)            { nbQuestions = v; }
}
