package com.senexpert.formationservice.dto;

import lombok.Data;

@Data
public class SeanceDTO {
    private Long moduleId;
    private String date;
    private String heureDebut;
    private String heureFin;
    private String salle;
    private String lienVisio;
    private String notes;
	public Long getModuleId() {
		return moduleId;
	}
	public void setModuleId(Long moduleId) {
		this.moduleId = moduleId;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getHeureDebut() {
		return heureDebut;
	}
	public void setHeureDebut(String heureDebut) {
		this.heureDebut = heureDebut;
	}
	public String getHeureFin() {
		return heureFin;
	}
	public void setHeureFin(String heureFin) {
		this.heureFin = heureFin;
	}
	public String getSalle() {
		return salle;
	}
	public void setSalle(String salle) {
		this.salle = salle;
	}
	public String getLienVisio() {
		return lienVisio;
	}
	public void setLienVisio(String lienVisio) {
		this.lienVisio = lienVisio;
	}
	public String getNotes() {
		return notes;
	}
	public void setNotes(String notes) {
		this.notes = notes;
	}
    
    
}
