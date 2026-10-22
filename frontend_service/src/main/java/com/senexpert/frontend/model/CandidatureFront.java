package com.senexpert.frontend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CandidatureFront {
    private Long id;
    private Long utilisateurId;
    private String nomCandidat;
    private String dateCreation;
    private String statut;
    private String commentaire;
    private OffreFormation offre;
    private String fichierParticipantsNom;
    private String fichierParticipantsType;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(Long utilisateurId) { this.utilisateurId = utilisateurId; }
    public String getNomCandidat() { return nomCandidat; }
    public void setNomCandidat(String nomCandidat) { this.nomCandidat = nomCandidat; }
    public String getDateCreation() { return dateCreation; }
    public void setDateCreation(String dateCreation) { this.dateCreation = dateCreation; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }
    public OffreFormation getOffre() { return offre; }
    public void setOffre(OffreFormation offre) { this.offre = offre; }
    public String getFichierParticipantsNom() { return fichierParticipantsNom; }
    public void setFichierParticipantsNom(String v) { this.fichierParticipantsNom = v; }
    public String getFichierParticipantsType() { return fichierParticipantsType; }
    public void setFichierParticipantsType(String v) { this.fichierParticipantsType = v; }
    public boolean hasFichierParticipants() { return fichierParticipantsNom != null && !fichierParticipantsNom.isBlank(); }
}
