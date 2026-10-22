package com.senexpert.formationservice.dto;

import lombok.Data;

@Data
public class ModuleDTO {
    private String titre;
    private String description;
    private Integer dureeHeures;
    private Integer ordre;
    private Long offreId;
    private Long formateurId; // optionnel : formateur assigné
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
	public Long getOffreId() {
		return offreId;
	}
	public void setOffreId(Long offreId) {
		this.offreId = offreId;
	}
	public Long getFormateurId() {
		return formateurId;
	}
	public void setFormateurId(Long formateurId) {
		this.formateurId = formateurId;
	}
    
    
    
}
