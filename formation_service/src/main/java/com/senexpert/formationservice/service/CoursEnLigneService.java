package com.senexpert.formationservice.service;

import com.senexpert.formationservice.dto.*;
import com.senexpert.formationservice.entity.*;
import com.senexpert.formationservice.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class CoursEnLigneService {

    private final CoursEnLigneRepository      coursRepo;
    private final QuizQuestionRepository      quizRepo;
    private final ProgressionApprenantRepository progRepo;
    private final OffreFormationRepository    offreRepo;
    private final FormateurRepository         formateurRepo;

    public CoursEnLigneService(CoursEnLigneRepository coursRepo,
                               QuizQuestionRepository quizRepo,
                               ProgressionApprenantRepository progRepo,
                               OffreFormationRepository offreRepo,
                               FormateurRepository formateurRepo) {
        this.coursRepo      = coursRepo;
        this.quizRepo       = quizRepo;
        this.progRepo       = progRepo;
        this.offreRepo      = offreRepo;
        this.formateurRepo  = formateurRepo;
    }

    // ─── COURS ───────────────────────────────────────────────────────────────

    public List<CoursEnLigneDTO> listeParOffre(Long offreId, boolean adminView) {
        List<CoursEnLigne> list = adminView
            ? coursRepo.findByOffreIdOrderByOrdreAsc(offreId)
            : coursRepo.findByOffreIdAndPublieOrderByOrdreAsc(offreId, true);
        return list.stream().map(this::toDTO).collect(Collectors.toList());
    }

    public CoursEnLigneDTO creer(CoursEnLigneDTO dto) {
        CoursEnLigne c = new CoursEnLigne();
        fillEntity(c, dto);
        return toDTO(coursRepo.save(c));
    }

    public CoursEnLigneDTO modifier(Long id, CoursEnLigneDTO dto) {
        CoursEnLigne c = coursRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Cours introuvable : " + id));
        fillEntity(c, dto);
        return toDTO(coursRepo.save(c));
    }

    public void supprimer(Long id) {
        coursRepo.deleteById(id);
    }

    public CoursEnLigneDTO publier(Long id, boolean publie) {
        CoursEnLigne c = coursRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Cours introuvable : " + id));
        c.setPublie(publie);
        return toDTO(coursRepo.save(c));
    }

    public CoursEnLigneDTO enregistrerFichier(Long id, String chemin, String nomOriginal, long taille) {
        CoursEnLigne c = coursRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Cours introuvable : " + id));
        c.setCheminFichier(chemin);
        c.setNomFichierOriginal(nomOriginal);
        c.setTailleFichier(taille);
        return toDTO(coursRepo.save(c));
    }

    // ─── QUIZ QUESTIONS ──────────────────────────────────────────────────────

    public QuizQuestionDTO ajouterQuestion(QuizQuestionDTO dto) {
        CoursEnLigne cours = coursRepo.findById(dto.getCoursId())
            .orElseThrow(() -> new RuntimeException("Cours introuvable"));
        QuizQuestion q = new QuizQuestion();
        q.setCours(cours);
        q.setQuestion(dto.getQuestion());
        q.setOptionA(dto.getOptionA());
        q.setOptionB(dto.getOptionB());
        q.setOptionC(dto.getOptionC());
        q.setOptionD(dto.getOptionD());
        q.setBonneReponse(dto.getBonneReponse());
        q.setPoints(dto.getPoints() != null ? dto.getPoints() : 1);
        List<QuizQuestion> existing = quizRepo.findByCoursIdOrderByOrdreAsc(dto.getCoursId());
        q.setOrdre(existing.isEmpty() ? 1 : existing.get(existing.size() - 1).getOrdre() + 1);
        return toQuizDTO(quizRepo.save(q));
    }

    public void supprimerQuestion(Long questionId) {
        quizRepo.deleteById(questionId);
    }

    public List<QuizQuestionDTO> questionsParCours(Long coursId) {
        return quizRepo.findByCoursIdOrderByOrdreAsc(coursId)
            .stream().map(this::toQuizDTO).collect(Collectors.toList());
    }

    // ─── PROGRESSION ─────────────────────────────────────────────────────────

    public ProgressionDTO acceder(Long utilisateurId, Long coursId) {
        ProgressionApprenant p = progRepo
            .findByUtilisateurIdAndCoursId(utilisateurId, coursId)
            .orElseGet(() -> {
                CoursEnLigne c = coursRepo.findById(coursId)
                    .orElseThrow(() -> new RuntimeException("Cours introuvable"));
                ProgressionApprenant np = new ProgressionApprenant();
                np.setUtilisateurId(utilisateurId);
                np.setCours(c);
                np.setDateAcces(LocalDateTime.now());
                return np;
            });
        if (p.getDateAcces() == null) p.setDateAcces(LocalDateTime.now());
        return toProgDTO(progRepo.save(p));
    }

    public ProgressionDTO completer(Long utilisateurId, Long coursId) {
        ProgressionApprenant p = progRepo
            .findByUtilisateurIdAndCoursId(utilisateurId, coursId)
            .orElseGet(() -> {
                CoursEnLigne c = coursRepo.findById(coursId)
                    .orElseThrow(() -> new RuntimeException("Cours introuvable"));
                ProgressionApprenant np = new ProgressionApprenant();
                np.setUtilisateurId(utilisateurId);
                np.setCours(c);
                np.setDateAcces(LocalDateTime.now());
                return np;
            });
        p.setComplete(true);
        p.setDateCompletion(LocalDateTime.now());
        return toProgDTO(progRepo.save(p));
    }

    public ProgressionDTO soumettreQuiz(Long utilisateurId, Long coursId,
                                        List<ReponseQuizDTO> reponses) {
        List<QuizQuestion> questions = quizRepo.findByCoursIdOrderByOrdreAsc(coursId);
        if (questions.isEmpty()) throw new RuntimeException("Aucune question pour ce quiz");

        Map<Long, String> repMap = reponses.stream()
            .collect(Collectors.toMap(ReponseQuizDTO::getQuestionId, ReponseQuizDTO::getReponse));

        int total  = questions.stream().mapToInt(q -> q.getPoints() != null ? q.getPoints() : 1).sum();
        int obtenu = questions.stream()
            .filter(q -> q.getBonneReponse() != null
                      && q.getBonneReponse().equalsIgnoreCase(repMap.getOrDefault(q.getId(), "")))
            .mapToInt(q -> q.getPoints() != null ? q.getPoints() : 1)
            .sum();

        int score = total > 0 ? (int) Math.round(obtenu * 100.0 / total) : 0;

        ProgressionApprenant p = progRepo
            .findByUtilisateurIdAndCoursId(utilisateurId, coursId)
            .orElseGet(() -> {
                CoursEnLigne c = coursRepo.findById(coursId)
                    .orElseThrow(() -> new RuntimeException("Cours introuvable"));
                ProgressionApprenant np = new ProgressionApprenant();
                np.setUtilisateurId(utilisateurId);
                np.setCours(c);
                np.setDateAcces(LocalDateTime.now());
                return np;
            });
        p.setScoreQuiz(score);
        p.setComplete(true);
        p.setDateCompletion(LocalDateTime.now());
        return toProgDTO(progRepo.save(p));
    }

    public List<ProgressionDTO> progressionParOffre(Long utilisateurId, Long offreId) {
        return progRepo.findByUtilisateurIdAndCoursOffreId(utilisateurId, offreId)
            .stream().map(this::toProgDTO).collect(Collectors.toList());
    }

    // ─── Mappers ─────────────────────────────────────────────────────────────

    private static final DateTimeFormatter FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private void fillEntity(CoursEnLigne c, CoursEnLigneDTO dto) {
        c.setTitre(dto.getTitre());
        c.setDescription(dto.getDescription());
        c.setInstructionsDetaillees(dto.getInstructionsDetaillees());
        if (dto.getType() != null) c.setType(TypeCours.valueOf(dto.getType()));
        c.setUrlContenu(dto.getUrlContenu());
        c.setOrdre(dto.getOrdre());
        c.setDureeMinutes(dto.getDureeMinutes());
        c.setPublie(dto.isPublie());
        c.setLienVisio(dto.getLienVisio());
        c.setPlateforme(dto.getPlateforme());
        if (dto.getDateHeureLive() != null && !dto.getDateHeureLive().isBlank())
            c.setDateHeureLive(LocalDateTime.parse(dto.getDateHeureLive(), FMT));
        // TP / DEVOIR / EXAMEN
        if (dto.getDateDebut() != null && !dto.getDateDebut().isBlank())
            c.setDateDebut(LocalDateTime.parse(dto.getDateDebut(), FMT));
        else
            c.setDateDebut(null);
        if (dto.getDateLimite() != null && !dto.getDateLimite().isBlank())
            c.setDateLimite(LocalDateTime.parse(dto.getDateLimite(), FMT));
        else
            c.setDateLimite(null);
        c.setBaremeNote(dto.getBaremeNote());
        c.setDureeExamen(dto.getDureeExamen());
        c.setTypeExamen(dto.getTypeExamen());
        if (dto.getOffreId() != null)
            offreRepo.findById(dto.getOffreId()).ifPresent(c::setOffre);
        if (dto.getFormateurId() != null)
            formateurRepo.findById(dto.getFormateurId()).ifPresent(c::setFormateur);
        else
            c.setFormateur(null);
    }

    CoursEnLigneDTO toDTO(CoursEnLigne c) {
        CoursEnLigneDTO dto = new CoursEnLigneDTO();
        dto.setId(c.getId());
        dto.setTitre(c.getTitre());
        dto.setDescription(c.getDescription());
        dto.setInstructionsDetaillees(c.getInstructionsDetaillees());
        dto.setType(c.getType() != null ? c.getType().name() : null);
        dto.setUrlContenu(c.getUrlContenu());
        dto.setOrdre(c.getOrdre());
        dto.setDureeMinutes(c.getDureeMinutes());
        dto.setPublie(c.isPublie());
        dto.setLienVisio(c.getLienVisio());
        dto.setPlateforme(c.getPlateforme());
        if (c.getDateHeureLive() != null) dto.setDateHeureLive(c.getDateHeureLive().format(FMT));
        if (c.getDateDebut()     != null) dto.setDateDebut(c.getDateDebut().format(FMT));
        if (c.getDateLimite()    != null) dto.setDateLimite(c.getDateLimite().format(FMT));
        dto.setBaremeNote(c.getBaremeNote());
        dto.setDureeExamen(c.getDureeExamen());
        dto.setTypeExamen(c.getTypeExamen());
        if (c.getOffre() != null) {
            dto.setOffreId(c.getOffre().getId());
            dto.setOffreTitre(c.getOffre().getTitre());
        }
        if (c.getFormateur() != null) {
            dto.setFormateurId(c.getFormateur().getId());
            dto.setFormateurNom(c.getFormateur().getPrenom() + " " + c.getFormateur().getNom());
        }
        dto.setCheminFichier(c.getCheminFichier());
        dto.setNomFichierOriginal(c.getNomFichierOriginal());
        dto.setTailleFichier(c.getTailleFichier());
        if (c.getCheminFichier() != null) {
            String nom = c.getCheminFichier().substring(c.getCheminFichier().lastIndexOf('/') + 1);
            dto.setUrlFichier("/api/elearning/fichiers/" + nom);
        }
        List<QuizQuestionDTO> qs = c.getQuestions() != null
            ? c.getQuestions().stream().map(this::toQuizDTO).collect(Collectors.toList())
            : List.of();
        dto.setQuestions(qs);
        dto.setNbQuestions(qs.size());
        return dto;
    }

    private QuizQuestionDTO toQuizDTO(QuizQuestion q) {
        QuizQuestionDTO dto = new QuizQuestionDTO();
        dto.setId(q.getId());
        dto.setQuestion(q.getQuestion());
        dto.setOptionA(q.getOptionA());
        dto.setOptionB(q.getOptionB());
        dto.setOptionC(q.getOptionC());
        dto.setOptionD(q.getOptionD());
        dto.setBonneReponse(q.getBonneReponse());
        dto.setPoints(q.getPoints());
        dto.setOrdre(q.getOrdre());
        if (q.getCours() != null) dto.setCoursId(q.getCours().getId());
        return dto;
    }

    private ProgressionDTO toProgDTO(ProgressionApprenant p) {
        ProgressionDTO dto = new ProgressionDTO();
        dto.setId(p.getId());
        dto.setUtilisateurId(p.getUtilisateurId());
        dto.setComplete(p.isComplete());
        dto.setScoreQuiz(p.getScoreQuiz());
        if (p.getDateAcces()      != null) dto.setDateAcces(p.getDateAcces().format(FMT));
        if (p.getDateCompletion() != null) dto.setDateCompletion(p.getDateCompletion().format(FMT));
        if (p.getCours() != null) {
            dto.setCoursId(p.getCours().getId());
            dto.setCoursTitre(p.getCours().getTitre());
            dto.setCoursType(p.getCours().getType() != null ? p.getCours().getType().name() : null);
        }
        return dto;
    }
}
