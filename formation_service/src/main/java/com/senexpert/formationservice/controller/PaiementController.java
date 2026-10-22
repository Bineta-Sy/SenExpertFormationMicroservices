package com.senexpert.formationservice.controller;

import com.senexpert.formationservice.dto.PaiementDTO;
import com.senexpert.formationservice.entity.DemandeFormation;
import com.senexpert.formationservice.entity.Paiement;
import com.senexpert.formationservice.entity.StatutDemande;
import com.senexpert.formationservice.repository.DemandeFormationRepository;
import com.senexpert.formationservice.repository.PaiementRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/paiements")
public class PaiementController {

    private final PaiementRepository repo;
    private final DemandeFormationRepository demandeRepo;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public PaiementController(PaiementRepository repo, DemandeFormationRepository demandeRepo) {
        this.repo = repo;
        this.demandeRepo = demandeRepo;
    }

    /** Enregistre un paiement et passe la demande à CONFIRMEE si le paiement est un succès. */
    @PostMapping
    @Transactional
    public ResponseEntity<PaiementDTO> enregistrer(@RequestBody PaiementDTO dto) {
        Paiement p = new Paiement();
        p.setUtilisateurId(dto.getUtilisateurId());
        p.setOffreId(dto.getOffreId() != null ? dto.getOffreId() : 0L);
        p.setDemandeId(dto.getDemandeId());
        p.setOffreTitre(dto.getOffreTitre() != null ? dto.getOffreTitre() : "");
        p.setMontant(dto.getMontant() != null ? dto.getMontant() : 0.0);
        p.setMethodePaiement(dto.getMethodePaiement() != null ? dto.getMethodePaiement() : "INCONNU");
        // Auto-générer une référence unique si absente
        String ref = dto.getReference();
        if (ref == null || ref.isBlank()) {
            ref = "PAY-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
        }
        p.setReference(ref);
        p.setStatut(dto.getStatut() != null ? dto.getStatut() : "SUCCES");
        PaiementDTO saved = toDTO(repo.save(p));

        // Si paiement réussi et demande connue → passer à CONFIRMEE
        if ("SUCCES".equals(p.getStatut()) && p.getDemandeId() != null) {
            demandeRepo.findById(p.getDemandeId()).ifPresent(d -> {
                if (d.getStatut() == StatutDemande.VALIDEE) {
                    d.setStatut(StatutDemande.CONFIRMEE);
                    demandeRepo.save(d);
                }
            });
        }

        return ResponseEntity.ok(saved);
    }

    /** Historique des paiements pour une demande d'institution. */
    @GetMapping("/demande/{demandeId}")
    public ResponseEntity<List<PaiementDTO>> paiementsDemande(@PathVariable Long demandeId) {
        return ResponseEntity.ok(
            repo.findByDemandeIdOrderByDatePaiementDesc(demandeId)
                .stream().map(this::toDTO).collect(Collectors.toList()));
    }

    /** Vérifie si une demande a déjà un paiement SUCCES. */
    @GetMapping("/demande/{demandeId}/statut")
    public ResponseEntity<Map<String, Object>> statutPaiementDemande(@PathVariable Long demandeId) {
        boolean paye = repo.existsByDemandeIdAndStatut(demandeId, "SUCCES");
        return ResponseEntity.ok(Map.of("paye", paye));
    }

    /** Historique des paiements d'un utilisateur. */
    @GetMapping("/utilisateur/{userId}")
    public ResponseEntity<List<PaiementDTO>> mesPaiements(@PathVariable Long userId) {
        return ResponseEntity.ok(
            repo.findByUtilisateurIdOrderByDatePaiementDesc(userId)
                .stream().map(this::toDTO).collect(Collectors.toList()));
    }

    /** Total payé par un utilisateur. */
    @GetMapping("/utilisateur/{userId}/total")
    public ResponseEntity<Map<String, Object>> total(@PathVariable Long userId) {
        Double t = repo.totalPaye(userId);
        return ResponseEntity.ok(Map.of("total", t != null ? t : 0.0));
    }

    private PaiementDTO toDTO(Paiement p) {
        PaiementDTO d = new PaiementDTO();
        d.setId(p.getId());
        d.setUtilisateurId(p.getUtilisateurId());
        d.setOffreId(p.getOffreId());
        d.setDemandeId(p.getDemandeId());
        d.setOffreTitre(p.getOffreTitre());
        d.setMontant(p.getMontant());
        d.setMethodePaiement(p.getMethodePaiement());
        d.setReference(p.getReference());
        d.setDatePaiement(p.getDatePaiement() != null ? p.getDatePaiement().format(FMT) : "");
        d.setStatut(p.getStatut());
        return d;
    }
}
