package com.senexpert.formationservice.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "evaluations_formation",
       uniqueConstraints = @UniqueConstraint(columnNames = {"apprenant_id", "offre_id"}))
public class EvaluationFormation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "apprenant_id", nullable = false)
    private Long apprenantId;

    @Column(name = "offre_id", nullable = false)
    private Long offreId;

    /** Note de 1 à 5 */
    @Column(nullable = false)
    private int note;

    @Column(columnDefinition = "TEXT")
    private String commentaire;

    @Column(nullable = false)
    private LocalDateTime dateEvaluation;

    @PrePersist
    void onCreate() {
        if (dateEvaluation == null) dateEvaluation = LocalDateTime.now();
    }

    // ── Getters / Setters ──────────────────────────────────────────────────
    public Long            getId()                  { return id; }
    public void            setId(Long v)            { id = v; }
    public Long            getApprenantId()         { return apprenantId; }
    public void            setApprenantId(Long v)   { apprenantId = v; }
    public Long            getOffreId()             { return offreId; }
    public void            setOffreId(Long v)       { offreId = v; }
    public int             getNote()                { return note; }
    public void            setNote(int v)           { note = v; }
    public String          getCommentaire()         { return commentaire; }
    public void            setCommentaire(String v) { commentaire = v; }
    public LocalDateTime   getDateEvaluation()      { return dateEvaluation; }
    public void            setDateEvaluation(LocalDateTime v){ dateEvaluation = v; }
    
}
