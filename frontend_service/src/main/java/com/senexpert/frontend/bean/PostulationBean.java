package com.senexpert.frontend.bean;

import com.senexpert.frontend.client.FormationServiceClient;
import com.senexpert.frontend.model.OffreFormation;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.faces.event.ActionEvent;
import org.primefaces.event.FileUploadEvent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Bean gérant la page dédiée de postulation à une offre de formation.
 * L'offreId est lu directement depuis les paramètres de requête dans @PostConstruct
 * (évite le problème JSF1103 lié à f:metadata dans un ui:define).
 * Les bytes des fichiers sont lus immédiatement dans handleUpload pour éviter
 * le NoSuchFileException : Undertow supprime le fichier temp après la requête AJAX.
 */
@Named
@ViewScoped
public class PostulationBean implements Serializable {

    @Inject private FormationServiceClient formationClient;
    @Inject private SessionBean session;

    // ── View param (aussi settable via f:viewParam pour compatibilité) ────
    private Long offreId;

    // ── Données ───────────────────────────────────────────
    private OffreFormation offre;

    // ── Upload générique (fallback quand pas de docs requis) ──────────────────
    private List<byte[]>  fichiersBytes = new ArrayList<>();
    private List<String>  nomsFichiers  = new ArrayList<>();

    // ── Upload par slot (un champ par document requis) ────────────────────────
    // Clé = nom du document requis (ex: "CV", "Lettre de motivation")
    private Map<String, byte[]>  slotBytes = new LinkedHashMap<>();
    private Map<String, String>  slotNoms  = new LinkedHashMap<>();

    // ── Fichier liste des participants (institution AGENT uniquement) ──────────
    private byte[] fichierParticipantsBytes;
    private String fichierParticipantsNom;
    private String fichierParticipantsType;

    @PostConstruct
    public void init() {
        // Lire offreId depuis les paramètres de requête si non déjà injecté
        if (offreId == null) {
            String param = FacesContext.getCurrentInstance()
                    .getExternalContext().getRequestParameterMap().get("offreId");
            if (param != null && !param.isBlank()) {
                try { offreId = Long.parseLong(param); } catch (NumberFormatException ignored) {}
            }
        }
        if (offreId != null) {
            offre = formationClient.trouverOffreParId(offreId);
        }
    }

    // ── Upload handler ────────────────────────────────────

    /** Lire les bytes immédiatement — le fichier temp Undertow sera supprimé après la requête. */
    public void handleUpload(FileUploadEvent event) {
        var f = event.getFile();
        if (f != null && f.getSize() > 0) {
            try {
                fichiersBytes.add(f.getInputStream().readAllBytes());
                nomsFichiers.add(f.getFileName() != null ? f.getFileName() : "document");
            } catch (Exception e) {
                e.printStackTrace();
                msg(false, "Erreur lecture fichier : " + f.getFileName());
            }
        }
    }

    /** Upload pour un slot de document spécifique — lit docNom via f:attribute. */
    public void handleUploadSlot(FileUploadEvent event) {
        String docNom = (String) event.getComponent().getAttributes().get("docNom");
        var f = event.getFile();
        if (f != null && f.getSize() > 0 && docNom != null) {
            try {
                slotBytes.put(docNom, f.getInputStream().readAllBytes());
                slotNoms.put(docNom, f.getFileName() != null ? f.getFileName() : docNom);
            } catch (Exception e) {
                e.printStackTrace();
                msg(false, "Erreur lecture fichier : " + f.getFileName());
            }
        }
    }

    /** Vide le slot d'un document (retirer le fichier choisi). */
    public void supprimerSlot(String docNom) {
        slotBytes.remove(docNom);
        slotNoms.remove(docNom);
    }

    /** Variante actionListener — lit docNom via f:attribute sur le commandLink. */
    public void supprimerSlotAction(ActionEvent event) {
        String docNom = (String) event.getComponent().getAttributes().get("docNom");
        if (docNom != null) {
            slotBytes.remove(docNom);
            slotNoms.remove(docNom);
        }
    }

    /** Indique si un fichier a déjà été uploadé pour ce slot. */
    public boolean slotRempli(String docNom) {
        return slotBytes.containsKey(docNom);
    }

    /** Nom du fichier uploadé pour ce slot. */
    public String slotFichierNom(String docNom) {
        return slotNoms.getOrDefault(docNom, "");
    }

    /** Reçoit le fichier liste des participants (p:fileUpload auto="true") */
    public void handleParticipantsUpload(FileUploadEvent event) {
        var f = event.getFile();
        if (f != null && f.getSize() > 0) {
            try {
                fichierParticipantsBytes = f.getInputStream().readAllBytes();
                fichierParticipantsNom  = f.getFileName() != null ? f.getFileName() : "participants";
                fichierParticipantsType = f.getContentType();
            } catch (Exception e) {
                msg(false, "Erreur lecture fichier participants : " + f.getFileName());
            }
        }
    }

