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
import java.util.Collections;

/**
 * Bean dédié au dashboard du formateur (design "professeur d'université").
 */
@Named
@ViewScoped
public class FormateurDashboardBean implements Serializable {

    @Inject private FormationServiceClient formationClient;
    @Inject private UserServiceClient      userClient;
    @Inject private SessionBean            session;

    // ── Offres / profil ───────────────────────────────────────────────────────
    private List<OffreFormation> mesOffres = new ArrayList<>();
    private FormateurFront       monProfil;

    // ── Cours ─────────────────────────────────────────────────────────────────
    private List<CoursEnLigneFront> tousLesCours = new ArrayList<>();
    private List<CoursEnLigneFront> coursRecents = new ArrayList<>();
    /** offreId → cours de cette offre (value expression pour la vue) */
    private Map<Long, List<CoursEnLigneFront>> coursParOffreMap = new HashMap<>();
    /** offreId → nombre de cours (value expression, évite getNbCoursParOffre(id) en EL) */
    private Map<Long, Integer> nbCoursParOffreMap = new HashMap<>();
    /** coursId → label statut (value expression, évite getCoursStatutLabel(c) en EL) */
    private Map<Long, String> coursStatutLabelMap = new HashMap<>();
    /** coursId → couleur de fond (value expression) */
    private Map<Long, String> coursStatutBgMap    = new HashMap<>();
    /** coursId → couleur texte (value expression) */
    private Map<Long, String> coursStatutColorMap = new HashMap<>();
    private int nbCoursTotal = 0;
    private int nbQuestions  = 0;

    // ── Sessions ──────────────────────────────────────────────────────────────
    private List<SessionFormationFront> prochainsSessions = new ArrayList<>();
    private int nbSessions = 0;

    // ── Apprenants ────────────────────────────────────────────────────────────
    private int nbApprenants = 0;
    private List<ApprenantProgression> apprenantProgressions = new ArrayList<>();

    // ── Planification session ─────────────────────────────────────────────────
    private Long    sessOffreId;
    private String  sessDate    = "";
    private String  sessHeure   = "";
    private Integer sessDuree   = 90;
    private String  sessSalle   = "";
    private Integer sessPlaces  = 20;

    // ── Stats avancées ────────────────────────────────────────────────────────
    private int tauxCompletionMoyen      = 0;
    private int nbCoursPubiles           = 0;
    private int nbCorrectionsEnAttente   = 0;

    // ── Profil édition ────────────────────────────────────────────────────────
    private boolean editEnCours = false;
    private String  editNom, editPrenom, editTelephone, editSpecialite, editBio;

    // ─────────────────────────────────────────────────────────────────────────

    @PostConstruct
    public void init() { charger(); }

