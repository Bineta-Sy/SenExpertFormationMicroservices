package com.senexpert.userservice.service;

import com.senexpert.userservice.entity.Institution;
import com.senexpert.userservice.entity.Statut;
import com.senexpert.userservice.exception.ResourceNotFoundException;
import com.senexpert.userservice.repository.InstitutionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class InstitutionService {

    @Autowired
    private InstitutionRepository institutionRepository;

    public Institution creer(Institution institution) {
        institution.setStatut(Statut.EN_ATTENTE);
        return institutionRepository.save(institution);
    }

    public List<Institution> listerToutes() {
        return institutionRepository.findAll();
    }

    public Institution valider(Long id) {
        Institution institution = institutionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Institution non trouvée : id=" + id));
        institution.setStatut(Statut.ACTIF);
        institution.setDateValidation(LocalDate.now());
        return institutionRepository.save(institution);
    }

    public Institution trouverParId(Long id) {
        return institutionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Institution non trouvée : id=" + id));
    }

    /** Désactive une institution (Admin) */
    public Institution desactiver(Long id) {
        Institution inst = trouverParId(id);
        inst.setStatut(Statut.INACTIF);
        return institutionRepository.save(inst);
    }

    /** Réactive une institution désactivée (Admin) */
    public Institution reactiver(Long id) {
        Institution inst = trouverParId(id);
        inst.setStatut(Statut.ACTIF);
        return institutionRepository.save(inst);
    }

    /** Supprime une institution (Admin) */
    public void supprimer(Long id) {
        institutionRepository.delete(trouverParId(id));
    }

    /** Modifie les informations d'une institution (agent connecté) */
    public Institution modifier(Long id, String nom, String adresse,
                                 String secteur, String ninea, String domainesInteret) {
        Institution inst = trouverParId(id);
        if (nom != null && !nom.isBlank()) inst.setNom(nom.trim());
        if (adresse != null)         inst.setAdresse(adresse.trim());
        if (secteur != null)         inst.setSecteur(secteur.trim());
        if (ninea != null)           inst.setNinea(ninea.trim());
        if (domainesInteret != null) inst.setDomainesInteret(domainesInteret.trim());
        return institutionRepository.save(inst);
    }
}
