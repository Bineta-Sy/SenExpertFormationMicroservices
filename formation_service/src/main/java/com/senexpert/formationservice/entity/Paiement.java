package com.senexpert.formationservice.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "paiements")
public class Paiement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long utilisateurId;

    /** nullable pour les paiements institution sans offre directe */
    @Column
    private Long offreId;

    /** Lien optionnel à une demande d'institution */
    @Column
    private Long demandeId;

    @Column(nullable = false, length = 200)
    private String offreTitre;

    @Column(nullable = false)
    private Double montant;

    @Column(nullable = false, length = 50)
    private String methodePaiement;

    @Column(nullable = false, unique = true, length = 60)
    private String reference;

    @Column(nullable = false)
    private LocalDateTime datePaiement;

    @Column(nullable = false, length = 20)
    private String statut = "SUCCES"; // SUCCES | ECHEC | REMBOURSE

    @PrePersist
    void onCreate() {
        if (datePaiement == null) datePaiement = LocalDateTime.now();
    }

    // ── Getters / Setters ──────────────────────────────────────────────────
    public Long           getId()               { return id; }
    public void           setId(Long v)         { id = v; }
    public Long           getUtilisateurId()    { return utilisateurId; }
    public void           setUtilisateurId(Long v){ utilisateurId = v; }
    public Long           getOffreId()          { return offreId; }
    public void           setOffreId(Long v)    { offreId = v; }
    public Long           getDemandeId()        { return demandeId; }
    public void           setDemandeId(Long v)  { demandeId = v; }
    public String         getOffreTitre()       { return offreTitre; }
    public void           setOffreTitre(String v){ offreTitre = v; }
    public Double         getMontant()          { return montant; }
    public void           setMontant(Double v)  { montant = v; }
    public String         getMethodePaiement()  { return methodePaiement; }
    public void           setMethodePaiement(String v){ methodePaiement = v; }
    public String         getReference()        { return reference; }
    public void           setReference(String v){ reference = v; }
    public LocalDateTime  getDatePaiement()     { return datePaiement; }
    public void           setDatePaiement(LocalDateTime v){ datePaiement = v; }
    public String         getStatut()           { return statut; }
    public void           setStatut(String v)   { statut = v; }
}
