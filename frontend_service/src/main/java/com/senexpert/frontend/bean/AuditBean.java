package com.senexpert.frontend.bean;

import com.senexpert.frontend.client.UserServiceClient;
import com.senexpert.frontend.model.AuditEntry;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Bean d'audit : persiste en base via user_service ET garde un buffer
 * en memoire pour l'affichage immediat (fallback si service indisponible).
 */
@Named
@ApplicationScoped
public class AuditBean implements Serializable {

    @Inject private UserServiceClient userClient;
    @Inject private SessionBean sessionBean;

    // Buffer memoire (dernieres 500 entrees pour affichage rapide)
    private static final List<AuditEntry> entries = new ArrayList<>();
    private static final int MAX_ENTRIES = 500;

    // Ref statique vers le client pour l'appel depuis methodes statiques
    private static UserServiceClient staticUserClient;
    private static SessionBean staticSession;

    // Appele par CDI apres injection
    @jakarta.annotation.PostConstruct
    public void init() {
        staticUserClient = userClient;
        staticSession = sessionBean;
    }

    /**
     * Methode statique appelee depuis tous les beans.
     * 1. Ajoute au buffer memoire (immediat, visible sans rechargement)
     * 2. Persiste en base via user_service (async, best-effort)
     */
    public static void log(String action, String entite, String detail, String adminEmail, String niveau) {
        // Buffer memoire
        AuditEntry entry = new AuditEntry(action, entite, detail, adminEmail, niveau);
        synchronized (entries) {
            entries.add(0, entry);
            if (entries.size() > MAX_ENTRIES) entries.remove(entries.size() - 1);
        }
        // Persistance en base (best-effort, dans un thread separe)
        if (staticUserClient != null) {
            String token = staticSession != null ? staticSession.getToken() : null;
            final String tok = token;
            new Thread(() -> {
                try {
                    staticUserClient.logAudit(action, entite, detail, adminEmail, niveau, tok);
                } catch (Exception ignored) {}
            }).start();
        }
    }

    /**
     * Retourne les entrees : depuis la base si disponible, sinon depuis le buffer.
     */
    public List<AuditEntry> getEntries() {
        // Essayer de charger depuis la base
        if (sessionBean != null && sessionBean.getToken() != null) {
            try {
                List<Map<String, Object>> dbLogs = userClient.listerAudit(200, sessionBean.getToken());
                if (dbLogs != null && !dbLogs.isEmpty()) {
                    List<AuditEntry> result = new ArrayList<>();
                    for (Map<String, Object> row : dbLogs) {
                        AuditEntry e = new AuditEntry();
                        e.setAction(str(row.get("action")));
                        e.setEntite(str(row.get("entite")));
                        e.setDetail(str(row.get("detail")));
                        e.setAdminEmail(str(row.get("adminEmail")));
                        e.setNiveau(str(row.get("niveau")));
                        // dateAction est au format ISO (ex: 2025-09-01T10:30:00)
                        String da = str(row.get("dateAction"));
                        e.setDate(da != null ? da.replace("T", " ").substring(0, Math.min(da.length(), 19)) : "");
                        result.add(e);
                    }
                    return result;
                }
            } catch (Exception ignored) {}
        }
        // Fallback : buffer memoire
        return new ArrayList<>(entries);
    }

    /** Nombre d'entrees en memoire (indicateur rapide) */
    public int getCount() { return entries.size(); }

    /** Vide le buffer memoire (ne supprime pas la BD) */
    public void vider() {
        synchronized (entries) { entries.clear(); }
    }

    private String str(Object o) { return o != null ? o.toString() : null; }
}
