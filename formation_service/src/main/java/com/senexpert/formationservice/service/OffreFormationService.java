package com.senexpert.formationservice.service;

import com.senexpert.formationservice.dto.OffreFormationDTO;
import com.senexpert.formationservice.entity.Domaine;
import com.senexpert.formationservice.entity.OffreFormation;
import com.senexpert.formationservice.entity.StatutDemande;
import com.senexpert.formationservice.entity.StatutOffre;
import com.senexpert.formationservice.entity.TypeFormation;
import com.senexpert.formationservice.exception.ResourceNotFoundException;
import com.senexpert.formationservice.repository.DemandeFormationRepository;
import com.senexpert.formationservice.repository.ModuleRepository;
import com.senexpert.formationservice.repository.OffreFormationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.List;

@Service
public class OffreFormationService {

    @Autowired
    private OffreFormationRepository offreFormationRepository;

    @Autowired
    private DomaineService domaineService;

    @Autowired
    private DemandeFormationRepository demandeFormationRepository;

    @Autowired
    private ModuleRepository moduleRepository;

    private static final List<StatutDemande> STATUTS_ACTIFS =
        List.of(StatutDemande.EN_ATTENTE, StatutDemande.VALIDEE);

    /** Injecte placesDisponibles et nbModules sur une offre avant de la retourner. */
    private OffreFormation enrichir(OffreFormation offre) {
        if (offre.getPlaces() != null) {
            long prises = demandeFormationRepository
                .countByOffreIdAndStatutIn(offre.getId(), STATUTS_ACTIFS);
            offre.setPlacesDisponibles((int) Math.max(0, offre.getPlaces() - prises));
        } else {
            offre.setPlacesDisponibles(null);
        }
        offre.setNbModules((int) moduleRepository.countByOffreId(offre.getId()));
        return offre;
    }

    public OffreFormation creer(OffreFormationDTO dto) {
        OffreFormation offre = new OffreFormation();
        appliquerDTO(offre, dto);
        return offreFormationRepository.save(offre);
    }

    private void appliquerDTO(OffreFormation offre, OffreFormationDTO dto) {
        offre.setTitre(dto.getTitre());
        offre.setDescription(dto.getDescription());
        offre.setDateDebut(dto.getDateDebut());
        offre.setDateFin(dto.getDateFin());
        offre.setPlaces(dto.getPlaces());
        offre.setType(TypeFormation.valueOf(dto.getType()));
        if (dto.getNiveau() != null) offre.setNiveau(dto.getNiveau());
        if (dto.getDuree() != null) offre.setDuree(dto.getDuree());
        if (dto.getCout() != null) offre.setCout(dto.getCout());
        if (dto.getDomaineId() != null) offre.setDomaine(domaineService.trouverParId(dto.getDomaineId()));
        if (dto.getDocumentsRequis() != null) offre.setDocumentsRequis(dto.getDocumentsRequis());
        if (dto.getPrestataireId() != null) offre.setPrestataireId(dto.getPrestataireId());
        if (dto.getPrestataireRole() != null) offre.setPrestataireRole(dto.getPrestataireRole());
        if (dto.getProgrammeUrl() != null) offre.setProgrammeUrl(dto.getProgrammeUrl());
        offre.setDateLimiteCandidature(dto.getDateLimiteCandidature()); // null = pas de limite
        // Nouveaux champs
        if (dto.getPrerequis() != null) offre.setPrerequis(dto.getPrerequis());
        if (dto.getLieu() != null) offre.setLieu(dto.getLieu());
        if (dto.getImageUrl() != null) offre.setImageUrl(dto.getImageUrl());
        if (dto.getLangue() != null) offre.setLangue(dto.getLangue());
        if (dto.getPublicCible() != null) offre.setPublicCible(dto.getPublicCible());
        if (dto.getModalitesPaiement() != null) offre.setModalitesPaiement(dto.getModalitesPaiement());
        if (dto.getFinancementDisponible() != null) offre.setFinancementDisponible(dto.getFinancementDisponible());
        if (dto.getCertification() != null) offre.setCertification(dto.getCertification());
    }

