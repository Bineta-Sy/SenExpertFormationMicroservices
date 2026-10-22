package com.senexpert.userservice.service;

import com.senexpert.userservice.entity.Role;
import com.senexpert.userservice.entity.Statut;
import com.senexpert.userservice.entity.Utilisateur;
import com.senexpert.userservice.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Desactive automatiquement les comptes inactifs.
 * Un compte est considere inactif si :
 *   - derniereConnexion < now - joursInactivite  (ou jamais connecte depuis creation + joursInactivite)
 *   - statut = ACTIF  (on ne touche pas aux comptes deja INACTIF ou EN_ATTENTE)
 *   - role != ADMIN   (les admins ne sont jamais expires automatiquement)
 */
@Service
public class InactiviteScheduler {

    @Autowired private UtilisateurRepository utilisateurRepository;

    @Value("${app.inactivite.jours:90}")
    private int joursInactivite;

    /**
     * Tourne chaque nuit a 2h00.
     * Cron : "0 0 2 * * *"
     */
    @Scheduled(cron = "${app.inactivite.cron:0 0 2 * * *}")
    public void expirerComptesInactifsAuto() {
        int count = expirerComptesInactifs(joursInactivite);
        if (count > 0) {
            System.out.println("[InactiviteScheduler] " + count + " compte(s) expire(s) automatiquement.");
        }
    }

    /**
     * Logique d'expiration (appelee par le scheduler ET par l'endpoint manuel).
     * @param jours nombre de jours d'inactivite avant expiration
     * @return nombre de comptes expires
     */
    public int expirerComptesInactifs(int jours) {
        LocalDateTime seuil = LocalDateTime.now().minusDays(jours);
        List<Utilisateur> actifs = utilisateurRepository.findAll().stream()
                .filter(u -> Statut.ACTIF.equals(u.getStatut()))
                .filter(u -> u.getRole() != Role.ADMIN)
                .filter(u -> {
                    if (u.getDerniereConnexion() == null) {
                        // Jamais connecte : on ne l'expire pas (il n'a peut-etre pas encore eu le temps)
                        return false;
                    }
                    return u.getDerniereConnexion().isBefore(seuil);
                })
                .toList();

        for (Utilisateur u : actifs) {
            u.setStatut(Statut.INACTIF);
        }
        utilisateurRepository.saveAll(actifs);
        return actifs.size();
    }
}
