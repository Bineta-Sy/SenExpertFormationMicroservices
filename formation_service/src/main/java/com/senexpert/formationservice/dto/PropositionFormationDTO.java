package com.senexpert.formationservice.dto;

import lombok.Data;

@Data
public class PropositionFormationDTO {
    private Long   demandeLibreId;
    private String formateurNom;
    private String dateDebut;   // yyyy-MM-dd
    private String dateFin;     // yyyy-MM-dd
    private String lieu;
    private Double montant;
    private String programme;
    private String noteAdmin;
    private String mode;
    private java.util.List<String> modulesProposition = new java.util.ArrayList<>();
	public Long getDemandeLibreId() {
		return demandeLibreId;
	}
	public void setDemandeLibreId(Long demandeLibreId) {
		this.demandeLibreId = demandeLibreId;
	}
	public String getFormateurNom() {
		return formateurNom;
	}
	public void setFormateurNom(String formateurNom) {
		this.formateurNom = formateurNom;
	}
	public String getDateDebut() {
		return dateDebut;
	}
	public void setDateDebut(String dateDebut) {
		this.dateDebut = dateDebut;
	}
	public String getDateFin() {
		return dateFin;
	}
	public void setDateFin(String dateFin) {
		this.dateFin = dateFin;
	}
	public String getLieu() {
		return lieu;
	}
	public void setLieu(String lieu) {
		this.lieu = lieu;
	}
	public Double getMontant() {
		return montant;
	}
	public void setMontant(Double montant) {
		this.montant = montant;
	}
	public String getProgramme() {
		return programme;
	}
	public void setProgramme(String programme) {
		this.programme = programme;
	}
	public String getNoteAdmin() {
		return noteAdmin;
	}
	public void setNoteAdmin(String noteAdmin) {
		this.noteAdmin = noteAdmin;
	}
	public String getMode() {
		return mode;
	}
	public void setMode(String mode) {
		this.mode = mode;
	}
	public java.util.List<String> getModulesProposition() {
		return modulesProposition;
	}
	public void setModulesProposition(java.util.List<String> modulesProposition) {
		this.modulesProposition = modulesProposition;
	}
    
    
    
}
