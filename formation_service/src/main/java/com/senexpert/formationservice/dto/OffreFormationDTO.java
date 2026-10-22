package com.senexpert.formationservice.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class OffreFormationDTO {
    private String titre;
    private String description;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private LocalDate dateLimiteCandidature;
    private Integer places;
    private String type;
    private Long domaineId;
    private String niveau;
    private String duree;
    private Double cout;
    private List<String> documentsRequis;
    private Long prestataireId;
    private String prestataireRole;
    private String programmeUrl;
    private String motifRejet;

    // ── Nouveaux champs ──────────────────────────────────────────────────────
    private String prerequis;
    private String lieu;
    private String imageUrl;
    private String langue;
    private String publicCible;
    private String modalitesPaiement;
    private String financementDisponible;
    private String certification;
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public LocalDate getDateDebut() {
		return dateDebut;
	}
	public void setDateDebut(LocalDate dateDebut) {
		this.dateDebut = dateDebut;
	}
	public LocalDate getDateFin() {
		return dateFin;
	}
	public void setDateFin(LocalDate dateFin) {
		this.dateFin = dateFin;
	}
	public LocalDate getDateLimiteCandidature() {
		return dateLimiteCandidature;
	}
	public void setDateLimiteCandidature(LocalDate dateLimiteCandidature) {
		this.dateLimiteCandidature = dateLimiteCandidature;
	}
	public Integer getPlaces() {
		return places;
	}
	public void setPlaces(Integer places) {
		this.places = places;
	}
	public Long getDomaineId() {
		return domaineId;
	}
	public void setDomaineId(Long domaineId) {
		this.domaineId = domaineId;
	}
	public String getNiveau() {
		return niveau;
	}
	public void setNiveau(String niveau) {
		this.niveau = niveau;
	}
	public String getDuree() {
		return duree;
	}
	public void setDuree(String duree) {
		this.duree = duree;
	}
	public Double getCout() {
		return cout;
	}
	public void setCout(Double cout) {
		this.cout = cout;
	}
	public List<String> getDocumentsRequis() {
		return documentsRequis;
	}
	public void setDocumentsRequis(List<String> documentsRequis) {
		this.documentsRequis = documentsRequis;
	}
	public Long getPrestataireId() {
		return prestataireId;
	}
	public void setPrestataireId(Long prestataireId) {
		this.prestataireId = prestataireId;
	}
	public String getPrestataireRole() {
		return prestataireRole;
	}
	public void setPrestataireRole(String prestataireRole) {
		this.prestataireRole = prestataireRole;
	}
	public String getProgrammeUrl() {
		return programmeUrl;
	}
	public void setProgrammeUrl(String programmeUrl) {
		this.programmeUrl = programmeUrl;
	}
	public String getMotifRejet() {
		return motifRejet;
	}
	public void setMotifRejet(String motifRejet) {
		this.motifRejet = motifRejet;
	}
	public String getPrerequis() {
		return prerequis;
	}
	public void setPrerequis(String prerequis) {
		this.prerequis = prerequis;
	}
	public String getLieu() {
		return lieu;
	}
	public void setLieu(String lieu) {
		this.lieu = lieu;
	}
	public String getImageUrl() {
		return imageUrl;
	}
	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
	public String getLangue() {
		return langue;
	}
	public void setLangue(String langue) {
		this.langue = langue;
	}
	public String getPublicCible() {
		return publicCible;
	}
	public void setPublicCible(String publicCible) {
		this.publicCible = publicCible;
	}
	public String getModalitesPaiement() {
		return modalitesPaiement;
	}
	public void setModalitesPaiement(String modalitesPaiement) {
		this.modalitesPaiement = modalitesPaiement;
	}
	public String getFinancementDisponible() {
		return financementDisponible;
	}
	public void setFinancementDisponible(String financementDisponible) {
		this.financementDisponible = financementDisponible;
	}
	public String getCertification() {
		return certification;
	}
	public void setCertification(String certification) {
		this.certification = certification;
	}
	public String getTitre() {
		return titre;
	}
	public void setTitre(String titre) {
		this.titre = titre;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
    
    
    
}
