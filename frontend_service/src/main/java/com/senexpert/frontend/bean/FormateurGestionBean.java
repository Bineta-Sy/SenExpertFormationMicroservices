package com.senexpert.frontend.bean;

import com.senexpert.frontend.client.FormationServiceClient;
import com.senexpert.frontend.model.*;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.primefaces.event.FileUploadEvent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Bean unique pour la page de gestion des cours du formateur.
 * Charge automatiquement toutes les offres sans paramètre URL.
 */
@Named
@ViewScoped
public class FormateurGestionBean implements Serializable {

    @Inject private FormationServiceClient formationClient;
    @Inject private SessionBean session;

    // ── Offres du formateur ───────────────────────────────────────────────────
    private List<OffreFormation> mesOffres   = new ArrayList<>();
    private Long    offreActiveId;
    private String  offreActiveTitre         = "";

    // ── Vue unifiée ───────────────────────────────────────────────────────────
    private List<CoursEnLigneFront> tousLesCours = new ArrayList<>();
    private Long    filtreOffreId; // null = toutes les formations

    // ── Cours affichés ────────────────────────────────────────────────────────
    private List<CoursEnLigneFront> cours    = new ArrayList<>();
    private CoursEnLigneFront coursForm      = new CoursEnLigneFront();
    private boolean modeEditionCours         = false;

    // ── Upload fichier ────────────────────────────────────────────────────────
    private CoursEnLigneFront coursUpload;

    // ── Quiz questions ────────────────────────────────────────────────────────
    private List<QuizQuestionFront> questions = new ArrayList<>();
    private QuizQuestionFront questionForm    = new QuizQuestionFront();
    private CoursEnLigneFront coursQuiz;

    // ── Soumissions ──────────────────────────────────────────────────────────
    private List<SoumissionFront> soumissions = new ArrayList<>();
    private CoursEnLigneFront coursSoumission;

    // ─────────────────────────────────────────────────────────────────────────

    @PostConstruct
    public void init() {
        String email = session.getEmail();
        mesOffres = formationClient.listerOffresAdmin(session.getToken()).stream()
            .filter(o -> o.getFormateurs() != null && o.getFormateurs().stream()
                .anyMatch(f -> email != null && email.equalsIgnoreCase(f.getEmail())))
            .collect(Collectors.toList());

        // Offre par défaut pour création de cours
        if (!mesOffres.isEmpty()) {
            offreActiveId    = mesOffres.get(0).getId();
            offreActiveTitre = mesOffres.get(0).getTitre();
        }
        chargerTousCours();
        nouveauCours();
    }

    /** Filtre la liste par offre (id=null → toutes). */
    public void filtrer(Long id) {
        filtreOffreId = (id == null || id == 0L) ? null : id;
        if (filtreOffreId != null) {
            offreActiveId = filtreOffreId;
            OffreFormation o = mesOffres.stream().filter(x -> filtreOffreId.equals(x.getId())).findFirst().orElse(null);
            offreActiveTitre = o != null ? o.getTitre() : "";
        } else {
            if (!mesOffres.isEmpty()) {
                offreActiveId    = mesOffres.get(0).getId();
                offreActiveTitre = mesOffres.get(0).getTitre();
            }
        }
        cours = getCoursFiltres();
        nouveauCours();
    }

    /** Retourne les cours selon le filtre actif. */
    public List<CoursEnLigneFront> getCoursFiltres() {
        if (filtreOffreId == null) return tousLesCours;
        return tousLesCours.stream()
            .filter(c -> filtreOffreId.equals(c.getOffreId()))
            .collect(Collectors.toList());
    }

    /** @deprecated conservé pour compatibilité — utiliser getCoursFiltres(). */
    public void activerOffre(Long id) {
        filtrer(id);
    }

    private void chargerTousCours() {
        tousLesCours = new ArrayList<>();
        boolean admin = session.isAdmin() || "FORMATEUR".equals(session.getRole());
        for (OffreFormation offre : mesOffres) {
            List<CoursEnLigneFront> c = formationClient.coursParOffre(offre.getId(), admin, session.getToken());
            if (c != null) {
                c.forEach(co -> {
                    co.setOffreId(offre.getId());
                    co.setOffreTitre(offre.getTitre());
                });
                tousLesCours.addAll(c);
            }
        }
        cours = getCoursFiltres();
    }

    /** @deprecated — utiliser chargerTousCours() */
    private void chargerCours() {
        chargerTousCours();
    }

    // ── Création / Édition cours ─────────────────────────────────────────────

    public void nouveauCours() {
        coursForm = new CoursEnLigneFront();
        coursForm.setOffreId(offreActiveId);
        coursForm.setOrdre(cours.size() + 1);
        coursForm.setType("");   // force "— Choisir —" sélectionné
        modeEditionCours = false;
    }

