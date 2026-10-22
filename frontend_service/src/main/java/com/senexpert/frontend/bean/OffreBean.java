package com.senexpert.frontend.bean;

import com.senexpert.frontend.client.FormationServiceClient;
import com.senexpert.frontend.client.UserServiceClient;
import com.senexpert.frontend.model.DemandeFormation;
import com.senexpert.frontend.model.DemandeLibreFront;
import com.senexpert.frontend.model.Domaine;
import com.senexpert.frontend.model.OffreFormation;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Named
@ViewScoped
public class OffreBean implements Serializable {

    @Inject private FormationServiceClient formationClient;
    @Inject private UserServiceClient      userClient;
    @Inject private SessionBean            session;

    // ── Formations ───────────────────────────────────────
    private List<OffreFormation>   offres;
    private List<Domaine>          domaines;
    private List<DemandeFormation> mesDemandes;
    private Long                   domaineFiltre;

    // ── Dialog de candidature ─────────────────────────────
    private OffreFormation          offreSelectionnee;
    private List<UploadedFile>      documentsPostulation = new ArrayList<>();
    private List<String>            nomsDocuments        = new ArrayList<>();

    // ── Dialog détails ────────────────────────────────────
    private OffreFormation          offreDetail;

    // ── IDs des offres déjà postulées (value expression, pas d'appel paramétré en EL) ──
    private java.util.Set<Long>     idsPostules = new java.util.HashSet<>();

    // ── Formations à la carte ─────────────────────────────
    private List<DemandeLibreFront> mesDemandesLibres;
    private String  libreDomaine;
    private String  libreMode        = "EN_LIGNE";
    private String  libreIntitule;
    private String  libreDescription;
    private Integer libreNbParticipants;
    private String  librePeriodeDebut;
    private String  librePeriodeFin;

    // ── Profil – édition ──────────────────────────────────
    private String  editNom;
    private String  editPrenom;
    private String  editTelephone;
    private boolean profilEnEdition = false;

    // ── Profil – mot de passe ─────────────────────────────
    private String  ancienMdp;
    private String  nouveauMdp;
    private String  confirmerMdp;
    private boolean mdpEnEdition    = false;

    // ════════════════════════════════════════════════════
    // Init
    // ════════════════════════════════════════════════════

    @PostConstruct
    public void init() {
        try { domaines = formationClient.listerDomaines(); } catch (Exception e) { domaines = new ArrayList<>(); }
        try { offres   = formationClient.listerOffres();   } catch (Exception e) { offres   = new ArrayList<>(); }

        if (session.isConnecte() && session.getUtilisateurId() != null) {
            try { mesDemandes = formationClient.mesDemandes(session.getUtilisateurId(), session.getToken()); }
            catch (Exception e) { mesDemandes = new ArrayList<>(); }
            try { mesDemandesLibres = formationClient.mesDemandesLibres(session.getUtilisateurId(), session.getToken()); }
            catch (Exception e) { mesDemandesLibres = new ArrayList<>(); }
        } else {
            mesDemandes       = new ArrayList<>();
            mesDemandesLibres = new ArrayList<>();
        }
        refreshIdsPostules();

        prefillProfil();
    }

    private void prefillProfil() {
        String nc = session.getNomComplet();
        if (nc != null && !nc.isBlank()) {
            String[] p = nc.trim().split(" ", 2);
            editPrenom = p.length > 0 ? p[0] : "";
            editNom    = p.length > 1 ? p[1] : "";
        }
    }

    // ════════════════════════════════════════════════════
    // Formations
    // ════════════════════════════════════════════════════

    public void filtrerParDomaine() {
        offres = domaineFiltre == null
                ? formationClient.listerOffres()
                : formationClient.listerOffresParDomaine(domaineFiltre);
    }

    // ════════════════════════════════════════════════════
    // Dialog de candidature
    // ════════════════════════════════════════════════════

    /** Ouvre le dialog de candidature pour une offre donnée. */
    public void ouvrirDialogPostuler(OffreFormation offre) {
        this.offreSelectionnee    = offre;
        this.documentsPostulation = new ArrayList<>();
        this.nomsDocuments        = new ArrayList<>();
    }

