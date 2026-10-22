package com.senexpert.frontend.bean;

import com.senexpert.frontend.client.FormationServiceClient;
import com.senexpert.frontend.client.UserServiceClient;
import com.senexpert.frontend.model.*;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Bean dédié à la page formateur-apprenants.xhtml.
 * Charge TOUS les apprenants des offres du formateur avec progression complète.
 */
@Named
@ViewScoped
public class FormateurApprenantsBean implements Serializable {

    @Inject private FormationServiceClient formationClient;
    @Inject private UserServiceClient      userClient;
    @Inject private SessionBean            session;

    private List<ApprenantDetail> apprenants = new ArrayList<>();
    private List<OffreFormation>  mesOffres  = new ArrayList<>();
    private String                recherche  = "";

    // ── Stats ──────────────────────────────────────────────────────────────────
    private int nbTotal         = 0;
    private int nbCompletions   = 0;  // nb apprenants ayant 100%
    private int moyennePct      = 0;

    // ─────────────────────────────────────────────────────────────────────────

    @PostConstruct
    public void init() {
        // Chargement différé via f:viewAction
    }

    public void charger() {
        String email = session.getEmail();
        String token = session.getToken();

        // 1. Offres du formateur
        mesOffres = formationClient.listerOffresAdmin(session.getToken()).stream()
            .filter(o -> o.getFormateurs() != null && o.getFormateurs().stream()
                .anyMatch(f -> email != null && email.equalsIgnoreCase(f.getEmail())))
            .collect(Collectors.toList());

        // 2. Tous les utilisateurs
        Map<Long, Utilisateur> usersMap = new HashMap<>();
        try {
            List<Utilisateur> all = userClient.listerUtilisateurs(token);
            if (all != null) all.forEach(u -> { if (u.getId() != null) usersMap.put(u.getId(), u); });
        } catch (Exception ignored) {}

        // 3. Apprenants par offre → map userId → Set<offreId>
        Map<Long, Set<Long>> userOffresMap = new LinkedHashMap<>();
        for (OffreFormation o : mesOffres) {
            try {
                List<DemandeFormation> dm = formationClient.demandesParOffre(o.getId(), token);
                if (dm != null) {
                    for (DemandeFormation d : dm) {
                        if ("ACCEPTEE".equals(d.getStatut()) && d.getUtilisateurId() != null) {
                            userOffresMap.computeIfAbsent(d.getUtilisateurId(), k -> new LinkedHashSet<>())
                                         .add(o.getId());
                        }
                    }
                }
            } catch (Exception ignored) {}
        }

        // 4. Construire ApprenantDetail pour chaque apprenant
        Map<Long, OffreFormation> offresById = mesOffres.stream()
            .collect(Collectors.toMap(OffreFormation::getId, o -> o, (a, b) -> a));

        apprenants = new ArrayList<>();
        for (Long uid : userOffresMap.keySet()) {
            Utilisateur u = usersMap.get(uid);
            String nom   = u != null ? (trim(u.getPrenom()) + " " + trim(u.getNom())).trim() : "";
            if (nom.isBlank()) nom = "Apprenant #" + uid;
            String emailU = u != null ? u.getEmail() : "";

            // Progression par offre
            List<OffreProgression> offresProgress = new ArrayList<>();
            int totalDone = 0, totalAll = 0;

            for (Long oid : userOffresMap.getOrDefault(uid, Set.of())) {
                OffreFormation offre = offresById.get(oid);
                if (offre == null) continue;
                int nbAll = 0, nbDone = 0;
                try {
                    List<ProgressionFront> pg = formationClient.progressionParOffre(oid, uid, token);
                    if (pg != null) {
                        nbAll  = pg.size();
                        nbDone = (int) pg.stream().filter(ProgressionFront::isComplete).count();
                    }
                } catch (Exception ignored) {}
                int pct = nbAll > 0 ? (int) Math.round(nbDone * 100.0 / nbAll) : 0;
                offresProgress.add(new OffreProgression(offre, nbDone, nbAll, pct));
                totalDone += nbDone;
                totalAll  += nbAll;
            }

            int pctGlobal = totalAll > 0 ? (int) Math.round(totalDone * 100.0 / totalAll) : 0;
            apprenants.add(new ApprenantDetail(uid, nom, emailU, buildInitiales(nom), offresProgress, pctGlobal));
        }

        // 5. Stats globales
        nbTotal       = apprenants.size();
        nbCompletions = (int) apprenants.stream().filter(a -> a.getPourcentage() >= 100).count();
        moyennePct    = apprenants.isEmpty() ? 0 :
            (int) Math.round(apprenants.stream().mapToInt(ApprenantDetail::getPourcentage).average().orElse(0));
    }

