package com.senexpert.frontend.bean;

import com.senexpert.frontend.client.FormationServiceClient;
import com.senexpert.frontend.client.UserServiceClient;
import com.senexpert.frontend.model.DemandeFormation;
import com.senexpert.frontend.model.DemandeLibreFront;
import com.senexpert.frontend.model.Institution;
import com.senexpert.frontend.model.Utilisateur;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.util.List;

@Named
@RequestScoped
public class NotifBean {

    @Inject private SessionBean sessionBean;
    @Inject private UserServiceClient userClient;
    @Inject private FormationServiceClient formationClient;

    private Integer totalPending          = null;
    private Integer pendingUsers          = null;
    private Integer pendingInstitutions   = null;
    private Integer pendingDemandes       = null;
    private Integer pendingDemandesLibres = null;

    // Notifications particulier : demandes dont le statut a changé (VALIDEE ou REJETEE)
    private Integer nbNonLues = null;

    private void charger() {
        if (totalPending != null) return;
        if (!sessionBean.isAdmin() || sessionBean.getToken() == null) {
            totalPending = 0; pendingUsers = 0; pendingInstitutions = 0;
            pendingDemandes = 0; pendingDemandesLibres = 0;
            return;
        }
        try {
            List<Utilisateur> users = userClient.listerUtilisateurs(sessionBean.getToken());
            pendingUsers = (int) users.stream().filter(u -> "EN_ATTENTE".equals(u.getStatut())).count();

            List<Institution> insts = userClient.listerInstitutions(sessionBean.getToken());
            pendingInstitutions = (int) insts.stream().filter(i -> "EN_ATTENTE".equals(i.getStatut())).count();

            List<DemandeFormation> dems = formationClient.toutesLesDemandes(sessionBean.getToken());
            pendingDemandes = (int) dems.stream().filter(d -> "EN_ATTENTE".equals(d.getStatut())).count();

            try {
                List<DemandeLibreFront> demsLibres = formationClient.toutesDemandesLibres(sessionBean.getToken());
                pendingDemandesLibres = (int) demsLibres.stream()
                        .filter(d -> "EN_ATTENTE".equals(d.getStatut())).count();
            } catch (Exception ex) { pendingDemandesLibres = 0; }

            totalPending = pendingUsers + pendingInstitutions + pendingDemandes + pendingDemandesLibres;
        } catch (Exception e) {
            totalPending = 0; pendingUsers = 0; pendingInstitutions = 0;
            pendingDemandes = 0; pendingDemandesLibres = 0;
        }
    }

    private void chargerNotifParticulier() {
        if (nbNonLues != null) return;
        if (sessionBean.getToken() == null || sessionBean.isAdmin()) {
            nbNonLues = 0;
            return;
        }
        try {
            List<DemandeFormation> dems = formationClient.mesDemandes(
                    sessionBean.getUtilisateurId(), sessionBean.getToken());
            // Compte les demandes VALIDEE ou REJETEE comme notifications à consulter
            nbNonLues = (int) dems.stream()
                    .filter(d -> "VALIDEE".equals(d.getStatut()) || "REJETEE".equals(d.getStatut()))
                    .count();
        } catch (Exception e) {
            nbNonLues = 0;
        }
    }

    public int getTotalPending()          { charger(); return totalPending; }
    public int getPendingUsers()          { charger(); return pendingUsers; }
    public int getPendingInstitutions()   { charger(); return pendingInstitutions; }
    public int getPendingDemandes()       { charger(); return pendingDemandes; }
    public int getPendingDemandesLibres() { charger(); return pendingDemandesLibres; }
    public boolean isHasPending()         { charger(); return totalPending > 0; }

    /** Nombre de notifications non lues pour le particulier connecté. */
    public int getNbNonLues()             { chargerNotifParticulier(); return nbNonLues; }
}
