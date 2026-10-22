package com.senexpert.frontend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FormationSouhaitee {
    private Long id;
    private Long demandeId;
    private String thematique;
    private Integer duree;
    private Integer nbParticipants;
    private String objectifs;
    private String niveauRequis;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getDemandeId() { return demandeId; }
    public void setDemandeId(Long demandeId) { this.demandeId = demandeId; }
    public String getThematique() { return thematique; }
    public void setThematique(String thematique) { this.thematique = thematique; }
    public Integer getDuree() { return duree; }
    public void setDuree(Integer duree) { this.duree = duree; }
    public Integer getNbParticipants() { return nbParticipants; }
    public void setNbParticipants(Integer nbParticipants) { this.nbParticipants = nbParticipants; }
    public String getObjectifs() { return objectifs; }
    public void setObjectifs(String objectifs) { this.objectifs = objectifs; }
    public String getNiveauRequis() { return niveauRequis; }
    public void setNiveauRequis(String niveauRequis) { this.niveauRequis = niveauRequis; }
}
