package com.senexpert.frontend.bean;

import com.senexpert.frontend.client.UserServiceClient;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.PhaseEvent;
import jakarta.faces.event.PhaseId;
import jakarta.faces.event.PhaseListener;
import jakarta.inject.Inject;
import java.util.List;

/**
 * Recharge les permissions effectives depuis le backend à chaque cycle JSF
 * (phase RESTORE_VIEW), afin que les changements de permissions faits par
 * un admin prennent effet immédiatement sans que l'utilisateur ait à se
 * reconnecter.
 */
public class PermissionRefreshListener implements PhaseListener {

    @Inject
    private SessionBean session;

    @Inject
    private UserServiceClient userClient;

    @Override
    public PhaseId getPhaseId() {
        return PhaseId.RESTORE_VIEW;
    }

    @Override
    public void beforePhase(PhaseEvent event) {
        // Rien avant
    }

    @Override
    public void afterPhase(PhaseEvent event) {
        if (session == null || !session.isConnecte()) return;
        if (session.getUtilisateurId() == null) return;
        try {
            List<String> perms = userClient.getPermissionsEffectives(
                    session.getUtilisateurId(), session.getToken());
            session.setPermissions(perms);
        } catch (Exception ignored) {
            // En cas d'erreur réseau, on conserve les permissions actuelles
        }
    }
}
