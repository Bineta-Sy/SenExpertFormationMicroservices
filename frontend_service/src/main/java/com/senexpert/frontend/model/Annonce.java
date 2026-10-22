package com.senexpert.frontend.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Annonce implements Serializable {

    private static long counter = 1;

    private Long id;
    private String titre;
    private String contenu;
    private String type;           // INFO, WARNING, SUCCESS, DANGER
    private String dateCreation;
    private String auteur;
    private boolean active;

    /** Ciblage : TOUS, ADMIN, AGENT, PARTICULIER */
    private String cibleRole = "TOUS";

    /** Ciblage optionnel par institution (null = toutes) */
    private Long cibleInstitutionId;

    /** Libellé lisible de l'institution ciblée (pour affichage) */
    private String cibleInstitutionNom;

    public Annonce() {}

    public Annonce(String titre, String contenu, String type, String auteur,
                   String cibleRole, Long cibleInstitutionId, String cibleInstitutionNom) {
        this.id = counter++;
        this.titre = titre;
        this.contenu = contenu;
        this.type = type;
        this.auteur = auteur;
        this.dateCreation = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        this.active = true;
        this.cibleRole = cibleRole != null ? cibleRole : "TOUS";
        this.cibleInstitutionId = cibleInstitutionId;
        this.cibleInstitutionNom = cibleInstitutionNom;
    }

    /** Vérifie si cette annonce est visible pour un utilisateur donné. */
    public boolean estVisiblePour(String role, Long institutionId) {
        if (!active) return false;
        // Filtre rôle
        if (!"TOUS".equals(cibleRole) && !cibleRole.equals(role)) return false;
        // Filtre institution (si précisé, seuls les membres de cette institution voient l'annonce)
        if (cibleInstitutionId != null) {
            if (institutionId == null || !cibleInstitutionId.equals(institutionId)) return false;
        }
        return true;
    }

    // Getters / Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getDateCreation() { return dateCreation; }
    public void setDateCreation(String dateCreation) { this.dateCreation = dateCreation; }
    public String getAuteur() { return auteur; }
    public void setAuteur(String auteur) { this.auteur = auteur; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public String getCibleRole() { return cibleRole; }
    public void setCibleRole(String cibleRole) { this.cibleRole = cibleRole; }
    public Long getCibleInstitutionId() { return cibleInstitutionId; }
    public void setCibleInstitutionId(Long cibleInstitutionId) { this.cibleInstitutionId = cibleInstitutionId; }
    public String getCibleInstitutionNom() { return cibleInstitutionNom; }
    public void setCibleInstitutionNom(String cibleInstitutionNom) { this.cibleInstitutionNom = cibleInstitutionNom; }
}
