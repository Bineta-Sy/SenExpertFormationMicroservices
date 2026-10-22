package com.senexpert.frontend.bean;

import com.senexpert.frontend.client.FormationServiceClient;
import com.senexpert.frontend.client.UserServiceClient;
import com.senexpert.frontend.model.*;
import com.senexpert.frontend.model.Institution;
import com.senexpert.frontend.model.AuditEntry;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.List;

/**
 * Bean central pour toutes les pages admin de la nouvelle version.
 */
@Named
@ViewScoped
public class AdminDashboardBean implements Serializable {

    @Inject private UserServiceClient userClient;
    @Inject private FormationServiceClient formationClient;
    @Inject private SessionBean session;

    // Données
    private List<Utilisateur> personnes;
    private List<Offre> offres;
    private List<Offre> formationsSoumises = new java.util.ArrayList<>();
    private List<Domaine> domaines;
    private List<DemandeFormation> demandes;
    private List<CandidatureFront> candidatures;
    private List<ProfilFront> profils;
    private List<PermissionFront> permissions;
    private List<BrancheFront> branches;
    private List<Institution> institutions;
    private List<AuditEntry> auditEntries;
    private List<DemandeLibreFront> demandesLibres;

    // filteredValue pour p:dataTable (recherche globale PrimeFaces)
    private List<Utilisateur>       filteredPersonnes;
    private List<DemandeFormation>  filteredDemandes;
    private List<CandidatureFront>  filteredCandidatures;
    private List<DemandeLibreFront> filteredDemandesLibres;

    // Filtres recherche personnes
    private String recherchePersonne;
    private String filtreStatutPersonne; // null = tous

    // Formulaires
    private String offreTitre, offreDescription, offreNiveau, offreDuree, offreType = "EN_LIGNE";
    private Integer offrePlaces, offreCapacite;
    private Double offreCout;
    private String offreDateDebut, offreDateFin;
    private Long offreDomaineId;
    private Long offreEnCoursId;
    private boolean offreModeEdition = false;
    /** Documents requis pour postuler à l'offre */
    private java.util.List<String> offreDocumentsRequis = new java.util.ArrayList<>();
    private String offreNouveauDocument;

    // ── Date limite de candidature (dialog rapide) ───────────────────────────
    private Long   offreDateLimiteId;
    private String offreDateLimiteValeur;

    private String domaineLibelle, domaineCode, domaineDescription;

    private String profilNom;
    private Long profilSelectionneId, permissionSelectionneId;
    private String permissionsSelectionneesIds = "";

    private String brancheNom, brancheCode;
    // Édition inline d'une branche
    private Long   brancheEditId;
    private String brancheEditNom, brancheEditCode;
    private String permissionLibelle, permissionCode, permissionNiveau = "LECTURE";
    private Long permissionBrancheId;

    // Reset mot de passe
    private String motDePasseTemporaire;

    // Historique utilisateur
    private Long historiqueutilisateurId;
    private List<DemandeFormation> historiquedemandes;
    private List<CandidatureFront> historiqueCandidatures;
    private String historiqueNomUtilisateur;

    // ── Détail utilisateur ───────────────────────────────
    private Utilisateur utilisateurDetail;

    // ── Edition utilisateur (dialog Modifier) ────────────
    private Long   editUtilisateurId;
    private String editNom;
    private String editPrenom;
    private String editTelephone;
    private String editRole;

    public Long   getEditUtilisateurId()   { return editUtilisateurId; }
    public String getEditNom()             { return editNom; }
    public void   setEditNom(String v)     { this.editNom = v; }
    public String getEditPrenom()          { return editPrenom; }
    public void   setEditPrenom(String v)  { this.editPrenom = v; }
    public String getEditTelephone()       { return editTelephone; }
    public void   setEditTelephone(String v) { this.editTelephone = v; }
    public String getEditRole()            { return editRole; }
    public void   setEditRole(String v)    { this.editRole = v; }

    public void ouvrirModifierUtilisateur(com.senexpert.frontend.model.Utilisateur u) {
        this.editUtilisateurId = u.getId();
        this.editNom           = u.getNom();
        this.editPrenom        = u.getPrenom();
        this.editTelephone     = u.getTelephone();
        this.editRole          = u.getRole();
    }

    public void sauvegarderModificationUtilisateur() {
        if (editUtilisateurId == null) return;
        String t = session.getToken();
        boolean ok = userClient.modifierUtilisateur(editUtilisateurId, editNom, editPrenom, editTelephone, t);
        if (ok && editRole != null) {
            userClient.changerRole(editUtilisateurId, editRole, t);
        }
        msg(ok, "Utilisateur modifié.", "Échec modification.");
        personnes = userClient.listerUtilisateurs(t);
        editUtilisateurId = null;
    }

    // ── Permission utilisateur (dialog Permission) ────────
    private Long   permissionUtilisateurId;
    private String permissionUtilisateurNom;
    private com.senexpert.frontend.model.ProfilFront permissionUtilisateurProfil;
    private Long   permissionProfilSelectId;
    /** Map permissionId → cochée (true/false) pour le dialog checkboxes */
    private java.util.Map<Long, Boolean> permissionChecks = new java.util.HashMap<>();

    public Long   getPermissionUtilisateurId()  { return permissionUtilisateurId; }
    public String getPermissionUtilisateurNom() { return permissionUtilisateurNom; }
    public com.senexpert.frontend.model.ProfilFront getPermissionUtilisateurProfil() { return permissionUtilisateurProfil; }
    public Long   getPermissionProfilSelectId()  { return permissionProfilSelectId; }
    public void   setPermissionProfilSelectId(Long v) { this.permissionProfilSelectId = v; }
    public java.util.Map<Long, Boolean> getPermissionChecks() { return permissionChecks; }
    public void setPermissionChecks(java.util.Map<Long, Boolean> v) { this.permissionChecks = v; }

    /** Ouvre le dialog permissions : initialise les checkboxes à partir des permissions directes de l'utilisateur */
    public void ouvrirPermissionsUtilisateur(com.senexpert.frontend.model.Utilisateur u) {
        this.permissionUtilisateurId     = u.getId();
        this.permissionUtilisateurNom    = u.getPrenom() + " " + u.getNom();
        this.permissionUtilisateurProfil = u.getProfil();
        this.permissionProfilSelectId    = (u.getProfil() != null) ? u.getProfil().getId() : null;
        // Initialiser toutes les checkboxes à false
        permissionChecks = new java.util.HashMap<>();
        if (permissions != null) {
            for (PermissionFront p : permissions) { permissionChecks.put(p.getId(), Boolean.FALSE); }
        }
        // Cocher les permissions directes actuelles de cet utilisateur
        java.util.List<Long> actuelles = userClient.getPermissionsUtilisateur(u.getId(), session.getToken());
        if (actuelles != null) {
            for (Long pid : actuelles) { permissionChecks.put(pid, Boolean.TRUE); }
        }
    }

    /** Sauvegarde les permissions cochées dans le dialog */
    public void sauvegarderPermissionsUtilisateur() {
        if (permissionUtilisateurId == null) return;
        java.util.List<Long> selected = permissionChecks.entrySet().stream()
            .filter(e -> Boolean.TRUE.equals(e.getValue()))
            .map(java.util.Map.Entry::getKey)
            .collect(java.util.stream.Collectors.toList());
        boolean ok = userClient.assignerPermissionsDirectes(permissionUtilisateurId, selected, session.getToken());
        msg(ok, "Permissions enregistrées.", "Échec enregistrement permissions.");
        personnes = userClient.listerUtilisateurs(session.getToken());
        permissionUtilisateurId = null;
        permissionChecks.clear();
    }

    public void ouvrirDetailUtilisateur(Utilisateur u) {
        this.utilisateurDetail = u;
    }

    public Utilisateur getUtilisateurDetail() { return utilisateurDetail; }
    public void setUtilisateurDetail(Utilisateur u) { this.utilisateurDetail = u; }

    // ── Candidats d'une offre ─────────────────────────────
    private Long   offreSelectionneeId;
    private String offreSelectionneetitre;
    private List<DemandeFormation> demandesOffre = new java.util.ArrayList<>();

    @PostConstruct
    public void init() {
        chargerTout();
    }

