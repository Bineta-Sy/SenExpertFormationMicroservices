package com.senexpert.frontend.bean;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.senexpert.frontend.client.FormationServiceClient;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Bean pour la page admin-analytics.xhtml (statistiques avancées).
 */
@Named
@ViewScoped
public class AnalyticsBean implements Serializable {

    @Inject private FormationServiceClient formationClient;
    @Inject private SessionBean session;

    // Alertes seuil
    private List<Map<String, Object>> alertes;
    private int seuilAlertesPourcent = 80;

    // Stats avancées
    private List<Map<String, Object>> acceptationParDomaine;
    private List<Map<String, Object>> domainesPopulaires;
    private Map<String, Object> demandesParMois;

    // Snapshot temps réel
    private Map<String, Object> snapshot;

    // JSON pour Chart.js
    private String acceptationDomaineLabelsJson;
    private String acceptationDomaineTauxJson;
    private String domainesPopulairesLabelsJson;
    private String domainesPopulairesDataJson;
    private String demandesParMoisLabelsJson;
    private String demandesParMoisDataJson;

    @PostConstruct
    public void init() {
        chargerTout();
    }

    public void chargerTout() {
        String t = session.getToken();
        chargerAlertes();
        chargerStatsAvancees(t);
        rafraichirSnapshot();
    }

    public void chargerAlertes() {
        alertes = formationClient.alertesSeuils(seuilAlertesPourcent, session.getToken());
    }

    private void chargerStatsAvancees(String token) {
        // Acceptation par domaine
        acceptationParDomaine = formationClient.acceptationParDomaine(token);
        buildAcceptationDomaineJson();

        // Domaines populaires
        domainesPopulaires = formationClient.domainesPopulaires(token);
        buildDomainesPopulairesJson();

        // Évolution demandes 12 mois
        demandesParMois = formationClient.demandesParMois(token);
        buildDemandesParMoisJson();
    }

    /** Appelé par p:poll pour rafraîchir les compteurs sans rechargement. */
    public void rafraichirSnapshot() {
        snapshot = formationClient.snapshot(session.getToken());
    }

    // ─── Construction JSON Chart.js ──────────────────────────────────────────

    private void buildAcceptationDomaineJson() {
        if (acceptationParDomaine == null || acceptationParDomaine.isEmpty()) {
            acceptationDomaineLabelsJson = "[]";
            acceptationDomaineTauxJson = "[]";
            return;
        }
        StringBuilder labels = new StringBuilder("[");
        StringBuilder taux = new StringBuilder("[");
        for (int i = 0; i < acceptationParDomaine.size(); i++) {
            Map<String, Object> row = acceptationParDomaine.get(i);
            if (i > 0) { labels.append(","); taux.append(","); }
            labels.append("\"").append(row.get("domaine")).append("\"");
            taux.append(row.get("tauxAcceptation"));
        }
        labels.append("]"); taux.append("]");
        acceptationDomaineLabelsJson = labels.toString();
        acceptationDomaineTauxJson = taux.toString();
    }

    private void buildDomainesPopulairesJson() {
        if (domainesPopulaires == null || domainesPopulaires.isEmpty()) {
            domainesPopulairesLabelsJson = "[]";
            domainesPopulairesDataJson = "[]";
            return;
        }
        // Top 8 seulement
        int max = Math.min(8, domainesPopulaires.size());
        StringBuilder labels = new StringBuilder("[");
        StringBuilder data = new StringBuilder("[");
        for (int i = 0; i < max; i++) {
            Map<String, Object> row = domainesPopulaires.get(i);
            if (i > 0) { labels.append(","); data.append(","); }
            labels.append("\"").append(row.get("domaine")).append("\"");
            data.append(row.get("nbDemandes"));
        }
        labels.append("]"); data.append("]");
        domainesPopulairesLabelsJson = labels.toString();
        domainesPopulairesDataJson = data.toString();
    }

    private void buildDemandesParMoisJson() {
        if (demandesParMois == null || demandesParMois.isEmpty()) {
            demandesParMoisLabelsJson = "[]";
            demandesParMoisDataJson = "[]";
            return;
        }
        try {
            ObjectMapper om = new ObjectMapper();
            demandesParMoisLabelsJson = om.writeValueAsString(demandesParMois.get("labels"));
            demandesParMoisDataJson   = om.writeValueAsString(demandesParMois.get("data"));
        } catch (Exception e) {
            demandesParMoisLabelsJson = "[]";
            demandesParMoisDataJson = "[]";
        }
    }

    // ─── Getters ──────────────────────────────────────────────────────────────

    public List<Map<String, Object>> getAlertes() { return alertes; }
    public int getSeuilAlertesPourcent() { return seuilAlertesPourcent; }
    public void setSeuilAlertesPourcent(int v) { this.seuilAlertesPourcent = v; }

    public List<Map<String, Object>> getAcceptationParDomaine() { return acceptationParDomaine; }
    public List<Map<String, Object>> getDomainesPopulaires() { return domainesPopulaires; }

    public String getAcceptationDomaineLabelsJson() { return acceptationDomaineLabelsJson; }
    public String getAcceptationDomaineTauxJson() { return acceptationDomaineTauxJson; }
    public String getDomainesPopulairesLabelsJson() { return domainesPopulairesLabelsJson; }
    public String getDomainesPopulairesDataJson() { return domainesPopulairesDataJson; }
    public String getDemandesParMoisLabelsJson() { return demandesParMoisLabelsJson; }
    public String getDemandesParMoisDataJson() { return demandesParMoisDataJson; }

    // Snapshot (utilisé par le poll temps réel du dashboard)
    public Map<String, Object> getSnapshot() { return snapshot; }

    public long getSnapshotVal(String key) {
        if (snapshot == null || !snapshot.containsKey(key)) return 0;
        Object v = snapshot.get(key);
        return v instanceof Number ? ((Number) v).longValue() : 0;
    }

    public boolean isAlerteActive() {
        return alertes != null && !alertes.isEmpty();
    }
}
