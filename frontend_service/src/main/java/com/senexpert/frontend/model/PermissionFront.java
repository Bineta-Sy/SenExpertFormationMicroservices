package com.senexpert.frontend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PermissionFront {
    private Long id;
    private String libelle;
    private String code;
    private String niveau;
    private BrancheFront branche;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getNiveau() { return niveau; }
    public void setNiveau(String niveau) { this.niveau = niveau; }
    public BrancheFront getBranche() { return branche; }
    public void setBranche(BrancheFront branche) { this.branche = branche; }
}