    public void nouveauTP() {
        nouveauCours();
        coursForm.setType("TP");
    }

    public void nouveauDevoir() {
        nouveauCours();
        coursForm.setType("DEVOIR");
    }

    public void nouveauExamen() {
        nouveauCours();
        coursForm.setType("EXAMEN");
    }

    public void editerCours(CoursEnLigneFront c) {
        coursForm        = clone(c);
        modeEditionCours = true;
        if ("QUIZ".equals(c.getType()) || ("EXAMEN".equals(c.getType()) && "QCM".equals(c.getTypeExamen()))) {
            questions = formationClient.questionsParCours(c.getId());
            coursQuiz = c;
        }
    }

    /** Mise à jour de l'offre cible pour la création quand le dropdown change. */
    public void onOffreChange() {
        // offreActiveId est déjà mis à jour par le setter JSF via p:ajax process="@this"
        // On met aussi à jour offreActiveTitre
        if (offreActiveId != null) {
            OffreFormation o = mesOffres.stream().filter(x -> offreActiveId.equals(x.getId())).findFirst().orElse(null);
            offreActiveTitre = o != null ? o.getTitre() : "";
        }
        nouveauCours();
    }

    public void sauvegarderCours() {
        if (coursForm.getType() == null || coursForm.getType().isBlank()) {
            msg(false, "Veuillez sélectionner un type de cours.");
            return;
        }
        if (modeEditionCours && coursForm.getId() != null) {
            CoursEnLigneFront upd = formationClient.modifierCours(coursForm.getId(), coursForm, session.getToken());
            msg(upd != null, upd != null ? "Cours modifié." : "Échec de la modification.");
        } else {
            CoursEnLigneFront cre = formationClient.creerCours(coursForm, session.getToken());
            msg(cre != null, cre != null ? "Cours créé." : "Échec de la création.");
        }
        chargerCours();
        nouveauCours();
        modeEditionCours = false;
    }

    public void supprimerCours(Long id) {
        boolean ok = formationClient.supprimerCours(id, session.getToken());
        msg(ok, ok ? "Cours supprimé." : "Échec de la suppression.");
        chargerCours();
    }

    public void togglePublier(CoursEnLigneFront c) {
        boolean nouvel = !c.isPublie();
        boolean ok = formationClient.publierCours(c.getId(), nouvel, session.getToken());
        msg(ok, ok ? (nouvel ? "Cours publié." : "Cours dépublié.") : "Échec.");
        chargerCours();
    }

    // ── Upload fichier ───────────────────────────────────────────────────────

    public void preparerUpload(CoursEnLigneFront c) { this.coursUpload = c; }

    public void handleUpload(FileUploadEvent event) {
        if (coursUpload == null) { msg(false, "Aucun cours sélectionné."); return; }
        var fichier = event.getFile();
        if (fichier == null || fichier.getSize() == 0) { msg(false, "Fichier vide."); return; }
        CoursEnLigneFront upd = formationClient.uploadFichierCours(coursUpload.getId(), fichier, session.getToken());
        if (upd != null) {
            msg(true, "Fichier \"" + fichier.getFileName() + "\" uploadé.");
            chargerCours(); coursUpload = null;
        } else {
            msg(false, "Échec de l'upload.");
        }
    }

    // ── Questions quiz ───────────────────────────────────────────────────────

    public void ouvrirQuiz(CoursEnLigneFront c) {
        coursQuiz     = c;
        questions     = formationClient.questionsParCours(c.getId());
        questionForm  = new QuizQuestionFront();
        questionForm.setCoursId(c.getId());
    }

    public void ajouterQuestion() {
        if (coursQuiz == null) return;
        questionForm.setCoursId(coursQuiz.getId());
        boolean ok = formationClient.ajouterQuestion(questionForm, session.getToken());
        msg(ok, ok ? "Question ajoutée." : "Échec.");
        if (ok) {
            questions    = formationClient.questionsParCours(coursQuiz.getId());
            questionForm = new QuizQuestionFront();
            questionForm.setCoursId(coursQuiz.getId());
        }
    }

    public void supprimerQuestion(Long id) {
        boolean ok = formationClient.supprimerQuestion(id, session.getToken());
        msg(ok, ok ? "Question supprimée." : "Échec.");
        if (coursQuiz != null) questions = formationClient.questionsParCours(coursQuiz.getId());
    }

    // ── Soumissions ──────────────────────────────────────────────────────────