    public void chargerTout() {
        String t = session.getToken();
        try { personnes     = userClient.listerUtilisateurs(t);          } catch (Exception ex) { personnes     = new java.util.ArrayList<>(); }
        try { formationClient.migrerOffresDemandesLibres(t); } catch (Exception ignored) {}
        try { offres        = formationClient.listerOffresCompletesAdmin(session.getToken());    } catch (Exception ex) { offres        = new java.util.ArrayList<>(); }
        try { domaines      = formationClient.listerDomaines();           } catch (Exception ex) { domaines      = new java.util.ArrayList<>(); }
        try { demandes      = formationClient.toutesLesDemandes(t);       } catch (Exception ex) { demandes      = new java.util.ArrayList<>(); }
        try { candidatures  = formationClient.toutesCandidatures(t);      } catch (Exception ex) { candidatures  = new java.util.ArrayList<>(); }
        trierEnAttenteFirst(demandes);
        trierEnAttenteFirst(candidatures);
        try { profils       = userClient.listerProfils(t);                } catch (Exception ex) { profils       = new java.util.ArrayList<>(); }
        try { permissions   = userClient.listerPermissions(t);            } catch (Exception ex) { permissions   = new java.util.ArrayList<>(); }
        try { branches      = userClient.listerBranches(t);               } catch (Exception ex) { branches      = new java.util.ArrayList<>(); }
        try { institutions  = userClient.listerInstitutions(t);           } catch (Exception ex) { institutions  = new java.util.ArrayList<>(); }
        // Charger les 20 dernières entrées d'audit
        try {
            List<java.util.Map<String, Object>> rawAudit = userClient.listerAudit(20, t);
            auditEntries = new java.util.ArrayList<>();
            if (rawAudit != null) {
                for (var m : rawAudit) {
                    AuditEntry e = new AuditEntry();
                    e.setAction(String.valueOf(m.getOrDefault("action", "")));
                    e.setEntite(String.valueOf(m.getOrDefault("entite", "")));
                    e.setDetail(String.valueOf(m.getOrDefault("detail", "")));
                    e.setAdminEmail(String.valueOf(m.getOrDefault("adminEmail", "")));
                    e.setNiveau(String.valueOf(m.getOrDefault("niveau", "INFO")));
                    e.setDate(String.valueOf(m.getOrDefault("date", "")));
                    auditEntries.add(e);
                }
            }
        } catch (Exception ex) { auditEntries = new java.util.ArrayList<>(); }
        // Demandes à la carte
        try { demandesLibres = formationClient.toutesDemandesLibres(t); }
        catch (Exception ex) { demandesLibres = new java.util.ArrayList<>(); }
        trierEnAttenteFirst(demandesLibres);
        // résoudre noms candidats
        resoudreNomsCandidats(t);
        // Formations soumises par prestataires
        try { formationsSoumises = formationClient.formationsSoumises(t); }
        catch (Exception ex) { formationsSoumises = new java.util.ArrayList<>(); }
    }

    /** Recharge uniquement les demandes et résout les noms — appelé après valider/rejeter */
    public void chargerDemandes() {
        String t = session.getToken();
        try { demandes = formationClient.toutesLesDemandes(t); } catch (Exception ex) { demandes = new java.util.ArrayList<>(); }
        trierEnAttenteFirst(demandes);
        resoudreNomsCandidats(t);
    }

    /** Tri générique : met les éléments EN_ATTENTE en premier. */
    private <T> void trierEnAttenteFirst(List<T> list) {
        if (list == null) return;
        list.sort((a, b) -> {
            String sA = getStatutOf(a);
            String sB = getStatutOf(b);
            int pA = "EN_ATTENTE".equals(sA) ? 0 : 1;
            int pB = "EN_ATTENTE".equals(sB) ? 0 : 1;
            return Integer.compare(pA, pB);
        });
    }

    private String getStatutOf(Object o) {
        try { return (String) o.getClass().getMethod("getStatut").invoke(o); }
        catch (Exception e) { return ""; }
    }

    private void resoudreNomsCandidats(String token) {
        if (demandes == null) return;
        for (var d : demandes) {
            if (d.getUtilisateurId() != null && (d.getNomCandidat() == null || d.getNomCandidat().isBlank())) {
                try {
                    Utilisateur u = userClient.trouverParId(d.getUtilisateurId(), token);
                    d.setNomCandidat(u != null ? u.getPrenom() + " " + u.getNom() : "#" + d.getUtilisateurId());
                } catch (Exception e) {
                    d.setNomCandidat("#" + d.getUtilisateurId());
                }
            }
        }
    }

    // ─── Personnes ───────────────────────────────────────────────────────────

    public void rechercherPersonnes() {
        if (recherchePersonne == null || recherchePersonne.isBlank()) {
            personnes = userClient.listerUtilisateurs(session.getToken());
        } else {
            personnes = userClient.rechercherUtilisateurs(recherchePersonne, session.getToken());
        }
    }

    /** Filtre la liste par statut (clique sur chip ou carte KPI). */
    public void filtrerParStatutPersonne(String statut) {
        this.filtreStatutPersonne = statut;
        recherchePersonne = null;
        personnes = userClient.listerUtilisateurs(session.getToken());
    }
    public void filtrerPersonnesTous()    { filtrerParStatutPersonne(null); }
    public void filtrerPersonnesAttente() { filtrerParStatutPersonne("EN_ATTENTE"); }
    public void filtrerPersonnesSuspendus(){ filtrerParStatutPersonne("INACTIF"); }

    /** Liste triée : EN_ATTENTE d'abord, puis filtrée si filtreStatutPersonne != null. */
    public java.util.List<Utilisateur> getPersonnesFiltrees() {
        if (personnes == null) return java.util.List.of();
        java.util.List<Utilisateur> list = personnes.stream()
            .filter(u -> filtreStatutPersonne == null || filtreStatutPersonne.equals(u.getStatut()))
            .sorted((a, b) -> {
                int pA = "EN_ATTENTE".equals(a.getStatut()) ? 0 : "INACTIF".equals(a.getStatut()) ? 1 : 2;
                int pB = "EN_ATTENTE".equals(b.getStatut()) ? 0 : "INACTIF".equals(b.getStatut()) ? 1 : 2;
                return Integer.compare(pA, pB);
            })
            .collect(java.util.stream.Collectors.toList());
        return list;
    }

    public String getFiltreStatutPersonne() { return filtreStatutPersonne; }
    public void setFiltreStatutPersonne(String v) { this.filtreStatutPersonne = v; }

    // ─── Filtre offres ────────────────────────────────────────────────────────
    private String filtreStatutOffre; // null = toutes

    public void filtrerParStatutOffre(String statut) { this.filtreStatutOffre = statut; }
    public void filtrerOffresTous()      { filtreStatutOffre = null; }
    public void filtrerOffresDisponibles() { filtreStatutOffre = "DISPONIBLE"; }
    public void filtrerOffresBrouillons() { filtreStatutOffre = "BROUILLON"; }
    public void filtrerOffresFermees()   { filtreStatutOffre = "FERME"; }

    public java.util.List<Offre> getOffresFiltrees() {
        if (offres == null) return java.util.List.of();
        if (filtreStatutOffre == null) return offres;
        return offres.stream()
            .filter(o -> filtreStatutOffre.equals(o.getStatut()))
            .collect(java.util.stream.Collectors.toList());
    }

    public String getFiltreStatutOffre() { return filtreStatutOffre; }
    public void setFiltreStatutOffre(String v) { this.filtreStatutOffre = v; }

    public void validerPersonne(Long id) {
        boolean ok = userClient.validerUtilisateur(id, session.getToken());
        msg(ok, "Compte validé.", "Échec validation.");
        if (ok) AuditBean.log("VALIDATION", "UTILISATEUR", "ID: " + id, session.getEmail(), "INFO");
        personnes = userClient.listerUtilisateurs(session.getToken());
    }

    public void suspendrePersonne(Long id) {
        msg(userClient.desactiverUtilisateur(id, session.getToken()), "Compte suspendu.", "Échec suspension.");
        personnes = userClient.listerUtilisateurs(session.getToken());
    }

