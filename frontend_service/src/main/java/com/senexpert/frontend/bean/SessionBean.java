package com.senexpert.frontend.bean;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Named
@SessionScoped
public class SessionBean implements Serializable {

    private String token;
    private Long utilisateurId;
    private String email;
    private String role;
    private String nomComplet;
    private String statut;        // ACTIF | EN_ATTENTE | INACTIF
    private Long institutionId;
    /** Codes des permissions effectives (profil + directes), chargés au login */
    private List<String> permissions = new ArrayList<>();

    /**
     * Map EL-friendly : permissionsMap['5013'] → true/null.
     * Bracket access est une value expression pure — pas de "method expression" error.
     * Les admins obtiennent toujours true via la surcharge de get().
     */
    private class PermissionMap extends HashMap<String, Boolean> {
        private static final long serialVersionUID = 1L;
        @Override
        public Boolean get(Object key) {
            if ("ADMIN".equals(role)) return Boolean.TRUE;
            return super.get(key);
        }
        @Override
        public boolean containsKey(Object key) {
            if ("ADMIN".equals(role)) return true;
            return super.containsKey(key);
        }
    }
    private final PermissionMap permissionsMap = new PermissionMap();

    public boolean isConnecte() {
        return token != null && !token.isBlank();
    }

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }

    public boolean isFormateur() {
        return "FORMATEUR".equals(role);
    }

    public boolean isAdminOrFormateur() {
        return "ADMIN".equals(role) || "FORMATEUR".equals(role);
    }

    /** Vrai si le compte est en attente de validation admin */
    public boolean isEnAttente() {
        return "EN_ATTENTE".equals(statut);
    }

    /** Vrai si le compte est pleinement actif */
    public boolean isActif() {
        return "ACTIF".equals(statut);
    }

    /**
     * Vérifie si l'utilisateur possède la permission identifiée par ce code.
     * Les admins ont toutes les permissions par convention.
     */
    public boolean hasPermission(String code) {
        if ("ADMIN".equals(role)) return true;
        if (code == null) return false;
        return permissions != null && permissions.contains(code);
    }

    /** Premier mot du nomComplet (prénom), ou email avant @. */
    public String getPrenom() {
        if (nomComplet != null && !nomComplet.isBlank()) {
            return nomComplet.split(" ")[0];
        }
        if (email != null && email.contains("@")) {
            return email.split("@")[0];
        }
        return "vous";
    }

    /** Première lettre du nom complet ou de l'email, en majuscule. */
    public String getInitiale() {
        if (nomComplet != null && !nomComplet.isBlank()) {
            return nomComplet.substring(0, 1).toUpperCase();
        }
        if (email != null && !email.isBlank()) {
            return email.substring(0, 1).toUpperCase();
        }
        return "F";
    }

    public void clear() {
        token = null;
        utilisateurId = null;
        email = null;
        role = null;
        nomComplet = null;
        statut = null;
        institutionId = null;
        permissions = new ArrayList<>();
        permissionsMap.clear();
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public Long getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(Long utilisateurId) { this.utilisateurId = utilisateurId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getNomComplet() { return nomComplet; }
    public void setNomComplet(String nomComplet) { this.nomComplet = nomComplet; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public Long getInstitutionId() { return institutionId; }
    public void setInstitutionId(Long institutionId) { this.institutionId = institutionId; }
    public List<String> getPermissions() { return permissions; }
    public void setPermissions(List<String> permissions) {
        this.permissions = permissions != null ? permissions : new ArrayList<>();
        permissionsMap.clear();
        for (String p : this.permissions) {
            permissionsMap.put(p, Boolean.TRUE);
        }
    }
    /** Map EL-friendly pour éviter les "method expression" errors du validateur JSF.
     *  Usage : permissionsMap['5013'] au lieu de hasPermission('5013'). */
    public Map<String, Boolean> getPermissionsMap() { return permissionsMap; }
}
