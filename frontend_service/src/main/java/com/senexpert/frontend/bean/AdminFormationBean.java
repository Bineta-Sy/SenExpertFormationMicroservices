package com.senexpert.frontend.bean;

import com.senexpert.frontend.client.FormationServiceClient;
import com.senexpert.frontend.model.FormateurFront;
import com.senexpert.frontend.model.ModuleFront;
import com.senexpert.frontend.model.OffreFormation;
import com.senexpert.frontend.model.SeanceFront;
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
 * Bean admin pour la gestion complète d'une formation :
 * - modules (CRUD + ordre + assignation formateur)
 * - séances / emploi du temps
 */
@Named
@ViewScoped
public class AdminFormationBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private FormationServiceClient client;
    @Inject private SessionBean session;

    // ── Paramètre URL ───────────────────────────────────────────
    /** Reçu via f:viewParam name="offreId" */
    private Long offreIdParam;

    // ── Offre sélectionnée ──────────────────────────────────────
    private OffreFormation offreSelectionnee;
    private List<FormateurFront> tousFormateurs = new ArrayList<>();

    // ── Modules ─────────────────────────────────────────────────
    private List<ModuleFront> modules = new ArrayList<>();

    // Formulaire module
    private Long    moduleId;
    private String  moduleTitre;
    private String  moduleDescription;
    private Integer moduleDureeHeures;
    private Integer moduleOrdre;
    private Long    moduleFormateurId;

    // ── Séances ─────────────────────────────────────────────────
    private List<SeanceFront> seances = new ArrayList<>();

    // Formulaire séance
    private Long    seanceId;
    private Long    seanceModuleId;
    private String  seanceDate;
    private String  seanceHeureDebut;
    private String  seanceHeureFin;
    private String  seanceSalle;
    private String  seanceLienVisio;
    private String  seanceNotes;

    @PostConstruct
    public void init() {
        this.tousFormateurs = client.listerFormateurs();
        // Charger l'offre directement depuis le paramètre HTTP (fiable même sans f:viewParam)
        String p = FacesContext.getCurrentInstance()
                .getExternalContext().getRequestParameterMap().get("offreId");
        if (p != null && !p.isEmpty()) {
            try { 
                this.offreIdParam = Long.parseLong(p); 
            } catch (NumberFormatException ignored) {}
        }
        if (this.offreIdParam != null) {
            this.offreSelectionnee = client.trouverOffreParId(this.offreIdParam);
            if (this.offreSelectionnee != null) {
                chargerModules();
                chargerSeances();
            }
        }
    }

    /** Appelé par f:viewAction (redondant mais conservé pour compatibilité) */
    public void charger() {
        if (this.offreSelectionnee != null) return; // déjà chargé dans @PostConstruct
        if (this.offreIdParam == null) return;
        this.offreSelectionnee = client.trouverOffreParId(this.offreIdParam);
        if (this.offreSelectionnee != null) {
            chargerModules();
            chargerSeances();
        }
    }

    // ── Méthode publique conservée pour appel programmatique ──
    public void selectionnerOffre(OffreFormation offre) {
        this.offreSelectionnee = offre;
        chargerModules();
        chargerSeances();
    }

    private void chargerModules() {
        System.err.println("[chargerModules] offre=" + (this.offreSelectionnee != null ? this.offreSelectionnee.getId() : "NULL"));
        if (this.offreSelectionnee != null) {
            this.modules = client.modulesParOffre(this.offreSelectionnee.getId(), session.getToken());
        }
    }

    private void chargerSeances() {
        if (this.offreSelectionnee != null) {
            this.seances = client.seancesParOffre(this.offreSelectionnee.getId(), session.getToken());
        }
    }

    // ── Securisation de la liste des formateurs lors des appels AJAX ──
    private void securiserFormateurs() {
        if (this.tousFormateurs == null || this.tousFormateurs.isEmpty()) {
            this.tousFormateurs = client.listerFormateurs();
        }
    }

    // ── CRUD Modules ────────────────────────────────────────────

    public void preparerNouveauModule() {
        securiserFormateurs();
        this.moduleId = null;
        this.moduleTitre = null; 
        this.moduleDescription = null;
        this.moduleDureeHeures = null;
        this.moduleOrdre = (this.modules != null) ? this.modules.size() + 1 : 1;
        this.moduleFormateurId = null;
    }

    public void preparerEditionModule(ModuleFront m) {
        securiserFormateurs();
        this.moduleId         = m.getId();
        this.moduleTitre       = m.getTitre();
        this.moduleDescription = m.getDescription();
        this.moduleDureeHeures = m.getDureeHeures();
        this.moduleOrdre       = m.getOrdre();
        this.moduleFormateurId = (m.getFormateur() != null) ? m.getFormateur().getId() : null;
    }

    /** Recharge l'offre si perdue (état de vue non sérialisé entre forms) */
    private void rechargerSiNecessaire() {
        if (this.offreSelectionnee == null && this.offreIdParam != null) {
            this.offreSelectionnee = client.trouverOffreParId(this.offreIdParam);
        }
        if ((this.modules == null || this.modules.isEmpty()) && this.offreSelectionnee != null) {
            chargerModules();
        }
    }

    public void sauvegarderModule() {
        rechargerSiNecessaire();
        System.err.println("[sauvegarderModule] offreId=" + this.offreIdParam
            + " | offre=" + (this.offreSelectionnee != null ? this.offreSelectionnee.getId() : "NULL")
            + " | titre=" + this.moduleTitre
            + " | token=" + (session.getToken() != null ? session.getToken().substring(0, Math.min(20, session.getToken().length())) + "..." : "NULL"));
        
        if (this.offreSelectionnee == null) { 
            msg(false, "Offre non chargée — rechargez la page."); 
            return; 
        }
        
        boolean ok;
        if (this.moduleId == null) {
            ModuleFront m = client.creerModule(this.moduleTitre, this.moduleDescription, this.moduleDureeHeures,
                    this.moduleOrdre, this.offreSelectionnee.getId(), this.moduleFormateurId, session.getToken());
            ok = (m != null);
            if (ok) {
                if (this.modules == null) {
                    this.modules = new ArrayList<>();
                }
                this.modules.add(m);
            }
        } else {
            ok = client.modifierModule(this.moduleId, this.moduleTitre, this.moduleDescription, this.moduleDureeHeures,
                    this.moduleOrdre, this.offreSelectionnee.getId(), this.moduleFormateurId, session.getToken());
        }
        
        if (ok) {
            msg(true, this.moduleId == null ? "Module créé." : "Module modifié.");
            preparerNouveauModule();
            chargerModules();
        } else {
            msg(false, "Échec de l'enregistrement du module.");
        }
    }

    public void supprimerModule(Long id) {
        boolean ok = client.supprimerModule(id, session.getToken());
        if (ok) { 
            msg(true, "Module supprimé."); 
            chargerModules(); 
            chargerSeances(); 
        } else {
            msg(false, "Impossible de supprimer ce module.");
        }
    }

    // ── CRUD Séances ────────────────────────────────────────────

    public void preparerNouvelleSeance(ModuleFront module) {
        this.seanceId = null;
        this.seanceModuleId = (module != null) ? module.getId() : null;
        this.seanceDate = null; 
        this.seanceHeureDebut = null; 
        this.seanceHeureFin = null;
        this.seanceSalle = null; 
        this.seanceLienVisio = null; 
        this.seanceNotes = null;
    }

    public void preparerEditionSeance(SeanceFront s) {
        this.seanceId        = s.getId();
        this.seanceModuleId  = s.getModuleId();
        this.seanceDate      = s.getDate();
        this.seanceHeureDebut = s.getHeureDebut();
        this.seanceHeureFin   = s.getHeureFin();
        this.seanceSalle      = s.getSalle();
        this.seanceLienVisio  = s.getLienVisio();
        this.seanceNotes      = s.getNotes();
    }

    public void sauvegarderSeance() {
        rechargerSiNecessaire();
        if (this.seanceModuleId == null) { 
            msg(false, "Sélectionnez un module."); 
            return; 
        }
        
        boolean ok;
        if (this.seanceId == null) {
            SeanceFront s = client.creerSeance(this.seanceModuleId, this.seanceDate, this.seanceHeureDebut, this.seanceHeureFin,
                    this.seanceSalle, this.seanceLienVisio, this.seanceNotes, session.getToken());
            ok = (s != null);
        } else {
            ok = client.modifierSeance(this.seanceId, this.seanceModuleId, this.seanceDate, this.seanceHeureDebut, this.seanceHeureFin,
                    this.seanceSalle, this.seanceLienVisio, this.seanceNotes, session.getToken());
        }
        
        if (ok) {
            msg(true, this.seanceId == null ? "Séance ajoutée." : "Séance modifiée.");
            preparerNouvelleSeance(null);
            chargerSeances();
        } else {
            msg(false, "Échec de l'enregistrement de la séance.");
        }
    }

    public void supprimerSeance(Long id) {
        boolean ok = client.supprimerSeance(id, session.getToken());
        if (ok) { 
            msg(true, "Séance supprimée."); 
            chargerSeances(); 
        } else {
            msg(false, "Impossible de supprimer cette séance.");
        }
    }

    // ── Stats ───────────────────────────────────────────────────
    public int getNbModules()    { return this.modules == null ? 0 : this.modules.size(); }
    public int getNbSeances()    { return this.seances == null ? 0 : this.seances.size(); }
    public int getNbFormateurs() { return this.tousFormateurs == null ? 0 : this.tousFormateurs.size(); }

    public int getDureeTotale() {
        if (this.modules == null) return 0;
        return this.modules.stream()
                .mapToInt(m -> m.getDureeHeures() != null ? m.getDureeHeures() : 0)
                .sum();
    }

    public long getNbModulesSansFormateur() {
        if (this.modules == null) return 0;
        return this.modules.stream().filter(m -> m.getFormateur() == null).count();
    }

    public long getNbModulesSansSeance() {
        if (this.modules == null || this.seances == null) return 0;
        return this.modules.stream()
                .filter(m -> this.seances.stream().noneMatch(s -> m.getId().equals(s.getModuleId())))
                .count();
    }

    private void msg(boolean ok, String text) {
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(ok ? FacesMessage.SEVERITY_INFO : FacesMessage.SEVERITY_ERROR,
                    ok ? "Succès" : "Erreur", text));
    }

    // ── Getters / Setters ────────────────────────────────────────
    public Long getOffreIdParam() { return offreIdParam; }
    public void setOffreIdParam(Long v) { this.offreIdParam = v; }
    
    public OffreFormation getOffreSelectionnee() { return offreSelectionnee; }
    public List<FormateurFront> getTousFormateurs() { return tousFormateurs; }
    public List<ModuleFront> getModules() { return modules; }
    public List<SeanceFront> getSeances() { return seances; }

    public Long getModuleId() { return moduleId; }
    public String getModuleTitre() { return moduleTitre; }
    public void setModuleTitre(String v) { this.moduleTitre = v; }
    
    public String getModuleDescription() { return moduleDescription; }
    public void setModuleDescription(String v) { this.moduleDescription = v; }
    
    public Integer getModuleDureeHeures() { return moduleDureeHeures; }
    public void setModuleDureeHeures(Integer v) { this.moduleDureeHeures = v; }
    
    public Integer getModuleOrdre() { return moduleOrdre; }
    public void setModuleOrdre(Integer v) { this.moduleOrdre = v; }
    
    public Long getModuleFormateurId() { return moduleFormateurId; }
    public void setModuleFormateurId(Long v) { this.moduleFormateurId = v; }

    public Long getSeanceId() { return seanceId; }
    public Long getSeanceModuleId() { return seanceModuleId; }
    public void setSeanceModuleId(Long v) { this.seanceModuleId = v; }
    
    public String getSeanceDate() { return seanceDate; }
    public void setSeanceDate(String v) { this.seanceDate = v; }
    
    public String getSeanceHeureDebut() { return seanceHeureDebut; }
    public void setSeanceHeureDebut(String v) { this.seanceHeureDebut = v; }
    
    public String getSeanceHeureFin() { return seanceHeureFin; }
    public void setSeanceHeureFin(String v) { this.seanceHeureFin = v; }
    
    public String getSeanceSalle() { return seanceSalle; }
    public void setSeanceSalle(String v) { this.seanceSalle = v; }
    
    public String getSeanceLienVisio() { return seanceLienVisio; }
    public void setSeanceLienVisio(String v) { this.seanceLienVisio = v; }
    
    public String getSeanceNotes() { return seanceNotes; }
    public void setSeanceNotes(String v) { this.seanceNotes = v; }
}