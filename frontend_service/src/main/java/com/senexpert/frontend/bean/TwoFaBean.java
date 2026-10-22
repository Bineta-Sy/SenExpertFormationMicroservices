package com.senexpert.frontend.bean;

import com.senexpert.frontend.client.UserServiceClient;
import com.senexpert.frontend.model.Utilisateur;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Bean pour la configuration et la gestion du 2FA TOTP (admin).
 */
@Named
@ViewScoped
public class TwoFaBean implements Serializable {

    @Inject private UserServiceClient userClient;
    @Inject private SessionBean session;

    private String secret;
    private String otpauthUrl;
    private String codeVerification;
    private boolean active;
    private List<Utilisateur> admins;

    @PostConstruct
    public void init() {
        charger();
    }

    private void charger() {
        // Charger la liste des admins pour le tableau de bord 2FA
        List<Utilisateur> tous = userClient.listerUtilisateurs(session.getToken());
        admins = tous.stream()
                .filter(u -> "ADMIN".equals(u.getRole()))
                .collect(Collectors.toList());
        // Determiner si le 2FA est actif pour le compte courant
        admins.stream()
                .filter(u -> session.getUtilisateurId() != null
                        && session.getUtilisateurId().equals(u.getId()))
                .findFirst()
                .ifPresent(u -> active = u.isTotpActif());
    }

    /** Etape 1 : generer le secret + QR */
    public void genererSecret() {
        if (session.getUtilisateurId() == null) { msg(false, "Session invalide."); return; }
        Map<String, String> result = userClient.setup2FA(session.getUtilisateurId(), session.getToken());
        if (result.isEmpty()) { msg(false, "Erreur lors de la generation du secret."); return; }
        secret = result.get("secret");
        otpauthUrl = result.get("otpauthUrl");
    }

    /** Etape 2 : verifier le code et activer le 2FA */
    public void verifierEtActiver() {
        if (codeVerification == null || codeVerification.isBlank()) {
            msg(false, "Entrez le code genere par votre application.");
            return;
        }
        Map<String, Object> result = userClient.verifier2FA(session.getEmail(), codeVerification);
        Boolean valid = (Boolean) result.get("valid");
        if (Boolean.TRUE.equals(valid)) {
            active = true;
            otpauthUrl = null;
            AuditBean.log("2FA_ACTIVATION", "UTILISATEUR", "2FA active pour " + session.getEmail(),
                    session.getEmail(), "INFO");
            msg(true, "2FA active avec succes ! A la prochaine connexion, le code vous sera demande.");
            charger();
        } else {
            msg(false, "Code invalide. Verifiez l'heure de votre appareil et reessayez.");
        }
        codeVerification = null;
    }

    /** Desactiver le 2FA sur son propre compte */
    public void desactiver() {
        if (session.getUtilisateurId() == null) return;
        boolean ok = userClient.disable2FA(session.getUtilisateurId(), session.getToken());
        if (ok) {
            active = false; secret = null; otpauthUrl = null;
            AuditBean.log("2FA_DESACTIVATION", "UTILISATEUR", "2FA desactive pour " + session.getEmail(),
                    session.getEmail(), "WARN");
            msg(true, "2FA desactive.");
            charger();
        } else {
            msg(false, "Echec desactivation.");
        }
    }

    /** Reinitialiser le 2FA d'un autre admin (urgence) */
    public void resetAutreAdmin(Long adminId) {
        boolean ok = userClient.disable2FA(adminId, session.getToken());
        AuditBean.log("2FA_RESET", "UTILISATEUR", "2FA reinitialise pour ID=" + adminId,
                session.getEmail(), "WARN");
        msg(ok, "2FA reinitialise.", "Echec reinitialisation.");
        charger();
    }

    /** URL encodee pour le QR code (via qrserver.com) */
    public String getOtpauthUrlEncoded() {
        if (otpauthUrl == null) return "";
        return URLEncoder.encode(otpauthUrl, StandardCharsets.UTF_8);
    }

    private void msg(boolean ok, String success, String error) {
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(ok ? FacesMessage.SEVERITY_INFO : FacesMessage.SEVERITY_ERROR,
                ok ? "Succes" : "Erreur", ok ? success : error));
    }
    private void msg(boolean ok, String msg) { msg(ok, msg, msg); }

    // Getters / Setters
    public String getSecret() { return secret; }
    public String getOtpauthUrl() { return otpauthUrl; }
    public String getCodeVerification() { return codeVerification; }
    public void setCodeVerification(String v) { this.codeVerification = v; }
    public boolean isActive() { return active; }
    public List<Utilisateur> getAdmins() { return admins; }
}
