package com.senexpert.frontend.bean;

import com.senexpert.frontend.client.FormationServiceClient;
import com.senexpert.frontend.model.CandidatureFront;
import com.senexpert.frontend.model.Domaine;
import com.senexpert.frontend.model.FormateurFront;
import com.senexpert.frontend.model.ModuleFront;
import com.senexpert.frontend.model.Offre;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Bean gérant l'espace "Mes formations" pour les prestataires (AGENT ou PARTICULIER).
 * Chaque prestataire peut créer des formations en brouillon, y ajouter des modules
 * avec un formateur assigné, puis soumettre à l'admin pour publication.
 */
@Named
@ViewScoped
public class PrestataireBean implements Serializable {

    @Inject private FormationServiceClient formationClient;
    @Inject private SessionBean session;

    // ── Mes formations ───────────────────────────────────────────────────────
    private List<Offre> mesFormations = new ArrayList<>();
    private List<Domaine> domaines    = new ArrayList<>();
    private List<FormateurFront> formateurs = new ArrayList<>();

    // ── Candidatures agrégées (toutes formations confondues) ─────────────────
    private List<CandidatureFront> toutesCandidaturesEnAttente = new ArrayList<>();
    private List<CandidatureFront> toutesCandidaturesValidees  = new ArrayList<>();

    // ── Formulaire nouvelle formation ────────────────────────────────────────
    private String  fTitre, fDescription, fType = "EN_LIGNE", fNiveau, fDuree;
    private String  fDateDebut, fDateFin;
    private Integer fPlaces;
    private Double  fCout;
    private Long    fDomaineId;
    private String  fProgrammeUrl;
    // Nouveaux champs
    private String  fPrerequis;
    private String  fLieu;
    private String  fImageUrl;
    private String  fLangue;
    private String  fPublicCible;
    private String  fModalitesPaiement;
    private String  fFinancementDisponible;
    private String  fCertification;

    private boolean modeEditionFormation = false;
    private Long    formationEnCoursId;

    // ── Modules de l'offre sélectionnée ─────────────────────────────────────
    private Offre offreSelectionnee;
    private List<ModuleFront> modules = new ArrayList<>();

    // ── Candidatures de l'offre sélectionnée ────────────────────────────────
    private Offre offreCandidatures;
    private List<CandidatureFront> candidatures = new ArrayList<>();

    // ── Inscrire un membre (AGENT uniquement) ────────────────────────────────
    private Long membreAInscrireId;

    // ── Formulaire module ────────────────────────────────────────────────────
    private String mTitre, mDescription;
    private Integer mDureeHeures, mOrdre;
    private Long    mFormateurId;
    private boolean modeEditionModule = false;
    private Long    moduleEnCoursId;

    @PostConstruct
    public void init() {
        charger();
    }

    public void charger() {
        Long uid = session.getUtilisateurId();
        String token = session.getToken();
        if (uid == null) return;
        try { mesFormations = formationClient.mesFormationsPrestataire(uid, token); }
        catch (Exception e) { mesFormations = new ArrayList<>(); }
        try { domaines    = formationClient.listerDomaines(); }
        catch (Exception e) { domaines = new ArrayList<>(); }
        try { formateurs  = formationClient.listerFormateurs(); }
        catch (Exception e) { formateurs = new ArrayList<>(); }
        chargerToutesCandidatures();
    }

    /** Charge toutes les candidatures EN_ATTENTE et ACCEPTEE sur toutes les formations du prestataire. */
    private void chargerToutesCandidatures() {
        toutesCandidaturesEnAttente = new ArrayList<>();
        toutesCandidaturesValidees  = new ArrayList<>();
        String tok = session.getToken();
        if (mesFormations == null) return;
        for (Offre o : mesFormations) {
            try {
                List<CandidatureFront> cands = formationClient.candidaturesParOffre(o.getId(), tok);
                if (cands == null) continue;
                for (CandidatureFront c : cands) {
                    if ("EN_ATTENTE".equals(c.getStatut())) toutesCandidaturesEnAttente.add(c);
                    else if ("ACCEPTEE".equals(c.getStatut()) || "VALIDEE".equals(c.getStatut()))
                        toutesCandidaturesValidees.add(c);
                }
            } catch (Exception ignored) {}
        }
    }

    // ── CRUD Formation ───────────────────────────────────────────────────────

