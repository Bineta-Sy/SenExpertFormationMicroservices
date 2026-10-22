package com.senexpert.formationservice.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "formations_souhaitees")
@Data
public class FormationSouhaitee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Demande à la carte parente */
    @Column(name = "demande_id", nullable = false)
    private Long demandeId;

    /** Thématique / intitulé de la formation souhaitée */
    @Column(nullable = false)
    private String thematique;

    /** Durée en jours */
    private Integer duree;

    /** Nombre de participants ciblé pour cette formation */
    private Integer nbParticipants;

    /** Objectifs pédagogiques */
    @Column(length = 1000)
    private String objectifs;

    /** Niveau requis (DEBUTANT, INTERMEDIAIRE, AVANCE) */
    private String niveauRequis;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getDemandeId() {
		return demandeId;
	}

	public void setDemandeId(Long demandeId) {
		this.demandeId = demandeId;
	}

	public String getThematique() {
		return thematique;
	}

	public void setThematique(String thematique) {
		this.thematique = thematique;
	}

	public Integer getDuree() {
		return duree;
	}

	public void setDuree(Integer duree) {
		this.duree = duree;
	}

	public Integer getNbParticipants() {
		return nbParticipants;
	}

	public void setNbParticipants(Integer nbParticipants) {
		this.nbParticipants = nbParticipants;
	}

	public String getObjectifs() {
		return objectifs;
	}

	public void setObjectifs(String objectifs) {
		this.objectifs = objectifs;
	}

	public String getNiveauRequis() {
		return niveauRequis;
	}

	public void setNiveauRequis(String niveauRequis) {
		this.niveauRequis = niveauRequis;
	}
    
}