    /** Liste publique : seulement les offres DISPONIBLE (validées par l'admin). */
    public List<OffreFormation> listerToutes() {
        return offreFormationRepository.findByStatut(StatutOffre.DISPONIBLE).stream()
            .map(this::enrichir).toList();
    }

    /** Liste publique filtrée par domaine : seulement DISPONIBLE. */
    public List<OffreFormation> listerParDomaine(Long domaineId) {
        return offreFormationRepository.findByDomaineIdAndStatut(domaineId, StatutOffre.DISPONIBLE).stream()
            .map(this::enrichir).toList();
    }

    /** Liste complète pour l'admin (tous statuts). */
    public List<OffreFormation> listerToutesAdmin() {
        return offreFormationRepository.findAll().stream()
            .map(this::enrichir).toList();
    }

    public OffreFormation trouverParId(Long id) {
        return enrichir(offreFormationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Offre non trouvée : id=" + id)));
    }

    public OffreFormation modifier(Long id, OffreFormationDTO dto) {
        OffreFormation offre = trouverParId(id);
        appliquerDTO(offre, dto);
        return offreFormationRepository.save(offre);
    }

    public OffreFormation publier(Long id) {
        OffreFormation offre = trouverParId(id);
        offre.setStatut(StatutOffre.DISPONIBLE);
        return offreFormationRepository.save(offre);
    }

    public OffreFormation fermer(Long id) {
        OffreFormation offre = trouverParId(id);
        offre.setStatut(StatutOffre.FERME);
        return offreFormationRepository.save(offre);
    }

    /** Met à jour uniquement la date limite de candidature */
    public OffreFormation majDateLimiteCandidature(Long id, LocalDate date) {
        OffreFormation offre = trouverParId(id);
        offre.setDateLimiteCandidature(date);
        return offreFormationRepository.save(offre);
    }

    public void supprimer(Long id) {
        offreFormationRepository.delete(trouverParId(id));
    }

    // ── Prestataire ──────────────────────────────────────────────────────────

    /** Formations d'un prestataire (tous statuts, triées par id desc) */
    public List<OffreFormation> mesFormations(Long prestataireId) {
        return offreFormationRepository.findByPrestataireIdOrderByIdDesc(prestataireId).stream()
                .map(this::enrichir).toList();
    }

    /** Soumet une formation en brouillon à l'admin pour validation (réinitialise le rejet si présent) */
    public OffreFormation soumettre(Long id, Long prestataireId) {
        OffreFormation offre = trouverParId(id);
        if (!prestataireId.equals(offre.getPrestataireId())) {
            throw new com.senexpert.formationservice.exception.BusinessException(
                    "Vous n'êtes pas le prestataire de cette offre.");
        }
        offre.setSoumis(true);
        offre.setRejete(false);
        offre.setMotifRejet(null);
        return offreFormationRepository.save(offre);
    }

    /** Retirer la soumission (reprendre en brouillon) */
    public OffreFormation annulerSoumission(Long id, Long prestataireId) {
        OffreFormation offre = trouverParId(id);
        if (!prestataireId.equals(offre.getPrestataireId())) {
            throw new com.senexpert.formationservice.exception.BusinessException(
                    "Vous n'êtes pas le prestataire de cette offre.");
        }
        offre.setSoumis(false);
        return offreFormationRepository.save(offre);
    }

    /** Admin : liste toutes les formations soumises en attente de validation */
    public List<OffreFormation> listerSoumises() {
        return offreFormationRepository.findBySoumisTrue().stream()
                .map(this::enrichir).toList();
    }

    /** Admin : rejette une formation soumise (repasse en brouillon, soumis=false, rejete=true) */
    public OffreFormation rejeter(Long id) {
        return rejeter(id, null);
    }

    /** Admin : rejette avec un motif */
    public OffreFormation rejeter(Long id, String motif) {
        OffreFormation offre = trouverParId(id);
        offre.setSoumis(false);
        offre.setStatut(StatutOffre.BROUILLON);
        offre.setRejete(true);
        if (motif != null && !motif.isBlank()) offre.setMotifRejet(motif);
        return offreFormationRepository.save(offre);
    }

}
