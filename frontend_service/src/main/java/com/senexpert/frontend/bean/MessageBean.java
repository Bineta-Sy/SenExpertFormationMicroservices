package com.senexpert.frontend.bean;

import com.senexpert.frontend.bean.ParticularDashboardBean.FormationResume;
import com.senexpert.frontend.client.FormationServiceClient;
import com.senexpert.frontend.model.MessageFront;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.*;

/**
 * Gestion du fil de messages entre l'apprenant et son formateur,
 * par formation (offre).
 */
@Named
@ViewScoped
public class MessageBean implements Serializable {

    @Inject private FormationServiceClient client;
    @Inject private SessionBean            session;
    @Inject private ParticularDashboardBean dashboard;
    @Inject private OffreBean              offreBean;

    /** Formation sélectionnée dans l'UI. */
    private Long   offreIdSelectionnee;
    private String offreTitreSelectionnee;

    /** Contenu du message en cours de rédaction. */
    private String contenu;

    /** Messages de la conversation sélectionnée. */
    private List<MessageFront> messages = new ArrayList<>();

    /** Nombre total de messages non lus (badge en sidebar). */
    private long nbNonLus = 0L;

    private boolean chargee = false;

    @PostConstruct
    public void init() {
        // Chargement différé
    }

    /** Charge les stats de non-lus (appelé par remoteCommand depuis dashboard). */
    public void charger() {
        if (chargee || session.getUtilisateurId() == null) return;
        chargee = true;
        try {
            nbNonLus = client.messagesNonLus(session.getUtilisateurId(), session.getToken());
        } catch (Exception ignored) {}

        // Sélectionner la première formation validée par défaut
        if (offreIdSelectionnee == null && !offreBean.getMesDemandes().isEmpty()) {
            offreBean.getMesDemandes().stream()
                .filter(d -> "VALIDEE".equals(d.getStatut()) && d.getOffre() != null)
                .findFirst()
                .ifPresent(d -> {
                    offreIdSelectionnee    = d.getOffre().getId();
                    offreTitreSelectionnee = d.getOffre().getTitre();
                });
            if (offreIdSelectionnee != null) chargerConversation();
        }
    }

    /** Charge la conversation pour l'offre sélectionnée. */
    public void chargerConversation() {
        if (offreIdSelectionnee == null || session.getUtilisateurId() == null) return;
        messages = new ArrayList<>(
            client.conversation(offreIdSelectionnee, session.getUtilisateurId(), session.getToken())
        );
        // Marquer comme lus
        client.marquerConversationLue(offreIdSelectionnee, session.getUtilisateurId(), session.getToken());
        nbNonLus = Math.max(0, nbNonLus - messages.stream().filter(m -> !m.isLu()).count());
    }

    /** Changer de formation dans le sélecteur. */
    public void changerFormation() {
        messages.clear();
        // Chercher le titre
        if (offreIdSelectionnee != null) {
            offreBean.getMesDemandes().stream()
                .filter(d -> d.getOffre() != null && offreIdSelectionnee.equals(d.getOffre().getId()))
                .findFirst()
                .ifPresent(d -> offreTitreSelectionnee = d.getOffre().getTitre());
            chargerConversation();
        }
    }

    /** Envoie le message au formateur (destinataireId = 0 = équipe pédagogique). */
    public void envoyer() {
        if (contenu == null || contenu.isBlank()) {
            addMsg(false, "Le message ne peut pas être vide.");
            return;
        }
        if (offreIdSelectionnee == null) {
            addMsg(false, "Sélectionnez une formation avant d'envoyer un message.");
            return;
        }

        MessageFront msg = new MessageFront();
        msg.setOffreId(offreIdSelectionnee);
        msg.setExpediteurId(session.getUtilisateurId());
        msg.setExpediteurRole("APPRENANT");
        msg.setExpediteurNom(
            session.getNomComplet() != null && !session.getNomComplet().isBlank()
                ? session.getNomComplet() : session.getEmail());
        msg.setDestinataireId(0L); // 0 = équipe pédagogique / formateur de l'offre
        msg.setContenu(contenu.trim());

        MessageFront envoye = client.envoyerMessage(msg, session.getToken());
        if (envoye != null) {
            messages.add(envoye);
            contenu = "";
        } else {
            addMsg(false, "Impossible d'envoyer le message. Réessayez.");
        }
    }

    private void addMsg(boolean ok, String text) {
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(ok ? FacesMessage.SEVERITY_INFO : FacesMessage.SEVERITY_ERROR,
                ok ? "OK" : "Erreur", text));
    }

    // ── Getters / Setters ──────────────────────────────────────────────────────

    public Long            getOffreIdSelectionnee()              { return offreIdSelectionnee; }
    public void            setOffreIdSelectionnee(Long v)        { this.offreIdSelectionnee = v; }
    /** Alias sans 'e' final — utilisé dans dashboard-particulier.xhtml */
    public Long            getOffreIdSelectionne()               { return offreIdSelectionnee; }
    public void            setOffreIdSelectionne(Long v)         { this.offreIdSelectionnee = v; }
    public String          getOffreTitreSelectionnee()           { return offreTitreSelectionnee; }
    public String          getContenu()                          { return contenu; }
    public void            setContenu(String v)                  { this.contenu = v; }
    public List<MessageFront> getMessages()                      { return messages; }
    /** Alias utilisé dans certaines vues pour vérifier si des conversations existent. */
    public List<MessageFront> getConversations()                 { return messages; }
    public long            getNbNonLus()                         { return nbNonLus; }
    public boolean         isAucunMessage()                      { return messages.isEmpty(); }
}
