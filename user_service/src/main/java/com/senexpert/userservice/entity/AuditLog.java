package com.senexpert.userservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs",
       indexes = @Index(name = "idx_audit_date", columnList = "dateAction"))
@Data
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Type d'action : CREATION, MODIFICATION, SUPPRESSION, CONNEXION, RESET_PASSWORD, etc. */
    @Column(nullable = false, length = 50)
    private String action;

    /** Entité concernée : UTILISATEUR, OFFRE, DEMANDE, CANDIDATURE, ANNONCE, etc. */
    @Column(length = 50)
    private String entite;

    /** Détail libre (id, nom, message) */
    @Column(length = 500)
    private String detail;

    /** Email de l'admin ayant effectué l'action */
    @Column(length = 150)
    private String adminEmail;

    /** Niveau : INFO, WARN, ERROR */
    @Column(length = 10)
    private String niveau;

    @Column(nullable = false)
    private LocalDateTime dateAction = LocalDateTime.now();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getEntite() {
		return entite;
	}

	public void setEntite(String entite) {
		this.entite = entite;
	}

	public String getDetail() {
		return detail;
	}

	public void setDetail(String detail) {
		this.detail = detail;
	}

	public String getAdminEmail() {
		return adminEmail;
	}

	public void setAdminEmail(String adminEmail) {
		this.adminEmail = adminEmail;
	}

	public String getNiveau() {
		return niveau;
	}

	public void setNiveau(String niveau) {
		this.niveau = niveau;
	}

	public LocalDateTime getDateAction() {
		return dateAction;
	}

	public void setDateAction(LocalDateTime dateAction) {
		this.dateAction = dateAction;
	}
    
    
}
