package com.senexpert.frontend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Utilisateur {
    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private String role;
    private String statut;
    private Institution institution;
    private ProfilFront profil;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public Institution getInstitution() { return institution; }
    public void setInstitution(Institution institution) { this.institution = institution; }
    public ProfilFront getProfil() { return profil; }
    public void setProfil(ProfilFront profil) { this.profil = profil; }
    public String getNomComplet() { return prenom + " " + nom; }

    // Champs 2FA
    private boolean totpActif;
    public boolean isTotpActif() { return totpActif; }
    public void setTotpActif(boolean totpActif) { this.totpActif = totpActif; }

    // Champs additionnels selon le rôle
    private String telephone;
    private String profession;
    private String numeroCni;
    private String numeroInstitution;
    private String derniereConnexion;

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public String getProfession() { return profession; }
    public void setProfession(String profession) { this.profession = profession; }
    public String getNumeroCni() { return numeroCni; }
    public void setNumeroCni(String numeroCni) { this.numeroCni = numeroCni; }
    public String getNumeroInstitution() { return numeroInstitution; }
    public void setNumeroInstitution(String numeroInstitution) { this.numeroInstitution = numeroInstitution; }
    public String getDerniereConnexion() { return derniereConnexion; }
    public void setDerniereConnexion(String derniereConnexion) { this.derniereConnexion = derniereConnexion; }
}
