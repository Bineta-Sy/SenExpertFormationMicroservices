package com.senexpert.frontend.bean;

import com.senexpert.frontend.client.FormationServiceClient;
import com.senexpert.frontend.model.*;
import com.senexpert.frontend.model.SoumissionFront;
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
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Bean central pour toutes les fonctionnalités e-learning :
 * – Apprenant  : consulter les cours d'une offre, marquer comme terminé, passer un quiz
 * – Formateur / Admin : ajouter/modifier/supprimer des cours, des questions quiz, des sessions live
 */
@Named
@ViewScoped
public class ElearningBean implements Serializable {

    @Inject private FormationServiceClient formationClient;
    @Inject private SessionBean session;

    // ── Contexte offre ───────────────────────────────────────────────────────
    private Long   offreId;
    private String offreTitre;

    // ── Cours de l'offre ─────────────────────────────────────────────────────
    private List<CoursEnLigneFront> cours = new ArrayList<>();
    private CoursEnLigneFront coursSelectionne;

    // ── Formateurs de l'offre (pour le sélecteur dans le dialog) ─────────────
    private List<FormateurFront> formateursOffre = new ArrayList<>();

    // ── Progression de l'apprenant ───────────────────────────────────────────
    private List<ProgressionFront> progressions = new ArrayList<>();
    private Map<Long, ProgressionFront> progressionMap;   // coursId → progression

    // ── Formulaire création/édition cours (formateur/admin) ──────────────────
    private CoursEnLigneFront coursForm = new CoursEnLigneFront();
    private boolean modeEditionCours = false;

    // ── Questions quiz ────────────────────────────────────────────────────────
    private List<QuizQuestionFront> questions = new ArrayList<>();
    private QuizQuestionFront questionForm = new QuizQuestionFront();
    private boolean quizEnCours = false;  // quiz ouvert par l'apprenant
    private ProgressionFront resultatQuiz;

    // ════════════════════════════════════════════════════════════════════════

    @PostConstruct
    public void init() {
        // L'offreId est passé via f:viewParam ou setOffreId() appelé depuis la page
    }

    /** Charge les cours de l'offre courante (appelé après setOffreId). */
    public void charger() {
        if (offreId == null) return;
        boolean admin = session.isAdmin() || "FORMATEUR".equals(session.getRole());
        cours = formationClient.coursParOffre(offreId, admin, session.getToken());
        formateursOffre = formationClient.formateursParOffre(offreId);
        if (session.getUtilisateurId() != null) {
            progressions = formationClient.progressionParOffre(offreId, session.getUtilisateurId(),
                                                                session.getToken());
            progressionMap = progressions.stream()
                .filter(p -> p.getCoursId() != null)
                .collect(Collectors.toMap(ProgressionFront::getCoursId, p -> p, (a, b) -> a));
        }
    }

    // ── Actions apprenant ────────────────────────────────────────────────────

    /** Ouvre un cours et enregistre l'accès. */
    public void ouvrirCours(CoursEnLigneFront c) {
        this.coursSelectionne = c;
        String type = c.getType();
        if ("QUIZ".equals(type) || ("EXAMEN".equals(type) && "QCM".equals(c.getTypeExamen()))) {
            questions = formationClient.questionsParCours(c.getId());
            questions.forEach(q -> q.setReponseApprenant(null));
            quizEnCours = true;
            resultatQuiz = null;
        } else {
            quizEnCours = false;
        }
        // Charge la soumission existante pour TP / Devoir / Examen
        if (c.isTravauxPratiques() && session.getUtilisateurId() != null) {
            chargerMaSoumission(c);
        }
        if (session.getUtilisateurId() != null) {
            formationClient.accederCours(c.getId(), session.getUtilisateurId(), session.getToken());
            charger();
        }
    }

    /** Marque le cours courant comme terminé. */
    public void marquerTermine() {
        if (coursSelectionne == null || session.getUtilisateurId() == null) return;
        formationClient.completerCours(coursSelectionne.getId(), session.getUtilisateurId(),
                                       session.getToken());
        charger();
        msg(true, "Cours marqué comme terminé.");
    }

