package com.senexpert.frontend.bean;

import com.senexpert.frontend.client.FormationServiceClient;
import com.senexpert.frontend.client.UserServiceClient;
import com.senexpert.frontend.model.*;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Bean pour la page de détail complet d'une offre de formation (admin).
 * Charge : offre, formateurs, cours/modules, sessions, demandes.
 */
@Named
@ViewScoped
public class OffreDetailBean implements Serializable {

    @Inject private FormationServiceClient formationClient;
    @Inject private UserServiceClient      userClient;
    @Inject private SessionBean            session;

    // ── Contexte (viewParam) ──────────────────────────────────────────────────
    private Long   offreId;

    // ── Données chargées ──────────────────────────────────────────────────────
    private Offre                        offre;
    private List<FormateurFront>         formateurs      = new ArrayList<>();
    private List<CoursEnLigneFront>      cours           = new ArrayList<>();
    private List<SessionFormationFront>  sessions        = new ArrayList<>();
    private List<DemandeFormation>       demandes        = new ArrayList<>();

    // ── Stats ─────────────────────────────────────────────────────────────────
    private int nbInscrits    = 0;
    private int nbEnAttente   = 0;
    private int nbValides     = 0;
    private int nbQuestions   = 0;

    // ── Édition demande ────────────────────────────────────────────────────────
    // (valider/rejeter inline sans quitter la page)

    // ─────────────────────────────────────────────────────────────────────────

    @PostConstruct
    public void init() { /* offreId injecté par f:viewParam → charger() via f:viewAction */ }

    /** Appelé par f:viewAction après injection de l'offreId. */
    public void charger() {
        if (offreId == null) return;
        String token = session.getToken();

        // 1. Offre complète
        List<Offre> toutes = formationClient.listerOffresCompletes();
        offre = toutes.stream().filter(o -> offreId.equals(o.getId())).findFirst().orElse(null);

        // 2. Formateurs de l'offre
        formateurs = formationClient.formateursParOffre(offreId);

        // 3. Cours (vue admin = tous, publiés ou non)
        cours = formationClient.coursParOffre(offreId, true);
        nbQuestions = 0;
        if (cours != null) {
            for (CoursEnLigneFront c : cours) {
                if ("QUIZ".equals(c.getType())) nbQuestions += c.getNbQuestions();
            }
        }

        // 4. Sessions de l'offre
        sessions = formationClient.listerSessionsParOffre(offreId, token);

        // 5. Demandes + résolution des noms depuis user_service
        demandes = formationClient.demandesParOffre(offreId, token);

        // Résolution noms candidats
        Map<Long, String> userNames = new HashMap<>();
        try {
            List<Utilisateur> users = userClient.listerUtilisateurs(token);
            if (users != null) {
                users.forEach(u -> {
                    if (u.getId() != null) {
                        String nom = trim(u.getPrenom()) + " " + trim(u.getNom());
                        userNames.put(u.getId(), nom.isBlank() ? u.getEmail() : nom.trim());
                    }
                });
            }
        } catch (Exception ignored) {}

        if (demandes != null) {
            demandes.forEach(d -> {
                if (d.getNomCandidat() == null || d.getNomCandidat().isBlank()) {
                    d.setNomCandidat(userNames.getOrDefault(d.getUtilisateurId(),
                        "Utilisateur #" + d.getUtilisateurId()));
                }
            });
            nbEnAttente = (int) demandes.stream().filter(d -> "EN_ATTENTE".equals(d.getStatut())).count();
            nbValides   = (int) demandes.stream().filter(d -> "VALIDEE".equals(d.getStatut()) || "ACCEPTEE".equals(d.getStatut())).count();
            nbInscrits  = demandes.size();
        }
    }

    // ── Actions demandes ──────────────────────────────────────────────────────

    public void validerDemande(Long id) {
        boolean ok = formationClient.validerDemande(id, session.getToken());
        msg(ok, ok ? "Demande validée." : "Échec de la validation.");
        charger();
    }

    public void rejeterDemande(Long id) {
        boolean ok = formationClient.rejeterDemande(id, session.getToken());
        msg(ok, ok ? "Demande rejetée." : "Échec du rejet.");
        charger();
    }

    // ── Helpers affichage ─────────────────────────────────────────────────────

    public String getStatutBg(String statut) {
        if (statut == null) return "#F1F5F9";
        return switch (statut) {
            case "DISPONIBLE" -> "#D1FAE5";
            case "FERME"      -> "#FEE2E2";
            default           -> "#FEF3C7";
        };
    }

