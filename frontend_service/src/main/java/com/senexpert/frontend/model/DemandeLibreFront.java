package com.senexpert.frontend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DemandeLibreFront {
    private Long id;
    private Long utilisateurId;
    private String institutionNom;
    private String domaine;
    private String description;
    private Integer nbParticipants;
    private String periodeDebut;
    private String periodeFin;
    private String dateDemande;
    private String statut;
    private Long offreId;
    private List<FormationSouhaitee> formationsSouhaitees = new ArrayList<>();
    private List<Long> participantIds = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(Long v) { this.utilisateurId = v; }
    public String getInstitutionNom() { return institutionNom; }
    public void setInstitutionNom(String v) { this.institutionNom = v; }
    public String getDomaine() { return domaine; }
    public void setDomaine(String v) { this.domaine = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { this.description = v; }
    public Integer getNbParticipants() { return nbParticipants; }
    public void setNbParticipants(Integer v) { this.nbParticipants = v; }
    public String getPeriodeDebut() { return periodeDebut; }
    public void setPeriodeDebut(String v) { this.periodeDebut = v; }
    public String getPeriodeFin() { return periodeFin; }
    public void setPeriodeFin(String v) { this.periodeFin = v; }
    public String getDateDemande() { return dateDemande; }
    public void setDateDemande(String v) { this.dateDemande = v; }
    public String getStatut() { return statut; }
    public void setStatut(String v) { this.statut = v; }
    public Long getOffreId() { return offreId; }
    public void setOffreId(Long v) { this.offreId = v; }
    public List<FormationSouhaitee> getFormationsSouhaitees() { return formationsSouhaitees; }
    public void setFormationsSouhaitees(List<FormationSouhaitee> v) { this.formationsSouhaitees = v; }
    public List<Long> getParticipantIds() { return participantIds; }
    public void setParticipantIds(List<Long> v) { this.participantIds = v; }
}
