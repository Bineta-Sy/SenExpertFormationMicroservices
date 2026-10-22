package com.senexpert.formationservice.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "modules_formation")
@Data
public class Module {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titre;

    @Column(length = 1000)
    private String description;

    /** Durée estimée en heures */
    private Integer dureeHeures;

    /** Ordre d'affichage dans la formation */
    private Integer ordre;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "offre_id", nullable = false)
    private OffreFormation offre;

    /** Formateur assigné à ce module (optionnel) */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "formateur_id")
    private Formateur formateur;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitre() {
		return titre;
	}

	public void setTitre(String titre) {
		this.titre = titre;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getDureeHeures() {
		return dureeHeures;
	}

	public void setDureeHeures(Integer dureeHeures) {
		this.dureeHeures = dureeHeures;
	}

	public Integer getOrdre() {
		return ordre;
	}

	public void setOrdre(Integer ordre) {
		this.ordre = ordre;
	}

	public OffreFormation getOffre() {
		return offre;
	}

	public void setOffre(OffreFormation offre) {
		this.offre = offre;
	}

	public Formateur getFormateur() {
		return formateur;
	}

	public void setFormateur(Formateur formateur) {
		this.formateur = formateur;
	}
    
    
}
