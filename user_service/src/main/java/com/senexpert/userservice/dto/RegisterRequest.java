package com.senexpert.userservice.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String nom;
    private String prenom;
    private String email;
    private String motDePasse;
    private String role;
    private Long institutionId; // optionnel : lier à une institution existante

    // Champs additionnels selon le rôle
    private String telephone;
    private String profession;      // PARTICULIER : profession
    private String numeroCni;       // PARTICULIER : numéro CNI
    private String numeroInstitution; // AGENT : numéro/identifiant de l'institution (NINEA)
    private String nomInstitution;    // AGENT : nom de l'institution
    private String typeInstitution;   // AGENT : type (ENTREPRISE, ONG, …)
    private String secteurInstitution;// AGENT : secteur d'activité
	public String getNom() {
		return nom;
	}
	public void setNom(String nom) {
		this.nom = nom;
	}
	public String getPrenom() {
		return prenom;
	}
	public void setPrenom(String prenom) {
		this.prenom = prenom;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getMotDePasse() {
		return motDePasse;
	}
	public void setMotDePasse(String motDePasse) {
		this.motDePasse = motDePasse;
	}
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	public Long getInstitutionId() {
		return institutionId;
	}
	public void setInstitutionId(Long institutionId) {
		this.institutionId = institutionId;
	}
	public String getTelephone() {
		return telephone;
	}
	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}
	public String getProfession() {
		return profession;
	}
	public void setProfession(String profession) {
		this.profession = profession;
	}
	public String getNumeroCni() {
		return numeroCni;
	}
	public void setNumeroCni(String numeroCni) {
		this.numeroCni = numeroCni;
	}
	public String getNumeroInstitution() {
		return numeroInstitution;
	}
	public void setNumeroInstitution(String numeroInstitution) {
		this.numeroInstitution = numeroInstitution;
	}
	public String getNomInstitution() {
		return nomInstitution;
	}
	public void setNomInstitution(String nomInstitution) {
		this.nomInstitution = nomInstitution;
	}
	public String getTypeInstitution() {
		return typeInstitution;
	}
	public void setTypeInstitution(String typeInstitution) {
		this.typeInstitution = typeInstitution;
	}
	public String getSecteurInstitution() {
		return secteurInstitution;
	}
	public void setSecteurInstitution(String secteurInstitution) {
		this.secteurInstitution = secteurInstitution;
	}
    
    
}