    public void charger() {
        String email = session.getEmail();
        String token = session.getToken();

        // 1. Offres assignées
        List<OffreFormation> toutes = formationClient.listerOffresAdmin(session.getToken());
        mesOffres = toutes.stream()
            .filter(o -> o.getFormateurs() != null && o.getFormateurs().stream()
                .anyMatch(f -> email != null && email.equalsIgnoreCase(f.getEmail())))
            .collect(Collectors.toList());

        // 2. Profil formateur
        List<FormateurFront> fmts = formationClient.listerFormateurs();
        monProfil = fmts.stream()
            .filter(f -> email != null && email.equalsIgnoreCase(f.getEmail()))
            .findFirst().orElse(null);

        // 3. Tous les cours + questions
        tousLesCours = new ArrayList<>();
        int totalQ = 0;
        for (OffreFormation o : mesOffres) {
            List<CoursEnLigneFront> oc = formationClient.coursParOffre(o.getId(), true, token);
            if (oc != null) {
                for (CoursEnLigneFront c : oc) {
                    c.setOffreId(o.getId());
                    c.setOffreTitre(o.getTitre());
                    tousLesCours.add(c);
                    if ("QUIZ".equals(c.getType())) {
                        List<QuizQuestionFront> qs = formationClient.questionsParCours(c.getId());
                        if (qs != null) totalQ += qs.size();
                    }
                }
            }
        }
        nbCoursTotal = tousLesCours.size();
        nbQuestions  = totalQ;
        coursRecents = tousLesCours.stream().limit(4).collect(Collectors.toList());

        // Index coursParOffreMap + maps statut (value expressions pour la vue)
        coursParOffreMap    = new HashMap<>();
        nbCoursParOffreMap  = new HashMap<>();
        coursStatutLabelMap = new HashMap<>();
        coursStatutBgMap    = new HashMap<>();
        coursStatutColorMap = new HashMap<>();
        for (CoursEnLigneFront c : tousLesCours) {
            coursParOffreMap.computeIfAbsent(c.getOffreId(), k -> new ArrayList<>()).add(c);
            if (c.getId() != null) {
                String label = getCoursStatutLabel(c);
                coursStatutLabelMap.put(c.getId(), label);
                coursStatutBgMap.put(c.getId(), getCoursStatutBg(c));
                coursStatutColorMap.put(c.getId(), getCoursStatutColor(c));
            }
        }
        coursParOffreMap.forEach((oid, list) -> nbCoursParOffreMap.put(oid, list.size()));

        // 4. Sessions
        String fmtNom = buildNomFormateur();
        List<SessionFormationFront> allSessions = formationClient.listerSessions(token);
        if (allSessions == null) allSessions = List.of();

        prochainsSessions = allSessions.stream()
            .filter(s -> {
                if (s.getFormateur() == null) return false;
                String f = s.getFormateur().trim().toLowerCase();
                return f.equals(email != null ? email.toLowerCase() : "")
                    || f.equals(fmtNom.toLowerCase());
            })
            .limit(5)
            .collect(Collectors.toList());

        // fallback : sessions des offres assignées
        if (prochainsSessions.isEmpty() && !mesOffres.isEmpty()) {
            Set<Long> offreIds = mesOffres.stream().map(OffreFormation::getId).collect(Collectors.toSet());
            prochainsSessions = allSessions.stream()
                .filter(s -> s.getOffre() != null && offreIds.contains(s.getOffre().getId()))
                .limit(5)
                .collect(Collectors.toList());
        }
        nbSessions = prochainsSessions.size();

        // 5. Apprenants + progression
        Map<Long, Utilisateur> usersMap = new HashMap<>();
        try {
            List<Utilisateur> all = userClient.listerUtilisateurs(token);
            if (all != null) all.forEach(u -> { if (u.getId() != null) usersMap.put(u.getId(), u); });
        } catch (Exception ignored) {}

        Map<Long, Set<Long>> userOffresMap = new LinkedHashMap<>();
        for (OffreFormation o : mesOffres) {
            try {
                List<DemandeFormation> dm = formationClient.demandesParOffre(o.getId(), token);
                if (dm != null) {
                    for (DemandeFormation d : dm) {
                        if ("ACCEPTEE".equals(d.getStatut()) && d.getUtilisateurId() != null) {
                            userOffresMap.computeIfAbsent(d.getUtilisateurId(), k -> new HashSet<>())
                                         .add(o.getId());
                        }
                    }
                }
            } catch (Exception ignored) {}
        }
        nbApprenants = userOffresMap.size();

        apprenantProgressions = new ArrayList<>();
        int shown = 0;
        for (Long uid : userOffresMap.keySet()) {
            if (shown >= 4) break;
            Utilisateur u = usersMap.get(uid);
            String nom = u != null
                ? (trim(u.getPrenom()) + " " + trim(u.getNom())).trim()
                : "Apprenant #" + uid;
            if (nom.isBlank()) nom = "Utilisateur " + uid;

            int nbDone = 0, nbAll = 0;
            for (Long oid : userOffresMap.getOrDefault(uid, Set.of())) {
                try {
                    List<ProgressionFront> pg = formationClient.progressionParOffre(oid, uid, token);
                    if (pg != null) { nbAll += pg.size(); nbDone += (int) pg.stream().filter(ProgressionFront::isComplete).count(); }
                } catch (Exception ignored) {}
            }
            int pct = nbAll > 0 ? (int) Math.round(nbDone * 100.0 / nbAll) : 0;
            apprenantProgressions.add(new ApprenantProgression(nom, buildInitiales(nom), nbDone, pct));
            shown++;
        }

        // Taux de complétion moyen + cours publiés
        tauxCompletionMoyen = apprenantProgressions.isEmpty() ? 0 :
            (int) Math.round(apprenantProgressions.stream()
                .mapToInt(ApprenantProgression::getPourcentage).average().orElse(0));
        nbCoursPubiles = (int) tousLesCours.stream()
            .filter(CoursEnLigneFront::isPublie).count();

        // Corrections en attente (TP / Devoir / Examen)
        nbCorrectionsEnAttente = 0;
        for (CoursEnLigneFront c : tousLesCours) {
            if (c.isTravauxPratiques()) {
                try {
                    nbCorrectionsEnAttente += (int) formationClient.countEnAttenteParCours(c.getId(), token);
                } catch (Exception ignored) {}
            }
        }

        // 6. Pré-remplir édition
        if (monProfil != null) {
            editNom        = monProfil.getNom();
            editPrenom     = monProfil.getPrenom();
            editTelephone  = monProfil.getTelephone();
            editSpecialite = monProfil.getSpecialite();
            editBio        = monProfil.getBio();
        }
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    // ── Sessions ──────────────────────────────────────────────────────────────

    /** Crée une session de formation depuis le formulaire du dashboard. */
    public void planifierSession() {
        if (sessOffreId == null) { msg(false, "Sélectionnez une formation."); return; }
        if (sessDate == null || sessDate.isBlank()) { msg(false, "La date est obligatoire."); return; }
        if (sessHeure == null || sessHeure.isBlank()) { msg(false, "L'heure est obligatoire."); return; }
        String dateHeure = sessDate + "T" + sessHeure + ":00";
        String fmtNom = buildNomFormateur();
        boolean ok = formationClient.creerSession(
            sessOffreId, dateHeure, sessDuree, sessSalle, fmtNom, sessPlaces, session.getToken());
        if (ok) {
            msg(true, "Session planifiée avec succès !");
            sessDate = ""; sessHeure = ""; sessDuree = 90; sessSalle = ""; sessPlaces = 20; sessOffreId = null;
            charger();
        } else {
            msg(false, "Erreur lors de la création de la session.");
        }
    }

    /** Supprime une session par ID. */
    public void supprimerSession(Long id) {
        if (id == null) return;
        boolean ok = formationClient.supprimerSession(id, session.getToken());
        if (ok) { msg(true, "Session supprimée."); charger(); }
        else    { msg(false, "Erreur lors de la suppression."); }
    }

    /** Getters / setters session form */
    public Long    getSessOffreId()          { return sessOffreId; }
    public void    setSessOffreId(Long v)    { sessOffreId = v; }
    public String  getSessDate()             { return sessDate; }
    public void    setSessDate(String v)     { sessDate = v; }
    public String  getSessHeure()            { return sessHeure; }
    public void    setSessHeure(String v)    { sessHeure = v; }
    public Integer getSessDuree()            { return sessDuree; }
    public void    setSessDuree(Integer v)   { sessDuree = v; }
    public String  getSessSalle()            { return sessSalle; }
    public void    setSessSalle(String v)    { sessSalle = v; }
    public Integer getSessPlaces()           { return sessPlaces; }
    public void    setSessPlaces(Integer v)  { sessPlaces = v; }

    public void toggleEditProfil() { editEnCours = !editEnCours; }

    public void sauvegarderProfil() {
        if (monProfil == null) { msg(false, "Profil introuvable."); return; }
        boolean ok = formationClient.modifierFormateur(monProfil.getId(),
            editNom, editPrenom, monProfil.getEmail(),
            editTelephone, editSpecialite, editBio, session.getToken());
        if (ok) { msg(true, "Profil mis à jour."); editEnCours = false; charger(); }
        else    { msg(false, "Échec de la mise à jour."); }
    }

    // ── Helpers affichage cours ────────────────────────────────────────────────

    public String getCoursAccentColor(CoursEnLigneFront c) {
        if (c == null || c.getType() == null) return "#94A3B8";
        return switch (c.getType()) {
            case "VIDEO"    -> "#EF4444";
            case "DOCUMENT" -> "#3B82F6";
            case "QUIZ"     -> "#8B5CF6";
            case "LIEN"     -> "#10B981";
            case "LIVE"     -> "#F59E0B";
            default -> "#94A3B8";
        };
    }

    public String getCoursBadgeBg(CoursEnLigneFront c) {
        if (c == null || c.getType() == null) return "#F1F5F9";
        return switch (c.getType()) {
            case "VIDEO"    -> "#FEE2E2";
            case "DOCUMENT" -> "#DBEAFE";
            case "QUIZ"     -> "#EDE9FE";
            case "LIEN"     -> "#D1FAE5";
            case "LIVE"     -> "#FEF3C7";
            default -> "#F1F5F9";
        };
    }

    public String getCoursStatutLabel(CoursEnLigneFront c) {
        if (c == null) return "";
        if ("LIVE".equals(c.getType())) return "À venir";
        return c.isPublie() ? "Publié" : "Brouillon";
    }

    public String getCoursStatutBg(CoursEnLigneFront c) {
        return switch (getCoursStatutLabel(c)) {
            case "Publié"  -> "#D1FAE5";
            case "À venir" -> "#FEF3C7";
            default        -> "#F1F5F9";
        };
    }

    public String getCoursStatutColor(CoursEnLigneFront c) {
        return switch (getCoursStatutLabel(c)) {
            case "Publié"  -> "#065F46";
            case "À venir" -> "#92400E";
            default        -> "#64748B";
        };
    }

    // ── Internes ─────────────────────────────────────────────────────────────

    private String buildNomFormateur() {
        if (monProfil == null) return session.getEmail() != null ? session.getEmail() : "";
        return (trim(monProfil.getPrenom()) + " " + trim(monProfil.getNom())).trim();
    }

    private static String trim(String s)  { return s != null ? s.trim() : ""; }

    private static String buildInitiales(String nom) {
        if (nom == null || nom.isBlank()) return "?";
        String[] parts = nom.trim().split("\\s+");
        if (parts.length >= 2 && parts[0].length() > 0 && parts[1].length() > 0)
            return (parts[0].substring(0,1) + parts[1].substring(0,1)).toUpperCase();
        return nom.substring(0, Math.min(2, nom.length())).toUpperCase();
    }

    private void msg(boolean ok, String text) {
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(ok ? FacesMessage.SEVERITY_INFO : FacesMessage.SEVERITY_ERROR,
                ok ? "Succès" : "Erreur", text));
    }

