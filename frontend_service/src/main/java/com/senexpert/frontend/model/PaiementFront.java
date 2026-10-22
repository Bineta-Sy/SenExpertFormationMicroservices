package com.senexpert.frontend.model;

public class PaiementFront {

    private Long   id;
    private Long   utilisateurId;
    private Long   offreId;
    private String offreTitre;
    private Double montant;
    private String methodePaiement;
    private String reference;
    private String datePaiement;
    private String statut;

    public Long   getId()                    { return id; }
    public void   setId(Long v)              { id = v; }
    public Long   getUtilisateurId()         { return utilisateurId; }
    public void   setUtilisateurId(Long v)   { utilisateurId = v; }
    public Long   getOffreId()               { return offreId; }
    public void   setOffreId(Long v)         { offreId = v; }
    public String getOffreTitre()            { return offreTitre; }
    public void   setOffreTitre(String v)    { offreTitre = v; }
    public Double getMontant()               { return montant; }
    public void   setMontant(Double v)       { montant = v; }
    public String getMethodePaiement()       { return methodePaiement; }
    public void   setMethodePaiement(String v){ methodePaiement = v; }
    public String getReference()             { return reference; }
    public void   setReference(String v)     { reference = v; }
    public String getDatePaiement()          { return datePaiement; }
    public void   setDatePaiement(String v)  { datePaiement = v; }
    public String getStatut()                { return statut; }
    public void   setStatut(String v)        { statut = v; }

    public String getMontantFormate() {
        if (montant == null || montant <= 0) return "Gratuit";
        return String.format("%,.0f FCFA", montant).replace(",", " ");
    }

    public boolean isGratuit() { return montant == null || montant <= 0; }
}
