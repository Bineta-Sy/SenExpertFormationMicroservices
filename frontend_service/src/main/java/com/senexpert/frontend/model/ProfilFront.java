package com.senexpert.frontend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProfilFront {
    private Long id;
    private String nom;
    private List<PermissionFront> permissions;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public List<PermissionFront> getPermissions() { return permissions; }
    public void setPermissions(List<PermissionFront> permissions) { this.permissions = permissions; }
}