    /** Soumet les réponses du quiz en cours. */
    public void soumettreQuiz() {
        if (coursSelectionne == null || session.getUtilisateurId() == null) return;
        resultatQuiz = formationClient.soumettreQuiz(coursSelectionne.getId(),
                session.getUtilisateurId(), questions, session.getToken());
        quizEnCours = false;
        charger();
        if (resultatQuiz != null) {
            msg(true, "Quiz soumis — Score : " + resultatQuiz.getScoreLabel());
        }
    }

    // ── Actions formateur/admin ───────────────────────────────────────────────

    public void nouveauCours() {
        coursForm = new CoursEnLigneFront();
        coursForm.setOffreId(offreId);
        coursForm.setOrdre(cours.size() + 1);
        modeEditionCours = false;
    }

    public void editerCours(CoursEnLigneFront c) {
        coursForm = c;
        modeEditionCours = true;
        if ("QUIZ".equals(c.getType())) {
            questions = formationClient.questionsParCours(c.getId());
        }
    }

    public void sauvegarderCours() {
        if (modeEditionCours && coursForm.getId() != null) {
            CoursEnLigneFront updated = formationClient.modifierCours(coursForm.getId(), coursForm,
                                                                       session.getToken());
            msg(updated != null, updated != null ? "Cours modifié." : "Échec de la modification.");
        } else {
            CoursEnLigneFront created = formationClient.creerCours(coursForm, session.getToken());
            msg(created != null, created != null ? "Cours créé." : "Échec de la création.");
        }
        charger();
        coursForm = new CoursEnLigneFront();
        coursForm.setOffreId(offreId);
        modeEditionCours = false;
    }

    public void supprimerCours(Long id) {
        boolean ok = formationClient.supprimerCours(id, session.getToken());
        msg(ok, ok ? "Cours supprimé." : "Échec de la suppression.");
        charger();
    }

    public void togglePublier(CoursEnLigneFront c) {
        boolean nouvelEtat = !c.isPublie();
        boolean ok = formationClient.publierCours(c.getId(), nouvelEtat, session.getToken());
        msg(ok, ok ? (nouvelEtat ? "Cours publié." : "Cours dépublié.") : "Échec.");
        charger();
    }

    // ── Upload de fichier (formateur/admin) ──────────────────────────────────

    private CoursEnLigneFront coursUpload;

    public void preparerUpload(CoursEnLigneFront c) {
        this.coursUpload = c;
    }

    public void handleUpload(FileUploadEvent event) {
        if (coursUpload == null) { msg(false, "Aucun cours sélectionné."); return; }
        UploadedFile fichier = event.getFile();
        if (fichier == null || fichier.getSize() == 0) { msg(false, "Fichier vide."); return; }
        CoursEnLigneFront updated = formationClient.uploadFichierCours(
                coursUpload.getId(), fichier, session.getToken());
        if (updated != null) {
            msg(true, "Fichier \"" + fichier.getFileName() + "\" uploadé avec succès.");
            charger();
            coursUpload = null;
        } else {
            msg(false, "Échec de l'upload (format ou taille > 100 Mo).");
        }
    }

    public CoursEnLigneFront getCoursUpload() { return coursUpload; }
    public void setCoursUpload(CoursEnLigneFront v) { this.coursUpload = v; }

    // ── Soumissions (TP / Devoir / Examen) ──────────────────────────────────

    private List<SoumissionFront> soumissions = new ArrayList<>();
    private CoursEnLigneFront     coursSoumission;   // cours cible pour la liste formateur ou la soumission apprenant
    private SoumissionFront       maSoumission;      // soumission de l'apprenant connecté (vue apprenant)
    private String                texteRendu;        // champ texte du formulaire apprenant
    private String                lienRendu;         // champ lien du formulaire apprenant

