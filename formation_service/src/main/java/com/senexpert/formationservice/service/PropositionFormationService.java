package com.senexpert.formationservice.service;

import com.senexpert.formationservice.dto.PropositionFormationDTO;
import com.senexpert.formationservice.entity.DemandeLibre;
import com.senexpert.formationservice.entity.OffreFormation;
import com.senexpert.formationservice.entity.PropositionFormation;
import com.senexpert.formationservice.entity.StatutDemande;
import com.senexpert.formationservice.entity.StatutOffre;
import com.senexpert.formationservice.entity.StatutProposition;
import com.senexpert.formationservice.entity.TypeFormation;
import com.senexpert.formationservice.exception.ResourceNotFoundException;
import com.senexpert.formationservice.repository.DemandeLibreRepository;
import com.senexpert.formationservice.repository.OffreFormationRepository;
import com.senexpert.formationservice.repository.PropositionFormationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class PropositionFormationService {

    @Autowired
    private PropositionFormationRepository repository;

    @Autowired
    private DemandeLibreRepository demandeLibreRepository;

    @Autowired
    private OffreFormationRepository offreRepository;

    public PropositionFormation creer(PropositionFormationDTO dto) {
        PropositionFormation p = new PropositionFormation();
        p.setDemandeLibreId(dto.getDemandeLibreId());
        p.setFormateurNom(dto.getFormateurNom());
        p.setLieu(dto.getLieu());
        p.setMontant(dto.getMontant());
        p.setProgramme(dto.getProgramme());
        p.setNoteAdmin(dto.getNoteAdmin());
        p.setDateProposition(LocalDate.now());
        p.setStatut(StatutProposition.EN_ATTENTE);
        if (dto.getDateDebut() != null && !dto.getDateDebut().isBlank())
            p.setDateDebut(LocalDate.parse(dto.getDateDebut()));
        if (dto.getDateFin() != null && !dto.getDateFin().isBlank())
            p.setDateFin(LocalDate.parse(dto.getDateFin()));
        if (dto.getMode() != null) p.setMode(dto.getMode());
        if (dto.getModulesProposition() != null && !dto.getModulesProposition().isEmpty())
            p.setModulesProposition(new java.util.ArrayList<>(dto.getModulesProposition()));
        repository.save(p);
        // Passer la demande en PROPOSITION_RECUE pour signaler a l'agent
        demandeLibreRepository.findById(p.getDemandeLibreId()).ifPresent(d -> {
            d.setStatut(StatutDemande.PROPOSITION_RECUE);
            demandeLibreRepository.save(d);
        });
        return p;
    }

    public List<PropositionFormation> listerParDemande(Long demandeLibreId) {
        return repository.findByDemandeLibreId(demandeLibreId);
    }

    public Optional<PropositionFormation> derniereparDemande(Long demandeLibreId) {
        return repository.findFirstByDemandeLibreIdOrderByDatePropositionDesc(demandeLibreId);
    }

    public PropositionFormation accepter(Long id) {
        PropositionFormation p = trouver(id);
        p.setStatut(StatutProposition.ACCEPTEE);
        repository.save(p);
        // Passer la demande a VALIDEE et creer l'offre correspondante
        demandeLibreRepository.findById(p.getDemandeLibreId()).ifPresent(d -> {
            d.setStatut(StatutDemande.VALIDEE);
            demandeLibreRepository.save(d);
            creerOffreDepuisProposition(d, p);
        });
        return p;
    }

    public PropositionFormation refuser(Long id, String motif) {
        PropositionFormation p = trouver(id);
        p.setStatut(StatutProposition.REFUSEE);
        p.setMotifRefus(motif);
        repository.save(p);
        // Repasser la demande en EN_ATTENTE pour permettre une nouvelle proposition
        demandeLibreRepository.findById(p.getDemandeLibreId()).ifPresent(d -> {
            d.setStatut(StatutDemande.EN_ATTENTE);
            demandeLibreRepository.save(d);
        });
        return p;
    }

    public List<PropositionFormation> listerToutes() {
        return repository.findAll();
    }

    private PropositionFormation trouver(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proposition non trouvee : id=" + id));
    }

    /**
     * Cree automatiquement une OffreFormation en Brouillon depuis une proposition acceptee.
     * Evite les doublons si l'offre existe deja (validation directe puis acceptation).
     */
    private void creerOffreDepuisProposition(DemandeLibre d, PropositionFormation p) {
        if (offreRepository.findByDemandeLibreId(d.getId()).isPresent()) return;
        OffreFormation offre = new OffreFormation();
        offre.setTitre("Formation sur mesure - " + d.getDomaine());
        offre.setDescription(d.getDescription());
        offre.setPlaces(d.getNbParticipants());
        offre.setDateDebut(p.getDateDebut() != null ? p.getDateDebut() : d.getPeriodeDebut());
        offre.setDateFin(p.getDateFin() != null ? p.getDateFin() : d.getPeriodeFin());
        offre.setLieu(p.getLieu());
        offre.setCout(p.getMontant() != null ? p.getMontant().doubleValue() : null);
        offre.setStatut(StatutOffre.BROUILLON);
        if (p.getMode() != null) {
            try { offre.setType(TypeFormation.valueOf(p.getMode())); }
            catch (IllegalArgumentException ignored) { offre.setType(TypeFormation.EN_LIGNE); }
        } else {
            offre.setType(TypeFormation.EN_LIGNE);
        }
        offre.setDemandeLibreId(d.getId());
        OffreFormation saved = offreRepository.save(offre);
        // Stocker l'offreId sur la demande
        d.setOffreId(saved.getId());
        demandeLibreRepository.save(d);
    }
}