    public void creerFormation() {
        String token = session.getToken();
        Long uid  = session.getUtilisateurId();
        String role = session.getRole();
        Offre created = formationClient.creerOffrePrestataire(
                fTitre, fDescription, fDateDebut, fDateFin,
                fPlaces, fType, fDomaineId, fNiveau, fDuree, fCout,
                uid, role, fProgrammeUrl, token,
                fPrerequis, fLieu, fImageUrl, fLangue,
                fPublicCible, fModalitesPaiement, fFinancementDisponible, fCertification);
        if (created != null) {
            msg(true, "Formation créée en brouillon.");
            resetFormation();
            charger();
        } else {
            msg(false, "Échec de la création.");
        }
    }

    public void ouvrirEditionFormation(Offre o) {
        modeEditionFormation = true;
        formationEnCoursId = o.getId();
        fTitre        = o.getTitre();
        fDescription  = o.getDescription();
        fDateDebut    = o.getDateDebut();
        fDateFin      = o.getDateFin();
        fPlaces       = o.getPlaces();
        fType         = o.getType() != null ? o.getType() : "EN_LIGNE";
        fNiveau       = o.getNiveau();
        fDuree        = o.getDuree();
        fCout         = o.getCout();
        fDomaineId             = o.getDomaine() != null ? o.getDomaine().getId() : null;
        fProgrammeUrl          = o.getProgrammeUrl();
        fPrerequis             = o.getPrerequis();
        fLieu                  = o.getLieu();
        fImageUrl              = o.getImageUrl();
        fLangue                = o.getLangue();
        fPublicCible           = o.getPublicCible();
        fModalitesPaiement     = o.getModalitesPaiement();
        fFinancementDisponible = o.getFinancementDisponible();
        fCertification         = o.getCertification();
    }

    public void modifierFormation() {
        boolean ok = formationClient.modifierOffreComplete(
                formationEnCoursId, fTitre, fDescription, fDateDebut, fDateFin,
                fPlaces, fType, fDomaineId, fNiveau, fDuree, fCout, null, fProgrammeUrl, session.getToken(),
                fPrerequis, fLieu, fImageUrl, fLangue,
                fPublicCible, fModalitesPaiement, fFinancementDisponible, fCertification);
        if (ok) { msg(true, "Formation modifiée."); resetFormation(); charger(); }
        else msg(false, "Échec modification.");
    }

    public void supprimerFormation(Long id) {
        boolean ok = formationClient.supprimerOffre(id, session.getToken());
        if (ok) { msg(true, "Formation supprimée."); charger(); }
        else msg(false, "Impossible de supprimer — vérifiez qu'elle n'est pas soumise.");
    }

    public void soumettreFormation(Long offreId) {
        boolean ok = formationClient.soumettreFormation(offreId, session.getUtilisateurId(), session.getToken());
        if (ok) { msg(true, "Formation soumise à l'administrateur pour validation."); charger(); }
        else msg(false, "Échec de la soumission.");
    }

    public void annulerSoumission(Long offreId) {
        boolean ok = formationClient.annulerSoumissionFormation(offreId, session.getUtilisateurId(), session.getToken());
        if (ok) { msg(true, "Formation remise en brouillon."); charger(); }
        else msg(false, "Échec annulation soumission.");
    }

    private void resetFormation() {
        modeEditionFormation = false; formationEnCoursId = null;
        fTitre = null; fDescription = null; fDateDebut = null; fDateFin = null;
        fPlaces = null; fType = "EN_LIGNE"; fNiveau = null; fDuree = null;
        fCout = null; fDomaineId = null; fProgrammeUrl = null;
        fPrerequis = null; fLieu = null; fImageUrl = null; fLangue = null;
        fPublicCible = null; fModalitesPaiement = null; fFinancementDisponible = null; fCertification = null;
    }

    // ── Modules ──────────────────────────────────────────────────────────────

    public void ouvrirModules(Offre o) {
        this.offreSelectionnee = o;
        chargerModules(o.getId());
    }

    public void chargerModules(Long offreId) {
        try { modules = formationClient.modulesParOffre(offreId, session.getToken()); }
        catch (Exception e) { modules = new ArrayList<>(); }
    }

    public void ajouterModule() {
        if (offreSelectionnee == null) { msg(false, "Aucune formation sélectionnée."); return; }
        ModuleFront m = formationClient.creerModule(
                mTitre, mDescription, mDureeHeures,
                mOrdre != null ? mOrdre : (modules.size() + 1),
                offreSelectionnee.getId(), mFormateurId, session.getToken());
        if (m != null) { msg(true, "Module ajouté."); resetModule(); chargerModules(offreSelectionnee.getId()); }
        else msg(false, "Échec ajout module.");
    }

    public void ouvrirEditionModule(ModuleFront m) {
        modeEditionModule = true;
        moduleEnCoursId = m.getId();
        mTitre       = m.getTitre();
        mDescription = m.getDescription();
        mDureeHeures = m.getDureeHeures();
        mOrdre       = m.getOrdre();
        mFormateurId = m.getFormateur() != null ? m.getFormateur().getId() : null;
    }