    // ── Compteurs ─────────────────────────────────────────────────────────────
    public int getNbOffres()     { return mesOffres == null ? 0 : mesOffres.size(); }
    public int getNbCoursTotal() { return nbCoursTotal; }
    public int getNbQuestions()  { return nbQuestions; }
    public int getNbApprenants() { return nbApprenants; }
    public int getNbSessions()   { return nbSessions; }

    public int    getTauxCompletionMoyen()      { return tauxCompletionMoyen; }
    public String getTauxCompletionLabel()      { return tauxCompletionMoyen + "%"; }
    public int    getNbCoursPubiles()           { return nbCoursPubiles; }
    public int    getNbCorrectionsEnAttente()   { return nbCorrectionsEnAttente; }

    public List<CoursEnLigneFront> getDocuments() {
        if (tousLesCours == null) return Collections.emptyList();
        return tousLesCours.stream()
            .filter(c -> "DOCUMENT".equals(c.getType()))
            .collect(Collectors.toList());
    }
    public int getNbDocuments() { return getDocuments().size(); }

    /** Cours filtrés par offre (utilisé dans la page "Mes formations"). */
    public List<CoursEnLigneFront> getCoursParOffre(Long offreId) {
        if (offreId == null || tousLesCours == null) return Collections.emptyList();
        return tousLesCours.stream()
            .filter(c -> offreId.equals(c.getOffreId()))
            .collect(Collectors.toList());
    }