    public void reactiverPersonne(Long id) {
        msg(userClient.reactiverUtilisateur(id, session.getToken()), "Compte réactivé.", "Échec réactivation.");
        personnes = userClient.listerUtilisateurs(session.getToken());
    }

    public void inactiverPersonne(Long id) {
        msg(userClient.desactiverUtilisateur(id, session.getToken()), "Compte inactivé.", "Échec inactivation.");
        personnes = userClient.listerUtilisateurs(session.getToken());
    }

    public void supprimerPersonne(Long id) {
        boolean ok = userClient.supprimerUtilisateur(id, session.getToken());
        msg(ok, "Compte supprimé.", "Échec suppression.");
        if (ok) AuditBean.log("SUPPRESSION", "UTILISATEUR", "ID: " + id, session.getEmail(), "WARN");
        personnes = userClient.listerUtilisateurs(session.getToken());
    }

    // Map userId → profilId sélectionné (Object car EL peut stocker String ou Long selon version JSF)
    private java.util.Map<Long, Object> profilParUtilisateur = new java.util.HashMap<>();

    public java.util.Map<Long, Object> getProfilParUtilisateur() { return profilParUtilisateur; }
    public void setProfilParUtilisateur(java.util.Map<Long, Object> m) { this.profilParUtilisateur = m; }

    public void assignerProfil(Long userId) {
        // EL peut stocker Long ou String selon version JSF/convertisseur — on normalise
        Object raw = profilParUtilisateur.get(userId);
        if (raw == null) raw = profilParUtilisateur.get(String.valueOf(userId));
        if (raw == null || raw.toString().isBlank()) {
            msg(false, "", "Sélectionnez un profil."); return;
        }
        Long pid;
        try {
            pid = raw instanceof Long ? (Long) raw : Long.valueOf(raw.toString());
        } catch (NumberFormatException e) {
            msg(false, "", "Profil invalide."); return;
        }
        msg(userClient.assignerProfil(userId, pid, session.getToken()),
            "Profil assigné.", "Échec assignation profil.");
        profilParUtilisateur.remove(userId);
        profilParUtilisateur.remove(String.valueOf(userId));
        personnes = userClient.listerUtilisateurs(session.getToken());
    }

    /** Stocke la sélection dans le Map sans assigner — le bouton Valider confirmera */
    public void storerSelectionProfil(jakarta.faces.event.AjaxBehaviorEvent event) {
        Object uidAttr = event.getComponent().getAttributes().get("uid");
        if (uidAttr == null) return;
        Long userId = uidAttr instanceof Long ? (Long) uidAttr : Long.valueOf(uidAttr.toString());
        Object val = ((jakarta.faces.component.UIInput) event.getComponent()).getValue();
        if (val == null || val.toString().isBlank()) {
            profilParUtilisateur.remove(userId);
            profilParUtilisateur.remove(String.valueOf(userId));
            return;
        }
        profilParUtilisateur.put(userId, val);
    }

    /** Appelé via p:ajax sur le selectOneMenu — assigne immédiatement dès la sélection */
    public void assignerProfilAjax(jakarta.faces.event.AjaxBehaviorEvent event) {
        // Lire l'userId passé via f:attribute
        Object uidAttr = event.getComponent().getAttributes().get("uid");
        if (uidAttr == null) return;
        Long userId = uidAttr instanceof Long ? (Long) uidAttr : Long.valueOf(uidAttr.toString());

        // Lire la valeur sélectionnée directement depuis le composant (évite les pb de type Long/String dans la Map)
        Object val = ((jakarta.faces.component.UIInput) event.getComponent()).getValue();
        if (val == null) return; // "— Changer —" sélectionné : rien à faire

        Long pid = val instanceof Long ? (Long) val : Long.valueOf(val.toString());

        boolean ok = userClient.assignerProfil(userId, pid, session.getToken());
        msg(ok, "Profil assigné.", "Échec assignation profil.");

        // Réinitialiser le dropdown et recharger la liste
        profilParUtilisateur.remove(userId);
        profilParUtilisateur.remove(String.valueOf(userId)); // sécurité si clé String
        if (ok) personnes = userClient.listerUtilisateurs(session.getToken());
    }

    // ─── Offres ──────────────────────────────────────────────────────────────

    public void creerOffre() {
        boolean ok = formationClient.creerOffreComplete(offreTitre, offreDescription, offreDateDebut, offreDateFin,
                offrePlaces, offreType, offreDomaineId, offreNiveau, offreDuree, offreCout,
                offreDocumentsRequis.isEmpty() ? null : offreDocumentsRequis, session.getToken());
        if (ok) { msg(true, "Offre créée."); resetOffre(); offres = formationClient.listerOffresCompletesAdmin(session.getToken()); }
        else msg(false, "Échec création offre.");
    }

    public void chargerOffrePourEdition(Offre o) {
        offreEnCoursId = o.getId(); offreModeEdition = true;
        offreTitre = o.getTitre(); offreDescription = o.getDescription();
        offreDateDebut = o.getDateDebut(); offreDateFin = o.getDateFin();
        offrePlaces = o.getPlaces(); offreType = o.getType();
        offreNiveau = o.getNiveau(); offreDuree = o.getDuree(); offreCout = o.getCout();
        offreDomaineId = o.getDomaine() != null ? o.getDomaine().getId() : null;
        offreDocumentsRequis = o.getDocumentsRequis() != null
                ? new java.util.ArrayList<>(o.getDocumentsRequis()) : new java.util.ArrayList<>();
    }

    public void modifierOffre() {
        boolean ok = formationClient.modifierOffreComplete(offreEnCoursId, offreTitre, offreDescription,
                offreDateDebut, offreDateFin, offrePlaces, offreType, offreDomaineId,
                offreNiveau, offreDuree, offreCout,
                offreDocumentsRequis.isEmpty() ? null : offreDocumentsRequis, session.getToken());
        if (ok) { msg(true, "Offre modifiée."); resetOffre(); offres = formationClient.listerOffresCompletesAdmin(session.getToken()); }
        else msg(false, "Échec modification.");
    }

    /** Ajoute un document requis à la liste temporaire */
    public void ajouterDocumentRequis() {
        if (offreNouveauDocument != null && !offreNouveauDocument.isBlank()) {
            offreDocumentsRequis.add(offreNouveauDocument.trim());
            offreNouveauDocument = null;
        }
    }

    /** Supprime le document requis à l'index donné */
    public void supprimerDocumentRequis(int index) {
        if (index >= 0 && index < offreDocumentsRequis.size()) {
            offreDocumentsRequis.remove(index);
        }
    }

    public void publierOffre(Long id) {
        msg(formationClient.publierOffre(id, session.getToken()), "Offre publiée.", "Échec publication.");
        offres = formationClient.listerOffresCompletesAdmin(session.getToken());
    }

    public void fermerOffre(Long id) {
        msg(formationClient.fermerOffre(id, session.getToken()), "Offre fermée.", "Échec fermeture.");
        offres = formationClient.listerOffresCompletesAdmin(session.getToken());
    }

    /** Ouvre le dialog de date limite pour une offre */
    public void ouvrirDialogDateLimite(com.senexpert.frontend.model.Offre o) {
        offreDateLimiteId    = o.getId();
        offreDateLimiteValeur = o.getDateLimiteCandidature();
    }

    /** Sauvegarde la date limite de candidature */
    public void sauvegarderDateLimite() {
        boolean ok = formationClient.majDateLimiteCandidature(
            offreDateLimiteId, offreDateLimiteValeur, session.getToken());
        if (ok) {
            msg(true, "Date limite mise à jour.");
            offres = formationClient.listerOffresCompletesAdmin(session.getToken());
        } else {
            msg(false, "Échec mise à jour date limite.");
        }
    }

    public void supprimerOffre(Long id) {
        msg(formationClient.supprimerOffre(id, session.getToken()), "Offre supprimée.", "Échec suppression.");
        offres = formationClient.listerOffresCompletesAdmin(session.getToken());
    }

    private void resetOffre() {
        offreEnCoursId = null; offreModeEdition = false;
        offreTitre = null; offreDescription = null; offreDateDebut = null; offreDateFin = null;
        offrePlaces = null; offreType = "EN_LIGNE"; offreDomaineId = null;
        offreNiveau = null; offreDuree = null; offreCapacite = null; offreCout = null;
        offreDocumentsRequis = new java.util.ArrayList<>(); offreNouveauDocument = null;
    }

