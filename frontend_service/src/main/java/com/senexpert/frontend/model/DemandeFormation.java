package com.senexpert.frontend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DemandeFormation {
    private Long id;
    private String dateDemande;
    private Long utilisateurId;
    private String statut;
    private OffreFormation offre;
    // champ transient : résolu côté frontend depuis user_service
    private String nomCandidat;

    // Fichier liste des participants (institution)
    private String fichierParticipantsNom;
    private String fichierParticipantsType;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDateDemande() { return dateDemande; }
    public void setDateDemande(String dateDemande) { this.dateDemande = dateDemande; }
    public Long getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(Long utilisateurId) { this.utilisateurId = utilisateurId; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public OffreFormation getOffre() { return offre; }
    public void setOffre(OffreFormation offre) { this.offre = offre; }
    public String getNomCandidat() { return nomCandidat; }
    public void setNomCandidat(String nomCandidat) { this.nomCandidat = nomCandidat; }
    public String getFichierParticipantsNom() { return fichierParticipantsNom; }
    public void setFichierParticipantsNom(String v) { this.fichierParticipantsNom = v; }
    public String getFichierParticipantsType() { return fichierParticipantsType; }
    public void setFichierParticipantsType(String v) { this.fichierParticipantsType = v; }
}
