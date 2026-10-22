//package com.senexpert.frontend.bean;
//
//import com.senexpert.frontend.client.UserServiceClient;
//import jakarta.enterprise.context.RequestScoped;
//import jakarta.faces.application.FacesMessage;
//import jakarta.faces.context.FacesContext;
//import jakarta.inject.Inject;
//import jakarta.inject.Named;
//
//import java.io.Serializable;
//import java.util.Map;
//
//@Named
//@RequestScoped
//public class AuthBean implements Serializable {
//
//    @Inject private UserServiceClient userClient;
//    @Inject private SessionBean session;
//
//    // Champs login
//    private String email;
//    private String motDePasse;
//
//    // Champs inscription
//    private String nom;
//    private String prenom;
//    private String emailRegister;
//    private String mdpRegister;
//    private String role = "PARTICULIER";
//
//    // Champs additionnels selon le rôle
//    private String telephone;
//    private String profession;         // PARTICULIER
//    private String numeroCni;          // PARTICULIER
//    private String numeroInstitution;   // AGENT : NINEA
//    private String nomInstitution;      // AGENT : nom de l'institution
//    private String typeInstitution = "ENTREPRISE"; // AGENT : type
//    private String secteurInstitution;  // AGENT : secteur d'activité
//
//    // Etape 2FA
//    private String code2FA;
//
//    /** Connexion : gere le cas 2FA pour les admins */
//    public String login() {
//        String loginResult = userClient.login(email, motDePasse);
//
//        if (loginResult == null) {
//            msg(false, "Email/mot de passe incorrect ou compte non valide");
//            return null;
//        }
//
//        // Cas 1 : le serveur retourne "REQUIRES_2FA:<email>" → etape 2FA requise
//        if (loginResult.startsWith("REQUIRES_2FA:")) {
//            session.setEmail(loginResult.substring("REQUIRES_2FA:".length()));
//            session.setRole("ADMIN"); // on sait que c'est un admin
//            return "login-2fa?faces-redirect=true";
//        }
//
//        // Cas 2 : token JWT normal
//        finaliserConnexion(loginResult, email);
//        String r = extraireRole(loginResult);
//        if ("ADMIN".equals(r))       return "admin/dashboard?faces-redirect=true";
//        if ("PARTICULIER".equals(r)) return "dashboard-particulier?faces-redirect=true";
//        if ("AGENT".equals(r))       return "dashboard-agent-institution?faces-redirect=true";
//        if ("FORMATEUR".equals(r))   return "dashboard-formateur?faces-redirect=true";
//        return "offres?faces-redirect=true";
//    }
//
//    /** Valide le code TOTP (etape 2 du login admin) */
//    public String verifier2FA() {
//        if (code2FA == null || code2FA.isBlank()) {
//            msg(false, "Entrez le code a 6 chiffres.");
//            return null;
//        }
//        Map<String, Object> result = userClient.verifier2FA(session.getEmail(), code2FA);
//        Boolean valid = (Boolean) result.get("valid");
//        if (!Boolean.TRUE.equals(valid)) {
//            msg(false, "Code invalide ou expire. Reessayez.");
//            return null;
//        }
//        String token = (String) result.get("token");
//        finaliserConnexion(token, session.getEmail());
//        return "admin/dashboard?faces-redirect=true";
//    }
//
//    private void finaliserConnexion(String token, String emailUser) {
//        String r = extraireRole(token);
//        session.setToken(token);
//        session.setEmail(emailUser);
//        session.setRole(r);
//        com.senexpert.frontend.model.Utilisateur moi = userClient.moi(token);
//        if (moi != null) {
//            session.setUtilisateurId(moi.getId());
//            session.setNomComplet(moi.getPrenom() + " " + moi.getNom());
//            session.setStatut(moi.getStatut());
//            if (moi.getInstitution() != null) session.setInstitutionId(moi.getInstitution().getId());
//            // Charger les permissions effectives (profil + directes)
//            java.util.List<String> perms = userClient.getPermissionsEffectives(moi.getId(), token);
//            session.setPermissions(perms);
//        }
//    }
//
//    /** Inscription */
//    public String register() {
//        boolean ok = userClient.register(nom, prenom, emailRegister, mdpRegister, role,
//                telephone, profession, numeroCni, numeroInstitution,
//                nomInstitution, typeInstitution, secteurInstitution);
//        if (!ok) { msg(false, "Inscription echouee. Email deja utilise ?"); return null; }
//        msg(true, "Compte cree. En attente de validation par l'administrateur.");
//        return "login?faces-redirect=true";
//    }
//
//    /** Deconnexion */
//    public String logout() {
//        session.clear();
//        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
//        return "/login?faces-redirect=true";
//    }
//
//    private String extraireRole(String token) {
//        try {
//            String payload = token.split("\\.")[1];
//            int padding = payload.length() % 4;
//            if (padding != 0) payload += "=".repeat(4 - padding);
//            String json = new String(java.util.Base64.getUrlDecoder().decode(payload));
//            int idx = json.indexOf("\"role\"");
//            if (idx >= 0) {
//                int start = json.indexOf("\"", idx + 7) + 1;
//                int end   = json.indexOf("\"", start);
//                return json.substring(start, end);
//            }
//        } catch (Exception ignored) {}
//        return "AGENT";
//    }
//
//    private void msg(boolean ok, String text) {
//        FacesContext.getCurrentInstance().addMessage(null,
//            new FacesMessage(ok ? FacesMessage.SEVERITY_INFO : FacesMessage.SEVERITY_ERROR,
//                ok ? "Succes" : "Erreur", text));
//    }
//
//    // Getters / Setters
//    public String getEmail() { return email; }
//    public void setEmail(String email) { this.email = email; }
//    public String getMotDePasse() { return motDePasse; }
//    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }
//    public String getNom() { return nom; }
//    public void setNom(String nom) { this.nom = nom; }
//    public String getPrenom() { return prenom; }
//    public void setPrenom(String prenom) { this.prenom = prenom; }
//    public String getEmailRegister() { return emailRegister; }
//    public void setEmailRegister(String emailRegister) { this.emailRegister = emailRegister; }
//    public String getMdpRegister() { return mdpRegister; }
//    public void setMdpRegister(String mdpRegister) { this.mdpRegister = mdpRegister; }
//    public String getRole() { return role; }
//    public void setRole(String role) { this.role = role; }
//    public String getCode2FA() { return code2FA; }
//    public void setCode2FA(String code2FA) { this.code2FA = code2FA; }
//    public String getTelephone() { return telephone; }
//    public void setTelephone(String telephone) { this.telephone = telephone; }
//    public String getProfession() { return profession; }
//    public void setProfession(String profession) { this.profession = profession; }
//    public String getNumeroCni() { return numeroCni; }
//    public void setNumeroCni(String numeroCni) { this.numeroCni = numeroCni; }
//    public String getNumeroInstitution() { return numeroInstitution; }
//    public void setNumeroInstitution(String numeroInstitution) { this.numeroInstitution = numeroInstitution; }
//    public String getNomInstitution() { return nomInstitution; }
//    public void setNomInstitution(String nomInstitution) { this.nomInstitution = nomInstitution; }
//    public String getTypeInstitution() { return typeInstitution; }
//    public void setTypeInstitution(String typeInstitution) { this.typeInstitution = typeInstitution; }
//    public String getSecteurInstitution() { return secteurInstitution; }
//    public void setSecteurInstitution(String secteurInstitution) { this.secteurInstitution = secteurInstitution; }
//}