    public void supprimerFichier(int index) {
        if (index >= 0 && index < fichiersBytes.size()) {
            fichiersBytes.remove(index);
            nomsFichiers.remove(index);
        }
    }

    // ── Soumission ────────────────────────────────────────

    public String soumettre() {
        if (!session.isConnecte()) {
            return "/login?faces-redirect=true";
        }
        if (!session.hasPermission("5014")) {
            msg(false, "Vous n'avez pas la permission de candidater à une offre.");
            return null;
        }
        if (offre == null) {
            msg(false, "Offre introuvable.");
            return null;
        }

        // Merger les fichiers des slots + les fichiers génériques
        List<byte[]> allBytes = new ArrayList<>(slotBytes.values());
        List<String> allNoms  = new ArrayList<>(slotNoms.values());
        allBytes.addAll(fichiersBytes);
        allNoms.addAll(nomsFichiers);

        // Upload des fichiers (bytes déjà en mémoire) → chemins stockés côté serveur
        List<String> chemins = new ArrayList<>();
        if (!allBytes.isEmpty()) {
            chemins = formationClient.uploadDocumentsDemande(
                    session.getUtilisateurId(), allBytes, allNoms, session.getToken());
        }

        // Création de la demande — on récupère l'ID pour pouvoir uploader le fichier participants
        Long demandeId = formationClient.soumettreDemandeReturnId(
                session.getUtilisateurId(), offreId, chemins, session.getToken());

        if (demandeId != null) {
            // Upload fichier participants si fourni (AGENT / institution)
            if (fichierParticipantsBytes != null && fichierParticipantsBytes.length > 0) {
                formationClient.uploadFichierParticipantsDemande(
                        demandeId, fichierParticipantsBytes,
                        fichierParticipantsNom, fichierParticipantsType,
                        session.getToken());
            }
            String role = session.getRole();
            if ("PARTICULIER".equals(role)) {
                return "/dashboard-particulier?postule=1&faces-redirect=true";
            } else if ("AGENT".equals(role)) {
                return "/dashboard-agent-institution?postule=1&faces-redirect=true";
            }
            return "/offres?postule=1&faces-redirect=true";
        }

        msg(false, "Impossible de soumettre la candidature (erreur réseau ou doublon).");
        return null;
    }

    // ── Helpers ───────────────────────────────────────────

    private void msg(boolean ok, String text) {
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(
                ok ? FacesMessage.SEVERITY_INFO : FacesMessage.SEVERITY_ERROR,
                ok ? "Succès" : "Erreur", text));
    }

    public int getNbFichiers()        { return (slotBytes == null ? 0 : slotBytes.size()) + (fichiersBytes == null ? 0 : fichiersBytes.size()); }
    public Map<String, String> getSlotNoms() { return slotNoms; }
    public boolean isOffreChargee()   { return offre != null; }
    public int getDocumentsRequisCount() { return offre != null && offre.getDocumentsRequis() != null ? offre.getDocumentsRequis().size() : 0; }
    public boolean isHasFichierParticipants() { return fichierParticipantsBytes != null && fichierParticipantsBytes.length > 0; }
    public boolean hasFichierParticipants()   { return isHasFichierParticipants(); }
    public String getFichierParticipantsNom() { return fichierParticipantsNom; }
    public boolean isAgent() { return "AGENT".equals(session.getRole()); }

    // ── Getters / Setters ─────────────────────────────────

    public Long getOffreId()               { return offreId; }
    public void setOffreId(Long offreId)   { this.offreId = offreId; }
    public OffreFormation getOffre()       { return offre; }
    public List<String> getNomsFichiers()  { return nomsFichiers; }

    // ── EL value-expression safe wrappers ────────────────────────────────────
    public class SlotsRempliMap extends java.util.HashMap<String, Boolean> {
        @Override public Boolean get(Object key) { return slotRempli((String) key); }
    }
    public class SlotNomMap extends java.util.HashMap<String, String> {
        @Override public String get(Object key) { return slotFichierNom((String) key); }
    }
    private final SlotsRempliMap slotsRempliMap = new SlotsRempliMap();
    private final SlotNomMap     slotNomMap     = new SlotNomMap();
    public java.util.Map<String, Boolean> getSlotsRempliMap() { return slotsRempliMap; }
    public java.util.Map<String, String>  getSlotNomMap()     { return slotNomMap; }

    /** Map retournant la classe CSS complète du slot — évite la coercition booléenne dans le ternaire EL. */
    public java.util.Map<String, String> getSlotsClassMap() {
        return new java.util.AbstractMap<String, String>() {
            @Override
            public String get(Object key) {
                return slotRempli((String) key) ? "pst-doc-slot slot-ok" : "pst-doc-slot";
            }
            @Override public java.util.Set<java.util.Map.Entry<String, String>> entrySet() { return java.util.Collections.emptySet(); }
            @Override public boolean isEmpty()               { return false; }
            @Override public boolean containsKey(Object key) { return key != null; }
        };
    }

}
