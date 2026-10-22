package com.senexpert.formationservice.dto;

import lombok.Data;

@Data
public class DemandeLibreDTO {
    private Long utilisateurId;
    private String institutionNom;
    private String domaine;
    private String description;
    private Integer nbParticipants;
    private String periodeDebut;   // format ISO : yyyy-MM-dd
    private String periodeFin;     // format ISO : yyyy-MM-dd
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
	public String getPeriodeDebut() {
		return periodeDebut;
	}
	public void setPeriodeDebut(String periodeDebut) {
		this.periodeDebut = periodeDebut;
	}
	public String getPeriodeFin() {
		return periodeFin;
	}
	public void setPeriodeFin(String periodeFin) {
		this.periodeFin = periodeFin;
	}
    
    
}
