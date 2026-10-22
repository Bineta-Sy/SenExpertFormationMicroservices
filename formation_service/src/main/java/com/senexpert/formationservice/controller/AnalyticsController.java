package com.senexpert.formationservice.controller;

import com.senexpert.formationservice.entity.*;
import com.senexpert.formationservice.repository.CandidatureRepository;
import com.senexpert.formationservice.repository.DemandeFormationRepository;
import com.senexpert.formationservice.repository.DomaineRepository;
import com.senexpert.formationservice.repository.OffreFormationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    @Autowired private OffreFormationRepository offreRepo;
    @Autowired private CandidatureRepository candidatureRepo;
    @Autowired private DemandeFormationRepository demandeRepo;
    @Autowired private DomaineRepository domaineRepo;

    // ─── 1. Alertes seuils ────────────────────────────────────────────────────

    /**
     * Retourne les offres dont le taux de remplissage (candidatures ACCEPTEE / places)
     * dépasse le seuil donné (défaut 80%).
     * Réponse : liste de maps { id, titre, places, acceptees, tauxRemplissage }
     */
    @GetMapping("/alertes")
    public ResponseEntity<List<Map<String, Object>>> alertesSeuils(
            @RequestParam(defaultValue = "80") int seuilPourcent) {

        List<OffreFormation> offres = offreRepo.findAll();
        List<Map<String, Object>> alertes = new ArrayList<>();

        for (OffreFormation offre : offres) {
            if (offre.getPlaces() == null || offre.getPlaces() == 0) continue;
            long acceptees = candidatureRepo.findByOffre_Id(offre.getId()).stream()
                    .filter(c -> StatutCandidature.ACCEPTEE.equals(c.getStatut()))
                    .count();
            int taux = (int) (acceptees * 100L / offre.getPlaces());
            if (taux >= seuilPourcent) {
                Map<String, Object> alerte = new LinkedHashMap<>();
                alerte.put("id", offre.getId());
                alerte.put("titre", offre.getTitre());
                alerte.put("places", offre.getPlaces());
                alerte.put("acceptees", acceptees);
                alerte.put("tauxRemplissage", taux);
                alerte.put("statut", offre.getStatut().name());
                alertes.add(alerte);
            }
        }

        // trier par taux décroissant
        alertes.sort((a, b) -> Integer.compare(
                (int) b.get("tauxRemplissage"), (int) a.get("tauxRemplissage")));
        return ResponseEntity.ok(alertes);
    }

    // ─── 2. Taux d'acceptation par domaine ────────────────────────────────────

    /**
     * Pour chaque domaine : nombre de candidatures totales, acceptées, taux.
     * Réponse : liste de maps { domaine, total, acceptees, tauxAcceptation }
     */
    @GetMapping("/acceptation-par-domaine")
    public ResponseEntity<List<Map<String, Object>>> acceptationParDomaine() {
        List<Domaine> domaines = domaineRepo.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Domaine domaine : domaines) {
            List<OffreFormation> offres = offreRepo.findByDomaineId(domaine.getId());
            long total = 0, acceptees = 0;
            for (OffreFormation offre : offres) {
                List<Candidature> cands = candidatureRepo.findByOffre_Id(offre.getId());
                total += cands.size();
                acceptees += cands.stream()
                        .filter(c -> StatutCandidature.ACCEPTEE.equals(c.getStatut()))
                        .count();
            }
            if (total == 0) continue;
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("domaine", domaine.getLibelle());
            row.put("total", total);
            row.put("acceptees", acceptees);
            row.put("tauxAcceptation", (int) (acceptees * 100L / total));
            result.add(row);
        }

        result.sort((a, b) -> Integer.compare(
                (int) b.get("tauxAcceptation"), (int) a.get("tauxAcceptation")));
        return ResponseEntity.ok(result);
    }

    // ─── 3. Évolution des demandes sur 12 mois ────────────────────────────────

    /**
     * Nombre de demandes créées par mois sur les 12 derniers mois.
     * Réponse : { labels: ["Jan", ...], data: [12, ...] }
     */
    @GetMapping("/demandes-par-mois")
    public ResponseEntity<Map<String, Object>> demandesParMois() {
        LocalDate aujourd = LocalDate.now();
        List<String> labels = new ArrayList<>();
        List<Long> data = new ArrayList<>();
        String[] moisNoms = {"Jan","Fév","Mar","Avr","Mai","Juin","Juil","Aoû","Sep","Oct","Nov","Déc"};

        List<DemandeFormation> toutes = demandeRepo.findAll();

        for (int i = 11; i >= 0; i--) {
            LocalDate mois = aujourd.minusMonths(i);
            int annee = mois.getYear();
            int m = mois.getMonthValue();
            long count = toutes.stream()
                    .filter(d -> d.getDateDemande() != null
                            && d.getDateDemande().getYear() == annee
                            && d.getDateDemande().getMonthValue() == m)
                    .count();
            labels.add(moisNoms[m - 1] + " " + (annee % 100));
            data.add(count);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("labels", labels);
        result.put("data", data);
        return ResponseEntity.ok(result);
    }

    // ─── 4. Domaines les plus demandés ────────────────────────────────────────

    /**
     * Nombre de demandes par domaine, trié par popularité décroissante.
     * Réponse : liste de maps { domaine, nbDemandes, nbOffres }
     */
    @GetMapping("/domaines-populaires")
    public ResponseEntity<List<Map<String, Object>>> domainesPopulaires() {
        List<Domaine> domaines = domaineRepo.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Domaine domaine : domaines) {
            List<OffreFormation> offres = offreRepo.findByDomaineId(domaine.getId());
            long nbDemandes = 0;
            for (OffreFormation offre : offres) {
                nbDemandes += demandeRepo.findByOffreId(offre.getId()).size();
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("domaine", domaine.getLibelle());
            row.put("nbDemandes", nbDemandes);
            row.put("nbOffres", offres.size());
            result.add(row);
        }

        result.sort((a, b) -> Long.compare((long) b.get("nbDemandes"), (long) a.get("nbDemandes")));
        return ResponseEntity.ok(result);
    }

    // ─── 5. Résumé global (pour le poll temps réel) ───────────────────────────

    /**
     * Snapshot rapide pour le rafraîchissement temps réel du dashboard.
     */
    @GetMapping("/snapshot")
    public ResponseEntity<Map<String, Object>> snapshot() {
        long totalCandidatures = candidatureRepo.count();
        long enAttente = candidatureRepo.findAll().stream()
                .filter(c -> StatutCandidature.EN_ATTENTE.equals(c.getStatut())).count();
        long acceptees = candidatureRepo.findAll().stream()
                .filter(c -> StatutCandidature.ACCEPTEE.equals(c.getStatut())).count();
        long totalDemandes = demandeRepo.count();
        long demandesEnAttente = demandeRepo.findAll().stream()
                .filter(d -> StatutDemande.EN_ATTENTE.equals(d.getStatut())).count();

        Map<String, Object> snap = new LinkedHashMap<>();
        snap.put("totalCandidatures", totalCandidatures);
        snap.put("candidaturesEnAttente", enAttente);
        snap.put("candidaturesAcceptees", acceptees);
        snap.put("totalDemandes", totalDemandes);
        snap.put("demandesEnAttente", demandesEnAttente);
        snap.put("totalOffres", offreRepo.count());
        snap.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(snap);
    }
}