    public void modifierModule() {
        if (offreSelectionnee == null) return;
        boolean ok = formationClient.modifierModule(
                moduleEnCoursId, mTitre, mDescription, mDureeHeures,
                mOrdre, offreSelectionnee.getId(), mFormateurId, session.getToken());
        if (ok) { msg(true, "Module modifié."); resetModule(); chargerModules(offreSelectionnee.getId()); }
        else msg(false, "Échec modification module.");
    }

    public void supprimerModule(Long id) {
        boolean ok = formationClient.supprimerModule(id, session.getToken());
        if (ok && offreSelectionnee != null) { chargerModules(offreSelectionnee.getId()); }
        else if (!ok) msg(false, "Échec suppression module.");
    }

    public void resetModule() {
        mTitre = null; mDescription = null; mDureeHeures = null; mOrdre = null;
        mFormateurId = null; modeEditionModule = false; moduleEnCoursId = null;
    }

    public void assignerFormateurModule(Long moduleId) {
        if (mFormateurId == null) { msg(false, "Sélectionnez un formateur."); return; }
        boolean ok = formationClient.assignerFormateurModule(moduleId, mFormateurId, session.getToken());
        if (ok && offreSelectionnee != null) chargerModules(offreSelectionnee.getId());
        else if (!ok) msg(false, "Échec assignation formateur.");
    }

    // ── Inscrire un membre ────────────────────────────────────────────────────

    public void ouvrirInscrireMembre(Offre o) {
        this.offreSelectionnee = o;
        this.membreAInscrireId = null;
    }

    public void inscrireMembre() {
        if (offreSelectionnee == null || membreAInscrireId == null) {
            msg(false, "Sélectionnez un membre.");
            return;
        }
        String erreur = formationClient.soumettreDemande(
                membreAInscrireId, offreSelectionnee.getId(), null, session.getToken());
        if (erreur == null) {
            msg(true, "Membre inscrit avec succès à la formation.");
            membreAInscrireId = null;
        } else {
            msg(false, "Échec : " + erreur);
        }
    }

    public Long getMembreAInscrireId()    { return membreAInscrireId; }
    public void setMembreAInscrireId(Long v) { this.membreAInscrireId = v; }

    // ── Candidatures ─────────────────────────────────────────────────────────

    public void ouvrirCandidatures(Offre o) {
        this.offreCandidatures = o;
        chargerCandidatures(o.getId());
    }

    public void chargerCandidatures(Long offreId) {
        try { candidatures = formationClient.candidaturesParOffre(offreId, session.getToken()); }
        catch (Exception e) { candidatures = new ArrayList<>(); }
    }

    public void accepterCandidature(Long id) {
        boolean ok = formationClient.accepterCandidature(id, session.getToken());
        if (ok) {
            msg(true, "Candidature acceptée.");
            if (offreCandidatures != null) chargerCandidatures(offreCandidatures.getId());
        } else msg(false, "Échec acceptation.");
    }

    public void refuserCandidature(Long id) {
        boolean ok = formationClient.refuserCandidature(id, session.getToken());
        if (ok) {
            msg(true, "Candidature refusée.");
            if (offreCandidatures != null) chargerCandidatures(offreCandidatures.getId());
        } else msg(false, "Échec refus.");
    }

    public List<CandidatureFront> getCandidatures()   { return candidatures; }
    public Offre getOffreCandidatures()               { return offreCandidatures; }
    public long getNbCandidaturesEnAttente() {
        if (candidatures == null) return 0;
        return candidatures.stream().filter(c -> "EN_ATTENTE".equals(c.getStatut())).count();
    }

    // ── Getters agrégées (toutes formations confondues) ──────────────────────
    public List<CandidatureFront> getToutesCandidaturesEnAttente() { return toutesCandidaturesEnAttente; }
    public int  getNbTotalEnAttente()  { return toutesCandidaturesEnAttente == null ? 0 : toutesCandidaturesEnAttente.size(); }
    public List<CandidatureFront> getToutesCandidaturesValidees()  { return toutesCandidaturesValidees; }
    public int  getNbApprenants()      { return toutesCandidaturesValidees  == null ? 0 : toutesCandidaturesValidees.size(); }

