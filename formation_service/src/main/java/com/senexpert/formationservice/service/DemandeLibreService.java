package com.senexpert.formationservice.service;

import com.senexpert.formationservice.dto.DemandeLibreDTO;
import com.senexpert.formationservice.dto.FormationSouhaiteeDTO;
import com.senexpert.formationservice.entity.DemandeLibre;
import com.senexpert.formationservice.entity.FormationSouhaitee;
import com.senexpert.formationservice.entity.OffreFormation;
import com.senexpert.formationservice.entity.StatutDemande;
import com.senexpert.formationservice.entity.StatutOffre;
import com.senexpert.formationservice.entity.TypeFormation;
import com.senexpert.formationservice.exception.BusinessException;
import com.senexpert.formationservice.exception.ResourceNotFoundException;
import com.senexpert.formationservice.repository.DemandeLibreRepository;
import com.senexpert.formationservice.repository.FormationSouhaiteeRepository;
import com.senexpert.formationservice.repository.OffreFormationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class DemandeLibreService {

    @Autowired
    private DemandeLibreRepository repository;

    @Autowired
    private FormationSouhaiteeRepository formationRepo;

    @Autowired
    private OffreFormationRepository offreRepository;

    public DemandeLibre creer(DemandeLibreDTO dto) {
        DemandeLibre d = new DemandeLibre();
        d.setUtilisateurId(dto.getUtilisateurId());
        d.setInstitutionNom(dto.getInstitutionNom());
        d.setDomaine(dto.getDomaine());
        d.setDescription(dto.getDescription());
        d.setNbParticipants(dto.getNbParticipants());
        d.setDateDemande(LocalDate.now());
        d.setStatut(StatutDemande.EN_ATTENTE);
        if (dto.getPeriodeDebut() != null && !dto.getPeriodeDebut().isBlank())
            d.setPeriodeDebut(LocalDate.parse(dto.getPeriodeDebut()));
        if (dto.getPeriodeFin() != null && !dto.getPeriodeFin().isBlank())
            d.setPeriodeFin(LocalDate.parse(dto.getPeriodeFin()));
        return repository.save(d);
    }

    public List<DemandeLibre> listerParUtilisateur(Long utilisateurId) {
        return repository.findByUtilisateurId(utilisateurId);
    }

    public List<DemandeLibre> listerToutes() {
        return repository.findAll();
    }

    public DemandeLibre valider(Long id) {
        DemandeLibre d = trouver(id);
        d.setStatut(StatutDemande.VALIDEE);
        repository.save(d);
        // Creer automatiquement une OffreFormation en Brouillon
        creerOffreDepuisDemande(d);
        return d;
    }

    private void creerOffreDepuisDemande(DemandeLibre d) {
        // Eviter les doublons
        if (offreRepository.findByDemandeLibreId(d.getId()).isPresent()) return;
        OffreFormation offre = new OffreFormation();
        offre.setTitre("Formation sur mesure - " + d.getDomaine());
        offre.setDescription(d.getDescription());
        offre.setPlaces(d.getNbParticipants());
        offre.setDateDebut(d.getPeriodeDebut());
        offre.setDateFin(d.getPeriodeFin());
        offre.setStatut(StatutOffre.BROUILLON);
        offre.setType(TypeFormation.EN_LIGNE);
        offre.setDemandeLibreId(d.getId());
        OffreFormation saved = offreRepository.save(offre);
        // Stocker l'offreId sur la demande pour que l'agent puisse y acceder
        d.setOffreId(saved.getId());
        repository.save(d);
    }

    public DemandeLibre rejeter(Long id) {
        DemandeLibre d = trouver(id);
        d.setStatut(StatutDemande.REJETEE);
        return repository.save(d);
    }

    public void annuler(Long id) {
        DemandeLibre d = trouver(id);
        if (d.getStatut() != StatutDemande.EN_ATTENTE) {
            throw new BusinessException("Seules les demandes en attente peuvent etre annulees.");
        }
        repository.delete(d);
    }

    public DemandeLibre mettreAJourParticipants(Long id, List<Long> participantIds) {
        DemandeLibre d = trouver(id);
        d.setParticipantIds(participantIds);
        return repository.save(d);
    }

    public FormationSouhaitee ajouterFormation(Long demandeId, FormationSouhaiteeDTO dto) {
        DemandeLibre d = trouver(demandeId);
        FormationSouhaitee fs = new FormationSouhaitee();
        fs.setThematique(dto.getThematique());
        fs.setDuree(dto.getDuree());
        fs.setNbParticipants(dto.getNbParticipants());
        fs.setObjectifs(dto.getObjectifs());
        fs.setNiveauRequis(dto.getNiveauRequis());
        d.getFormationsSouhaitees().add(fs);
        repository.save(d);
        return fs;
    }

    public List<FormationSouhaitee> listerFormations(Long demandeId) {
        return trouver(demandeId).getFormationsSouhaitees();
    }

    public void supprimerFormation(Long demandeId, Long formationId) {
        DemandeLibre d = trouver(demandeId);
        d.getFormationsSouhaitees().removeIf(f -> f.getId().equals(formationId));
        repository.save(d);
    }

    /** Crée les offres manquantes pour toutes les demandes VALIDEE. Retourne le nombre créé. */
    public int creerOffresManquantes() {
        List<DemandeLibre> validees = repository.findAll().stream()
                .filter(d -> d.getStatut() == StatutDemande.VALIDEE)
                .collect(java.util.stream.Collectors.toList());
        int count = 0;
        for (DemandeLibre d : validees) {
            java.util.Optional<OffreFormation> existing = offreRepository.findByDemandeLibreId(d.getId());
            if (existing.isPresent()) {
                // Offre existe mais offreId non sauvegarde sur la demande → corriger
                if (d.getOffreId() == null) {
                    d.setOffreId(existing.get().getId());
                    repository.save(d);
                    count++;
                }
            } else {
                creerOffreDepuisDemande(d);
                count++;
            }
        }
        return count;
    }

    private DemandeLibre trouver(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Demande non trouvee : id=" + id));
    }
}
