package com.senexpert.formationservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "demandes_formation")
@Data
public class DemandeFormation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate dateDemande;

    private Long utilisateurId;

    @Enumerated(EnumType.STRING)
    private StatutDemande statut;

    /** Chemins des fichiers déposés (CV, diplôme, lettre…) — JSON array. */
    @Column(columnDefinition = "TEXT")
    private String documentsJson;

    @ManyToOne
    @JoinColumn(name = "offre_id")
    private OffreFormation offre;

    /** Fichier liste des participants fourni par l'institution (Word, Excel, PDF…) */
    @Column(name = "fichier_participants_path")
    private String fichierParticipantsPath;

    @Column(name = "fichier_participants_nom")
    private String fichierParticipantsNom;

    @Column(name = "fichier_participants_type")
    private String fichierParticipantsType;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public LocalDate getDateDemande() {
		return dateDemande;
	}

	public void setDateDemande(LocalDate dateDemande) {
		this.dateDemande = dateDemande;
	}

	public Long getUtilisateurId() {
		return utilisateurId;
	}

	public void setUtilisateurId(Long utilisateurId) {
		this.utilisateurId = utilisateurId;
	}

	public StatutDemande getStatut() {
		return statut;
	}

	public void setStatut(StatutDemande statut) {
		this.statut = statut;
	}

	public String getDocumentsJson() {
		return documentsJson;
	}

	public void setDocumentsJson(String documentsJson) {
		this.documentsJson = documentsJson;
	}

	public OffreFormation getOffre() {
		return offre;
	}

	public void setOffre(OffreFormation offre) {
		this.offre = offre;
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