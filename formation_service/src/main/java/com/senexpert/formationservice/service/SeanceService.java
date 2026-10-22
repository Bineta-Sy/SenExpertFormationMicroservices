package com.senexpert.formationservice.service;

import com.senexpert.formationservice.dto.SeanceDTO;
import com.senexpert.formationservice.entity.Module;
import com.senexpert.formationservice.entity.Seance;
import com.senexpert.formationservice.exception.ResourceNotFoundException;
import com.senexpert.formationservice.repository.ModuleRepository;
import com.senexpert.formationservice.repository.SeanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class SeanceService {

    @Autowired private SeanceRepository seanceRepo;
    @Autowired private ModuleRepository moduleRepo;

    public List<Seance> parModule(Long moduleId) {
        return seanceRepo.findByModuleIdOrderByDateAscHeureDebutAsc(moduleId);
    }

    public List<Seance> parOffre(Long offreId) {
        return seanceRepo.findByOffreId(offreId);
    }

    public Seance trouver(Long id) {
        return seanceRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Séance non trouvée : id=" + id));
    }

    public Seance creer(SeanceDTO dto) {
        Module module = moduleRepo.findById(dto.getModuleId())
                .orElseThrow(() -> new ResourceNotFoundException("Module non trouvé : id=" + dto.getModuleId()));
        Seance s = new Seance();
        mapper(s, dto, module);
        return seanceRepo.save(s);
    }

    public Seance modifier(Long id, SeanceDTO dto) {
        Seance s = trouver(id);
        Module module = moduleRepo.findById(dto.getModuleId())
                .orElseThrow(() -> new ResourceNotFoundException("Module non trouvé : id=" + dto.getModuleId()));
        mapper(s, dto, module);
        return seanceRepo.save(s);
    }

    @Transactional
    public void supprimer(Long id) {
        seanceRepo.delete(trouver(id));
    }

    @Transactional
    public void supprimerParModule(Long moduleId) {
        seanceRepo.findByModuleIdOrderByDateAscHeureDebutAsc(moduleId)
                .forEach(seanceRepo::delete);
    }

    private void mapper(Seance s, SeanceDTO dto, Module module) {
        s.setModule(module);
        s.setDate(dto.getDate());
        s.setHeureDebut(dto.getHeureDebut());
        s.setHeureFin(dto.getHeureFin());
        s.setSalle(dto.getSalle());
        s.setLienVisio(dto.getLienVisio());
        s.setNotes(dto.getNotes());
    }
}
