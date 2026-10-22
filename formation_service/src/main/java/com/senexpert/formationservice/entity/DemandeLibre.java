package com.senexpert.formationservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "demandes_libre")
@Data
public class DemandeLibre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long utilisateurId;

    private String institutionNom;

    private String domaine;

    @Column(length = 1000)
    private String description;

    private Integer nbParticipants;

    private LocalDate periodeDebut;

    private LocalDate periodeFin;

    private LocalDate dateDemande;

    @Enumerated(EnumType.STRING)
    private StatutDemande statut = StatutDemande.EN_ATTENTE;

    /** Formations souhaitées associées à cette demande */
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "demande_id")
    private List<FormationSouhaitee> formationsSouhaitees = new ArrayList<>();

    /** ID de l'offre de formation créée à partir de cette demande (null si pas encore créée) */
    private Long offreId;

    /** IDs des membres institution participants à cette demande */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "demande_libre_participants", joinColumns = @JoinColumn(name = "demande_id"))
    @Column(name = "utilisateur_id")
    private List<Long> participantIds = new ArrayList<>();

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

	public String getInstitutionNom() {
		return institutionNom;
	}

	public void setInstitutionNom(String institutionNom) {
		this.institutionNom = institutionNom;
	}

	public String getDomaine() {
		return domaine;
	}

	public void setDomaine(String domaine) {
		this.domaine = domaine;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getNbParticipants() {
		return nbParticipants;
	}

	public void setNbParticipants(Integer nbParticipants) {
		this.nbParticipants = nbParticipants;
	}

	public LocalDate getPeriodeDebut() {
		return periodeDebut;
	}

	public void setPeriodeDebut(LocalDate periodeDebut) {
		this.periodeDebut = periodeDebut;
	}

	public LocalDate getPeriodeFin() {
		return periodeFin;
	}

	public void setPeriodeFin(LocalDate periodeFin) {
		this.periodeFin = periodeFin;
	}

	public LocalDate getDateDemande() {
		return dateDemande;
	}

	public void setDateDemande(LocalDate dateDemande) {
		this.dateDemande = dateDemande;
	}

	public StatutDemande getStatut() {
		return statut;
	}

	public void setStatut(StatutDemande statut) {
		this.statut = statut;
	}

	public List<FormationSouhaitee> getFormationsSouhaitees() {
		return formationsSouhaitees;
	}

	public void setFormationsSouhaitees(List<FormationSouhaitee> formationsSouhaitees) {
		this.formationsSouhaitees = formationsSouhaitees;
	}

	public Long getOffreId() {
		return offreId;
	}

	public void setOffreId(Long offreId) {
		this.offreId = offreId;
	}

	public List<Long> getParticipantIds() {
		return participantIds;
	}

	public void setParticipantIds(List<Long> participantIds) {
		this.participantIds = participantIds;
	}
    
}