    /** Formateur : ouvre la liste des soumissions d'un cours. */
    public void ouvrirSoumissions(CoursEnLigneFront c) {
        this.coursSoumission = c;
        soumissions = formationClient.soumissionsParCours(c.getId(), session.getToken());
        // Pré-initialise les champs de correction
        soumissions.forEach(s -> {
            s.setNoteSaisie(s.getNote() != null ? s.getNote() : 0);
            s.setCommentaireSaisie(s.getCommentaireFormateur() != null ? s.getCommentaireFormateur() : "");
        });
    }

    /** Formateur : attribue une note à une soumission. */
    public void noter(SoumissionFront s) {
        SoumissionFront updated = formationClient.noterSoumission(
                s.getId(), s.getNoteSaisie(), s.getCommentaireSaisie(), session.getToken());
        if (updated != null) {
            msg(true, "Note " + s.getNoteSaisie() + "/" +
                (coursSoumission != null ? coursSoumission.getBaremeOu20() : 20) +
                " attribuée à " + s.getNomApprenant() + ".");
            ouvrirSoumissions(coursSoumission);
        } else {
            msg(false, "Échec de la notation.");
        }
    }

    /** Apprenant : charge sa soumission pour un cours TP/Devoir/Examen. */
    public void chargerMaSoumission(CoursEnLigneFront c) {
        this.coursSoumission = c;
        if (session.getUtilisateurId() != null) {
            maSoumission = formationClient.maSoumission(c.getId(), session.getUtilisateurId(), session.getToken());
        }
        texteRendu = maSoumission != null ? maSoumission.getContenuTexte() : null;
        lienRendu  = maSoumission != null ? maSoumission.getLienUrl()       : null;
    }

    /** Apprenant : soumet un texte / lien (sans fichier). */
    public void soumettreTravailTexte() {
        if (coursSoumission == null || session.getUtilisateurId() == null) return;
        SoumissionFront s = formationClient.soumettreTravail(
                coursSoumission.getId(),
                session.getUtilisateurId(),
                session.getNomComplet() != null ? session.getNomComplet() : session.getEmail(),
                texteRendu, lienRendu,
                session.getToken());
        if (s != null) {
            maSoumission = s;
            msg(true, "Travail soumis avec succès.");
            charger();
        } else {
            msg(false, "Erreur lors de la soumission.");
        }
    }

    /** Apprenant : soumet un fichier (appelé par p:fileUpload). */
    public void handleSoumissionFichier(org.primefaces.event.FileUploadEvent event) {
        if (coursSoumission == null || session.getUtilisateurId() == null) {
            msg(false, "Aucun cours sélectionné."); return;
        }
        org.primefaces.model.file.UploadedFile fichier = event.getFile();
        if (fichier == null || fichier.getSize() == 0) { msg(false, "Fichier vide."); return; }
        SoumissionFront s = formationClient.soumettreFichier(
                coursSoumission.getId(),
                session.getUtilisateurId(),
                session.getNomComplet() != null ? session.getNomComplet() : session.getEmail(),
                texteRendu, lienRendu, fichier,
                session.getToken());
        if (s != null) {
            maSoumission = s;
            msg(true, "Fichier \"" + fichier.getFileName() + "\" soumis avec succès.");
            charger();
        } else {
            msg(false, "Échec de l'envoi (format ou taille > 100 Mo).");
        }
    }

    /** Apprenant : démarre un examen QCM chronométré. */
    public void demarrerExamenQCM(CoursEnLigneFront c) {
        if (session.getUtilisateurId() == null) return;
        this.coursSoumission = c;
        SoumissionFront s = formationClient.demarrerExamen(
                c.getId(),
                session.getUtilisateurId(),
                session.getNomComplet() != null ? session.getNomComplet() : session.getEmail(),
                session.getToken());
        if (s != null) {
            maSoumission = s;
            // Lance aussi le quiz (questions)
            ouvrirCours(c);
        }
    }