    public int getNbCoursParOffre(Long offreId) { return getCoursParOffre(offreId).size(); }

    public List<NotificationFormateur> getNotificationsFormateur() {
        List<NotificationFormateur> list = new ArrayList<>();
        if (nbSessions > 0)
            list.add(new NotificationFormateur("ti-calendar-event", "#3B82F6", "#EFF6FF",
                nbSessions + " session(s) planifiée(s)",
                "Consultez votre planning pour les prochaines dates."));
        if (nbApprenants > 0)
            list.add(new NotificationFormateur("ti-users", "#10B981", "#D1FAE5",
                nbApprenants + " apprenant(s) inscrit(s)",
                "Des apprenants suivent actuellement vos formations."));
        if (nbCoursPubiles > 0)
            list.add(new NotificationFormateur("ti-book", "#8B5CF6", "#EDE9FE",
                nbCoursPubiles + " cours publié(s) en ligne",
                "Vos cours sont visibles par les apprenants inscrits."));
        if (tauxCompletionMoyen > 0)
            list.add(new NotificationFormateur("ti-chart-line", "#F59E0B", "#FEF3C7",
                "Complétion moyenne : " + tauxCompletionMoyen + "%",
                "Progression globale de vos apprenants."));
        return list;
    }
    public int getNbNotificationsFormateur() { return getNotificationsFormateur().size(); }

