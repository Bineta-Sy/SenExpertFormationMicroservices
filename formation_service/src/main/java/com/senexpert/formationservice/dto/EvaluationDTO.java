package com.senexpert.formationservice.dto;

public class EvaluationDTO {

    private Long   id;
    private Long   apprenantId;
    private Long   offreId;
    private int    note;
    private String commentaire;
    private String dateEvaluation;

    // Pour les réponses synthétiques
    private Double moyenneNote;
    private long   nombreEvaluations;

    // ── Getters / Setters ──────────────────────────────────────────────────
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
}
