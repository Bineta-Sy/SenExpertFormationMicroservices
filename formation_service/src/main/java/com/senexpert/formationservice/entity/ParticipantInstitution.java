package com.senexpert.formationservice.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "participants_institution")
public class ParticipantInstitution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Demande de formation à laquelle ce participant est rattaché */
    @Column(nullable = false)
    private Long demandeId;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, length = 100)
    private String prenom;

    @Column(length = 150)
    private String email;

    @Column(length = 20)
    private String telephone;

    /** INSCRIT | EN_COURS | TERMINE | ABSENT */
    @Column(nullable = false, length = 20)
    private String statut = "INSCRIT";

    @Column(nullable = false)
    private LocalDateTime dateInscription;

    @PrePersist
    void onCreate() {
        if (dateInscription == null) dateInscription = LocalDateTime.now();
    }

    // ── Getters / Setters ──────────────────────────────────────────────────
    public Long          getId()               { return id; }
    public void          setId(Long v)         { id = v; }
    public Long          getDemandeId()        { return demandeId; }
    public void          setDemandeId(Long v)  { demandeId = v; }
    public String        getNom()              { return nom; }
    public void          setNom(String v)      { nom = v; }
    public String        getPrenom()           { return prenom; }
    public void          setPrenom(String v)   { prenom = v; }
    public String        getEmail()            { return email; }
    public void          setEmail(String v)    { email = v; }
    public String        getTelephone()        { return telephone; }
    public void          setTelephone(String v){ telephone = v; }
    public String        getStatut()           { return statut; }
    public void          setStatut(String v)   { statut = v; }
    public LocalDateTime getDateInscription()  { return dateInscription; }
    public void          setDateInscription(LocalDateTime v){ dateInscription = v; }
}
