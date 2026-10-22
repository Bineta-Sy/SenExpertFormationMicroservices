package com.senexpert.formationservice.service;

import com.senexpert.formationservice.entity.OffreFormation;
import com.senexpert.formationservice.entity.SessionFormation;
import com.senexpert.formationservice.entity.StatutSession;
import com.senexpert.formationservice.exception.ResourceNotFoundException;
import com.senexpert.formationservice.repository.OffreFormationRepository;
import com.senexpert.formationservice.repository.SessionFormationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class SessionFormationService {

    @Autowired private SessionFormationRepository sessionRepo;
    @Autowired private OffreFormationRepository offreRepo;

    public List<SessionFormation> listerToutes() {
        return sessionRepo.findAll();
    }

    public List<SessionFormation> listerParOffre(Long offreId) {
        return sessionRepo.findByOffreId(offreId);
    }

    public SessionFormation trouverParId(Long id) {
        return sessionRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Session non trouvée : id=" + id));
    }

    public SessionFormation creer(Map<String, Object> data) {
        Long offreId = Long.valueOf(data.get("offreId").toString());
        OffreFormation offre = offreRepo.findById(offreId)
            .orElseThrow(() -> new ResourceNotFoundException("Offre non trouvée : id=" + offreId));

        SessionFormation s = new SessionFormation();
        s.setOffre(offre);
        s.setDateHeure(LocalDateTime.parse(data.get("dateHeure").toString()));
        s.setDureeMinutes(Integer.valueOf(data.get("dureeMinutes").toString()));
        s.setSalle(data.getOrDefault("salle", "").toString());
        s.setFormateur(data.getOrDefault("formateur", "").toString());
        s.setNbPlaces(data.containsKey("nbPlaces")
            ? Integer.valueOf(data.get("nbPlaces").toString()) : offre.getPlaces());
        return sessionRepo.save(s);
    }

    public SessionFormation modifier(Long id, Map<String, Object> data) {
        SessionFormation s = trouverParId(id);
        if (data.containsKey("dateHeure")) s.setDateHeure(LocalDateTime.parse(data.get("dateHeure").toString()));
        if (data.containsKey("dureeMinutes")) s.setDureeMinutes(Integer.valueOf(data.get("dureeMinutes").toString()));
        if (data.containsKey("salle")) s.setSalle(data.get("salle").toString());
        if (data.containsKey("formateur")) s.setFormateur(data.get("formateur").toString());
        if (data.containsKey("nbPlaces")) s.setNbPlaces(Integer.valueOf(data.get("nbPlaces").toString()));
        return sessionRepo.save(s);
    }

    public SessionFormation changerStatut(Long id, String statut) {
        SessionFormation s = trouverParId(id);
        s.setStatut(StatutSession.valueOf(statut));
        return sessionRepo.save(s);
    }

    public void supprimer(Long id) {
        sessionRepo.deleteById(id);
    }
}