package com.senexpert.frontend.bean;

import com.senexpert.frontend.client.UserServiceClient;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;

@Named
@RequestScoped
public class AuthBean implements Serializable {

    @Inject private UserServiceClient userClient;
    @Inject private SessionBean session;

    // Champs login
    private String email;
    private String motDePasse;

    // Champs inscription
    private String nom;
    private String prenom;
    private String emailRegister;
    private String mdpRegister;
    private String role = "PARTICULIER";

    // Champs additionnels selon le rôle
    private String telephone;
    private String profession;          // PARTICULIER
    private String numeroCni;           // PARTICULIER
    private String numeroInstitution;   // AGENT : NINEA
    private String nomInstitution;      // AGENT : nom de l'institution
    private String typeInstitution = "ENTREPRISE"; // AGENT : type
    private String secteurInstitution;  // AGENT : secteur d'activité

    /** Connexion directe sans étape 2FA */
    public String login() {
        String loginResult = userClient.login(email, motDePasse);

        if (loginResult == null) {
            msg(false, "Email/mot de passe incorrect ou compte non valide");
            return null;
        }

        // Finalisation immédiate et extraction du rôle depuis le JWT
        finaliserConnexion(loginResult, email);
        String r = extraireRole(loginResult);
        
        if ("ADMIN".equals(r))       return "admin/dashboard?faces-redirect=true";
        if ("PARTICULIER".equals(r)) return "dashboard-particulier?faces-redirect=true";
        if ("AGENT".equals(r))       return "dashboard-agent-institution?faces-redirect=true";
        if ("FORMATEUR".equals(r))   return "dashboard-formateur?faces-redirect=true";
        return "offres?faces-redirect=true";
    }

