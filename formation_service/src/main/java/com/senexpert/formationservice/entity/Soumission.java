package com.senexpert.formationservice.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "soumissions")
public class Soumission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long coursId;

    @Column(nullable = false)
    private Long apprenantId;

    @Column(length = 200)
    private String nomApprenant; // dénormalisé pour affichage formateur

    // ── Contenu soumis ─────────────────────────────────────────────────────
    @Column(length = 500)
    private String cheminFichier;

    @Column(length = 255)
    private String nomFichierOriginal;

    private Long tailleFichier;

    @Column(columnDefinition = "TEXT")
    private String contenuTexte;

    @Column(length = 500)
    private String lienUrl;

    // ── Horodatage ─────────────────────────────────────────────────────────
    @Column(nullable = false)
    private LocalDateTime dateSoumission;

    /** Pour les examens QCM : heure de début (calcul du temps restant). */
    private LocalDateTime debutExamen;

    private boolean enRetard = false;

    // ── Correction ─────────────────────────────────────────────────────────
    private Double note;

    @Column(columnDefinition = "TEXT")
    private String commentaireFormateur;

    private LocalDateTime dateCorrection;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutSoumission statut = StatutSoumission.EN_ATTENTE;

    @PrePersist
    void onCreate() {
        if (dateSoumission == null) dateSoumission = LocalDateTime.now();
    }

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
    public String getContenuTexte()               { return contenuTexte; }
    public void   setContenuTexte(String v)       { contenuTexte = v; }
    public String getLienUrl()                    { return lienUrl; }
    public void   setLienUrl(String v)            { lienUrl = v; }
    public LocalDateTime getDateSoumission()      { return dateSoumission; }
    public void   setDateSoumission(LocalDateTime v){ dateSoumission = v; }
    public LocalDateTime getDebutExamen()         { return debutExamen; }
    public void   setDebutExamen(LocalDateTime v) { debutExamen = v; }
    public boolean isEnRetard()                   { return enRetard; }
    public void   setEnRetard(boolean v)          { enRetard = v; }
    public Double  getNote()                      { return note; }
    public void   setNote(Double v)               { note = v; }
    public String getCommentaireFormateur()       { return commentaireFormateur; }
    public void   setCommentaireFormateur(String v){ commentaireFormateur = v; }
    public LocalDateTime getDateCorrection()      { return dateCorrection; }
    public void   setDateCorrection(LocalDateTime v){ dateCorrection = v; }
    public StatutSoumission getStatut()           { return statut; }
    public void   setStatut(StatutSoumission v)   { statut = v; }
}
