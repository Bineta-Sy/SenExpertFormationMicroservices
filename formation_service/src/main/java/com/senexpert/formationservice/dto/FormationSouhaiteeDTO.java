package com.senexpert.formationservice.dto;

import lombok.Data;

@Data
public class FormationSouhaiteeDTO {
    private String thematique;
    private Integer duree;
    private Integer nbParticipants;
    private String objectifs;
    private String niveauRequis;
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
