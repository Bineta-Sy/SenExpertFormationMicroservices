package com.senexpert.formationservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "propositions_formation")
@Data
public class PropositionFormation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Demande à la carte liée */
    private Long demandeLibreId;

    /** Formateur / prestataire assigné */
    private String formateurNom;

    private LocalDate dateDebut;
    private LocalDate dateFin;

    private String lieu;

    private Double montant;

    @Column(length = 2000)
    private String programme;

    /** Note complémentaire de l'admin */
    @Column(length = 1000)
    private String noteAdmin;

    private LocalDate dateProposition;

    @Enumerated(EnumType.STRING)
    private StatutProposition statut = StatutProposition.EN_ATTENTE;

    /** Mode : EN_LIGNE | PRESENTIEL | HYBRIDE */
    private String mode;

    /** Modules inclus dans la proposition */
    @ElementCollection(fetch = jakarta.persistence.FetchType.EAGER)
    @CollectionTable(name = "proposition_modules", joinColumns = @JoinColumn(name = "proposition_id"))
    @Column(name = "module_titre")
    private java.util.List<String> modulesProposition = new java.util.ArrayList<>();

    /** Motif si refusée par l'agent */
    private String motifRefus;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

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

	public LocalDate getDateProposition() {
		return dateProposition;
	}

	public void setDateProposition(LocalDate dateProposition) {
		this.dateProposition = dateProposition;
	}

	public StatutProposition getStatut() {
		return statut;
	}

	public void setStatut(StatutProposition statut) {
		this.statut = statut;
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

	public String getMotifRefus() {
		return motifRefus;
	}

	public void setMotifRefus(String motifRefus) {
		this.motifRefus = motifRefus;
	}
    
    
}
