package com.senexpert.frontend.model;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Domaine implements Serializable {
    private Long   id;
    private String libelle;
    private String code;
    private String description;

    public Long   getId()              { return id; }
    public void   setId(Long id)       { this.id = id; }
    public String getLibelle()         { return libelle; }
    public void   setLibelle(String v) { this.libelle = v; }
    public String getCode()            { return code; }
    public void   setCode(String v)    { this.code = v; }
    public String getDescription()     { return description; }
    public void   setDescription(String v) { this.description = v; }

    /** Alias EL : domaine.nom → domaine.libelle */
    public String getNom() { return libelle; }
}
