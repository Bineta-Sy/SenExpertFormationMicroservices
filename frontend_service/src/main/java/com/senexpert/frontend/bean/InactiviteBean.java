package com.senexpert.frontend.bean;

import com.senexpert.frontend.client.UserServiceClient;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;

@Named
@ViewScoped
public class InactiviteBean implements Serializable {

    @Inject private UserServiceClient userClient;
    @Inject private SessionBean session;

    private int joursInactivite = 90;
    private int derniereExpiration = -1;

    public void expirerInactifs() {
        derniereExpiration = userClient.expirerComptesInactifs(joursInactivite, session.getToken());
        AuditBean.log("EXPIRATION_INACTIFS", "UTILISATEUR",
                derniereExpiration + " compte(s) expire(s) apres " + joursInactivite + "j d'inactivite",
                session.getEmail(), "WARN");
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_INFO, "Expiration effectuee",
                derniereExpiration + " compte(s) desactive(s) (inactifs depuis " + joursInactivite + " jours)."));
    }

    public int getJoursInactivite() { return joursInactivite; }
    public void setJoursInactivite(int v) { this.joursInactivite = v; }
    public int getDerniereExpiration() { return derniereExpiration; }
}
