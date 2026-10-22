package com.senexpert.formationservice.service;

import com.senexpert.formationservice.dto.ModuleDTO;
import com.senexpert.formationservice.entity.Formateur;
import com.senexpert.formationservice.entity.Module;
import com.senexpert.formationservice.entity.OffreFormation;
import com.senexpert.formationservice.exception.ResourceNotFoundException;
import com.senexpert.formationservice.repository.FormateurRepository;
import com.senexpert.formationservice.repository.ModuleRepository;
import com.senexpert.formationservice.repository.OffreFormationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ModuleService {

    @Autowired private ModuleRepository moduleRepository;
    @Autowired private OffreFormationRepository offreRepository;
    @Autowired private FormateurRepository formateurRepository;

    public List<Module> listerParOffre(Long offreId) {
        return moduleRepository.findByOffreIdOrderByOrdreAsc(offreId);
    }

    public Module trouverParId(Long id) {
        return moduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Module non trouvé : id=" + id));
    }

    public Module creer(ModuleDTO dto) {
        OffreFormation offre = offreRepository.findById(dto.getOffreId())
                .orElseThrow(() -> new ResourceNotFoundException("Offre non trouvée : id=" + dto.getOffreId()));
        Module m = new Module();
        mapper(m, dto, offre);
        return moduleRepository.save(m);
    }

    public Module modifier(Long id, ModuleDTO dto) {
        Module m = trouverParId(id);
        OffreFormation offre = offreRepository.findById(dto.getOffreId())
                .orElseThrow(() -> new ResourceNotFoundException("Offre non trouvée : id=" + dto.getOffreId()));
        mapper(m, dto, offre);
        return moduleRepository.save(m);
    }

    public Module assignerFormateur(Long moduleId, Long formateurId) {
        Module m = trouverParId(moduleId);
        Formateur f = formateurRepository.findById(formateurId)
                .orElseThrow(() -> new ResourceNotFoundException("Formateur non trouvé : id=" + formateurId));
        m.setFormateur(f);
        return moduleRepository.save(m);
    }

    public Module retirerFormateur(Long moduleId) {
        Module m = trouverParId(moduleId);
        m.setFormateur(null);
        return moduleRepository.save(m);
    }

    public void supprimer(Long id) {
        moduleRepository.delete(trouverParId(id));
    }

    @Transactional
    public void supprimerParOffre(Long offreId) {
        moduleRepository.deleteByOffreId(offreId);
    }

    private void mapper(Module m, ModuleDTO dto, OffreFormation offre) {
        m.setTitre(dto.getTitre());
        m.setDescription(dto.getDescription());
        m.setDureeHeures(dto.getDureeHeures());
        m.setOrdre(dto.getOrdre() != null ? dto.getOrdre() : 0);
        m.setOffre(offre);
        if (dto.getFormateurId() != null) {
            formateurRepository.findById(dto.getFormateurId())
                    .ifPresent(m::setFormateur);
        } else {
            m.setFormateur(null);
        }
    }
}
