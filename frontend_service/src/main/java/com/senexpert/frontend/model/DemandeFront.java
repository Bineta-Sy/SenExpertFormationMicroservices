package com.senexpert.frontend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DemandeFront {
    private Long id;
    private String dateDemande;
    private Long utilisateurId;
    private String statut;
    private String nomCandidat;
    private OffreFormation offre;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDateDemande() { return dateDemande; }
    public void setDateDemande(String dateDemande) { this.dateDemande = dateDemande; }
    public Long getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(Long utilisateurId) { this.utilisateurId = utilisateurId; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public String getNomCandidat() { return nomCandidat; }
    public void setNomCandidat(String nomCandidat) { this.nomCandidat = nomCandidat; }
    public OffreFormation getOffre() { return offre; }
    public void setOffre(OffreFormation offre) { this.offre = offre; }
}