    // ── Getters ────────────────────────────────────────────────────────────────

    /** Liste filtrée selon la recherche. */
    public List<ApprenantDetail> getApprenants() {
        if (recherche == null || recherche.isBlank()) return apprenants;
        String q = recherche.toLowerCase().trim();
        return apprenants.stream()
            .filter(a -> a.getNom().toLowerCase().contains(q)
                      || a.getEmail().toLowerCase().contains(q))
            .collect(Collectors.toList());
    }

    public List<OffreFormation> getMesOffres()   { return mesOffres; }
    public int getNbTotal()                      { return nbTotal; }
    public int getNbCompletions()                { return nbCompletions; }
    public int getMoyennePct()                   { return moyennePct; }
    public String getRecherche()                 { return recherche; }
    public void   setRecherche(String v)         { recherche = v; }

    public void chercher() { /* filtrée côté getter */ }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private static String trim(String s) { return s != null ? s.trim() : ""; }

    private static String buildInitiales(String nom) {
        if (nom == null || nom.isBlank()) return "?";
        String[] parts = nom.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        return (parts[0].charAt(0) + "" + parts[parts.length - 1].charAt(0)).toUpperCase();
    }

    // ── DTOs internes ──────────────────────────────────────────────────────────

    public static class ApprenantDetail implements Serializable {
        private final Long   id;
        private final String nom;
        private final String email;
        private final String initiales;
        private final List<OffreProgression> offresProgress;
        private final int    pourcentage;

        public ApprenantDetail(Long id, String nom, String email, String initiales,
                               List<OffreProgression> offresProgress, int pourcentage) {
            this.id             = id;
            this.nom            = nom;
            this.email          = email;
            this.initiales      = initiales;
            this.offresProgress = offresProgress;
            this.pourcentage    = pourcentage;
        }

        public Long   getId()                              { return id; }
        public String getNom()                             { return nom; }
        public String getEmail()                           { return email; }
        public String getInitiales()                       { return initiales; }
        public List<OffreProgression> getOffresProgress() { return offresProgress; }
        public int    getPourcentage()                     { return pourcentage; }
        public String getPourcentageLabel()                { return pourcentage + "%"; }
        public int    getNbOffres()                        { return offresProgress.size(); }

        /** Barre de couleur selon avancement global. */
        public String getBarColor() {
            if (pourcentage >= 100) return "#10B981";
            if (pourcentage >= 50)  return "#3B82F6";
            if (pourcentage > 0)    return "#F59E0B";
            return "#E2E8F0";
        }
    }

    public static class OffreProgression implements Serializable {
        private final OffreFormation offre;
        private final int nbDone;
        private final int nbAll;
        private final int pourcentage;

        public OffreProgression(OffreFormation offre, int nbDone, int nbAll, int pourcentage) {
            this.offre       = offre;
            this.nbDone      = nbDone;
            this.nbAll       = nbAll;
            this.pourcentage = pourcentage;
        }

        public OffreFormation getOffre()       { return offre; }
        public int    getNbDone()              { return nbDone; }
        public int    getNbAll()               { return nbAll; }
        public int    getPourcentage()         { return pourcentage; }
        public String getPourcentageLabel()    { return pourcentage + "%"; }
        public boolean isTermine()             { return pourcentage >= 100; }
    }
}
