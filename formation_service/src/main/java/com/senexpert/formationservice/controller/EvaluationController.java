package com.senexpert.formationservice.controller;

import com.senexpert.formationservice.dto.EvaluationDTO;
import com.senexpert.formationservice.entity.EvaluationFormation;
import com.senexpert.formationservice.repository.EvaluationFormationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/evaluations")
public class EvaluationController {

    private final EvaluationFormationRepository repo;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public EvaluationController(EvaluationFormationRepository repo) {
        this.repo = repo;
    }

    /** Soumettre ou mettre à jour une évaluation (upsert). */
    @PostMapping
    public ResponseEntity<EvaluationDTO> evaluer(@RequestBody EvaluationDTO dto) {
        if (dto.getNote() < 1 || dto.getNote() > 5) {
            return ResponseEntity.badRequest().build();
        }
        Optional<EvaluationFormation> existing =
                repo.findByApprenantIdAndOffreId(dto.getApprenantId(), dto.getOffreId());
        EvaluationFormation e = existing.orElse(new EvaluationFormation());
        e.setApprenantId(dto.getApprenantId());
        e.setOffreId(dto.getOffreId());
        e.setNote(dto.getNote());
        e.setCommentaire(dto.getCommentaire());
        return ResponseEntity.ok(toDTO(repo.save(e)));
    }

    /** Évaluation d'un apprenant pour une offre donnée. */
    @GetMapping("/apprenant/{userId}/offre/{offreId}")
    public ResponseEntity<EvaluationDTO> monEvaluation(
            @PathVariable Long userId,
            @PathVariable Long offreId) {
        return repo.findByApprenantIdAndOffreId(userId, offreId)
                .map(e -> ResponseEntity.ok(toDTO(e)))
                .orElse(ResponseEntity.noContent().build());
    }

    /** Toutes les évaluations d'un apprenant. */
    @GetMapping("/apprenant/{userId}")
    public ResponseEntity<List<EvaluationDTO>> parApprenant(@PathVariable Long userId) {
        return ResponseEntity.ok(repo.findByApprenantId(userId)
                .stream().map(this::toDTO).collect(Collectors.toList()));
    }

    /** Moyenne et nombre d'avis pour une offre. */
    @GetMapping("/offre/{offreId}/stats")
    public ResponseEntity<Map<String, Object>> statsOffre(@PathVariable Long offreId) {
        Double moy = repo.moyenneParOffre(offreId);
        long nb    = repo.nombreParOffre(offreId);
        return ResponseEntity.ok(Map.of(
                "moyenne",  moy != null ? Math.round(moy * 10.0) / 10.0 : 0.0,
                "nombre",   nb));
    }

    /** Toutes les évaluations d'une offre (pour formateur/admin). */
    @GetMapping("/offre/{offreId}")
    public ResponseEntity<List<EvaluationDTO>> parOffre(@PathVariable Long offreId) {
        return ResponseEntity.ok(repo.findByOffreId(offreId)
                .stream().map(this::toDTO).collect(Collectors.toList()));
    }

    private EvaluationDTO toDTO(EvaluationFormation e) {
        EvaluationDTO d = new EvaluationDTO();
        d.setId(e.getId());
        d.setApprenantId(e.getApprenantId());
        d.setOffreId(e.getOffreId());
        d.setNote(e.getNote());
        d.setCommentaire(e.getCommentaire());
        d.setDateEvaluation(e.getDateEvaluation() != null ? e.getDateEvaluation().format(FMT) : "");
        return d;
    }
}
