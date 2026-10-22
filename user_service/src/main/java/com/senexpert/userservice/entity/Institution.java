package com.senexpert.userservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "institutions")
@Data
public class Institution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;

    @Enumerated(EnumType.STRING)
    private TypeInstitution type;

    private String adresse;

    /** Secteur d'activité (ex: Industrie et Manufacturing) */
    private String secteur;

    /** NINEA ou registre de commerce */
    private String ninea;

    /** Domaines d'intérêt, séparés par des virgules */
    @Column(length = 500)
    private String domainesInteret;

    /** Date de validation par l'admin */
    private LocalDate dateValidation;

    @Enumerated(EnumType.STRING)
    private Statut statut;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNom() {
		return nom;
	}

	public void setNom(String nom) {
		this.nom = nom;
	}

	public TypeInstitution getType() {
		return type;
	}

	public void setType(TypeInstitution type) {
		this.type = type;
	}

	public String getAdresse() {
		return adresse;
	}

	public void setAdresse(String adresse) {
		this.adresse = adresse;
	}

	public String getSecteur() {
		return secteur;
	}

	public void setSecteur(String secteur) {
		this.secteur = secteur;
	}

	public String getNinea() {
		return ninea;
	}

	public void setNinea(String ninea) {
		this.ninea = ninea;
	}

	public String getDomainesInteret() {
		return domainesInteret;
	}

	public void setDomainesInteret(String domainesInteret) {
		this.domainesInteret = domainesInteret;
	}

	public LocalDate getDateValidation() {
		return dateValidation;
	}

	public void setDateValidation(LocalDate dateValidation) {
		this.dateValidation = dateValidation;
	}

	public Statut getStatut() {
		return statut;
	}

	public void setStatut(Statut statut) {
		this.statut = statut;
	}
    
}