    public List<SoumissionFront> getSoumissions()         { return soumissions; }
    public CoursEnLigneFront     getCoursSoumission()     { return coursSoumission; }
    public SoumissionFront       getMaSoumission()        { return maSoumission; }
    public String  getTexteRendu()                        { return texteRendu; }
    public void    setTexteRendu(String v)                { texteRendu = v; }
    public String  getLienRendu()                         { return lienRendu; }
    public void    setLienRendu(String v)                 { lienRendu = v; }
    public int     getNbSoumissions()                     { return soumissions == null ? 0 : soumissions.size(); }
    public int     getNbEnAttente() {
        if (soumissions == null) return 0;
        return (int) soumissions.stream().filter(SoumissionFront::isEnAttente).count();
    }

    // ── Quiz questions (formateur) ────────────────────────────────────────────

    public void ouvrirGestionQuiz(CoursEnLigneFront c) {
        this.coursSelectionne = c;
        questions = formationClient.questionsParCours(c.getId());
        questionForm = new QuizQuestionFront();
        questionForm.setCoursId(c.getId());
    }

    public void ajouterQuestion() {
        questionForm.setCoursId(coursSelectionne.getId());
        boolean ok = formationClient.ajouterQuestion(questionForm, session.getToken());
        msg(ok, ok ? "Question ajoutée." : "Échec.");
        if (ok) {
            questions = formationClient.questionsParCours(coursSelectionne.getId());
            questionForm = new QuizQuestionFront();
            questionForm.setCoursId(coursSelectionne.getId());
        }
    }

    public void supprimerQuestion(Long id) {
        boolean ok = formationClient.supprimerQuestion(id, session.getToken());
        msg(ok, ok ? "Question supprimée." : "Échec.");
        questions = formationClient.questionsParCours(coursSelectionne.getId());
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    /** Retourne la progression de l'apprenant pour un cours donné (null si jamais ouvert). */
    public ProgressionFront progressionPour(Long coursId) {
        return progressionMap != null ? progressionMap.get(coursId) : null;
    }

    /** % de complétion de l'offre pour l'apprenant connecté. */
    public int getPourcentageCompletion() {
        if (cours.isEmpty()) return 0;
        long done = cours.stream()
            .filter(c -> {
                ProgressionFront p = progressionPour(c.getId());
                return p != null && p.isComplete();
            }).count();
        return (int) Math.round(done * 100.0 / cours.size());
    }

    /** Nombre de cours publiés dans l'offre. */
    public int getNbCours()           { return cours == null ? 0 : cours.size(); }
    public int getNbQuestions()       { return questions == null ? 0 : questions.size(); }

    // Getters / Setters ──────────────────────────────────────────────────────
    public Long   getOffreId()                  { return offreId; }
    public void   setOffreId(Long v)            { this.offreId = v; }
    public String getOffreTitre()               { return offreTitre; }
    public void   setOffreTitre(String v)       { this.offreTitre = v; }
    public List<CoursEnLigneFront> getCours()   { return cours; }
    public CoursEnLigneFront getCoursSelectionne() { return coursSelectionne; }
    public void setCoursSelectionne(CoursEnLigneFront v){ this.coursSelectionne = v; }
    public List<ProgressionFront> getProgressions()            { return progressions; }
    public Map<Long, ProgressionFront> getProgressionMap()     { return progressionMap; }
    public CoursEnLigneFront getCoursForm()     { return coursForm; }
    public void setCoursForm(CoursEnLigneFront v){ this.coursForm = v; }
    public boolean isModeEditionCours()         { return modeEditionCours; }
    public List<QuizQuestionFront> getQuestions(){ return questions; }
    public QuizQuestionFront getQuestionForm()  { return questionForm; }
    public void setQuestionForm(QuizQuestionFront v){ this.questionForm = v; }
    public boolean isQuizEnCours()              { return quizEnCours; }
    public ProgressionFront getResultatQuiz()   { return resultatQuiz; }
    public List<FormateurFront> getFormateursOffre() { return formateursOffre; }

    private void msg(boolean ok, String text) {
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(ok ? FacesMessage.SEVERITY_INFO : FacesMessage.SEVERITY_ERROR,
                ok ? "Succès" : "Erreur", text));
    }
}