    /** Ouvre le dialog de détail pour une offre donnée (lecture seule). */
    public void ouvrirDetail(OffreFormation offre) {
        this.offreSelectionnee = offre;
    }

    /** Appelé par p:fileUpload à chaque ajout de fichier. */
    public void handleDocumentUpload(FileUploadEvent event) {
        UploadedFile fichier = event.getFile();
        if (fichier != null && fichier.getSize() > 0) {
            documentsPostulation.add(fichier);
            nomsDocuments.add(fichier.getFileName());
        }
    }

    /** Supprime un document de la liste avant soumission. */
    public void supprimerDocument(int index) {
        if (index >= 0 && index < documentsPostulation.size()) {
            documentsPostulation.remove(index);
            nomsDocuments.remove(index);
        }
    }

    /** Soumet la candidature avec les documents. */
    public void postuler() {
        if (!session.isConnecte())    { msg(false, "Connexion requise.");                 return; }
        if (offreSelectionnee == null){ msg(false, "Aucune offre sélectionnée.");          return; }
        if (documentsPostulation.isEmpty()) { msg(false, "Veuillez joindre au moins un document (CV, diplôme…)."); return; }

        // 1. Upload des fichiers
        List<String> chemins = formationClient.uploadDocumentsDemande(
                session.getUtilisateurId(), documentsPostulation, session.getToken());

        // 2. Soumission de la demande avec les chemins
        String erreur = formationClient.soumettreDemande(
                session.getUtilisateurId(), offreSelectionnee.getId(), chemins, session.getToken());

        if (erreur == null) {
            msg(true, "Candidature soumise ! Vos documents ont été transmis. En attente de validation.");
            mesDemandes       = formationClient.mesDemandes(session.getUtilisateurId(), session.getToken());
            offreSelectionnee = null;
            documentsPostulation.clear();
            nomsDocuments.clear();
        } else {
            msg(false, erreur);
        }
    }

    // ════════════════════════════════════════════════════
    // Annuler une demande classique (EN_ATTENTE)
    // ════════════════════════════════════════════════════

    public void annulerDemande(Long id) {
        boolean ok = formationClient.annulerDemande(id, session.getToken());
        if (ok) {
            msg(true, "Demande annulée.");
            mesDemandes = formationClient.mesDemandes(session.getUtilisateurId(), session.getToken());
            refreshIdsPostules();
        } else {
            msg(false, "Impossible d'annuler cette demande.");
        }
    }

    // ════════════════════════════════════════════════════
    // Formations à la carte
    // ════════════════════════════════════════════════════

    public void soumettreDemandeLibre() {
        if (!session.hasPermission("5015")) { msg(false, "Vous n'avez pas la permission de soumettre une demande."); return; }
        if (libreDomaine == null || libreDomaine.isBlank()) { msg(false, "Domaine requis."); return; }
        if (libreDescription == null || libreDescription.isBlank()) { msg(false, "Description requise."); return; }
        String intitule = libreIntitule != null && !libreIntitule.isBlank() ? libreIntitule : libreDomaine;
        String desc = "[" + (libreMode != null ? libreMode : "EN_LIGNE") + "] " + intitule + " — " + libreDescription;
        String erreur = formationClient.soumettreDemandeLibre(
                session.getUtilisateurId(),
                session.getNomComplet() != null ? session.getNomComplet() : session.getEmail(),
                libreDomaine, desc, libreNbParticipants,
                librePeriodeDebut, librePeriodeFin, session.getToken());
        if (erreur == null) {
            msg(true, "Demande à la carte soumise !");
            mesDemandesLibres = formationClient.mesDemandesLibres(session.getUtilisateurId(), session.getToken());
            libreDomaine = null; libreIntitule = null; libreDescription = null;
            libreNbParticipants = null; librePeriodeDebut = null; librePeriodeFin = null;
        } else {
            msg(false, erreur);
        }
    }

    public void annulerDemandeLibre(Long id) {
        boolean ok = formationClient.annulerDemandeLibre(id, session.getToken());
        if (ok) {
            msg(true, "Demande à la carte annulée.");
            mesDemandesLibres = formationClient.mesDemandesLibres(session.getUtilisateurId(), session.getToken());
        } else {
            msg(false, "Impossible d'annuler.");
        }
    }

    // ════════════════════════════════════════════════════
    // Profil
    // ════════════════════════════════════════════════════

