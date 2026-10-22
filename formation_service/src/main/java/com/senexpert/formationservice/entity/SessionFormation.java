package com.senexpert.formationservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "sessions_formation")
@Data
public class SessionFormation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "offre_id", nullable = false)
    private OffreFormation offre;

    /** Date et heure de début de la session */
    private LocalDateTime dateHeure;

    /** Durée en minutes */
    private Integer dureeMinutes;

    /** Salle ou lieu */
    private String salle;

    /** Formateur responsable */
    private String formateur;

    /** Nombre de places disponibles pour cette session */
    private Integer nbPlaces;

    /** Statut : PLANIFIEE, EN_COURS, TERMINEE, ANNULEE */
    @Enumerated(EnumType.STRING)
    private StatutSession statut = StatutSession.PLANIFIEE;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public OffreFormation getOffre() {
		return offre;
	}

	public void setOffre(OffreFormation offre) {
		this.offre = offre;
	}

	public LocalDateTime getDateHeure() {
		return dateHeure;
	}

	public void setDateHeure(LocalDateTime dateHeure) {
		this.dateHeure = dateHeure;
	}

	public Integer getDureeMinutes() {
		return dureeMinutes;
	}

	public void setDureeMinutes(Integer dureeMinutes) {
		this.dureeMinutes = dureeMinutes;
	}

	public String getSalle() {
		return salle;
	}

	public void setSalle(String salle) {
		this.salle = salle;
	}

	public String getFormateur() {
		return formateur;
	}

	public void setFormateur(String formateur) {
		this.formateur = formateur;
	}

	public Integer getNbPlaces() {
		return nbPlaces;
	}

	public void setNbPlaces(Integer nbPlaces) {
		this.nbPlaces = nbPlaces;
	}

	public StatutSession getStatut() {
		return statut;
	}

	public void setStatut(StatutSession statut) {
		this.statut = statut;
	}
    
    
}