    private void finaliserConnexion(String token, String emailUser) {
        String r = extraireRole(token);
        session.setToken(token);
        session.setEmail(emailUser);
        session.setRole(r);
        com.senexpert.frontend.model.Utilisateur moi = userClient.moi(token);
        if (moi != null) {
            session.setUtilisateurId(moi.getId());
            session.setNomComplet(moi.getPrenom() + " " + moi.getNom());
            session.setStatut(moi.getStatut());
            if (moi.getInstitution() != null) session.setInstitutionId(moi.getInstitution().getId());
            
            java.util.List<String> perms = userClient.getPermissionsEffectives(moi.getId(), token);
            session.setPermissions(perms);
        }
    }

    /** Inscription */
    public String register() {
        boolean ok = userClient.register(nom, prenom, emailRegister, mdpRegister, role,
                telephone, profession, numeroCni, numeroInstitution,
                nomInstitution, typeInstitution, secteurInstitution);
        if (!ok) { msg(false, "Inscription échouée. Email déjà utilisé ?"); return null; }
        msg(true, "Compte créé. En attente de validation par l'administrateur.");
        return "login?faces-redirect=true";
    }

    /** Déconnexion */
    public String logout() {
        session.clear();
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return "/login?faces-redirect=true";
    }

    private String extraireRole(String token) {
        try {
            String payload = token.split("\\.")[1];
            int padding = payload.length() % 4;
            if (padding != 0) payload += "=".repeat(4 - padding);
            String json = new String(java.util.Base64.getUrlDecoder().decode(payload));
            int idx = json.indexOf("\"role\"");
            if (idx >= 0) {
                int start = json.indexOf("\"", idx + 7) + 1;
                int end   = json.indexOf("\"", start);
                return json.substring(start, end);
            }
        } catch (Exception ignored) {}
        return "AGENT";
    }

    private void msg(boolean ok, String text) {
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(ok ? FacesMessage.SEVERITY_INFO : FacesMessage.SEVERITY_ERROR,
                ok ? "Succès" : "Erreur", text));
    }

    // Getters / Setters épurés
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public String getEmailRegister() { return emailRegister; }
    public void setEmailRegister(String emailRegister) { this.emailRegister = emailRegister; }
    public String getMdpRegister() { return mdpRegister; }
    public void setMdpRegister(String mdpRegister) { this.mdpRegister = mdpRegister; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public String getProfession() { return profession; }
    public void setProfession(String profession) { this.profession = profession; }
    public String getNumeroCni() { return numeroCni; }
    public void setNumeroCni(String numeroCni) { this.numeroCni = numeroCni; }
    public String getNumeroInstitution() { return numeroInstitution; }
    public void setNumeroInstitution(String numeroInstitution) { this.numeroInstitution = numeroInstitution; }
    public String getNomInstitution() { return nomInstitution; }
    public void setNomInstitution(String nomInstitution) { this.nomInstitution = nomInstitution; }
    public String getTypeInstitution() { return typeInstitution; }
    public void setTypeInstitution(String typeInstitution) { this.typeInstitution = typeInstitution; }
    public String getSecteurInstitution() { return secteurInstitution; }
    public void setSecteurInstitution(String secteurInstitution) { this.secteurInstitution = secteurInstitution; }
}