    // ─── Domaines ────────────────────────────────────────────────────────────

    public void creerDomaine() {
        msg(formationClient.creerDomaineComplet(domaineLibelle, domaineCode, domaineDescription, session.getToken()),
            "Domaine créé.", "Échec création domaine.");
        domaineLibelle = null; domaineCode = null; domaineDescription = null;
        domaines = formationClient.listerDomaines();
    }

    public void supprimerDomaine(Long id) {
        msg(formationClient.supprimerDomaine(id, session.getToken()), "Domaine supprimé.", "Échec suppression domaine.");
        domaines = formationClient.listerDomaines();
    }

    // ─── Profils ─────────────────────────────────────────────────────────────

    public void creerProfil() {
        msg(userClient.creerProfil(profilNom, session.getToken()), "Profil créé.", "Échec création profil.");
        profilNom = null;
        profils = userClient.listerProfils(session.getToken());
    }

    public void supprimerProfil(Long id) {
        msg(userClient.supprimerProfil(id, session.getToken()), "Profil supprimé.", "Échec suppression profil.");
        profils = userClient.listerProfils(session.getToken());
    }

    public void ajouterPermissionAuProfil(Long profilId) {
        if (permissionSelectionneId == null) { msg(false, "", "Sélectionnez une permission."); return; }
        msg(userClient.ajouterPermissionAuProfil(profilId, permissionSelectionneId, session.getToken()),
            "Permission ajoutée.", "Échec ajout permission.");
        profils = userClient.listerProfils(session.getToken());
    }

    /** Ajouter plusieurs permissions (IDs en CSV) depuis les checkboxes */
    public void ajouterPermissionsIdsAuProfil(Long profilId) {
        if (permissionsSelectionneesIds == null || permissionsSelectionneesIds.isBlank()) {
            msg(false, "", "Sélectionnez au moins une permission."); return;
        }
        boolean ok = true;
        for (String s : permissionsSelectionneesIds.split(",")) {
            try {
                Long permId = Long.parseLong(s.trim());
                if (!userClient.ajouterPermissionAuProfil(profilId, permId, session.getToken())) ok = false;
            } catch (Exception ignored) {}
        }
        permissionsSelectionneesIds = "";
        profils = userClient.listerProfils(session.getToken());
        msg(ok, "Permissions ajoutées.", "Certaines permissions ont échoué.");
    }

    /** Ajouter une permission via clic direct (pas de dropdown intermédiaire) */
    public void ajouterPermissionAuProfilDirect(Long profilId, Long permissionId) {
        msg(userClient.ajouterPermissionAuProfil(profilId, permissionId, session.getToken()),
            "Permission ajoutée.", "Échec ajout permission.");
        profils = userClient.listerProfils(session.getToken());
    }

    public void retirerPermissionDuProfil(Long profilId, Long permId) {
        msg(userClient.retirerPermissionDuProfil(profilId, permId, session.getToken()),
            "Permission retirée.", "Échec retrait permission.");
        profils = userClient.listerProfils(session.getToken());
    }

    // ─── Branches ────────────────────────────────────────────────────────────

    public void ouvrirEditionBranche(BrancheFront b) {
        brancheEditId   = b.getId();
        brancheEditNom  = b.getNom();
        brancheEditCode = b.getCode();
    }

    public void annulerEditionBranche() {
        brancheEditId = null; brancheEditNom = null; brancheEditCode = null;
    }

    public void sauvegarderBranche() {
        if (brancheEditId == null) return;
        msg(userClient.modifierBranche(brancheEditId, brancheEditNom, brancheEditCode, session.getToken()),
            "Branche modifiée.", "Échec modification branche.");
        brancheEditId = null; brancheEditNom = null; brancheEditCode = null;
        branches = userClient.listerBranches(session.getToken());
    }

    public void creerBranche() {
        msg(userClient.creerBranche(brancheNom, brancheCode, session.getToken()),
            "Branche créée.", "Échec création branche.");
        brancheNom = null; brancheCode = null;
        branches = userClient.listerBranches(session.getToken());
    }

    public void supprimerBranche(Long id) {
        msg(userClient.supprimerBranche(id, session.getToken()), "Branche supprimée.", "Échec suppression.");
        branches = userClient.listerBranches(session.getToken());
    }

    // ─── Permissions ─────────────────────────────────────────────────────────

    public void creerPermission() {
        // Auto-génération du code : code_branche + 1 incrément (ex: 1000 → 1001, 1002…)
        if (permissionBrancheId != null && branches != null) {
            BrancheFront branche = branches.stream()
                    .filter(b -> b.getId().equals(permissionBrancheId))
                    .findFirst().orElse(null);
            if (branche != null) {
                try {
                    int base = Integer.parseInt(branche.getCode());
                    int maxCode = (permissions == null ? java.util.Collections.<PermissionFront>emptyList() : permissions)
                            .stream()
                            .filter(p -> p.getBranche() != null && p.getBranche().getId().equals(permissionBrancheId))
                            .mapToInt(p -> { try { return Integer.parseInt(p.getCode()); } catch (Exception e) { return base; } })
                            .max().orElse(base);
                    permissionCode = String.valueOf(maxCode + 1);
                } catch (NumberFormatException e) {
                    // branche code non numérique : garder permissionCode saisi manuellement
                }
            }
        }
        msg(userClient.creerPermission(permissionLibelle, permissionCode, permissionNiveau,
                permissionBrancheId, session.getToken()), "Permission créée.", "Échec création permission.");
        permissionLibelle = null; permissionCode = null; permissionNiveau = "LECTURE"; permissionBrancheId = null;
        permissions = userClient.listerPermissions(session.getToken());
    }

    public void supprimerPermission(Long id) {
        msg(userClient.supprimerPermission(id, session.getToken()), "Permission supprimée.", "Échec suppression.");
        permissions = userClient.listerPermissions(session.getToken());
    }

    // ─── Institutions ────────────────────────────────────────────────────────

    public void validerInstitution(Long id) {
        msg(userClient.validerInstitution(id, session.getToken()), "Institution validée.", "Échec validation.");
        institutions = userClient.listerInstitutions(session.getToken());
    }

    // ─── Demandes ────────────────────────────────────────────────────────────

    public void validerDemande(Long id) {
        msg(formationClient.validerDemande(id, session.getToken()), "Demande validée.", "Échec validation.");
        chargerDemandes();
    }

    public void rejeterDemande(Long id) {
        msg(formationClient.rejeterDemande(id, session.getToken()), "Demande rejetée.", "Échec rejet.");
        chargerDemandes();
    }

    // ─── Candidatures ────────────────────────────────────────────────────────

    public void accepterCandidature(Long id) {
        msg(formationClient.accepterCandidature(id, session.getToken()), "Candidature acceptée.", "Échec.");
        candidatures = formationClient.toutesCandidatures(session.getToken());
    }

    public void refuserCandidature(Long id) {
        msg(formationClient.refuserCandidature(id, session.getToken()), "Candidature refusée.", "Échec.");
        candidatures = formationClient.toutesCandidatures(session.getToken());
    }

    // ─── Utilitaire ──────────────────────────────────────────────────────────

