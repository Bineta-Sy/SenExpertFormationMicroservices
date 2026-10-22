package com.senexpert.userservice.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "permissions")
@Data
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String libelle;

    @Column(nullable = false, unique = true)
    private String code;

    @Enumerated(EnumType.STRING)
    private NiveauPermission niveau;

    @ManyToOne
    @JoinColumn(name = "branche_id")
    private Branche branche;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getLibelle() {
		return libelle;
	}

	public void setLibelle(String libelle) {
		this.libelle = libelle;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public NiveauPermission getNiveau() {
		return niveau;
	}

	public void setNiveau(NiveauPermission niveau) {
		this.niveau = niveau;
	}

	public Branche getBranche() {
		return branche;
	}

	public void setBranche(Branche branche) {
		this.branche = branche;
	}
    
    
}
