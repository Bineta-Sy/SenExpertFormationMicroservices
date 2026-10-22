package com.senexpert.frontend.model;

public class EvaluationFront {

    private Long   id;
    private Long   apprenantId;
    private Long   offreId;
    private int    note;
    private String commentaire;
    private String dateEvaluation;

    // Stats synthétiques (réponse stats endpoint)
    private Double moyenneNote;
    private long   nombreEvaluations;

    public Long   getId()                       { return id; }
    public void   setId(Long v)                 { id = v; }
    public Long   getApprenantId()              { return apprenantId; }
    public void   setApprenantId(Long v)        { apprenantId = v; }
    public Long   getOffreId()                  { return offreId; }
    public void   setOffreId(Long v)            { offreId = v; }
    public int    getNote()                     { return note; }
    public void   setNote(int v)                { note = v; }
    public String getCommentaire()              { return commentaire; }
    public void   setCommentaire(String v)      { commentaire = v; }
    public String getDateEvaluation()           { return dateEvaluation; }
    public void   setDateEvaluation(String v)   { dateEvaluation = v; }
    public Double getMoyenneNote()              { return moyenneNote; }
    public void   setMoyenneNote(Double v)      { moyenneNote = v; }
    public long   getNombreEvaluations()        { return nombreEvaluations; }
    public void   setNombreEvaluations(long v)  { nombreEvaluations = v; }

    /** Retourne les étoiles pleines (★) selon la note. */
    public String getEtoiles() {
        return "★".repeat(Math.max(0, Math.min(5, note)))
             + "☆".repeat(Math.max(0, 5 - Math.min(5, note)));
    }
}