    private void msg(boolean ok, String success, String error) {
        FacesContext.getCurrentInstance().addMessage(null, ok
            ? new FacesMessage(FacesMessage.SEVERITY_INFO, "Succès", success)
            : new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", error));
    }
    private void msg(boolean ok, String success) { msg(ok, success, "Erreur inattendue."); }

    /**
     * Retourne les offres les plus demandées (top 7) :
     * liste de tableaux [titre, count] triée par nombre de demandes décroissant.
     */
    public java.util.List<Object[]> getTopOffresParDemandes() {
        java.util.Map<String, Long> counts = new java.util.LinkedHashMap<>();
        if (demandes != null) {
            for (var d : demandes) {
                if (d.getOffre() != null && d.getOffre().getTitre() != null) {
                    String titre = d.getOffre().getTitre();
                    counts.merge(titre, 1L, Long::sum);
                }
            }
        }
        if (candidatures != null) {
            for (var c : candidatures) {
                if (c.getOffre() != null && c.getOffre().getTitre() != null) {
                    // compter 1 point par candidature uniquement si pas déjà dans demandes
                    String titre = c.getOffre().getTitre();
                    counts.merge(titre, 1L, Long::sum);
                }
            }
        }
        return counts.entrySet().stream()
                .sorted(java.util.Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(7)
                .map(e -> new Object[]{e.getKey(), e.getValue()})
                .collect(java.util.stream.Collectors.toList());
    }

    /** Max des demandes pour calculer les % de la barre */
    public long getMaxDemandesOffre() {
        return getTopOffresParDemandes().stream()
                .mapToLong(a -> (Long) a[1])
                .max().orElse(1L);
    }

    /** Demandes EN_ATTENTE à traiter */
    public java.util.List<DemandeFormation> getDemandesEnAttente() {
        if (demandes == null) return java.util.List.of();
        return demandes.stream()
                .filter(d -> "EN_ATTENTE".equals(d.getStatut()))
                .collect(java.util.stream.Collectors.toList());
    }

    /** Candidatures EN_ATTENTE à traiter */
    public java.util.List<CandidatureFront> getCandidaturesEnAttente() {
        if (candidatures == null) return java.util.List.of();
        return candidatures.stream()
                .filter(c -> "EN_ATTENTE".equals(c.getStatut()))
                .collect(java.util.stream.Collectors.toList());
    }

    /** Nombre total d'éléments à valider (badge topbar) */
    public long getTotalAValider() {
        return countPersonnesParStatut("EN_ATTENTE")
             + getInstitutionsEnAttente().size()
             + getDemandesEnAttente().size();
    }

    /** Institutions à valider uniquement */
    public java.util.List<com.senexpert.frontend.model.Institution> getInstitutionsEnAttente() {
        if (institutions == null) return java.util.List.of();
        return institutions.stream()
                .filter(i -> "EN_ATTENTE".equals(i.getStatut()))
                .collect(java.util.stream.Collectors.toList());
    }

    public long countCandidaturesParStatut(String statut) {
        if (candidatures == null) return 0;
        return candidatures.stream().filter(c -> statut.equals(c.getStatut())).count();
    }

    public long countPersonnesParStatut(String statut) {
        if (personnes == null) return 0;
        return personnes.stream().filter(u -> statut.equals(u.getStatut())).count();
    }

    public long countDemandesParStatut(String statut) {
        if (demandes == null) return 0;
        return demandes.stream().filter(d -> statut.equals(d.getStatut())).count();
    }

    /** JSON pour Chart.js : stats demandes par statut */
    public String getDemandesChartJson() {
        long enAttente = countDemandesParStatut("EN_ATTENTE");
        long validees  = countDemandesParStatut("VALIDEE");
        long rejetees  = countDemandesParStatut("REJETEE");
        return "[" + enAttente + "," + validees + "," + rejetees + "]";
    }

    /** JSON pour Chart.js : offres par statut */
    public String getOffresChartJson() {
        if (offres == null) return "[0,0,0]";
        long brouillon   = offres.stream().filter(o -> "BROUILLON".equals(o.getStatut())).count();
        long disponible  = offres.stream().filter(o -> "DISPONIBLE".equals(o.getStatut())).count();
        long ferme       = offres.stream().filter(o -> "FERME".equals(o.getStatut())).count();
        return "[" + brouillon + "," + disponible + "," + ferme + "]";
    }

    /** JSON pour Chart.js : candidatures par statut */
    public String getCandidaturesChartJson() {
        long enAttente = countCandidaturesParStatut("EN_ATTENTE");
        long acceptees = countCandidaturesParStatut("ACCEPTEE");
        long refusees  = countCandidaturesParStatut("REFUSEE");
        return "[" + enAttente + "," + acceptees + "," + refusees + "]";
    }

    /** JSON pour Chart.js : personnes par rôle */
    public String getPersonnesRoleJson() {
        if (personnes == null) return "[0,0,0]";
        long admins      = personnes.stream().filter(u -> "ADMIN".equals(u.getRole())).count();
        long agents      = personnes.stream().filter(u -> "AGENT".equals(u.getRole())).count();
        long particuliers = personnes.stream().filter(u -> "PARTICULIER".equals(u.getRole())).count();
        return "[" + admins + "," + agents + "," + particuliers + "]";
    }

    /** Recherche globale dans personnes, offres, demandes */
    private String rechercheGlobale;
    private List<Utilisateur> resultatsPersonnes;
    private List<Offre> resultatsOffres;
    private List<DemandeFormation> resultatsdemandes;

    public void rechercheGlobale() {
        if (rechercheGlobale == null || rechercheGlobale.isBlank()) {
            resultatsPersonnes = new java.util.ArrayList<>();
            resultatsOffres    = new java.util.ArrayList<>();
            resultatsdemandes  = new java.util.ArrayList<>();
            return;
        }
        String q = rechercheGlobale.toLowerCase();
        resultatsPersonnes = personnes == null ? new java.util.ArrayList<>() :
            personnes.stream().filter(u ->
                (u.getNom() != null && u.getNom().toLowerCase().contains(q)) ||
                (u.getPrenom() != null && u.getPrenom().toLowerCase().contains(q)) ||
                (u.getEmail() != null && u.getEmail().toLowerCase().contains(q))
            ).collect(java.util.stream.Collectors.toList());

        resultatsOffres = offres == null ? new java.util.ArrayList<>() :
            offres.stream().filter(o ->
                (o.getTitre() != null && o.getTitre().toLowerCase().contains(q)) ||
                (o.getDescription() != null && o.getDescription().toLowerCase().contains(q))
            ).collect(java.util.stream.Collectors.toList());

        resultatsdemandes = demandes == null ? new java.util.ArrayList<>() :
            demandes.stream().filter(d ->
                (d.getNomCandidat() != null && d.getNomCandidat().toLowerCase().contains(q)) ||
                (d.getOffre() != null && d.getOffre().getTitre() != null && d.getOffre().getTitre().toLowerCase().contains(q))
            ).collect(java.util.stream.Collectors.toList());
    }

    public int getTotalResultats() {
        int r = 0;
        if (resultatsPersonnes != null) r += resultatsPersonnes.size();
        if (resultatsOffres != null) r += resultatsOffres.size();
        if (resultatsdemandes != null) r += resultatsdemandes.size();
        return r;
    }

    public String getRechercheGlobale() { return rechercheGlobale; }
    public void setRechercheGlobale(String v) { this.rechercheGlobale = v; }
    public List<Utilisateur> getResultatsPersonnes() { return resultatsPersonnes; }
    public List<Offre> getResultatsOffres() { return resultatsOffres; }
    public List<DemandeFormation> getResultatsdemandes() { return resultatsdemandes; }

    // Getters / Setters
    public List<Utilisateur> getPersonnes() { return personnes; }
    public List<Offre> getOffres() { return offres; }
    public List<Domaine> getDomaines() { return domaines; }
    public List<DemandeFormation> getDemandes() { return demandes; }
    public List<CandidatureFront> getCandidatures() { return candidatures; }
    public List<Utilisateur>       getFilteredPersonnes()      { return filteredPersonnes; }
    public void setFilteredPersonnes(List<Utilisateur> v)      { this.filteredPersonnes = v; }
    public List<DemandeFormation>  getFilteredDemandes()       { return filteredDemandes; }
    public void setFilteredDemandes(List<DemandeFormation> v)  { this.filteredDemandes = v; }
    public List<CandidatureFront>  getFilteredCandidatures()   { return filteredCandidatures; }
    public void setFilteredCandidatures(List<CandidatureFront> v) { this.filteredCandidatures = v; }
    public List<DemandeLibreFront> getFilteredDemandesLibres() { return filteredDemandesLibres; }
    public void setFilteredDemandesLibres(List<DemandeLibreFront> v) { this.filteredDemandesLibres = v; }
    public List<ProfilFront> getProfils() { return profils; }

    /** Rôles système qui ne sont pas des profils de permissions gérables. */
    private static final java.util.Set<String> ROLES_SYSTEME = new java.util.HashSet<>(
        java.util.Arrays.asList("SUPER_ADMIN"));

    /** Profils affichables dans la grille de gestion (exclut les rôles système purs). */
    public List<ProfilFront> getProfilsGerables() {
        if (profils == null) return java.util.Collections.emptyList();
        return profils.stream()
            .filter(p -> p.getNom() != null && !ROLES_SYSTEME.contains(p.getNom().toUpperCase()))
            .collect(java.util.stream.Collectors.toList());
    }
    public List<PermissionFront> getPermissions() { return permissions; }
    public List<BrancheFront> getBranches() { return branches; }

    /** Permissions d'un profil filtrées par branche (pour l'affichage groupé) */
    public List<PermissionFront> permissionsPourBranche(List<PermissionFront> perms, Long brancheId) {
        if (perms == null || brancheId == null) return java.util.Collections.emptyList();
        return perms.stream()
                .filter(p -> p.getBranche() != null && brancheId.equals(p.getBranche().getId()))
                .collect(java.util.stream.Collectors.toList());
    }

    /** Permissions sans branche associée */
    public List<PermissionFront> permissionsSansBranche(List<PermissionFront> perms) {
        if (perms == null) return java.util.Collections.emptyList();
        return perms.stream()
                .filter(p -> p.getBranche() == null)
                .collect(java.util.stream.Collectors.toList());
    }

    /** Permissions disponibles (catalogue) filtrées par branche — pour le dropdown groupé */
    public List<PermissionFront> permissionsCataloguePourBranche(Long brancheId) {
        if (permissions == null || brancheId == null) return java.util.Collections.emptyList();
        return permissions.stream()
                .filter(p -> p.getBranche() != null && brancheId.equals(p.getBranche().getId()))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Map EL-friendly : permsCatalogueParBranche[brancheId] → liste de permissions.
     * Évite les erreurs "method expression" d'Eclipse dans les attributs rendered/value.
     */
    public java.util.Map<Long, List<PermissionFront>> getPermsCatalogueParBranche() {
        return new java.util.AbstractMap<Long, List<PermissionFront>>() {
            @Override
            public List<PermissionFront> get(Object key) {
                if (key instanceof Long)   return permissionsCataloguePourBranche((Long) key);
                if (key instanceof Number) return permissionsCataloguePourBranche(((Number) key).longValue());
                return java.util.Collections.emptyList();
            }
            @Override
            public java.util.Set<java.util.Map.Entry<Long, List<PermissionFront>>> entrySet() {
                return java.util.Collections.emptySet();
            }
            @Override public boolean isEmpty()                 { return false; }
            @Override public boolean containsKey(Object key)   { return key != null; }
        };
    }
    /**
     * Map EL-friendly : permsPourProfilParBranche[profil.id][br.id] → liste de permissions.
     * Remplace permissionsPourBranche(profil.permissions, br.id) dans les attributs rendered/value.
     */
    public java.util.Map<Long, java.util.Map<Long, List<PermissionFront>>> getPermsPourProfilParBranche() {
        return new java.util.AbstractMap<Long, java.util.Map<Long, List<PermissionFront>>>() {
            @Override
            public java.util.Map<Long, List<PermissionFront>> get(Object profilIdObj) {
                final Long profilId = profilIdObj instanceof Long ? (Long) profilIdObj
                        : profilIdObj instanceof Number ? ((Number) profilIdObj).longValue() : null;
                final List<PermissionFront> perms;
                if (profilId != null && profils != null) {
                    com.senexpert.frontend.model.ProfilFront p = profils.stream()
                            .filter(x -> profilId.equals(x.getId())).findFirst().orElse(null);
                    perms = p != null && p.getPermissions() != null
                            ? p.getPermissions() : java.util.Collections.emptyList();
                } else {
                    perms = java.util.Collections.emptyList();
                }
                return new java.util.AbstractMap<Long, List<PermissionFront>>() {
                    @Override
                    public List<PermissionFront> get(Object brancheIdObj) {
                        Long brancheId = brancheIdObj instanceof Long ? (Long) brancheIdObj
                                : brancheIdObj instanceof Number ? ((Number) brancheIdObj).longValue() : null;
                        return permissionsPourBranche(perms, brancheId);
                    }
                    @Override
                    public java.util.Set<java.util.Map.Entry<Long, List<PermissionFront>>> entrySet() {
                        return java.util.Collections.emptySet();
                    }
                    @Override public boolean isEmpty()               { return false; }
                    @Override public boolean containsKey(Object key) { return key != null; }
                };
            }
            @Override
            public java.util.Set<java.util.Map.Entry<Long, java.util.Map<Long, List<PermissionFront>>>> entrySet() {
                return java.util.Collections.emptySet();
            }
            @Override public boolean isEmpty()               { return false; }
            @Override public boolean containsKey(Object key) { return key != null; }
        };
    }

    /**
     * Map EL-friendly : permsSansBrancheParProfil[profil.id] → liste de permissions sans branche.
     * Remplace permissionsSansBranche(profil.permissions) dans les attributs rendered/value.
     */
    public java.util.Map<Long, List<PermissionFront>> getPermsSansBrancheParProfil() {
        return new java.util.AbstractMap<Long, List<PermissionFront>>() {
            @Override
            public List<PermissionFront> get(Object profilIdObj) {
                Long profilId = profilIdObj instanceof Long ? (Long) profilIdObj
                        : profilIdObj instanceof Number ? ((Number) profilIdObj).longValue() : null;
                if (profilId == null || profils == null) return java.util.Collections.emptyList();
                com.senexpert.frontend.model.ProfilFront p = profils.stream()
                        .filter(x -> profilId.equals(x.getId())).findFirst().orElse(null);
                List<PermissionFront> perms = p != null && p.getPermissions() != null
                        ? p.getPermissions() : java.util.Collections.emptyList();
                return permissionsSansBranche(perms);
            }
            @Override
            public java.util.Set<java.util.Map.Entry<Long, List<PermissionFront>>> entrySet() {
                return java.util.Collections.emptySet();
            }
            @Override public boolean isEmpty()               { return false; }
            @Override public boolean containsKey(Object key) { return key != null; }
        };
    }

    public List<Institution> getInstitutions() { return institutions; }
    public List<AuditEntry> getAuditEntries() { return auditEntries; }

    public String getRecherchePersonne() { return recherchePersonne; }
    public void setRecherchePersonne(String v) { this.recherchePersonne = v; }

    public String getOffreTitre() { return offreTitre; }
    public void setOffreTitre(String v) { this.offreTitre = v; }
    public String getOffreDescription() { return offreDescription; }
    public void setOffreDescription(String v) { this.offreDescription = v; }
    public String getOffreNiveau() { return offreNiveau; }
    public void setOffreNiveau(String v) { this.offreNiveau = v; }
    public String getOffreDuree() { return offreDuree; }
    public void setOffreDuree(String v) { this.offreDuree = v; }
    public String getOffreType() { return offreType; }
    public void setOffreType(String v) { this.offreType = v; }
    public Integer getOffrePlaces() { return offrePlaces; }
    public void setOffrePlaces(Integer v) { this.offrePlaces = v; }
    public Integer getOffreCapacite() { return offreCapacite; }
    public void setOffreCapacite(Integer v) { this.offreCapacite = v; }
    public Double getOffreCout() { return offreCout; }
    public void setOffreCout(Double v) { this.offreCout = v; }
    public String getOffreDateDebut() { return offreDateDebut; }
    public void setOffreDateDebut(String v) { this.offreDateDebut = v; }
    public String getOffreDateFin() { return offreDateFin; }
    public void setOffreDateFin(String v) { this.offreDateFin = v; }
    public Long getOffreDomaineId() { return offreDomaineId; }
    public void setOffreDomaineId(Long v) { this.offreDomaineId = v; }
    public java.util.List<String> getOffreDocumentsRequis() { return offreDocumentsRequis; }
    public String getOffreNouveauDocument() { return offreNouveauDocument; }
    public void setOffreNouveauDocument(String v) { this.offreNouveauDocument = v; }
    public boolean isOffreModeEdition() { return offreModeEdition; }
    public Long   getOffreDateLimiteId()     { return offreDateLimiteId; }
    public void   setOffreDateLimiteId(Long v)   { this.offreDateLimiteId = v; }
    public String getOffreDateLimiteValeur() { return offreDateLimiteValeur; }
    public void   setOffreDateLimiteValeur(String v) { this.offreDateLimiteValeur = v; }

    public String getDomaineLibelle() { return domaineLibelle; }
    public void setDomaineLibelle(String v) { this.domaineLibelle = v; }
    public String getDomaineCode() { return domaineCode; }
    public void setDomaineCode(String v) { this.domaineCode = v; }
    public String getDomaineDescription() { return domaineDescription; }
    public void setDomaineDescription(String v) { this.domaineDescription = v; }

    public String getProfilNom() { return profilNom; }
    public void setProfilNom(String v) { this.profilNom = v; }
    public Long getProfilSelectionneId() { return profilSelectionneId; }
    public void setProfilSelectionneId(Long v) { this.profilSelectionneId = v; }
    public Long getPermissionSelectionneId() { return permissionSelectionneId; }
    public void setPermissionSelectionneId(Long v) { this.permissionSelectionneId = v; }
    public String getPermissionsSelectionneesIds() { return permissionsSelectionneesIds; }
    public void setPermissionsSelectionneesIds(String v) { this.permissionsSelectionneesIds = v; }

    public String getBrancheNom() { return brancheNom; }
    public void setBrancheNom(String v) { this.brancheNom = v; }
    public String getBrancheCode() { return brancheCode; }
    public void setBrancheCode(String v) { this.brancheCode = v; }
    public Long   getBrancheEditId()   { return brancheEditId; }
    public void   setBrancheEditId(Long v) { this.brancheEditId = v; }
    public String getBrancheEditNom()  { return brancheEditNom; }
    public void   setBrancheEditNom(String v) { this.brancheEditNom = v; }
    public String getBrancheEditCode() { return brancheEditCode; }
    public void   setBrancheEditCode(String v) { this.brancheEditCode = v; }

    public String getPermissionLibelle() { return permissionLibelle; }
    public void setPermissionLibelle(String v) { this.permissionLibelle = v; }
    public String getPermissionCode() { return permissionCode; }
    public void setPermissionCode(String v) { this.permissionCode = v; }
    public String getPermissionNiveau() { return permissionNiveau; }
    public void setPermissionNiveau(String v) { this.permissionNiveau = v; }
    public Long getPermissionBrancheId() { return permissionBrancheId; }
    public void setPermissionBrancheId(Long v) { this.permissionBrancheId = v; }

    // Reset mot de passe
    public void reinitialiserMotDePasse(Long id) {
        String tmp = userClient.reinitialiserMotDePasse(id, session.getToken());
        if (tmp != null) {
            motDePasseTemporaire = tmp;
            AuditBean.log("RESET_PASSWORD", "UTILISATEUR", "ID: " + id, session.getEmail(), "WARN");
            msg(true, "Mot de passe reinitialisé. Temporaire : " + tmp);
        } else {
            msg(false, "", "Echec reinitialisation du mot de passe.");
        }
    }

    public String getMotDePasseTemporaire() { return motDePasseTemporaire; }
    public void setMotDePasseTemporaire(String v) { this.motDePasseTemporaire = v; }

    // Historique par utilisateur
    public void chargerHistoriqueUtilisateur(Long userId, String nomPrenom) {
        this.historiqueutilisateurId = userId;
        this.historiqueNomUtilisateur = nomPrenom;
        this.historiquedemandes = demandes == null ? new java.util.ArrayList<>() :
            demandes.stream()
                .filter(d -> userId.equals(d.getUtilisateurId()))
                .collect(java.util.stream.Collectors.toList());
        this.historiqueCandidatures = candidatures == null ? new java.util.ArrayList<>() :
            candidatures.stream()
                .filter(c -> userId.equals(c.getUtilisateurId()))
                .collect(java.util.stream.Collectors.toList());
    }

    public Long getHistoriqueutilisateurId() { return historiqueutilisateurId; }
    public void setHistoriqueutilisateurId(Long v) { this.historiqueutilisateurId = v; }
    public List<DemandeFormation> getHistoriquedemandes() { return historiquedemandes; }
    public List<CandidatureFront> getHistoriqueCandidatures() { return historiqueCandidatures; }
    public String getHistoriqueNomUtilisateur() { return historiqueNomUtilisateur; }

    // Export CSV
    public void exporterCandidaturesCSV() {
        telechargerCSV(formationClient.exporterCandidaturesCSV(null, session.getToken()), "candidatures.csv");
    }

    public void exporterCandidaturesAccepteesCSV() {
        telechargerCSV(formationClient.exporterCandidaturesCSV("ACCEPTEE", session.getToken()), "candidatures-acceptees.csv");
    }

    public void exporterRapportCSV() {
        telechargerCSV(formationClient.exporterRapportCSV(session.getToken()), "rapport-activite.csv");
    }

    // ════════════════════════════════════════════════════
    // Demandes à la carte (admin)
    // ════════════════════════════════════════════════════

    public List<DemandeLibreFront> getDemandesLibres() { return demandesLibres; }

    public long countDemandesLibresParStatut(String statut) {
        if (demandesLibres == null) return 0;
        return demandesLibres.stream().filter(d -> statut.equals(d.getStatut())).count();
    }

    public void validerDemandeLibre(Long id) {
        boolean ok = formationClient.validerDemandeLibreAdmin(id, session.getToken());
        if (ok) {
            msg(true, "À la carte", "Demande validée.");
            try { demandesLibres = formationClient.toutesDemandesLibres(session.getToken()); }
            catch (Exception e) {}
        } else {
            msg(false, "À la carte", "Échec de la validation.");
        }
    }

    public void rejeterDemandeLibre(Long id) {
        boolean ok = formationClient.rejeterDemandeLibreAdmin(id, session.getToken());
        if (ok) {
            msg(true, "À la carte", "Demande rejetée.");
            try { demandesLibres = formationClient.toutesDemandesLibres(session.getToken()); }
            catch (Exception e) {}
        } else {
            msg(false, "À la carte", "Échec du rejet.");
        }
    }

    // ════════════════════════════════════════
    //  PROPOSITIONS DE FORMATION (A la carte)
    // ════════════════════════════════════════

    private Long   propDemandeLibreId;
    private String propFormateurNom;
    private java.time.LocalDate propDateDebut;
    private java.time.LocalDate propDateFin;
    private String propLieu;
    private Double propMontant;
    private String propProgramme;
    private String propNoteAdmin;
    private String propMode = "PRESENTIEL";
    private java.util.List<String> propModules = new java.util.ArrayList<>();
    private String propNouveauModule;

    public void ouvrirFormulaireProposition(Long demandeLibreId) {
        this.propDemandeLibreId = demandeLibreId;
        // Also load detail for the summary shown in the proposition dialog
        this.demandeLibreSelectee = demandesLibres == null ? null
            : demandesLibres.stream().filter(d -> demandeLibreId.equals(d.getId())).findFirst().orElse(null);
        this.propFormateurNom = null;
        this.propDateDebut = null;
        this.propDateFin = null;
        this.propLieu = null;
        this.propMontant = null;
        this.propProgramme = null;
        this.propNoteAdmin = null;
        this.propMode = "PRESENTIEL";
        this.propModules = new java.util.ArrayList<>();
        this.propNouveauModule = null;
    }

    public void soumettreProposition() {
        if (propDemandeLibreId == null) { msg(false, "Proposition", "Demande non selectionnee."); return; }
        if (propMontant == null || propMontant <= 0) { msg(false, "Proposition", "Montant invalide."); return; }
        com.senexpert.frontend.model.PropositionFormationFront p = formationClient.creerProposition(
                propDemandeLibreId, propFormateurNom,
                (propDateDebut != null ? propDateDebut.toString() : null),
                (propDateFin   != null ? propDateFin.toString()   : null),
                propLieu, propMode, propMontant, propProgramme, propNoteAdmin,
                propModules, session.getToken());
        if (p != null) {
            msg(true, "Proposition", "Proposition envoyee avec succes !");
            propDemandeLibreId = null;
        } else {
            msg(false, "Proposition", "Echec de l'envoi de la proposition.");
        }
    }

    public Long   getPropDemandeLibreId()       { return propDemandeLibreId; }
    public void   setPropDemandeLibreId(Long v) { this.propDemandeLibreId = v; }
    public String getPropFormateurNom()         { return propFormateurNom; }
    public void   setPropFormateurNom(String v) { this.propFormateurNom = v; }
    public java.time.LocalDate getPropDateDebut()                    { return propDateDebut; }
    public void   setPropDateDebut(java.time.LocalDate v)            { this.propDateDebut = v; }
    public java.time.LocalDate getPropDateFin()                      { return propDateFin; }
    public void   setPropDateFin(java.time.LocalDate v)              { this.propDateFin = v; }
    public String getPropLieu()                 { return propLieu; }
    public void   setPropLieu(String v)         { this.propLieu = v; }
    public Double getPropMontant()              { return propMontant; }
    public void   setPropMontant(Double v)      { this.propMontant = v; }
    public String getPropProgramme()            { return propProgramme; }
    public void   setPropProgramme(String v)    { this.propProgramme = v; }
    public String getPropNoteAdmin()            { return propNoteAdmin; }
    public void   setPropNoteAdmin(String v)    { this.propNoteAdmin = v; }


    private void telechargerCSV(byte[] data, String filename) {
        if (data == null) { msg(false, "Export", "Aucune donnée à exporter."); return; }
        try {
            jakarta.faces.context.FacesContext fc = jakarta.faces.context.FacesContext.getCurrentInstance();
            jakarta.servlet.http.HttpServletResponse response =
                    (jakarta.servlet.http.HttpServletResponse) fc.getExternalContext().getResponse();
            response.setContentType("text/csv;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
            response.setContentLength(data.length);
            try (java.io.OutputStream os = response.getOutputStream()) { os.write(data); }
            fc.responseComplete();
        } catch (Exception e) { e.printStackTrace(); }
    }


    private String motifRejet;
    private DemandeLibreFront demandeLibreSelectee;

    public DemandeLibreFront getDemandeLibreSelectee() { return demandeLibreSelectee; }
    public void setDemandeLibreSelectee(DemandeLibreFront v) { this.demandeLibreSelectee = v; }

    public void ouvrirDetailDemandeLibre(Long id) {
        demandeLibreSelectee = demandesLibres == null ? null
            : demandesLibres.stream().filter(d -> id.equals(d.getId())).findFirst().orElse(null);
    }
    public String getMotifRejet() { return motifRejet; }
    public void   setMotifRejet(String v) { this.motifRejet = v; }

    public String getPropMode()           { return propMode; }
    public void   setPropMode(String v)   { this.propMode = v; }
    public java.util.List<String> getPropModules()       { return propModules; }
    public void   setPropModules(java.util.List<String> v) { this.propModules = v; }
    public String getPropNouveauModule()  { return propNouveauModule; }
    public void   setPropNouveauModule(String v) { this.propNouveauModule = v; }

    public void ajouterModuleProposition() {
        if (propNouveauModule != null && !propNouveauModule.isBlank()) {
            if (propModules == null) propModules = new java.util.ArrayList<>();
            propModules.add(propNouveauModule.trim());
            propNouveauModule = null;
        }
    }

    public void supprimerModuleProposition(int index) {
        if (propModules != null && index >= 0 && index < propModules.size())
            propModules.remove(index);
    }

    // ─── Permissions directes par utilisateur ─────────────────────────────────

    private Long   permUserTargetId;
    private String permUserTargetNom;
    private String permissionsDirectesUtilisateurIds = "";

    /** Ouvre le dialog de gestion des permissions directes pour un utilisateur.
     *  Pré-coche toutes les permissions effectives (profil + directes). */
    public void ouvrirGestionPermissionsUser(Utilisateur u) {
        permUserTargetId  = u.getId();
        permUserTargetNom = u.getPrenom() + " " + u.getNom();

        java.util.LinkedHashSet<String> ids = new java.util.LinkedHashSet<>();
        // 1) Permissions héritées du profil
        if (u.getProfil() != null && u.getProfil().getPermissions() != null) {
            u.getProfil().getPermissions().forEach(p -> {
                if (p.getId() != null) ids.add(p.getId().toString());
            });
        }
        // 2) Permissions directes déjà assignées
        userClient.getPermissionsUtilisateur(u.getId(), session.getToken())
                  .forEach(id -> ids.add(id.toString()));

        permissionsDirectesUtilisateurIds = String.join(",", ids);
    }

    /** Enregistre les permissions directes (remplace l'ensemble existant). */
    public void sauvegarderPermissionsUser() {
        java.util.List<Long> ids = new java.util.ArrayList<>();
        if (permissionsDirectesUtilisateurIds != null && !permissionsDirectesUtilisateurIds.isBlank()) {
            for (String s : permissionsDirectesUtilisateurIds.split(",")) {
                try { ids.add(Long.parseLong(s.trim())); } catch (Exception ignored) {}
            }
        }
        boolean ok = userClient.assignerPermissionsDirectes(permUserTargetId, ids, session.getToken());
        msg(ok, "Permissions mises à jour.", "Échec mise à jour des permissions.");
        if (ok) personnes = userClient.listerUtilisateurs(session.getToken());
    }

    public Long   getPermUserTargetId()                      { return permUserTargetId; }
    public void   setPermUserTargetId(Long v)               { this.permUserTargetId = v; }
    public String getPermUserTargetNom()                     { return permUserTargetNom; }
    public void   setPermUserTargetNom(String v)            { this.permUserTargetNom = v; }
    public String getPermissionsDirectesUtilisateurIds()     { return permissionsDirectesUtilisateurIds; }
    public void   setPermissionsDirectesUtilisateurIds(String v) { this.permissionsDirectesUtilisateurIds = v; }

    // ── KPI getters utilisés dans dashboard.xhtml ──────────────────────────
    public long getNbOffresDisponibles()      { return offres == null ? 0 : offres.stream().filter(o -> "DISPONIBLE".equals(o.getStatut())).count(); }
    public long getNbOffresBrouillon()        { return offres == null ? 0 : offres.stream().filter(o -> "BROUILLON".equals(o.getStatut())).count(); }
    public long getNbOffresFermees()          { return offres == null ? 0 : offres.stream().filter(o -> "FERME".equals(o.getStatut())).count(); }
    public long getNbDemandesOffreEnAttente() { return demandesOffre == null ? 0 : demandesOffre.stream().filter(d -> "EN_ATTENTE".equals(d.getStatut())).count(); }
    public long getNbDemandesOffreValidees()  { return demandesOffre == null ? 0 : demandesOffre.stream().filter(d -> "VALIDEE".equals(d.getStatut())).count(); }
    public long getNbDemandesLibresRejetees() { return countDemandesLibresParStatut("REJETEE"); }
    public long getNbFormationsSoumises()     { return formationsSoumises == null ? 0 : formationsSoumises.size(); }
    public java.util.List<DemandeFormation> getDemandesOffre() { return demandesOffre; }
    public java.util.List<Offre> getFormationsSoumises() { return formationsSoumises; }
    public long getNbPersonnesEnAttente()     { return countPersonnesParStatut("EN_ATTENTE"); }
    public long getNbPersonnes()              { return personnes == null ? 0 : personnes.size(); }
    public long getNbPersonnesActives()       { return personnes == null ? 0 : personnes.stream().filter(u -> "ACTIF".equals(u.getStatut())).count(); }
    public long getNbOffres()                 { return offres == null ? 0 : offres.size(); }
    public long getNbDemandes()               { return demandes == null ? 0 : demandes.size(); }
    public long getNbDemandesEnAttenteCnt()   { return countDemandesParStatut("EN_ATTENTE"); }
    public long getNbCandidatures()           { return candidatures == null ? 0 : candidatures.size(); }
    public long getNbInstitutions()           { return institutions == null ? 0 : institutions.size(); }
    public long getNbInstitutionsEnAttente()  { return institutions == null ? 0 : institutions.stream().filter(i -> "EN_ATTENTE".equals(i.getStatut())).count(); }
    public long getNbDemandesLibres()         { return demandesLibres == null ? 0 : demandesLibres.size(); }
    public long getNbDemandesLibresEnAttente(){ return countDemandesLibresParStatut("EN_ATTENTE"); }
    public long getNbDemandesLibresValidees() { return countDemandesLibresParStatut("VALIDEE"); }


}
