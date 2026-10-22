package com.senexpert.userservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "utilisateurs")
@Data
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private String prenom;

    @Column(unique = true, nullable = false)
    private String email;

    @JsonIgnore
    private String motDePasse;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    private Statut statut;

    private String telephone;

    /** Profession du candidat (PARTICULIER uniquement) */
    private String profession;

    /** Numéro de la CNI (PARTICULIER uniquement) */
    @Column(name = "numero_cni")
    private String numeroCni;

    /** Numéro/identifiant de l'institution représentée (AGENT uniquement) */
    @Column(name = "numero_institution")
    private String numeroInstitution;

    @ManyToOne
    @JoinColumn(name = "institution_id")
    private Institution institution;

    @ManyToOne
    @JoinColumn(name = "profil_id")
    private Profil profil;

    /** Permissions directement attribuées à cet utilisateur (indépendantes du profil) */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "utilisateur_permissions",
        joinColumns    = @JoinColumn(name = "utilisateur_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private java.util.Set<Permission> permissionsDirectes = new java.util.HashSet<>();

    /** Derniere connexion reussie (mise a jour par AuthController) */
    private LocalDateTime derniereConnexion;

    /** Secret TOTP pour la double authentification (base32, null si non active) */
    @JsonIgnore
    private String totpSecret;

    /** 2FA active pour ce compte */
    @Column(name = "totp_actif", nullable = false, columnDefinition = "boolean default false")
    private boolean totpActif = false;

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

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public Statut getStatut() {
		return statut;
	}

	public void setStatut(Statut statut) {
		this.statut = statut;
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

	public Institution getInstitution() {
		return institution;
	}

	public void setInstitution(Institution institution) {
		this.institution = institution;
	}

	public Profil getProfil() {
		return profil;
	}

	public void setProfil(Profil profil) {
		this.profil = profil;
	}

	public java.util.Set<Permission> getPermissionsDirectes() {
		return permissionsDirectes;
	}

	public void setPermissionsDirectes(java.util.Set<Permission> permissionsDirectes) {
		this.permissionsDirectes = permissionsDirectes;
	}

	public LocalDateTime getDerniereConnexion() {
		return derniereConnexion;
	}

	public void setDerniereConnexion(LocalDateTime derniereConnexion) {
		this.derniereConnexion = derniereConnexion;
	}

	public String getTotpSecret() {
		return totpSecret;
	}

	public void setTotpSecret(String totpSecret) {
		this.totpSecret = totpSecret;
	}

	public boolean isTotpActif() {
		return totpActif;
	}

	public void setTotpActif(boolean totpActif) {
		this.totpActif = totpActif;
	}
    
}