package com.senexpert.formationservice.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "seances")
@Data
public class Seance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "module_id", nullable = false)
    private Module module;

    /** Date de la séance : YYYY-MM-DD */
    private String date;

    /** Heure de début : HH:mm */
    private String heureDebut;

    /** Heure de fin : HH:mm */
    private String heureFin;

    /** Salle physique */
    private String salle;

    /** Lien visioconférence (optionnel) */
    private String lienVisio;

    @Column(length = 1000)
    private String notes;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Module getModule() {
		return module;
	}

	public void setModule(Module module) {
		this.module = module;
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
