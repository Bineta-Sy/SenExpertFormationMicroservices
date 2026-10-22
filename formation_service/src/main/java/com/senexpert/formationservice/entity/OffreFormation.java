package com.senexpert.formationservice.entity;

import com.senexpert.formationservice.converter.StringListConverter;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "offres_formation")
@Data
public class OffreFormation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titre;

    @Column(length = 1000)
    private String description;

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

	public Integer getPlacesDisponibles() {
		return placesDisponibles;
	}

	public void setPlacesDisponibles(Integer placesDisponibles) {
		this.placesDisponibles = placesDisponibles;
	}

	public Integer getNbModules() {
		return nbModules;
	}

	public void setNbModules(Integer nbModules) {
		this.nbModules = nbModules;
	}

	public TypeFormation getType() {
		return type;
	}

	public void setType(TypeFormation type) {
		this.type = type;
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

	public StatutOffre getStatut() {
		return statut;
	}

	public void setStatut(StatutOffre statut) {
		this.statut = statut;
	}

	public Domaine getDomaine() {
		return domaine;
	}

	public void setDomaine(Domaine domaine) {
		this.domaine = domaine;
	}

	public List<String> getDocumentsRequis() {
		return documentsRequis;
	}

	public void setDocumentsRequis(List<String> documentsRequis) {
		this.documentsRequis = documentsRequis;
	}

	public List<Formateur> getFormateurs() {
		return formateurs;
	}

	public void setFormateurs(List<Formateur> formateurs) {
		this.formateurs = formateurs;
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

	public boolean isSoumis() {
		return soumis;
	}

	public void setSoumis(boolean soumis) {
		this.soumis = soumis;
	}

	public boolean isRejete() {
		return rejete;
	}

	public void setRejete(boolean rejete) {
		this.rejete = rejete;
	}

	public String getMotifRejet() {
		return motifRejet;
	}

	public void setMotifRejet(String motifRejet) {
		this.motifRejet = motifRejet;
	}

	public String getProgrammeUrl() {
		return programmeUrl;
	}

	public void setProgrammeUrl(String programmeUrl) {
		this.programmeUrl = programmeUrl;
	}

	public Long getDemandeLibreId() {
		return demandeLibreId;
	}

	public void setDemandeLibreId(Long demandeLibreId) {
		this.demandeLibreId = demandeLibreId;
	}

	private LocalDate dateDebut;
    private LocalDate dateFin;

    /** Date limite de candidature : après cette date, plus aucune candidature n'est acceptée */
    private LocalDate dateLimiteCandidature;

    private Integer places;

    /** Calculé dynamiquement (non persisté) : places - demandes EN_ATTENTE ou VALIDEE */
    @Transient
    private Integer placesDisponibles;

    /** Calculé dynamiquement (non persisté) : nombre de modules liés à cette offre */
    @Transient
    private Integer nbModules;

    @Enumerated(EnumType.STRING)
    private TypeFormation type;

    private String niveau;
    private String duree;
    private Double cout;

    /** Prérequis : niveau ou compétences requis pour suivre la formation */
    @Column(length = 500)
    private String prerequis;

    /** Lieu : adresse ou salle (utile si présentiel) */
    private String lieu;

    /** URL de l'image de couverture */
    private String imageUrl;

    /** Langue d'enseignement (ex. Français, Anglais) */
    private String langue;

    /** Public cible (ex. salariés, demandeurs d'emploi, étudiants) */
    @Column(length = 300)
    private String publicCible;

    /** Modalités de paiement (ex. en une fois, échelonné sur 3 mois) */
    @Column(length = 300)
    private String modalitesPaiement;

    /** Financement disponible (ex. bourse, prise en charge employeur) */
    @Column(length = 300)
    private String financementDisponible;

    /** Certification ou attestation délivrée à la fin */
    @Column(length = 300)
    private String certification;

    @Enumerated(EnumType.STRING)
    private StatutOffre statut = StatutOffre.BROUILLON;

    @ManyToOne
    @JoinColumn(name = "domaine_id")
    private Domaine domaine;

    /** Liste des documents requis pour postuler (définie par le prestataire). */
    @Convert(converter = StringListConverter.class)
    @Column(name = "documents_requis", columnDefinition = "TEXT")
    private List<String> documentsRequis = new ArrayList<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "offre_formateurs",
        joinColumns = @JoinColumn(name = "offre_id"),
        inverseJoinColumns = @JoinColumn(name = "formateur_id")
    )
    private List<Formateur> formateurs = new ArrayList<>();

    // ── Prestataire (AGENT ou PARTICULIER qui soumet la formation) ──────────

    /** ID utilisateur du prestataire qui a créé cette offre (null si créée par l'admin) */
    private Long prestataireId;

    /** Rôle du prestataire : "AGENT" ou "PARTICULIER" */
    private String prestataireRole;

    /** true = soumis à l'admin pour validation, false = encore en brouillon chez le prestataire */
    @Column(columnDefinition = "boolean default false")
    private boolean soumis = false;

    /** true = formation rejetée par l'admin (visible chez le prestataire avec motif) */
    @Column(columnDefinition = "boolean default false")
    private boolean rejete = false;

    /** Motif de rejet fourni par l'admin */
    @Column(length = 500)
    private String motifRejet;

    /** URL ou lien vers le programme / syllabus de la formation */
    private String programmeUrl;

    /** ID de la demande à la carte à l'origine de cette offre (null si offre standard) */
    private Long demandeLibreId;
    
    
}