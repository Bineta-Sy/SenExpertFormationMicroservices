package com.senexpert.userservice.service;

import com.senexpert.userservice.entity.Branche;
import com.senexpert.userservice.exception.ResourceNotFoundException;
import com.senexpert.userservice.repository.BrancheRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BrancheService {

    @Autowired private BrancheRepository brancheRepository;

    public Branche creer(Branche branche) {
        return brancheRepository.save(branche);
    }

    public List<Branche> listerTous() {
        return brancheRepository.findAll();
    }

    public Branche trouverParId(Long id) {
        return brancheRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Branche non trouvée : id=" + id));
    }

    public Branche modifier(Long id, String nom, String code) {
        Branche b = trouverParId(id);
        if (nom  != null && !nom.isBlank())  b.setNom(nom);
        if (code != null && !code.isBlank()) b.setCode(code);
        return brancheRepository.save(b);
    }

    public void supprimer(Long id) {
        brancheRepository.delete(trouverParId(id));
    }
}
