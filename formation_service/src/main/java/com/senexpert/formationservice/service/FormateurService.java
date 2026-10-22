package com.senexpert.formationservice.service;

import com.senexpert.formationservice.dto.FormateurDTO;
import com.senexpert.formationservice.entity.Formateur;
import com.senexpert.formationservice.entity.OffreFormation;
import com.senexpert.formationservice.exception.ResourceNotFoundException;
import com.senexpert.formationservice.repository.FormateurRepository;
import com.senexpert.formationservice.repository.OffreFormationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FormateurService {

    @Autowired
    private FormateurRepository formateurRepository;

    @Autowired
    private OffreFormationRepository offreRepository;

    public List<Formateur> listerTous() {
        return formateurRepository.findAll();
    }

    public Formateur trouverParId(Long id) {
        return formateurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Formateur non trouvé : id=" + id));
    }

    public Formateur creer(FormateurDTO dto) {
        Formateur f = new Formateur();
        mapper(f, dto);
        return formateurRepository.save(f);
    }

    public Formateur modifier(Long id, FormateurDTO dto) {
        Formateur f = trouverParId(id);
        mapper(f, dto);
        return formateurRepository.save(f);
    }

    public void supprimer(Long id) {
        Formateur f = trouverParId(id);
        // Retirer de toutes les offres avant suppression
        List<OffreFormation> offres = offreRepository.findAll();
        for (OffreFormation o : offres) {
            if (o.getFormateurs().removeIf(x -> x.getId().equals(id))) {
                offreRepository.save(o);
            }
        }
        formateurRepository.delete(f);
    }

    public OffreFormation assignerAOffre(Long formateurId, Long offreId) {
        Formateur f = trouverParId(formateurId);
        OffreFormation o = offreRepository.findById(offreId)
                .orElseThrow(() -> new ResourceNotFoundException("Offre non trouvée : id=" + offreId));
        if (!o.getFormateurs().contains(f)) {
            o.getFormateurs().add(f);
            offreRepository.save(o);
        }
        return o;
    }

    public OffreFormation retirerDeOffre(Long formateurId, Long offreId) {
        Formateur f = trouverParId(formateurId);
        OffreFormation o = offreRepository.findById(offreId)
                .orElseThrow(() -> new ResourceNotFoundException("Offre non trouvée : id=" + offreId));
        o.getFormateurs().removeIf(x -> x.getId().equals(formateurId));
        return offreRepository.save(o);
    }

    public List<Formateur> formateursParOffre(Long offreId) {
        OffreFormation o = offreRepository.findById(offreId)
                .orElseThrow(() -> new ResourceNotFoundException("Offre non trouvée : id=" + offreId));
        return o.getFormateurs();
    }

    private void mapper(Formateur f, FormateurDTO dto) {
        f.setNom(dto.getNom());
        f.setPrenom(dto.getPrenom());
        f.setEmail(dto.getEmail());
        f.setTelephone(dto.getTelephone());
        f.setSpecialite(dto.getSpecialite());
        f.setBio(dto.getBio());
    }
}