    public void toggleProfilEdition() { profilEnEdition = !profilEnEdition; mdpEnEdition = false; }
    public void toggleMdpEdition()    { mdpEnEdition = !mdpEnEdition; profilEnEdition = false; }

    public void sauvegarderProfil() {
        if (editNom == null || editNom.isBlank() || editPrenom == null || editPrenom.isBlank()) {
            msg(false, "Nom et prénom obligatoires."); return;
        }
        boolean ok = userClient.modifierProfil(
                session.getUtilisateurId(), editNom.trim(), editPrenom.trim(),
                editTelephone, session.getToken());
        if (ok) {
            session.setNomComplet(editPrenom.trim() + " " + editNom.trim());
            msg(true, "Profil mis à jour.");
            profilEnEdition = false;
        } else {
            msg(false, "Échec de la mise à jour.");
        }
    }

    public void changerMotDePasse() {
        if (ancienMdp == null || ancienMdp.isBlank())      { msg(false, "Ancien mot de passe requis."); return; }
        if (nouveauMdp == null || nouveauMdp.length() < 6) { msg(false, "Nouveau mot de passe : 6 caractères min."); return; }
        if (!nouveauMdp.equals(confirmerMdp))               { msg(false, "Les mots de passe ne correspondent pas."); return; }
        boolean ok = userClient.changerMotDePasse(
                session.getUtilisateurId(), ancienMdp, nouveauMdp, session.getToken());
        if (ok) {
            msg(true, "Mot de passe modifié.");
            ancienMdp = null; nouveauMdp = null; confirmerMdp = null;
            mdpEnEdition = false;
        } else {
            msg(false, "Ancien mot de passe incorrect.");
        }
    }

    // ════════════════════════════════════════════════════
    // Stats
    // ════════════════════════════════════════════════════

    public List<OffreFormation> getOffresTop() {
        if (offres == null || offres.isEmpty()) return List.of();
        return offres.subList(0, Math.min(2, offres.size()));
    }

    public List<DemandeFormation> getDemandesRecentes() {
        if (mesDemandes == null || mesDemandes.isEmpty()) return List.of();
        return mesDemandes.subList(0, Math.min(3, mesDemandes.size()));
    }

    /** Demandes actives uniquement (EN_ATTENTE ou VALIDEE) — pour le stepper du tableau de bord. */
    public List<DemandeFormation> getDemandesEnCours() {
        if (mesDemandes == null) return List.of();
        return mesDemandes.stream()
                .filter(d -> "EN_ATTENTE".equals(d.getStatut()) || "VALIDEE".equals(d.getStatut()))
                .collect(java.util.stream.Collectors.toList());
    }

    public List<DemandeFormation> getDemandesNotifs() {
        if (mesDemandes == null || mesDemandes.isEmpty()) return List.of();
        return mesDemandes.subList(0, Math.min(5, mesDemandes.size()));
    }

    /** Retourne true si l'utilisateur connecté a déjà postulé à cette offre (quel que soit le statut). */
    public boolean dejaPostule(Long offreId) {
        if (mesDemandes == null || offreId == null) return false;
        return mesDemandes.stream()
                .anyMatch(d -> d.getOffre() != null && offreId.equals(d.getOffre().getId()));
    }

    /** Inverse de dejaPostule — évite l'opérateur ! sur un appel paramétré en EL. */
    public boolean peutPostuler(Long offreId) {
        return !dejaPostule(offreId);
    }

    /** Recalcule le Set des IDs d'offres postulées (value expression safe pour EL). */
    private void refreshIdsPostules() {
        idsPostules = new java.util.HashSet<>();
        if (mesDemandes != null) {
            for (DemandeFormation d : mesDemandes) {
                if (d.getOffre() != null && d.getOffre().getId() != null) {
                    idsPostules.add(d.getOffre().getId());
                }
            }
        }
    }

    /** Set des IDs d'offres où l'utilisateur a déjà postulé — utilisable avec .contains() en EL. */
    public java.util.Set<Long> getIdsPostules() { return idsPostules; }

    public long getNbDemandesEnAttente() {
        if (mesDemandes == null) return 0;
        return mesDemandes.stream().filter(d -> "EN_ATTENTE".equals(d.getStatut())).count();
    }

