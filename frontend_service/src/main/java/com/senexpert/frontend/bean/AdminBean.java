package com.senexpert.frontend.bean;

import com.senexpert.frontend.client.FormationServiceClient;
import com.senexpert.frontend.client.UserServiceClient;
import com.senexpert.frontend.model.DemandeFormation;
import com.senexpert.frontend.model.Institution;
import com.senexpert.frontend.model.Utilisateur;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.List;

@Named
@ViewScoped
public class AdminBean implements Serializable {

    @Inject
    private UserServiceClient userClient;

    @Inject
    private FormationServiceClient formationClient;

    @Inject
    private SessionBean session;

    private List<Utilisateur> utilisateurs;
    private List<Institution> institutions;
    private List<DemandeFormation> demandes;

    // Formulaire créer admin
    private String adminNom;
    private String adminPrenom;
    private String adminEmail;
    private String adminMotDePasse;

    @PostConstruct
    public void init() {
        charger();
    }

    public void charger() {
        String token = session.getToken();
        utilisateurs  = userClient.listerUtilisateurs(token);
        institutions  = userClient.listerInstitutions(token);
        demandes      = formationClient.toutesLesDemandes(token);
        // Résolution du nom du candidat pour chaque demande
        for (DemandeFormation d : demandes) {
            if (d.getUtilisateurId() != null) {
                Utilisateur u = userClient.trouverParId(d.getUtilisateurId(), token);
                d.setNomCandidat(u != null ? u.getPrenom() + " " + u.getNom()
                                           : "ID #" + d.getUtilisateurId());
            }
        }
    }

    public void validerUtilisateur(Long id) {
        boolean ok = userClient.validerUtilisateur(id, session.getToken());
        message(ok, "Utilisateur validé.", "Échec validation utilisateur.");
        utilisateurs = userClient.listerUtilisateurs(session.getToken());
    }

    public void validerInstitution(Long id) {
        boolean ok = userClient.validerInstitution(id, session.getToken());
        message(ok, "Institution validée.", "Échec validation institution.");
        institutions = userClient.listerInstitutions(session.getToken());
    }

    public void desactiverInstitution(Long id) {
        boolean ok = userClient.desactiverInstitution(id, session.getToken());
        message(ok, "Institution désactivée.", "Échec désactivation.");
        institutions = userClient.listerInstitutions(session.getToken());
    }

    public void reactiverInstitution(Long id) {
        boolean ok = userClient.reactiverInstitution(id, session.getToken());
        message(ok, "Institution réactivée.", "Échec réactivation.");
        institutions = userClient.listerInstitutions(session.getToken());
    }

    public void supprimerInstitution(Long id) {
        boolean ok = userClient.supprimerInstitution(id, session.getToken());
        message(ok, "Institution supprimée.", "Échec suppression.");
        institutions = userClient.listerInstitutions(session.getToken());
    }

    public void validerDemande(Long id) {
        boolean ok = formationClient.validerDemande(id, session.getToken());
        message(ok, "Demande validée.", "Échec validation demande.");
        charger();
    }

    public void rejeterDemande(Long id) {
        boolean ok = formationClient.rejeterDemande(id, session.getToken());
        message(ok, "Demande rejetée.", "Échec rejet demande.");
        charger();
    }

    public void changerRole(Long id, String role) {
        boolean ok = userClient.changerRole(id, role, session.getToken());
        message(ok, "Rôle modifié : " + role, "Échec modification du rôle.");
        utilisateurs = userClient.listerUtilisateurs(session.getToken());
    }

    public void desactiverUtilisateur(Long id) {
        boolean ok = userClient.desactiverUtilisateur(id, session.getToken());
        message(ok, "Compte désactivé.", "Échec désactivation.");
        utilisateurs = userClient.listerUtilisateurs(session.getToken());
    }

    public void reactiverUtilisateur(Long id) {
        boolean ok = userClient.reactiverUtilisateur(id, session.getToken());
        message(ok, "Compte réactivé.", "Échec réactivation.");
        utilisateurs = userClient.listerUtilisateurs(session.getToken());
    }

    public void supprimerUtilisateur(Long id) {
        boolean ok = userClient.supprimerUtilisateur(id, session.getToken());
        message(ok, "Compte supprimé.", "Échec suppression.");
        utilisateurs = userClient.listerUtilisateurs(session.getToken());
    }

    public void creerAdmin() {
        boolean ok = userClient.creerAdmin(adminNom, adminPrenom, adminEmail,
                adminMotDePasse, session.getToken());
        if (ok) {
            message(true, "Compte admin créé pour " + adminEmail + ".", "");
            adminNom = null; adminPrenom = null; adminEmail = null; adminMotDePasse = null;
            utilisateurs = userClient.listerUtilisateurs(session.getToken());
        } else {
            message(false, "", "Échec création admin (email déjà utilisé ?).");
        }
    }

    private void message(boolean ok, String success, String error) {
        if (ok) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Succès", success));
        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", error));
        }
    }

    public long countDemandesParStatut(String statut) {
        if (demandes == null) return 0;
        return demandes.stream().filter(d -> statut.equals(d.getStatut())).count();
    }

    public List<Utilisateur> getUtilisateurs() { return utilisateurs; }
    public List<Institution> getInstitutions() {
        if (institutions == null) return java.util.List.of();
        return institutions.stream()
            .sorted(java.util.Comparator.comparingInt(i -> {
                if ("EN_ATTENTE".equals(i.getStatut())) return 0;
                if ("INACTIF".equals(i.getStatut()))   return 1;
                return 2;
            }))
            .collect(java.util.stream.Collectors.toList());
    }
    public List<DemandeFormation> getDemandes() { return demandes; }
    public String getAdminNom() { return adminNom; }
    public void setAdminNom(String v) { this.adminNom = v; }
    public String getAdminPrenom() { return adminPrenom; }
    public void setAdminPrenom(String v) { this.adminPrenom = v; }
    public String getAdminEmail() { return adminEmail; }
    public void setAdminEmail(String v) { this.adminEmail = v; }
    public String getAdminMotDePasse() { return adminMotDePasse; }
    public void setAdminMotDePasse(String v) { this.adminMotDePasse = v; }
}
