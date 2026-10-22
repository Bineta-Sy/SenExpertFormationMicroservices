package com.senexpert.formationservice.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ID de l'offre de formation concernée (nullable pour messagerie institution) */
    @Column
    private Long offreId;

    /** ID de la demande — utilisé pour la messagerie agent institution ↔ admin */
    @Column
    private Long demandeId;

    /** ID de l'expéditeur */
    @Column(nullable = false)
    private Long expediteurId;

    /** APPRENANT ou FORMATEUR */
    @Column(nullable = false, length = 20)
    private String expediteurRole;

    @Column(nullable = false, length = 150)
    private String expediteurNom;

    /** ID du destinataire (formateur ou apprenant) */
    @Column(nullable = false)
    private Long destinataireId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String contenu;

    @Column(nullable = false)
    private LocalDateTime dateEnvoi;

    private boolean lu = false;

    @PrePersist
    void onCreate() {
        if (dateEnvoi == null) dateEnvoi = LocalDateTime.now();
    }

    // ── Getters / Setters ──────────────────────────────────────────────────
    public Long            getId()               { return id; }
    public void            setId(Long v)         { id = v; }
    public Long            getOffreId()          { return offreId; }
    public void            setOffreId(Long v)    { offreId = v; }
    public Long            getDemandeId()        { return demandeId; }
    public void            setDemandeId(Long v)  { demandeId = v; }
    public Long            getExpediteurId()     { return expediteurId; }
    public void            setExpediteurId(Long v){ expediteurId = v; }
    public String          getExpediteurRole()   { return expediteurRole; }
    public void            setExpediteurRole(String v){ expediteurRole = v; }
    public String          getExpediteurNom()    { return expediteurNom; }
    public void            setExpediteurNom(String v){ expediteurNom = v; }
    public Long            getDestinataireId()   { return destinataireId; }
    public void            setDestinataireId(Long v){ destinataireId = v; }
    public String          getContenu()          { return contenu; }
    public void            setContenu(String v)  { contenu = v; }
    public LocalDateTime   getDateEnvoi()        { return dateEnvoi; }
    public void            setDateEnvoi(LocalDateTime v){ dateEnvoi = v; }
    public boolean         isLu()                { return lu; }
    public void            setLu(boolean v)      { lu = v; }
}
