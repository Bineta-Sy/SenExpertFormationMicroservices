package com.senexpert.frontend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Offre {
    private Long id;
    private String titre;
    private String description;
    private String dateDebut;
    private String dateFin;
    private String dateLimiteCandidature;
    private Integer places;
    private Double cout;
    private String niveau;
    private String duree;
    private String type;
    private String statut;
    private Domaine domaine;
    private List<FormateurFront> formateurs = new ArrayList<>();
    private List<ModuleFront>   modules    = new ArrayList<>();
    private Integer             nbModules;
    private Long    prestataireId;
    private Long    demandeLibreId;
    private String  prestataireRole;
    private Boolean soumis     = false;
    private Boolean rejete     = false;
    private String  motifRejet;
    private String  programmeUrl;
    private List<String> documentsRequis = new ArrayList<>();

    // ── Nouveaux champs ──────────────────────────────────────────────────────
    private String  prerequis;
    private String  lieu;
    private String  imageUrl;
    private String  langue;
    private String  publicCible;
    private String  modalitesPaiement;
    private String  financementDisponible;
    private String  certification;

    // ── Getters / Setters ────────────────────────────────────────────────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getDateDebut() { return dateDebut; }
    public void setDateDebut(String dateDebut) { this.dateDebut = dateDebut; }
    public String getDateFin() { return dateFin; }
    public void setDateFin(String dateFin) { this.dateFin = dateFin; }
    public String getDateLimiteCandidature() { return dateLimiteCandidature; }
    public void setDateLimiteCandidature(String v) { this.dateLimiteCandidature = v; }

    /** true si la date limite est définie et dépassée */
    public boolean isCandidatureExpiree() {
        if (dateLimiteCandidature == null || dateLimiteCandidature.isBlank()) return false;
        try {
            return java.time.LocalDate.parse(dateLimiteCandidature).isBefore(java.time.LocalDate.now());
        } catch (Exception e) { return false; }
    }
    public Integer getPlaces() { return places; }
    public void setPlaces(Integer places) { this.places = places; }
    public Double getCout() { return cout; }
    public void setCout(Double cout) { this.cout = cout; }
    public String getNiveau() { return niveau; }
    public void setNiveau(String niveau) { this.niveau = niveau; }
    public String getDuree() { return duree; }
    public void setDuree(String duree) { this.duree = duree; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public Domaine getDomaine() { return domaine; }
    public void setDomaine(Domaine domaine) { this.domaine = domaine; }
    public List<FormateurFront> getFormateurs() { return formateurs; }
    public void setFormateurs(List<FormateurFront> formateurs) { this.formateurs = formateurs; }
    public List<ModuleFront>   getModules()    { return modules; }
    public void setModules(List<ModuleFront> modules) { this.modules = modules; }
    public Integer getNbModules() { return nbModules; }
    public void setNbModules(Integer nbModules) { this.nbModules = nbModules; }
    public Long    getPrestataireId()    { return prestataireId; }
    public void    setPrestataireId(Long v)    { this.prestataireId = v; }
    public Long    getDemandeLibreId()    { return demandeLibreId; }
    public void    setDemandeLibreId(Long v)    { this.demandeLibreId = v; }
    public String  getPrestataireRole()  { return prestataireRole; }
    public void    setPrestataireRole(String v){ this.prestataireRole = v; }
    public Boolean getSoumis()           { return soumis; }
    public void    setSoumis(Boolean v)  { this.soumis = v; }
    public Boolean getRejete()           { return rejete; }
    public void    setRejete(Boolean v)  { this.rejete = v; }
    public String  getMotifRejet()       { return motifRejet; }
    public void    setMotifRejet(String v){ this.motifRejet = v; }
    public String  getProgrammeUrl()     { return programmeUrl; }
    public void    setProgrammeUrl(String v){ this.programmeUrl = v; }
    public List<String> getDocumentsRequis()              { return documentsRequis; }
    public void         setDocumentsRequis(List<String> v){ this.documentsRequis = v != null ? v : new ArrayList<>(); }

    public String  getPrerequis()             { return prerequis; }
    public void    setPrerequis(String v)     { this.prerequis = v; }
    public String  getLieu()                  { return lieu; }
    public void    setLieu(String v)          { this.lieu = v; }
    public String  getImageUrl()              { return imageUrl; }
    public void    setImageUrl(String v)      { this.imageUrl = v; }
    public String  getLangue()                { return langue; }
    public void    setLangue(String v)        { this.langue = v; }
    public String  getPublicCible()           { return publicCible; }
    public void    setPublicCible(String v)   { this.publicCible = v; }
    public String  getModalitesPaiement()     { return modalitesPaiement; }
    public void    setModalitesPaiement(String v){ this.modalitesPaiement = v; }
    public String  getFinancementDisponible() { return financementDisponible; }
    public void    setFinancementDisponible(String v){ this.financementDisponible = v; }
    public String  getCertification()         { return certification; }
    public void    setCertification(String v) { this.certification = v; }

    // ── Helpers ──────────────────────────────────────────────────────────────
    public boolean isPayante() { return cout != null && cout > 0; }
    public String  getCoutFormate() {
        if (cout == null || cout == 0) return "Gratuit";
        return String.format("%,.0f FCFA", cout);
    }
    public Integer getPlacesDisponibles() { return places; }
    public int     getNbDocumentsRequis()  { return documentsRequis != null ? documentsRequis.size() : 0; }
}