    // ── KPIs mes formations ──────────────────────────────────────────────────
    public int getNbMesFormations() { return mesFormations == null ? 0 : mesFormations.size(); }
    public long getNbPubliees()   {
        if (mesFormations == null) return 0;
        return mesFormations.stream().filter(o -> "DISPONIBLE".equals(o.getStatut())).count();
    }
    public long getNbBrouillons() {
        if (mesFormations == null) return 0;
        return mesFormations.stream().filter(o -> "BROUILLON".equals(o.getStatut()) && !Boolean.TRUE.equals(o.getSoumis())).count();
    }
    public long getNbSoumises()   {
        if (mesFormations == null) return 0;
        return mesFormations.stream().filter(o -> Boolean.TRUE.equals(o.getSoumis()) && !Boolean.TRUE.equals(o.getRejete())).count();
    }
    public long getNbRejetees()   {
        if (mesFormations == null) return 0;
        return mesFormations.stream().filter(o -> Boolean.TRUE.equals(o.getRejete())).count();
    }

    // ── Getters / Setters formation ──────────────────────────────────────────
    public List<Offre>         getMesFormations()  { return mesFormations; }
    public List<Domaine>       getDomaines()       { return domaines; }
    public List<FormateurFront> getFormateurs()    { return formateurs; }
    public boolean isModeEditionFormation()        { return modeEditionFormation; }
    public Offre getOffreSelectionnee()            { return offreSelectionnee; }

    public String  getfTitre()                { return fTitre; }                 public void setfTitre(String v)                 { this.fTitre = v; }
    public String  getfDescription()          { return fDescription; }           public void setfDescription(String v)           { this.fDescription = v; }
    public String  getfType()                 { return fType; }                  public void setfType(String v)                  { this.fType = v; }
    public String  getfNiveau()               { return fNiveau; }                public void setfNiveau(String v)                { this.fNiveau = v; }
    public String  getfDuree()                { return fDuree; }                 public void setfDuree(String v)                 { this.fDuree = v; }
    public String  getfDateDebut()            { return fDateDebut; }             public void setfDateDebut(String v)             { this.fDateDebut = v; }
    public String  getfDateFin()              { return fDateFin; }               public void setfDateFin(String v)               { this.fDateFin = v; }
    public Integer getfPlaces()               { return fPlaces; }                public void setfPlaces(Integer v)               { this.fPlaces = v; }
    public Double  getfCout()                 { return fCout; }                  public void setfCout(Double v)                  { this.fCout = v; }
    public Long    getfDomaineId()            { return fDomaineId; }             public void setfDomaineId(Long v)               { this.fDomaineId = v; }
    public String  getfProgrammeUrl()         { return fProgrammeUrl; }          public void setfProgrammeUrl(String v)          { this.fProgrammeUrl = v; }
    public String  getfPrerequis()            { return fPrerequis; }             public void setfPrerequis(String v)             { this.fPrerequis = v; }
    public String  getfLieu()                 { return fLieu; }                  public void setfLieu(String v)                  { this.fLieu = v; }
    public String  getfImageUrl()             { return fImageUrl; }              public void setfImageUrl(String v)              { this.fImageUrl = v; }
    public String  getfLangue()               { return fLangue; }                public void setfLangue(String v)                { this.fLangue = v; }
    public String  getfPublicCible()          { return fPublicCible; }           public void setfPublicCible(String v)           { this.fPublicCible = v; }
    public String  getfModalitesPaiement()    { return fModalitesPaiement; }     public void setfModalitesPaiement(String v)     { this.fModalitesPaiement = v; }
    public String  getfFinancementDisponible(){ return fFinancementDisponible; } public void setfFinancementDisponible(String v) { this.fFinancementDisponible = v; }
    public String  getfCertification()        { return fCertification; }         public void setfCertification(String v)         { this.fCertification = v; }

    // ── Getters / Setters module ─────────────────────────────────────────────
    public List<ModuleFront>  getModules()         { return modules; }
    public boolean isModeEditionModule()           { return modeEditionModule; }
    public String  getmTitre()                     { return mTitre; }            public void setmTitre(String v)       { this.mTitre = v; }
    public String  getmDescription()               { return mDescription; }      public void setmDescription(String v) { this.mDescription = v; }
    public Integer getmDureeHeures()               { return mDureeHeures; }      public void setmDureeHeures(Integer v){ this.mDureeHeures = v; }
    public Integer getmOrdre()                     { return mOrdre; }            public void setmOrdre(Integer v)      { this.mOrdre = v; }
    public Long    getmFormateurId()               { return mFormateurId; }      public void setmFormateurId(Long v)   { this.mFormateurId = v; }

    // ── Utilitaire ───────────────────────────────────────────────────────────
    private void msg(boolean ok, String txt) {
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(ok ? FacesMessage.SEVERITY_INFO : FacesMessage.SEVERITY_ERROR, txt, null));
    }
}