    public long getNbDemandesValidees() {
        if (mesDemandes == null) return 0;
        return mesDemandes.stream().filter(d -> "VALIDEE".equals(d.getStatut())).count();
    }

    public int getNbDemandes()       { return mesDemandes == null ? 0 : mesDemandes.size(); }
    public int getNbOffres()         { return offres == null ? 0 : offres.size(); }
    public int getNbDemandesLibres() { return mesDemandesLibres == null ? 0 : mesDemandesLibres.size(); }

    // ════════════════════════════════════════════════════
    // Helper
    // ════════════════════════════════════════════════════

    private void msg(boolean ok, String text) {
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(ok ? FacesMessage.SEVERITY_INFO : FacesMessage.SEVERITY_ERROR,
                             ok ? "Succès" : "Erreur", text));
    }

    // ════════════════════════════════════════════════════
    // Getters / Setters
    // ════════════════════════════════════════════════════

    public List<OffreFormation>    getOffres()            { return offres; }
    public List<Domaine>           getDomaines()          { return domaines; }
    public List<DemandeFormation>  getMesDemandes()       { return mesDemandes; }
    public List<DemandeLibreFront> getMesDemandesLibres() { return mesDemandesLibres; }
    public Long  getDomaineFiltre()        { return domaineFiltre; }
    public void  setDomaineFiltre(Long v)  { this.domaineFiltre = v; }

    public OffreFormation       getOffreSelectionnee()  { return offreSelectionnee; }
    public OffreFormation       getOffreDetail()         { return offreDetail; }
    public List<String>         getNomDocuments()        { return nomsDocuments; }
    public int                  getNbDocuments()         { return nomsDocuments.size(); }

    public void voirDetail(Long id) {
        offreDetail = formationClient.trouverOffreParId(id);
    }

    public String  getLibreDomaine()          { return libreDomaine; }
    public void    setLibreDomaine(String v)  { this.libreDomaine = v; }
    public String  getLibreMode()             { return libreMode; }
    public void    setLibreMode(String v)     { this.libreMode = v; }
    public String  getLibreIntitule()         { return libreIntitule; }
    public void    setLibreIntitule(String v) { this.libreIntitule = v; }
    public String  getLibreDescription()      { return libreDescription; }
    public void    setLibreDescription(String v){ this.libreDescription = v; }
    public Integer getLibreNbParticipants()   { return libreNbParticipants; }
    public void    setLibreNbParticipants(Integer v){ this.libreNbParticipants = v; }
    public String  getLibrePeriodeDebut()     { return librePeriodeDebut; }
    public void    setLibrePeriodeDebut(String v){ this.librePeriodeDebut = v; }
    public String  getLibrePeriodeFin()       { return librePeriodeFin; }
    public void    setLibrePeriodeFin(String v)  { this.librePeriodeFin = v; }

    public String  getEditNom()          { return editNom; }
    public void    setEditNom(String v)  { this.editNom = v; }
    public String  getEditPrenom()       { return editPrenom; }
    public void    setEditPrenom(String v){ this.editPrenom = v; }
    public String  getEditTelephone()    { return editTelephone; }
    public void    setEditTelephone(String v){ this.editTelephone = v; }
    public boolean isProfilEnEdition()   { return profilEnEdition; }
    public boolean isMdpEnEdition()      { return mdpEnEdition; }
    public String  getAncienMdp()        { return ancienMdp; }
    public void    setAncienMdp(String v){ this.ancienMdp = v; }
    public String  getNouveauMdp()       { return nouveauMdp; }
    public void    setNouveauMdp(String v){ this.nouveauMdp = v; }
    public String  getConfirmerMdp()     { return confirmerMdp; }
    public void    setConfirmerMdp(String v){ this.confirmerMdp = v; }

    // ── PostulesMap — EL value-expression safe wrapper ───────────────────────
    /** Permet #{offreBean.postulesMap[offre.id]} sans appel de méthode en EL. */
    public class PostulesMap extends java.util.HashMap<Long, Boolean> {
        @Override
        public Boolean get(Object key) {
            return idsPostules.contains(key);
        }
    }
    private final PostulesMap postulesMap = new PostulesMap();
    public PostulesMap getPostulesMap() { return postulesMap; }
}
