package com.senexpert.formationservice.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cours_en_ligne")
@Data
public class CoursEnLigne {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titre;

    @Column(length = 2000)
    private String description;

    @Column(columnDefinition = "TEXT")
    private String instructionsDetaillees; // pour TP / DEVOIR / EXAMEN

    @Enumerated(EnumType.STRING)
    private TypeCours type; // VIDEO, DOCUMENT, QUIZ, LIEN, LIVE, TP, DEVOIR, EXAMEN

    /** URL externe du contenu (YouTube, Drive, lien article…). Null pour QUIZ ou upload local. */
    @Column(length = 1000)
    private String urlContenu;

    /** Chemin relatif du fichier uploadé localement. */
    @Column(length = 500)
    private String cheminFichier;

    /** Nom original du fichier uploadé. */
    @Column(length = 255)
    private String nomFichierOriginal;

    /** Taille du fichier en octets. */
    private Long tailleFichier;

    /** Position dans le parcours de l'offre (1, 2, 3…). */
    private Integer ordre;

    private Integer dureeMinutes;

    private boolean publie = false;

    // ── Champs TP / DEVOIR / EXAMEN ──────────────────────────────────────────
    /** Date d'ouverture : avant cette date, aucune soumission n'est acceptée. */
    private LocalDateTime dateDebut;

    /** Date limite de remise : après cette date, toute soumission est refusée
     *  sauf si le formateur repousse la date. */
    private LocalDateTime dateLimite;

    /** Note maximum (ex : 20). Par défaut 20. */
    private Integer baremeNote;

    /** Durée en minutes pour un EXAMEN QCM chronométré. */
    private Integer dureeExamen;

    /**
     * Pour EXAMEN uniquement : "QCM" (questions à choix multiple, chronométré)
     * ou "FICHIER" (remise de copie, correction manuelle).
     */
    @Column(length = 10)
    private String typeExamen;

    // ── Champs spécifiques LIVE ──────────────────────────────────────────────
    @Column(length = 1000)
    private String lienVisio;

    private String plateforme;

    private LocalDateTime dateHeureLive;

    // ── Méta ──────────────────────────────────────────────────────────────────
    private LocalDateTime dateCreation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offre_id")
    private OffreFormation offre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "formateur_id", nullable = true)
    private Formateur formateur;

    @OneToMany(mappedBy = "cours", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("ordre ASC")
    private List<QuizQuestion> questions = new ArrayList<>();

    @PrePersist
    void onCreate() { dateCreation = LocalDateTime.now(); }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitre() {
		return titre;
	}

	public void setTitre(String titre) {
		this.titre = titre;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getInstructionsDetaillees() {
		return instructionsDetaillees;
	}

	public void setInstructionsDetaillees(String instructionsDetaillees) {
		this.instructionsDetaillees = instructionsDetaillees;
	}

	public TypeCours getType() {
		return type;
	}

	public void setType(TypeCours type) {
		this.type = type;
	}

	public String getUrlContenu() {
		return urlContenu;
	}

	public void setUrlContenu(String urlContenu) {
		this.urlContenu = urlContenu;
	}

	public String getCheminFichier() {
		return cheminFichier;
	}

	public void setCheminFichier(String cheminFichier) {
		this.cheminFichier = cheminFichier;
	}

	public String getNomFichierOriginal() {
		return nomFichierOriginal;
	}

	public void setNomFichierOriginal(String nomFichierOriginal) {
		this.nomFichierOriginal = nomFichierOriginal;
	}

	public Long getTailleFichier() {
		return tailleFichier;
	}

	public void setTailleFichier(Long tailleFichier) {
		this.tailleFichier = tailleFichier;
	}

	public Integer getOrdre() {
		return ordre;
	}

	public void setOrdre(Integer ordre) {
		this.ordre = ordre;
	}

	public Integer getDureeMinutes() {
		return dureeMinutes;
	}

	public void setDureeMinutes(Integer dureeMinutes) {
		this.dureeMinutes = dureeMinutes;
	}

	public boolean isPublie() {
		return publie;
	}

	public void setPublie(boolean publie) {
		this.publie = publie;
	}

	public LocalDateTime getDateDebut() {
		return dateDebut;
	}

	public void setDateDebut(LocalDateTime dateDebut) {
		this.dateDebut = dateDebut;
	}

	public LocalDateTime getDateLimite() {
		return dateLimite;
	}

	public void setDateLimite(LocalDateTime dateLimite) {
		this.dateLimite = dateLimite;
	}

	public Integer getBaremeNote() {
		return baremeNote;
	}

	public void setBaremeNote(Integer baremeNote) {
		this.baremeNote = baremeNote;
	}

	public Integer getDureeExamen() {
		return dureeExamen;
	}

	public void setDureeExamen(Integer dureeExamen) {
		this.dureeExamen = dureeExamen;
	}

	public String getTypeExamen() {
		return typeExamen;
	}

	public void setTypeExamen(String typeExamen) {
		this.typeExamen = typeExamen;
	}

	public String getLienVisio() {
		return lienVisio;
	}

	public void setLienVisio(String lienVisio) {
		this.lienVisio = lienVisio;
	}

	public String getPlateforme() {
		return plateforme;
	}

	public void setPlateforme(String plateforme) {
		this.plateforme = plateforme;
	}

	public LocalDateTime getDateHeureLive() {
		return dateHeureLive;
	}

	public void setDateHeureLive(LocalDateTime dateHeureLive) {
		this.dateHeureLive = dateHeureLive;
	}

	public LocalDateTime getDateCreation() {
		return dateCreation;
	}

	public void setDateCreation(LocalDateTime dateCreation) {
		this.dateCreation = dateCreation;
	}

	public OffreFormation getOffre() {
		return offre;
	}

	public void setOffre(OffreFormation offre) {
		this.offre = offre;
	}

	public Formateur getFormateur() {
		return formateur;
	}

	public void setFormateur(Formateur formateur) {
		this.formateur = formateur;
	}

	public List<QuizQuestion> getQuestions() {
		return questions;
	}

	public void setQuestions(List<QuizQuestion> questions) {
		this.questions = questions;
	}
    
    
}