    public String getStatutColor(String statut) {
        if (statut == null) return "#64748B";
        return switch (statut) {
            case "DISPONIBLE" -> "#065F46";
            case "FERME"      -> "#991B1B";
            default           -> "#92400E";
        };
    }

    public String getStatutLabel(String statut) {
        if (statut == null) return "—";
        return switch (statut) {
            case "DISPONIBLE" -> "Disponible";
            case "FERME"      -> "Fermé";
            case "BROUILLON"  -> "Brouillon";
            default -> statut;
        };
    }

    public String getTypeCoursColor(String type) {
        if (type == null) return "#94A3B8";
        return switch (type) {
            case "VIDEO"    -> "#EF4444";
            case "DOCUMENT" -> "#3B82F6";
            case "QUIZ"     -> "#8B5CF6";
            case "LIEN"     -> "#10B981";
            case "LIVE"     -> "#F59E0B";
            default -> "#94A3B8";
        };
    }

    public String getTypeCoursLabel(String type) {
        if (type == null) return "";
        return switch (type) {
            case "VIDEO"    -> "Vidéo";
            case "DOCUMENT" -> "Document";
            case "QUIZ"     -> "Quiz";
            case "LIEN"     -> "Lien";
            case "LIVE"     -> "Live";
            default -> type;
        };
    }

    public String getDemandeBg(String statut) {
        if (statut == null) return "#F1F5F9";
        return switch (statut) {
            case "VALIDEE", "ACCEPTEE" -> "#D1FAE5";
            case "REJETEE"             -> "#FEE2E2";
            default                    -> "#FEF3C7";
        };
    }

    public String getDemandeColor(String statut) {
        if (statut == null) return "#64748B";
        return switch (statut) {
            case "VALIDEE", "ACCEPTEE" -> "#065F46";
            case "REJETEE"             -> "#991B1B";
            default                    -> "#92400E";
        };
    }

    public String getDemandeLabel(String statut) {
        if (statut == null) return "—";
        return switch (statut) {
            case "VALIDEE", "ACCEPTEE" -> "Acceptée";
            case "REJETEE"             -> "Rejetée";
            case "EN_ATTENTE"          -> "En attente";
            default -> statut;
        };
    }

    /** Initiales pour l'avatar d'un formateur */
    public String getInitiales(FormateurFront f) {
        if (f == null) return "?";
        String p = f.getPrenom() != null ? f.getPrenom().trim() : "";
        String n = f.getNom() != null ? f.getNom().trim() : "";
        StringBuilder sb = new StringBuilder();
        if (!p.isEmpty()) sb.append(p.substring(0, 1).toUpperCase());
        if (!n.isEmpty()) sb.append(n.substring(0, 1).toUpperCase());
        return sb.length() > 0 ? sb.toString() : "?";
    }

    /** Initiales pour l'avatar d'un apprenant (nom complet) */
    public String getInitialesNom(String nomComplet) {
        if (nomComplet == null || nomComplet.isBlank()) return "?";
        String[] parts = nomComplet.trim().split("\\s+");
        if (parts.length >= 2)
            return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
        return nomComplet.substring(0, Math.min(2, nomComplet.length())).toUpperCase();
    }

    // ── Internes ─────────────────────────────────────────────────────────────
    private static String trim(String s) { return s != null ? s.trim() : ""; }

    private void msg(boolean ok, String text) {
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(ok ? FacesMessage.SEVERITY_INFO : FacesMessage.SEVERITY_ERROR,
                ok ? "Succès" : "Erreur", text));
    }

    // ── Getters / Setters ────────────────────────────────────────────────────
    public Long    getOffreId()                          { return offreId; }
    public void    setOffreId(Long v)                    { this.offreId = v; }
    public Offre   getOffre()                            { return offre; }
    public List<FormateurFront>        getFormateurs()   { return formateurs; }
    public List<CoursEnLigneFront>     getCours()        { return cours; }
    public List<SessionFormationFront> getSessions()     { return sessions; }
    public List<DemandeFormation>      getDemandes()     { return demandes; }
    public int getNbFormateurs() { return formateurs == null ? 0 : formateurs.size(); }
    public int getNbCours()      { return cours      == null ? 0 : cours.size(); }
    public int getNbSessions()   { return sessions   == null ? 0 : sessions.size(); }
    public int getNbInscrits()   { return nbInscrits; }
    public int getNbEnAttente()  { return nbEnAttente; }
    public int getNbValides()    { return nbValides; }
    public int getNbQuestions()  { return nbQuestions; }
}
