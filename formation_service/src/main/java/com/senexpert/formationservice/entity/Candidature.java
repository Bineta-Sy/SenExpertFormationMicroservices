package com.senexpert.formationservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "candidatures")
@Data
public class Candidature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long utilisateurId;
    private String nomCandidat;

    @ManyToOne
    @JoinColumn(name = "offre_id")
    private OffreFormation offre;

    private LocalDate dateCreation = LocalDate.now();

    @Enumerated(EnumType.STRING)
    private StatutCandidature statut = StatutCandidature.EN_ATTENTE;

    private String commentaire;

    /** Fichier liste des participants (institutions) — chemin sur le filesystem */
    @Column(name = "fichier_participants_path")
    private String fichierParticipantsPath;

    /** Nom original du fichier uploadé (ex: participants.xlsx) */
    @Column(name = "fichier_participants_nom")
    private String fichierParticipantsNom;

    /** Type MIME du fichier (ex: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet) */
    @Column(name = "fichier_participants_type")
    private String fichierParticipantsType;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getUtilisateurId() {
		return utilisateurId;
	}

	public void setUtilisateurId(Long utilisateurId) {
		this.utilisateurId = utilisateurId;
	}

	public String getNomCandidat() {
		return nomCandidat;
	}

	public void setNomCandidat(String nomCandidat) {
		this.nomCandidat = nomCandidat;
	}

	public OffreFormation getOffre() {
		return offre;
	}

	public void setOffre(OffreFormation offre) {
		this.offre = offre;
	}

	public LocalDate getDateCreation() {
		return dateCreation;
	}

	public void setDateCreation(LocalDate dateCreation) {
		this.dateCreation = dateCreation;
	}

	public StatutCandidature getStatut() {
		return statut;
	}

	public void setStatut(StatutCandidature statut) {
		this.statut = statut;
	}

	public String getCommentaire() {
		return commentaire;
	}

	public void setCommentaire(String commentaire) {
		this.commentaire = commentaire;
	}

	public String getFichierParticipantsPath() {
		return fichierParticipantsPath;
	}

	public void setFichierParticipantsPath(String fichierParticipantsPath) {
		this.fichierParticipantsPath = fichierParticipantsPath;
	}

	public String getFichierParticipantsNom() {
		return fichierParticipantsNom;
	}

	public void setFichierParticipantsNom(String fichierParticipantsNom) {
		this.fichierParticipantsNom = fichierParticipantsNom;
	}

	public String getFichierParticipantsType() {
		return fichierParticipantsType;
	}

	public void setFichierParticipantsType(String fichierParticipantsType) {
		this.fichierParticipantsType = fichierParticipantsType;
	}
    
    
}