    // ── Getters / Setters ─────────────────────────────────────────────────────
    public List<OffreFormation>        getMesOffres()             { return mesOffres; }
    public FormateurFront              getMonProfil()             { return monProfil; }
    public List<CoursEnLigneFront>     getTousLesCours()          { return tousLesCours; }
    public List<CoursEnLigneFront>     getCoursRecents()          { return coursRecents; }
    public Map<Long, List<CoursEnLigneFront>> getCoursParOffreMap()  { return coursParOffreMap; }
    public Map<Long, Integer>               getNbCoursParOffreMap() { return nbCoursParOffreMap; }
    public Map<Long, String>                getCoursStatutLabelMap(){ return coursStatutLabelMap; }
    public Map<Long, String>                getCoursStatutBgMap()   { return coursStatutBgMap; }
    public Map<Long, String>                getCoursStatutColorMap(){ return coursStatutColorMap; }
    public List<SessionFormationFront> getProchainsSessions()     { return prochainsSessions; }
    public List<ApprenantProgression>  getApprenantProgressions() { return apprenantProgressions; }
    public boolean isEditEnCours()      { return editEnCours; }
    public String  getEditNom()         { return editNom; }
    public void    setEditNom(String v) { editNom = v; }
    public String  getEditPrenom()               { return editPrenom; }
    public void    setEditPrenom(String v)       { editPrenom = v; }
    public String  getEditTelephone()            { return editTelephone; }
    public void    setEditTelephone(String v)    { editTelephone = v; }
    public String  getEditSpecialite()           { return editSpecialite; }
    public void    setEditSpecialite(String v)   { editSpecialite = v; }
    public String  getEditBio()                  { return editBio; }
    public void    setEditBio(String v)          { editBio = v; }

    // ─────────────────────────────────────────────────────────────────────────
    // Classes internes
    // ─────────────────────────────────────────────────────────────────────────
    public static class NotificationFormateur implements Serializable {
        private final String icone;
        private final String couleur;
        private final String bg;
        private final String titre;
        private final String detail;

        public NotificationFormateur(String icone, String couleur, String bg, String titre, String detail) {
            this.icone = icone; this.couleur = couleur; this.bg = bg;
            this.titre = titre; this.detail = detail;
        }
        public String getIcone()   { return icone; }
        public String getCouleur() { return couleur; }
        public String getBg()      { return bg; }
        public String getTitre()   { return titre; }
        public String getDetail()  { return detail; }
    }

    public static class ApprenantProgression implements Serializable {
        private final String nom;
        private final String initiales;
        private final int    nbCoursCompletes;
        private final int    pourcentage;

        public ApprenantProgression(String nom, String initiales, int nbCoursCompletes, int pourcentage) {
            this.nom = nom; this.initiales = initiales;
            this.nbCoursCompletes = nbCoursCompletes; this.pourcentage = pourcentage;
        }

        public String getNom()               { return nom; }
        public String getInitiales()         { return initiales; }
        public int    getNbCoursCompletes()   { return nbCoursCompletes; }
        public int    getPourcentage()        { return pourcentage; }
        public String getPourcentageLabel()   { return pourcentage + "%"; }
    }
}
