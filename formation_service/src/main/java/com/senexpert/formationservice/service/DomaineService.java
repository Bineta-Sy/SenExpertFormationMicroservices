package com.senexpert.formationservice.service;

import com.senexpert.formationservice.entity.Domaine;
import com.senexpert.formationservice.exception.ResourceNotFoundException;
import com.senexpert.formationservice.repository.DomaineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DomaineService {

    @Autowired
    private DomaineRepository domaineRepository;

    public Domaine creer(Domaine domaine) {
        return domaineRepository.save(domaine);
    }

    public List<Domaine> listerTous() {
        return domaineRepository.findAll();
    }

    public Domaine trouverParId(Long id) {
        return domaineRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Domaine non trouvé : id=" + id));
    }

    public void supprimer(Long id) {
        domaineRepository.delete(trouverParId(id));
    }
}