    public void ouvrirSoumissions(CoursEnLigneFront c) {
        coursSoumission = c;
        soumissions = formationClient.soumissionsParCours(c.getId(), session.getToken());
        if (soumissions == null) soumissions = new ArrayList<>();
        soumissions.forEach(s -> {
            if (s.getNoteSaisie() == 0 && s.getNote() != null) s.setNoteSaisie(s.getNote());
            if (s.getCommentaireSaisie() == null && s.getCommentaireFormateur() != null)
                s.setCommentaireSaisie(s.getCommentaireFormateur());
        });
    }

    public void noter(SoumissionFront s) {
        SoumissionFront upd = formationClient.noterSoumission(
                s.getId(), s.getNoteSaisie(), s.getCommentaireSaisie(), session.getToken());
        if (upd != null) {
            msg(true, "Note attribuée à " + s.getNomApprenant() + ".");
            ouvrirSoumissions(coursSoumission);
        } else {
            msg(false, "Échec de la notation.");
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    public boolean isOffreActive(Long id) { return id != null && id.equals(filtreOffreId); }

    public int getNbEnAttente() {
        if (soumissions == null) return 0;
        return (int) soumissions.stream().filter(SoumissionFront::isEnAttente).count();
    }

    public int getNbCours()          { return getCoursFiltres().size(); }
    public int getNbTousLesCours()   { return tousLesCours == null ? 0 : tousLesCours.size(); }
    public int getNbMesOffres()      { return mesOffres == null ? 0 : mesOffres.size(); }
    public int getNbQuestions()      { return questions == null ? 0 : questions.size(); }
    public int getNbSoumissions()    { return soumissions == null ? 0 : soumissions.size(); }

    private void msg(boolean ok, String text) {
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(ok ? FacesMessage.SEVERITY_INFO : FacesMessage.SEVERITY_ERROR,
                ok ? "Succès" : "Erreur", text));
    }

    /** Crée une copie superficielle d'un CoursEnLigneFront pour édition. */
    private CoursEnLigneFront clone(CoursEnLigneFront src) {
        CoursEnLigneFront dst = new CoursEnLigneFront();
        dst.setId(src.getId());             dst.setTitre(src.getTitre());
        dst.setDescription(src.getDescription());
        dst.setInstructionsDetaillees(src.getInstructionsDetaillees());
        dst.setType(src.getType());         dst.setUrlContenu(src.getUrlContenu());
        dst.setOrdre(src.getOrdre());       dst.setDureeMinutes(src.getDureeMinutes());
        dst.setPublie(src.isPublie());
        dst.setDateDebut(src.getDateDebut());
        dst.setDateLimite(src.getDateLimite()); dst.setBaremeNote(src.getBaremeNote());
        dst.setDureeExamen(src.getDureeExamen()); dst.setTypeExamen(src.getTypeExamen());
        dst.setLienVisio(src.getLienVisio()); dst.setPlateforme(src.getPlateforme());
        dst.setDateHeureLive(src.getDateHeureLive());
        dst.setOffreId(src.getOffreId());   dst.setOffreTitre(src.getOffreTitre());
        dst.setCheminFichier(src.getCheminFichier());
        dst.setNomFichierOriginal(src.getNomFichierOriginal());
        dst.setTailleFichier(src.getTailleFichier());
        dst.setUrlFichier(src.getUrlFichier());
        return dst;
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────
    public List<OffreFormation>      getMesOffres()         { return mesOffres; }
    public Long                      getOffreActiveId()     { return offreActiveId; }
    public void                      setOffreActiveId(Long v){ offreActiveId = v; }
    public String                    getOffreActiveTitre()  { return offreActiveTitre; }
    public List<CoursEnLigneFront>   getTousLesCours()      { return tousLesCours; }
    public Long                      getFiltreOffreId()     { return filtreOffreId; }
    public void                      setFiltreOffreId(Long v){ filtreOffreId = v; }
    public List<CoursEnLigneFront>   getCours()             { return getCoursFiltres(); }
    public CoursEnLigneFront         getCoursForm()         { return coursForm; }
    public void setCoursForm(CoursEnLigneFront v)           { coursForm = v; }
    public boolean                   isModeEditionCours()   { return modeEditionCours; }
    public CoursEnLigneFront         getCoursUpload()       { return coursUpload; }
    public List<QuizQuestionFront>   getQuestions()         { return questions; }
    public QuizQuestionFront         getQuestionForm()      { return questionForm; }
    public void setQuestionForm(QuizQuestionFront v)        { questionForm = v; }
    public CoursEnLigneFront         getCoursQuiz()         { return coursQuiz; }
    public List<SoumissionFront>     getSoumissions()       { return soumissions; }
    public CoursEnLigneFront         getCoursSoumission()   { return coursSoumission; }
}
