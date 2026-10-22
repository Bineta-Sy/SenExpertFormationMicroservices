package com.senexpert.frontend.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.senexpert.frontend.model.*;
import com.senexpert.frontend.model.DemandeFormation;
import com.senexpert.frontend.model.DemandeLibreFront;
import com.senexpert.frontend.model.Domaine;
import com.senexpert.frontend.model.OffreFormation;
import com.senexpert.frontend.model.SessionFormationFront;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import jakarta.servlet.ServletContext;
import jakarta.inject.Inject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

@Named
@ApplicationScoped
public class FormationServiceClient {

    @Inject
    private ServletContext ctx;

    private String baseUrl;
    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @PostConstruct
    public void init() {
        // Priorité : variable d'environnement (Docker/K8s) > context-param (web.xml) > défaut
        String envUrl = System.getenv("FORMATION_SERVICE_URL");
        if (envUrl != null && !envUrl.isBlank()) {
            baseUrl = envUrl;
        } else {
            baseUrl = ctx.getInitParameter("FORMATION_SERVICE_URL");
            if (baseUrl == null) baseUrl = "http://localhost:8082";
        }
    }

    /** Helper : envoie un PUT vide et retourne true si 2xx */
    private boolean put(String url, String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + token)
                    .PUT(HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            return resp.statusCode() >= 200 && resp.statusCode() < 300;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    /** Liste les offres DISPONIBLE uniquement (endpoint public). */
    public List<OffreFormation> listerOffres() {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/offres"))
                    .GET().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                return mapper.readValue(resp.body(), new TypeReference<List<OffreFormation>>() {});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return List.of();
    }

    /** Liste toutes les offres (tous statuts : BROUILLON, DISPONIBLE, FERME).
     *  Réservé aux rôles ADMIN / FORMATEUR / AGENT. */
    public List<OffreFormation> listerOffresAdmin(String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/offres/admin"))
                    .header("Authorization", "Bearer " + token)
                    .GET().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                return mapper.readValue(resp.body(), new TypeReference<List<OffreFormation>>() {});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return List.of();
    }

    /** Charge une offre par son ID (endpoint public GET /api/offres/{id}). */
    public OffreFormation trouverOffreParId(Long id) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/offres/" + id))
                    .GET().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                return mapper.readValue(resp.body(), OffreFormation.class);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    /** Liste les offres par domaine */
    public List<OffreFormation> listerOffresParDomaine(Long domaineId) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/offres/domaine/" + domaineId))
                    .GET().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                return mapper.readValue(resp.body(), new TypeReference<List<OffreFormation>>() {});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return List.of();
    }

    /** Liste tous les domaines (public) */
    public List<Domaine> listerDomaines() {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/domaines"))
                    .GET().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                return mapper.readValue(resp.body(), new TypeReference<List<Domaine>>() {});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return List.of();
    }

    /**
     * Upload de documents de candidature à partir de bytes déjà lus en mémoire.
     * À utiliser depuis les beans @ViewScoped pour éviter le NoSuchFileException :
     * Undertow supprime le fichier temp à la fin de la requête AJAX, donc les bytes
     * doivent être lus immédiatement dans handleUpload et stockés en mémoire.
     */
    public List<String> uploadDocumentsDemande(Long utilisateurId,
                                               List<byte[]> fichiersBytes,
                                               List<String> noms,
                                               String token) {
        try {
            String boundary = "----FormBoundary" + System.currentTimeMillis();
            var body = new java.io.ByteArrayOutputStream();

            // Champ utilisateurId
            body.write(("--" + boundary + "\r\n"
                    + "Content-Disposition: form-data; name=\"utilisateurId\"\r\n\r\n"
                    + utilisateurId + "\r\n").getBytes());

            // Fichiers (bytes déjà en mémoire)
            for (int i = 0; i < fichiersBytes.size(); i++) {
                String nom = (noms != null && i < noms.size() && noms.get(i) != null)
                        ? noms.get(i) : "document";
                body.write(("--" + boundary + "\r\n"
                        + "Content-Disposition: form-data; name=\"fichiers\"; filename=\"" + nom + "\"\r\n"
                        + "Content-Type: application/octet-stream\r\n\r\n").getBytes());
                body.write(fichiersBytes.get(i));
                body.write("\r\n".getBytes());
            }
            body.write(("--" + boundary + "--\r\n").getBytes());

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/demandes/upload"))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(body.toByteArray()))
                    .build();

            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                return mapper.readValue(resp.body(), new TypeReference<List<String>>() {});
            }
            System.err.println("uploadDocumentsDemande HTTP " + resp.statusCode() + " : " + resp.body());
        } catch (Exception e) { e.printStackTrace(); }
        return List.of();
    }

    /**
     * Surcharge legacy avec UploadedFile — uniquement pour les cas où le fichier
     * est traité dans la même requête que l'upload (ex: upload synchrone sans auto="true").
     * @deprecated Préférer la surcharge avec List&lt;byte[]&gt; pour éviter le NoSuchFileException.
     */
    @Deprecated
    public List<String> uploadDocumentsDemande(Long utilisateurId,
                                               List<org.primefaces.model.file.UploadedFile> fichiers,
                                               String token) {
        try {
            List<byte[]> bytes = new java.util.ArrayList<>();
            List<String> noms  = new java.util.ArrayList<>();
            for (org.primefaces.model.file.UploadedFile f : fichiers) {
                bytes.add(f.getInputStream().readAllBytes());
                noms.add(f.getFileName() != null ? f.getFileName() : "document");
            }
            return uploadDocumentsDemande(utilisateurId, bytes, noms, token);
        } catch (Exception e) { e.printStackTrace(); }
        return List.of();
    }

    /** Soumet une demande de formation avec documents optionnels */
    public String soumettreDemande(Long utilisateurId, Long offreId,
                                   List<String> documents, String token) {
        try {
            ObjectNode body = mapper.createObjectNode();
            body.put("utilisateurId", utilisateurId);
            body.put("offreId", offreId);
            if (documents != null && !documents.isEmpty()) {
                body.set("documents", mapper.valueToTree(documents));
            }

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/demandes"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                    .build();

            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) return null; // succès
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> err = mapper.readValue(resp.body(), Map.class);
                Object msg = err.get("message");
                if (msg != null) return msg.toString();
            } catch (Exception ignored) {}
            return resp.body().isBlank() ? "Erreur inconnue" : resp.body();
        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur réseau : " + e.getMessage();
        }
    }

    /** Mes demandes (utilisateur connecté) */
    public List<DemandeFormation> mesDemandes(Long utilisateurId, String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/demandes/utilisateur/" + utilisateurId))
                    .header("Authorization", "Bearer " + token)
                    .GET().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                return mapper.readValue(resp.body(), new TypeReference<List<DemandeFormation>>() {});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return List.of();
    }

    /** Demandes d'une offre spécifique (Admin) */
    public List<DemandeFormation> demandesParOffre(Long offreId, String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/demandes/offre/" + offreId))
                    .header("Authorization", "Bearer " + token)
                    .GET().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200)
                return mapper.readValue(resp.body(), new TypeReference<List<DemandeFormation>>() {});
        } catch (Exception e) { e.printStackTrace(); }
        return List.of();
    }

    /** Toutes les demandes (Admin) */
    public List<DemandeFormation> toutesLesDemandes(String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/demandes"))
                    .header("Authorization", "Bearer " + token)
                    .GET().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                return mapper.readValue(resp.body(), new TypeReference<List<DemandeFormation>>() {});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return List.of();
    }

    /** Annuler une demande EN_ATTENTE (agent) */
    public boolean annulerDemande(Long id, String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/demandes/" + id))
                    .header("Authorization", "Bearer " + token)
                    .DELETE().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            return resp.statusCode() == 204 || resp.statusCode() == 200;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    /** Soumettre une demande à la carte */
    /** Créer demande et retourner l'entité (pour récupérer l'ID et poster les modules) */
    public com.senexpert.frontend.model.DemandeLibreFront creerDemandeLibre(
            Long utilisateurId, String institutionNom, String domaine,
            String description, Integer nbParticipants,
            String periodeDebut, String periodeFin, String token) {
        try {
            com.fasterxml.jackson.databind.node.ObjectNode body = mapper.createObjectNode();
            body.put("utilisateurId", utilisateurId);
            body.put("institutionNom", institutionNom != null ? institutionNom : "");
            body.put("domaine", domaine != null ? domaine : "");
            body.put("description", description != null ? description : "");
            if (nbParticipants != null) body.put("nbParticipants", nbParticipants);
            if (periodeDebut != null && !periodeDebut.isBlank()) body.put("periodeDebut", periodeDebut);
            if (periodeFin != null && !periodeFin.isBlank()) body.put("periodeFin", periodeFin);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/demandes-libre"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                    .build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200)
                return mapper.readValue(resp.body(), com.senexpert.frontend.model.DemandeLibreFront.class);
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public String soumettreDemandeLibre(Long utilisateurId, String institutionNom, String domaine,
                                        String description, Integer nbParticipants,
                                        String periodeDebut, String periodeFin, String token) {
        try {
            ObjectNode body = mapper.createObjectNode();
            body.put("utilisateurId", utilisateurId);
            body.put("institutionNom", institutionNom != null ? institutionNom : "");
            body.put("domaine", domaine != null ? domaine : "");
            body.put("description", description != null ? description : "");
            if (nbParticipants != null) body.put("nbParticipants", nbParticipants);
            if (periodeDebut != null && !periodeDebut.isBlank()) body.put("periodeDebut", periodeDebut);
            if (periodeFin != null && !periodeFin.isBlank()) body.put("periodeFin", periodeFin);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/demandes-libre"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                    .build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) return null;
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> err = mapper.readValue(resp.body(), Map.class);
                Object msg = err.get("message");
                if (msg != null) return msg.toString();
            } catch (Exception ignored) {}
            return resp.body().isBlank() ? "Erreur inconnue" : resp.body();
        } catch (Exception e) { e.printStackTrace(); return "Erreur réseau : " + e.getMessage(); }
    }

    /** Mes demandes à la carte */
    public List<DemandeLibreFront> mesDemandesLibres(Long utilisateurId, String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/demandes-libre/utilisateur/" + utilisateurId))
                    .header("Authorization", "Bearer " + token)
                    .GET().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200)
                return mapper.readValue(resp.body(), new TypeReference<List<DemandeLibreFront>>() {});
        } catch (Exception e) { e.printStackTrace(); }
        return List.of();
    }

    /** Toutes les demandes à la carte (Admin) */
    public List<DemandeLibreFront> toutesDemandesLibres(String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/demandes-libre"))
                    .header("Authorization", "Bearer " + token).GET().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200)
                return mapper.readValue(resp.body(), new TypeReference<List<DemandeLibreFront>>() {});
        } catch (Exception e) { e.printStackTrace(); }
        return List.of();
    }

    /** Valider une demande à la carte (Admin) */
    public boolean validerDemandeLibreAdmin(Long id, String token) {
        return put(baseUrl + "/api/demandes-libre/valider/" + id, token);
    }

    /** Rejeter une demande à la carte (Admin) */
    public boolean rejeterDemandeLibreAdmin(Long id, String token) {
        return put(baseUrl + "/api/demandes-libre/rejeter/" + id, token);
    }

    /** Annuler une demande à la carte */
    public boolean annulerDemandeLibre(Long id, String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/demandes-libre/" + id))
                    .header("Authorization", "Bearer " + token)
                    .DELETE().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            return resp.statusCode() == 204 || resp.statusCode() == 200;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    // ── Propositions de formation ─────────────────────────────────────────────

    /** Admin : créer une proposition pour une demande validée */
    public com.senexpert.frontend.model.PropositionFormationFront creerProposition(
            Long demandeLibreId, String formateurNom, String dateDebut, String dateFin,
            String lieu, String mode, Double montant, String programme, String noteAdmin,
            java.util.List<String> modules, String token) {
        try {
            com.fasterxml.jackson.databind.node.ObjectNode body = mapper.createObjectNode();
            body.put("demandeLibreId", demandeLibreId);
            if (formateurNom != null) body.put("formateurNom", formateurNom);
            if (dateDebut    != null) body.put("dateDebut",    dateDebut);
            if (dateFin      != null) body.put("dateFin",      dateFin);
            if (lieu         != null) body.put("lieu",         lieu);
            if (mode         != null) body.put("mode",         mode);
            if (montant      != null) body.put("montant",      montant);
            if (programme    != null) body.put("programme",    programme);
            if (noteAdmin    != null) body.put("noteAdmin",    noteAdmin);
            if (modules != null && !modules.isEmpty())
                body.set("modulesProposition", mapper.valueToTree(modules));
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/propositions"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                    .build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            System.out.println("[PROPOSITION] status=" + resp.statusCode() + " body=" + resp.body());
            if (resp.statusCode() == 200 || resp.statusCode() == 201)
                return mapper.readValue(resp.body(), com.senexpert.frontend.model.PropositionFormationFront.class);
        } catch (Exception e) {
            System.err.println("[PROPOSITION] exception: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /** Dernière proposition pour une demande à la carte */
    public com.senexpert.frontend.model.PropositionFormationFront derniereProposition(Long demandeLibreId, String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/propositions/demande/" + demandeLibreId + "/derniere"))
                    .header("Authorization", "Bearer " + token)
                    .GET().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200)
                return mapper.readValue(resp.body(), com.senexpert.frontend.model.PropositionFormationFront.class);
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    /** Agent : accepter une proposition */
    public boolean accepterProposition(Long propositionId, String token) {
        return put(baseUrl + "/api/propositions/" + propositionId + "/accepter", token);
    }

    /** Agent : refuser une proposition */
    public boolean refuserProposition(Long propositionId, String motif, String token) {
        try {
            com.fasterxml.jackson.databind.node.ObjectNode body = mapper.createObjectNode();
            if (motif != null) body.put("motif", motif);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/propositions/" + propositionId + "/refuser"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .PUT(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                    .build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            return resp.statusCode() == 200;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    /** Ajouter une formation souhaitée à une demande libre */
    public com.senexpert.frontend.model.FormationSouhaitee ajouterFormationSouhaitee(
            Long demandeId, String thematique, Integer duree, Integer nbParticipants,
            String objectifs, String niveauRequis, String token) {
        try {
            String body = String.format(
                "{\"thematique\":\"%s\",\"duree\":%s,\"nbParticipants\":%s,\"objectifs\":\"%s\",\"niveauRequis\":\"%s\"}",
                thematique != null ? thematique.replace("\"","\\\"") : "",
                duree != null ? duree : "null",
                nbParticipants != null ? nbParticipants : "null",
                objectifs != null ? objectifs.replace("\"","\\\"") : "",
                niveauRequis != null ? niveauRequis : "");
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/demandes-libre/" + demandeId + "/formations"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.ofString(body)).build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                return mapper.readValue(resp.body(), com.senexpert.frontend.model.FormationSouhaitee.class);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    /** Supprimer une formation souhaitée */
    public boolean supprimerFormationSouhaitee(Long demandeId, Long formationId, String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/demandes-libre/" + demandeId + "/formations/" + formationId))
                    .header("Authorization", "Bearer " + token)
                    .DELETE().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            return resp.statusCode() == 204 || resp.statusCode() == 200;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    /** Lister les formations souhaitées d'une demande */
    public List<com.senexpert.frontend.model.FormationSouhaitee> listerFormationsSouhaitees(Long demandeId, String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/demandes-libre/" + demandeId + "/formations"))
                    .header("Authorization", "Bearer " + token)
                    .GET().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                return mapper.readValue(resp.body(),
                    new TypeReference<List<com.senexpert.frontend.model.FormationSouhaitee>>() {});
            }
        } catch (Exception e) { e.printStackTrace(); }
        return List.of();
    }

    /** Valider une demande (Admin) */
    public boolean validerDemande(Long id, String token) {
        return put(baseUrl + "/api/demandes/valider/" + id, token);
    }

    /** Rejeter une demande (Admin) */
    public boolean rejeterDemande(Long id, String token) {
        return put(baseUrl + "/api/demandes/rejeter/" + id, token);
    }

    /** Crée une offre de formation (Admin) — version basique */
    public boolean creerOffre(String titre, String description, String dateDebut, String dateFin,
                               Integer places, String type, Long domaineId, String token) {
        return creerOffreComplete(titre, description, dateDebut, dateFin, places, type, domaineId,
                null, null, null, null, token);
    }

    /** Crée une offre de formation avec tous les champs (Admin) */
    public boolean creerOffreComplete(String titre, String description, String dateDebut, String dateFin,
                                       Integer places, String type, Long domaineId,
                                       String niveau, String duree, Double cout,
                                       List<String> documentsRequis, String token) {
        try {
            ObjectNode body = mapper.createObjectNode();
            body.put("titre", titre);
            if (description != null) body.put("description", description);
            body.put("dateDebut", dateDebut);
            body.put("dateFin", dateFin);
            body.put("places", places);
            body.put("type", type);
            body.put("domaineId", domaineId);
            if (niveau != null && !niveau.isBlank()) body.put("niveau", niveau);
            if (duree  != null && !duree.isBlank())  body.put("duree",  duree);
            if (cout   != null && cout > 0)           body.put("cout",   cout);
            if (documentsRequis != null && !documentsRequis.isEmpty())
                body.set("documentsRequis", mapper.valueToTree(documentsRequis));
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/offres"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                    .build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            return resp.statusCode() == 200 || resp.statusCode() == 201;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Supprime une offre (Admin) */
    public boolean supprimerOffre(Long id, String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/offres/" + id))
                    .header("Authorization", "Bearer " + token)
                    .DELETE().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            return resp.statusCode() == 200 || resp.statusCode() == 204;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Modifie une offre existante (Admin) — version basique */
    public boolean modifierOffre(Long id, String titre, String description, String dateDebut,
                                  String dateFin, Integer places, String type, Long domaineId, String token) {
        return modifierOffreComplete(id, titre, description, dateDebut, dateFin, places, type, domaineId,
                null, null, null, null, token);
    }

    /** Modifie une offre avec tous les champs (Admin) — rétrocompat sans programmeUrl */
    public boolean modifierOffreComplete(Long id, String titre, String description, String dateDebut,
                                          String dateFin, Integer places, String type, Long domaineId,
                                          String niveau, String duree, Double cout,
                                          List<String> documentsRequis, String token) {
        return modifierOffreComplete(id, titre, description, dateDebut, dateFin, places, type, domaineId,
                niveau, duree, cout, documentsRequis, null, token);
    }

    /** Modifie une offre avec tous les champs + programmeUrl */
    public boolean modifierOffreComplete(Long id, String titre, String description, String dateDebut,
                                          String dateFin, Integer places, String type, Long domaineId,
                                          String niveau, String duree, Double cout,
                                          List<String> documentsRequis, String programmeUrl, String token) {
        return modifierOffreComplete(id, titre, description, dateDebut, dateFin, places, type, domaineId,
                niveau, duree, cout, documentsRequis, programmeUrl, token,
                null, null, null, null, null, null, null, null);
    }

    /** Modifie une offre avec tous les champs (version complète) */
    public boolean modifierOffreComplete(Long id, String titre, String description, String dateDebut,
                                          String dateFin, Integer places, String type, Long domaineId,
                                          String niveau, String duree, Double cout,
                                          List<String> documentsRequis, String programmeUrl, String token,
                                          String prerequis, String lieu, String imageUrl, String langue,
                                          String publicCible, String modalitesPaiement,
                                          String financementDisponible, String certification) {
        try {
            ObjectNode body = mapper.createObjectNode();
            body.put("titre", titre);
            if (description != null) body.put("description", description);
            body.put("dateDebut", dateDebut);
            body.put("dateFin", dateFin);
            body.put("places", places);
            body.put("type", type);
            body.put("domaineId", domaineId);
            if (niveau != null && !niveau.isBlank()) body.put("niveau", niveau);
            if (duree  != null && !duree.isBlank())  body.put("duree",  duree);
            if (cout   != null && cout > 0)           body.put("cout",   cout);
            if (programmeUrl          != null && !programmeUrl.isBlank())          body.put("programmeUrl",          programmeUrl);
            if (prerequis             != null && !prerequis.isBlank())             body.put("prerequis",             prerequis);
            if (lieu                  != null && !lieu.isBlank())                  body.put("lieu",                  lieu);
            if (imageUrl              != null && !imageUrl.isBlank())              body.put("imageUrl",              imageUrl);
            if (langue                != null && !langue.isBlank())                body.put("langue",                langue);
            if (publicCible           != null && !publicCible.isBlank())           body.put("publicCible",           publicCible);
            if (modalitesPaiement     != null && !modalitesPaiement.isBlank())     body.put("modalitesPaiement",     modalitesPaiement);
            if (financementDisponible != null && !financementDisponible.isBlank()) body.put("financementDisponible", financementDisponible);
            if (certification         != null && !certification.isBlank())         body.put("certification",         certification);
            if (documentsRequis != null && !documentsRequis.isEmpty())
                body.set("documentsRequis", mapper.valueToTree(documentsRequis));
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/offres/" + id))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .PUT(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                    .build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            return resp.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Supprime un domaine (Admin) */
    public boolean supprimerDomaine(Long id, String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/domaines/" + id))
                    .header("Authorization", "Bearer " + token)
                    .DELETE().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            return resp.statusCode() == 200 || resp.statusCode() == 204;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Crée un domaine simple (Admin) */
    public boolean creerDomaine(String libelle, String token) {
        return creerDomaineComplet(libelle, null, null, token);
    }

    /** Crée un domaine avec code et description (Admin) */
    public boolean creerDomaineComplet(String libelle, String code, String description, String token) {
        try {
            ObjectNode body = mapper.createObjectNode();
            body.put("libelle", libelle);
            if (code != null) body.put("code", code);
            if (description != null) body.put("description", description);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/domaines"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body))).build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            return resp.statusCode() == 200 || resp.statusCode() == 201;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    /** Publie une offre */
    public boolean publierOffre(Long id, String token) {
        return put(baseUrl + "/api/offres/publier/" + id, token);
    }

    /** Ferme une offre */
    public boolean fermerOffre(Long id, String token) {
        return put(baseUrl + "/api/offres/fermer/" + id, token);
    }

    /** Met à jour la date limite de candidature (date = null ou "" pour supprimer la limite) */
    public boolean majDateLimiteCandidature(Long id, String date, String token) {
        String url = baseUrl + "/api/offres/" + id + "/date-limite"
            + (date != null && !date.isBlank() ? "?date=" + date : "");
        return put(url, token);
    }

    /** Liste toutes les candidatures (Admin) */
    public List<CandidatureFront> toutesCandidatures(String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/candidatures"))
                    .header("Authorization", "Bearer " + token).GET().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200)
                return mapper.readValue(resp.body(), new TypeReference<List<CandidatureFront>>() {});
        } catch (Exception e) { e.printStackTrace(); }
        return List.of();
    }

    /** Candidatures d'une offre (pour le prestataire propriétaire) */
    public List<CandidatureFront> candidaturesParOffre(Long offreId, String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/candidatures/offre/" + offreId))
                    .header("Authorization", "Bearer " + token).GET().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200)
                return mapper.readValue(resp.body(), new TypeReference<List<CandidatureFront>>() {});
        } catch (Exception e) { e.printStackTrace(); }
        return new java.util.ArrayList<>();
    }

    /** Accepte une candidature */
    public boolean accepterCandidature(Long id, String token) {
        return put(baseUrl + "/api/candidatures/accepter/" + id, token);
    }

    /** Refuse une candidature */
    public boolean refuserCandidature(Long id, String token) {
        return put(baseUrl + "/api/candidatures/refuser/" + id, token);
    }

    /** Liste offres sous forme d'Offre (avec champs enrichis : statut, cout, niveau…) */
    public List<Offre> listerOffresCompletes() {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/offres")).GET().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200)
                return mapper.readValue(resp.body(), new TypeReference<List<Offre>>() {});
        } catch (Exception e) { e.printStackTrace(); }
        return List.of();
    }

    /** Crée les offres manquantes pour les demandes à la carte VALIDEE (migration). */
    public void migrerOffresDemandesLibres(String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/demandes-libre/migrer-offres"))
                    .header("Authorization", "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.noBody()).build();
            http.send(req, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) { e.printStackTrace(); }
    }

    /** Liste TOUTES les offres pour l'admin sous forme Offre (BROUILLON + DISPONIBLE + FERME). */
    public List<Offre> listerOffresCompletesAdmin(String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/offres/admin"))
                    .header("Authorization", "Bearer " + token)
                    .GET().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200)
                return mapper.readValue(resp.body(), new TypeReference<List<Offre>>() {});
        } catch (Exception e) { e.printStackTrace(); }
        return List.of();
    }

    // ─── Sessions de formation ────────────────────────────────────────────────

    /** Liste toutes les sessions */
    public List<SessionFormationFront> listerSessions(String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/sessions"))
                    .header("Authorization", "Bearer " + token).GET().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200)
                return mapper.readValue(resp.body(), new TypeReference<List<SessionFormationFront>>() {});
        } catch (Exception e) { e.printStackTrace(); }
        return List.of();
    }

    /** Liste les sessions d'une offre */
    public List<SessionFormationFront> listerSessionsParOffre(Long offreId, String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/sessions/offre/" + offreId))
                    .header("Authorization", "Bearer " + token).GET().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200)
                return mapper.readValue(resp.body(), new TypeReference<List<SessionFormationFront>>() {});
        } catch (Exception e) { e.printStackTrace(); }
        return List.of();
    }

    /** Crée une session de formation */
    public boolean creerSession(Long offreId, String dateHeure, Integer dureeMinutes,
                                 String salle, String formateur, Integer nbPlaces, String token) {
        try {
            ObjectNode body = mapper.createObjectNode();
            body.put("offreId", offreId);
            body.put("dateHeure", dateHeure);
            body.put("dureeMinutes", dureeMinutes);
            if (salle != null) body.put("salle", salle);
            if (formateur != null) body.put("formateur", formateur);
            if (nbPlaces != null) body.put("nbPlaces", nbPlaces);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/sessions"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body))).build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            return resp.statusCode() == 200 || resp.statusCode() == 201;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    /** Change le statut d'une session */
    public boolean changerStatutSession(Long id, String statut, String token) {
        return put(baseUrl + "/api/sessions/" + id + "/statut?statut=" + statut, token);
    }

    /** Supprime une session */
    public boolean supprimerSession(Long id, String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/sessions/" + id))
                    .header("Authorization", "Bearer " + token).DELETE().build();
            return http.send(req, HttpResponse.BodyHandlers.ofString()).statusCode() == 204;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    // ─── Export CSV ───────────────────────────────────────────────────────────

    /** Télécharge le CSV des candidatures depuis le backend */
    public byte[] exporterCandidaturesCSV(String statut, String token) {
        try {
            String url = baseUrl + "/api/candidatures/export-csv";
            if (statut != null && !statut.isBlank()) url += "?statut=" + statut;
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + token).GET().build();
            HttpResponse<byte[]> resp = http.send(req, HttpResponse.BodyHandlers.ofByteArray());
            if (resp.statusCode() == 200) return resp.body();
        } catch (Exception e) { e.printStackTrace(); }
        return new byte[0];
    }

    /** Télécharge le rapport d'activité CSV depuis le backend */
    public byte[] exporterRapportCSV(String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/candidatures/rapport-csv"))
                    .header("Authorization", "Bearer " + token).GET().build();
            HttpResponse<byte[]> resp = http.send(req, HttpResponse.BodyHandlers.ofByteArray());
            if (resp.statusCode() == 200) return resp.body();
        } catch (Exception e) { e.printStackTrace(); }
        return new byte[0];
    }

    // ─── Analytics ────────────────────────────────────────────────────────────

    /** Alertes seuil de remplissage (défaut 80%) */
    public List<Map<String, Object>> alertesSeuils(int seuil, String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/analytics/alertes?seuilPourcent=" + seuil))
                    .header("Authorization", "Bearer " + token).GET().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200)
                return mapper.readValue(resp.body(), new TypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) { e.printStackTrace(); }
        return List.of();
    }

    /** Taux d'acceptation par domaine */
    public List<Map<String, Object>> acceptationParDomaine(String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/analytics/acceptation-par-domaine"))
                    .header("Authorization", "Bearer " + token).GET().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200)
                return mapper.readValue(resp.body(), new TypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) { e.printStackTrace(); }
        return List.of();
    }

    /** Évolution des demandes sur 12 mois */
    public Map<String, Object> demandesParMois(String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/analytics/demandes-par-mois"))
                    .header("Authorization", "Bearer " + token).GET().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200)
                return mapper.readValue(resp.body(), new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) { e.printStackTrace(); }
        return Map.of();
    }

    /** Domaines les plus demandés */
    public List<Map<String, Object>> domainesPopulaires(String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/analytics/domaines-populaires"))
                    .header("Authorization", "Bearer " + token).GET().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200)
                return mapper.readValue(resp.body(), new TypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) { e.printStackTrace(); }
        return List.of();
    }

    /** Snapshot temps réel */
    public Map<String, Object> snapshot(String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/analytics/snapshot"))
                    .header("Authorization", "Bearer " + token).GET().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200)
                return mapper.readValue(resp.body(), new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) { e.printStackTrace(); }
        return Map.of();
    }

    // ════════════════════════════════════
    // MODULES DE FORMATION (prestataire)
    // ════════════════════════════════════

    /** Modules d'une offre triés par ordre */
    public List<ModuleFront> modulesParOffre(Long offreId, String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/modules/offre/" + offreId))
                    .header("Authorization", "Bearer " + (token != null ? token : ""))
                    .GET().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            System.err.println("[modulesParOffre] offreId=" + offreId + " HTTP " + resp.statusCode() + " | body=" + resp.body().substring(0, Math.min(200, resp.body().length())));
            if (resp.statusCode() == 200)
                return mapper.readValue(resp.body(), new TypeReference<List<ModuleFront>>() {});
        } catch (Exception e) { e.printStackTrace(); }
        return new java.util.ArrayList<>();
    }

    /** Créer un module */
    public ModuleFront creerModule(String titre, String description, Integer dureeHeures,
                                    Integer ordre, Long offreId, Long formateurId, String token) {
        try {
            var node = mapper.createObjectNode();
            node.put("titre", titre);
            if (description != null) node.put("description", description);
            if (dureeHeures != null) node.put("dureeHeures", dureeHeures);
            node.put("ordre", ordre != null ? ordre : 0);
            node.put("offreId", offreId);
            if (formateurId != null) node.put("formateurId", formateurId);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/modules"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(node))).build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            System.err.println("[creerModule] HTTP " + resp.statusCode() + " | body=" + resp.body());
            if (resp.statusCode() == 200 || resp.statusCode() == 201)
                return mapper.readValue(resp.body(), ModuleFront.class);
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    /** Modifier un module */
    public boolean modifierModule(Long id, String titre, String description, Integer dureeHeures,
                                   Integer ordre, Long offreId, Long formateurId, String token) {
        try {
            var node = mapper.createObjectNode();
            node.put("titre", titre);
            if (description != null) node.put("description", description);
            if (dureeHeures != null) node.put("dureeHeures", dureeHeures);
            node.put("ordre", ordre != null ? ordre : 0);
            node.put("offreId", offreId);
            if (formateurId != null) node.put("formateurId", formateurId);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/modules/" + id))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .PUT(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(node))).build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            return resp.statusCode() == 200;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    /** Assigner un formateur à un module */
    public boolean assignerFormateurModule(Long moduleId, Long formateurId, String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/modules/" + moduleId + "/formateur/" + formateurId))
                    .header("Authorization", "Bearer " + token)
                    .PUT(HttpRequest.BodyPublishers.noBody()).build();
            return http.send(req, HttpResponse.BodyHandlers.ofString()).statusCode() == 200;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    /** Supprimer un module */
    public boolean supprimerModule(Long id, String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/modules/" + id))
                    .header("Authorization", "Bearer " + token)
                    .DELETE().build();
            int sc = http.send(req, HttpResponse.BodyHandlers.ofString()).statusCode();
            return sc == 200 || sc == 204;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    // ── Prestataire : gestion de ses propres formations ──────────────────────

    /** Créer une offre en brouillon pour un prestataire */
    public Offre creerOffrePrestataire(String titre, String description, String dateDebut, String dateFin,
                                        Integer places, String type, Long domaineId,
                                        String niveau, String duree, Double cout,
                                        Long prestataireId, String prestataireRole, String token) {
        return creerOffrePrestataire(titre, description, dateDebut, dateFin, places, type, domaineId,
                niveau, duree, cout, prestataireId, prestataireRole, null, token);
    }

    /** Créer une offre en brouillon pour un prestataire (avec programmeUrl) */
    public Offre creerOffrePrestataire(String titre, String description, String dateDebut, String dateFin,
                                        Integer places, String type, Long domaineId,
                                        String niveau, String duree, Double cout,
                                        Long prestataireId, String prestataireRole,
                                        String programmeUrl, String token) {
        return creerOffrePrestataire(titre, description, dateDebut, dateFin, places, type, domaineId,
                niveau, duree, cout, prestataireId, prestataireRole, programmeUrl, token,
                null, null, null, null, null, null, null, null);
    }

    /** Créer une offre en brouillon pour un prestataire (tous les champs) */
    public Offre creerOffrePrestataire(String titre, String description, String dateDebut, String dateFin,
                                        Integer places, String type, Long domaineId,
                                        String niveau, String duree, Double cout,
                                        Long prestataireId, String prestataireRole,
                                        String programmeUrl, String token,
                                        String prerequis, String lieu, String imageUrl, String langue,
                                        String publicCible, String modalitesPaiement,
                                        String financementDisponible, String certification) {
        try {
            var node = mapper.createObjectNode();
            node.put("titre", titre);
            if (description != null) node.put("description", description);
            if (dateDebut != null) node.put("dateDebut", dateDebut);
            if (dateFin   != null) node.put("dateFin",   dateFin);
            if (places    != null) node.put("places",    places);
            node.put("type", type != null ? type : "EN_LIGNE");
            if (domaineId != null) node.put("domaineId", domaineId);
            if (niveau != null && !niveau.isBlank()) node.put("niveau", niveau);
            if (duree  != null && !duree.isBlank())  node.put("duree",  duree);
            if (cout   != null && cout > 0)           node.put("cout",   cout);
            if (programmeUrl         != null && !programmeUrl.isBlank())         node.put("programmeUrl",         programmeUrl);
            if (prerequis            != null && !prerequis.isBlank())            node.put("prerequis",            prerequis);
            if (lieu                 != null && !lieu.isBlank())                 node.put("lieu",                 lieu);
            if (imageUrl             != null && !imageUrl.isBlank())             node.put("imageUrl",             imageUrl);
            if (langue               != null && !langue.isBlank())               node.put("langue",               langue);
            if (publicCible          != null && !publicCible.isBlank())          node.put("publicCible",          publicCible);
            if (modalitesPaiement    != null && !modalitesPaiement.isBlank())    node.put("modalitesPaiement",    modalitesPaiement);
            if (financementDisponible!= null && !financementDisponible.isBlank())node.put("financementDisponible",financementDisponible);
            if (certification        != null && !certification.isBlank())        node.put("certification",        certification);
            node.put("prestataireId",   prestataireId);
            node.put("prestataireRole", prestataireRole);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/offres"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(node))).build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200 || resp.statusCode() == 201)
                return mapper.readValue(resp.body(), Offre.class);
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    /** Formations d'un prestataire (tous statuts) */
    public List<Offre> mesFormationsPrestataire(Long prestataireId, String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/offres/prestataire/" + prestataireId))
                    .header("Authorization", "Bearer " + token).GET().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200)
                return mapper.readValue(resp.body(), new TypeReference<List<Offre>>() {});
        } catch (Exception e) { e.printStackTrace(); }
        return new java.util.ArrayList<>();
    }

    /** Soumettre une formation à l'admin */
    public boolean soumettreFormation(Long offreId, Long prestataireId, String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/offres/soumettre/" + offreId
                                    + "?prestataireId=" + prestataireId))
                    .header("Authorization", "Bearer " + token)
                    .PUT(HttpRequest.BodyPublishers.noBody()).build();
            return http.send(req, HttpResponse.BodyHandlers.ofString()).statusCode() == 200;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    /** Annuler la soumission (reprendre en brouillon) */
    public boolean annulerSoumissionFormation(Long offreId, Long prestataireId, String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/offres/annuler-soumission/" + offreId
                                    + "?prestataireId=" + prestataireId))
                    .header("Authorization", "Bearer " + token)
                    .PUT(HttpRequest.BodyPublishers.noBody()).build();
            return http.send(req, HttpResponse.BodyHandlers.ofString()).statusCode() == 200;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    /** Admin : formations soumises en attente de validation */
    public List<Offre> formationsSoumises(String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/offres/soumises"))
                    .header("Authorization", "Bearer " + token).GET().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200)
                return mapper.readValue(resp.body(), new TypeReference<List<Offre>>() {});
        } catch (Exception e) { e.printStackTrace(); }
        return new java.util.ArrayList<>();
    }

    /** Admin : rejette une formation soumise avec motif */
    public boolean rejeterFormationSoumise(Long offreId, String motif, String token) {
        try {
            String url = baseUrl + "/api/offres/rejeter/" + offreId;
            if (motif != null && !motif.isBlank())
                url += "?motif=" + java.net.URLEncoder.encode(motif, java.nio.charset.StandardCharsets.UTF_8);
            return put(url, token);
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    /** Admin : rejette une formation soumise sans motif (rétrocompat) */
    public boolean rejeterFormationSoumise(Long offreId, String token) {
        return rejeterFormationSoumise(offreId, null, token);
    }

    // ════════════════════════════════════
    // FORMATEURS
    // ════════════════════════════════════

    public List<FormateurFront> listerFormateurs() {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/formateurs"))
                    .GET().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200)
                return mapper.readValue(resp.body(), new TypeReference<List<FormateurFront>>() {});
        } catch (Exception e) { e.printStackTrace(); }
        return List.of();
    }

    public boolean creerFormateur(String nom, String prenom, String email,
                                   String telephone, String specialite, String bio,
                                   String token) {
        try {
            var node = mapper.createObjectNode();
            node.put("nom", nom); node.put("prenom", prenom);
            node.put("email", email); node.put("telephone", telephone);
            node.put("specialite", specialite); node.put("bio", bio);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/formateurs"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(node))).build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            return resp.statusCode() == 200 || resp.statusCode() == 201;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    public boolean modifierFormateur(Long id, String nom, String prenom, String email,
                                     String telephone, String specialite, String bio,
                                     String token) {
        try {
            var node = mapper.createObjectNode();
            node.put("nom", nom); node.put("prenom", prenom);
            node.put("email", email); node.put("telephone", telephone);
            node.put("specialite", specialite); node.put("bio", bio);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/formateurs/" + id))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .PUT(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(node))).build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            return resp.statusCode() >= 200 && resp.statusCode() < 300;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    public boolean supprimerFormateur(Long id, String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/formateurs/" + id))
                    .header("Authorization", "Bearer " + token)
                    .DELETE().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            return resp.statusCode() == 200 || resp.statusCode() == 204;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    public List<FormateurFront> formateursParOffre(Long offreId) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/formateurs/offre/" + offreId))
                    .GET().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200)
                return mapper.readValue(resp.body(), new TypeReference<List<FormateurFront>>() {});
        } catch (Exception e) { e.printStackTrace(); }
        return List.of();
    }

    public boolean assignerFormateurAOffre(Long formateurId, Long offreId, String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/formateurs/" + formateurId + "/assigner/" + offreId))
                    .header("Authorization", "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.noBody()).build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            return resp.statusCode() == 200 || resp.statusCode() == 204;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    public boolean retirerFormateurDeOffre(Long formateurId, Long offreId, String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/formateurs/" + formateurId + "/retirer/" + offreId))
                    .header("Authorization", "Bearer " + token)
                    .DELETE().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            return resp.statusCode() == 200 || resp.statusCode() == 204;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    // ════════════════════════════════════════════════════
    // E-LEARNING : COURS
    // ════════════════════════════════════════════════════

    /** Surcharge sans token — pour les endpoints publics (apprenants, détail offre). */
    public List<com.senexpert.frontend.model.CoursEnLigneFront> coursParOffre(Long offreId, boolean admin) {
        return coursParOffre(offreId, admin, null);
    }

    public List<com.senexpert.frontend.model.CoursEnLigneFront> coursParOffre(Long offreId, boolean admin, String token) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/elearning/cours/offre/" + offreId + "?admin=" + admin))
                    .GET();
            if (token != null && !token.isBlank())
                builder.header("Authorization", "Bearer " + token);
            HttpResponse<String> resp = http.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200)
                return mapper.readValue(resp.body(),
                    new TypeReference<List<com.senexpert.frontend.model.CoursEnLigneFront>>() {});
        } catch (Exception e) { e.printStackTrace(); }
        return List.of();
    }

    public com.senexpert.frontend.model.CoursEnLigneFront creerCours(
            com.senexpert.frontend.model.CoursEnLigneFront cours, String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/elearning/cours"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(cours))).build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200)
                return mapper.readValue(resp.body(), com.senexpert.frontend.model.CoursEnLigneFront.class);
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public com.senexpert.frontend.model.CoursEnLigneFront modifierCours(Long id,
            com.senexpert.frontend.model.CoursEnLigneFront cours, String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/elearning/cours/" + id))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .PUT(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(cours))).build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200)
                return mapper.readValue(resp.body(), com.senexpert.frontend.model.CoursEnLigneFront.class);
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public boolean supprimerCours(Long id, String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/elearning/cours/" + id))
                    .header("Authorization", "Bearer " + token)
                    .DELETE().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            return resp.statusCode() == 200 || resp.statusCode() == 204;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    public boolean publierCours(Long id, boolean publie, String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/elearning/cours/" + id + "/publier?publie=" + publie))
                    .header("Authorization", "Bearer " + token)
                    .method("PATCH", HttpRequest.BodyPublishers.noBody()).build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            return resp.statusCode() == 200;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    /**
     * Upload un fichier pour un cours via multipart/form-data.
     * Retourne le CoursEnLigneFront mis à jour, ou null en cas d'échec.
     */
    public CoursEnLigneFront uploadFichierCours(Long coursId,
                                                org.primefaces.model.file.UploadedFile fichier,
                                                String token) {
        try {
            String boundary = "----FormBoundary" + System.currentTimeMillis();
            byte[] fileBytes = fichier.getInputStream().readAllBytes();
            String nomFichier = fichier.getFileName() != null ? fichier.getFileName() : "fichier";

            // Construction manuelle du corps multipart
            var bodyParts = new java.io.ByteArrayOutputStream();
            String partHeader = "--" + boundary + "\r\n"
                    + "Content-Disposition: form-data; name=\"fichier\"; filename=\"" + nomFichier + "\"\r\n"
                    + "Content-Type: application/octet-stream\r\n\r\n";
            bodyParts.write(partHeader.getBytes());
            bodyParts.write(fileBytes);
            bodyParts.write(("\r\n--" + boundary + "--\r\n").getBytes());

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/elearning/cours/" + coursId + "/upload"))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(bodyParts.toByteArray()))
                    .build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                return mapper.readValue(resp.body(), CoursEnLigneFront.class);
            }
            System.err.println("uploadFichierCours HTTP " + resp.statusCode() + " : " + resp.body());
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    // ════════════════════════════════════════════════════
    // E-LEARNING : QUIZ
    // ════════════════════════════════════════════════════

    public List<com.senexpert.frontend.model.QuizQuestionFront> questionsParCours(Long coursId) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/elearning/quiz/" + coursId + "/questions"))
                    .GET().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200)
                return mapper.readValue(resp.body(),
                    new TypeReference<List<com.senexpert.frontend.model.QuizQuestionFront>>() {});
        } catch (Exception e) { e.printStackTrace(); }
        return List.of();
    }

    public boolean ajouterQuestion(com.senexpert.frontend.model.QuizQuestionFront q, String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/elearning/quiz/question"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(q))).build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            return resp.statusCode() == 200;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    public boolean supprimerQuestion(Long id, String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/elearning/quiz/question/" + id))
                    .header("Authorization", "Bearer " + token)
                    .DELETE().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            return resp.statusCode() == 200 || resp.statusCode() == 204;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    public com.senexpert.frontend.model.ProgressionFront soumettreQuiz(
            Long coursId, Long utilisateurId,
            List<com.senexpert.frontend.model.QuizQuestionFront> questions, String token) {
        try {
            // Construit la liste de réponses
            var reponses = new java.util.ArrayList<java.util.Map<String,Object>>();
            for (var q : questions) {
                var r = new java.util.LinkedHashMap<String,Object>();
                r.put("questionId", q.getId());
                r.put("reponse", q.getReponseApprenant() != null ? q.getReponseApprenant() : "");
                reponses.add(r);
            }
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/elearning/quiz/" + coursId
                                    + "/soumettre?utilisateurId=" + utilisateurId))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(reponses))).build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200)
                return mapper.readValue(resp.body(), com.senexpert.frontend.model.ProgressionFront.class);
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    // ════════════════════════════════════════════════════
    // E-LEARNING : PROGRESSION
    // ════════════════════════════════════════════════════

    public com.senexpert.frontend.model.ProgressionFront accederCours(Long coursId, Long utilisateurId, String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/elearning/progression/" + coursId
                                    + "/acceder?utilisateurId=" + utilisateurId))
                    .header("Authorization", "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.noBody()).build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200)
                return mapper.readValue(resp.body(), com.senexpert.frontend.model.ProgressionFront.class);
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public com.senexpert.frontend.model.ProgressionFront completerCours(Long coursId, Long utilisateurId, String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/elearning/progression/" + coursId
                                    + "/completer?utilisateurId=" + utilisateurId))
                    .header("Authorization", "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.noBody()).build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200)
                return mapper.readValue(resp.body(), com.senexpert.frontend.model.ProgressionFront.class);
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public List<com.senexpert.frontend.model.ProgressionFront> progressionParOffre(
            Long offreId, Long utilisateurId, String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/elearning/progression/offre/" + offreId
                                    + "?utilisateurId=" + utilisateurId))
                    .header("Authorization", "Bearer " + token)
                    .GET().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200)
                return mapper.readValue(resp.body(),
                    new TypeReference<List<com.senexpert.frontend.model.ProgressionFront>>() {});
        } catch (Exception e) { e.printStackTrace(); }
        return List.of();
    }

    // ════════════════════════════════════════════════════
    // E-LEARNING : SOUMISSIONS (TP / DEVOIR / EXAMEN)
    // ════════════════════════════════════════════════════

    /** Soumet un travail (texte + lien uniquement, sans fichier). */
    public com.senexpert.frontend.model.SoumissionFront soumettreTravail(
            Long coursId, Long apprenantId, String nomApprenant,
            String contenuTexte, String lienUrl, String token) {
        try {
            String url = baseUrl + "/api/elearning/cours/" + coursId + "/soumettre"
                + "?apprenantId=" + apprenantId
                + "&nomApprenant=" + java.net.URLEncoder.encode(nomApprenant != null ? nomApprenant : "", "UTF-8")
                + (contenuTexte != null ? "&contenuTexte=" + java.net.URLEncoder.encode(contenuTexte, "UTF-8") : "")
                + (lienUrl      != null ? "&lienUrl="      + java.net.URLEncoder.encode(lienUrl, "UTF-8") : "");
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.noBody()).build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200)
                return mapper.readValue(resp.body(), com.senexpert.frontend.model.SoumissionFront.class);
            System.err.println("soumettreTravail HTTP " + resp.statusCode() + " : " + resp.body());
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    /** Soumet un fichier comme travail (multipart/form-data). */
    public com.senexpert.frontend.model.SoumissionFront soumettreFichier(
            Long coursId, Long apprenantId, String nomApprenant,
            String contenuTexte, String lienUrl,
            org.primefaces.model.file.UploadedFile fichier, String token) {
        try {
            String boundary = "----SoumBoundary" + System.currentTimeMillis();
            var bodyParts = new java.io.ByteArrayOutputStream();

            // Champ apprenantId
            bodyParts.write(("--" + boundary + "\r\nContent-Disposition: form-data; name=\"apprenantId\"\r\n\r\n"
                + apprenantId + "\r\n").getBytes());
            // Champ nomApprenant
            if (nomApprenant != null)
                bodyParts.write(("--" + boundary + "\r\nContent-Disposition: form-data; name=\"nomApprenant\"\r\n\r\n"
                    + nomApprenant + "\r\n").getBytes());
            // Champ contenuTexte
            if (contenuTexte != null && !contenuTexte.isBlank())
                bodyParts.write(("--" + boundary + "\r\nContent-Disposition: form-data; name=\"contenuTexte\"\r\n\r\n"
                    + contenuTexte + "\r\n").getBytes());
            // Champ lienUrl
            if (lienUrl != null && !lienUrl.isBlank())
                bodyParts.write(("--" + boundary + "\r\nContent-Disposition: form-data; name=\"lienUrl\"\r\n\r\n"
                    + lienUrl + "\r\n").getBytes());
            // Fichier
            String nomFichier = fichier.getFileName() != null ? fichier.getFileName() : "fichier";
            byte[] fileBytes  = fichier.getInputStream().readAllBytes();
            bodyParts.write(("--" + boundary + "\r\nContent-Disposition: form-data; name=\"fichier\"; filename=\""
                + nomFichier + "\"\r\nContent-Type: application/octet-stream\r\n\r\n").getBytes());
            bodyParts.write(fileBytes);
            bodyParts.write(("\r\n--" + boundary + "--\r\n").getBytes());

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/elearning/cours/" + coursId + "/soumettre/fichier"))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(bodyParts.toByteArray()))
                    .build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200)
                return mapper.readValue(resp.body(), com.senexpert.frontend.model.SoumissionFront.class);
            System.err.println("soumettreFichier HTTP " + resp.statusCode() + " : " + resp.body());
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    /** Démarre un examen QCM (enregistre l'heure de début). */
    public com.senexpert.frontend.model.SoumissionFront demarrerExamen(
            Long coursId, Long apprenantId, String nomApprenant, String token) {
        try {
            String url = baseUrl + "/api/elearning/cours/" + coursId + "/demarrer-examen"
                + "?apprenantId=" + apprenantId
                + "&nomApprenant=" + java.net.URLEncoder.encode(nomApprenant != null ? nomApprenant : "", "UTF-8");
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.noBody()).build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200)
                return mapper.readValue(resp.body(), com.senexpert.frontend.model.SoumissionFront.class);
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    /** Récupère la soumission de l'apprenant pour un cours. */
    public com.senexpert.frontend.model.SoumissionFront maSoumission(
            Long coursId, Long apprenantId, String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/elearning/cours/" + coursId
                                    + "/ma-soumission?apprenantId=" + apprenantId))
                    .header("Authorization", "Bearer " + token)
                    .GET().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200)
                return mapper.readValue(resp.body(), com.senexpert.frontend.model.SoumissionFront.class);
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    /** Récupère toutes les soumissions d'un cours (vue formateur). */
    public List<com.senexpert.frontend.model.SoumissionFront> soumissionsParCours(
            Long coursId, String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/elearning/cours/" + coursId + "/soumissions"))
                    .header("Authorization", "Bearer " + token)
                    .GET().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200)
                return mapper.readValue(resp.body(),
                    new TypeReference<List<com.senexpert.frontend.model.SoumissionFront>>() {});
        } catch (Exception e) { e.printStackTrace(); }
        return List.of();
    }

    /** Formateur : nombre de soumissions en attente de correction pour un cours. */
    public long countEnAttenteParCours(Long coursId, String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/elearning/cours/" + coursId + "/soumissions/count"))
                    .header("Authorization", "Bearer " + token)
                    .GET().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> body = mapper.readValue(resp.body(), java.util.Map.class);
                Object v = body.get("enAttente");
                if (v instanceof Number) return ((Number) v).longValue();
            }
        } catch (Exception e) { e.printStackTrace(); }
        return 0L;
    }

    // ════════════════════════════════════════════════════
    // PAIEMENTS
    // ════════════════════════════════════════════════════

    /** Enregistre un paiement dans l'historique. */
    public boolean enregistrerPaiement(Long userId, Long offreId, String offreTitre,
                                       Double montant, String methode, String reference,
                                       String token) {
        try {
            var node = mapper.createObjectNode();
            node.put("utilisateurId", userId);
            node.put("offreId",       offreId);
            node.put("offreTitre",    offreTitre != null ? offreTitre : "");
            node.put("montant",       montant != null ? montant : 0.0);
            node.put("methodePaiement", methode != null ? methode : "INCONNU");
            node.put("reference",     reference != null ? reference : "");
            node.put("statut",        "SUCCES");
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/paiements"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(node))).build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            return resp.statusCode() == 200 || resp.statusCode() == 201;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    /** Historique des paiements d'un utilisateur. */
    public List<com.senexpert.frontend.model.PaiementFront> mesPaiements(Long userId, String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/paiements/utilisateur/" + userId))
                    .header("Authorization", "Bearer " + token).GET().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200)
                return mapper.readValue(resp.body(),
                    new TypeReference<List<com.senexpert.frontend.model.PaiementFront>>() {});
        } catch (Exception e) { e.printStackTrace(); }
        return List.of();
    }

    /**
     * Soumet une demande de formation et retourne l'ID de la demande creee (null si erreur).
     * Utilise en complement de soumettreDemande() quand on a besoin de l'ID pour uploader
     * le fichier participants juste apres.
     */
    public Long soumettreDemandeReturnId(Long utilisateurId, Long offreId,
                                          List<String> documents, String token) {
        try {
            ObjectNode body = mapper.createObjectNode();
            body.put("utilisateurId", utilisateurId);
            body.put("offreId", offreId);
            if (documents != null && !documents.isEmpty()) {
                body.set("documents", mapper.valueToTree(documents));
            }
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/demandes"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                    .build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> result = mapper.readValue(resp.body(), java.util.Map.class);
                Object id = result.get("id");
                if (id != null) return Long.valueOf(id.toString());
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    /**
     * Upload du fichier participants pour une DemandeFormation (institution).
     */
    public boolean uploadFichierParticipantsDemande(Long demandeId,
                                                     byte[] fichiersBytes,
                                                     String nomFichier,
                                                     String contentType,
                                                     String token) {
        try {
            String boundary = "----SenExpertBoundaryD" + System.currentTimeMillis();
            java.io.ByteArrayOutputStream body = new java.io.ByteArrayOutputStream();
            String CRLF = "\r\n";
            body.write(("--" + boundary + CRLF).getBytes());
            body.write(("Content-Disposition: form-data; name=\"fichier\"; filename=\"" + nomFichier + "\"" + CRLF).getBytes());
            body.write(("Content-Type: " + (contentType != null ? contentType : "application/octet-stream") + CRLF).getBytes());
            body.write(CRLF.getBytes());
            body.write(fichiersBytes);
            body.write(CRLF.getBytes());
            body.write(("--" + boundary + "--" + CRLF).getBytes());
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/demandes/" + demandeId + "/upload-participants"))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(body.toByteArray()))
                    .build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            return resp.statusCode() == 200;
        } catch (Exception e) {
            System.err.println("uploadFichierParticipantsDemande error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Telecharge le fichier participants d'une DemandeFormation.
     */
    public byte[] downloadFichierParticipantsDemande(Long demandeId, String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/demandes/" + demandeId + "/participants-file"))
                    .header("Authorization", "Bearer " + token)
                    .GET().build();
            HttpResponse<byte[]> resp = http.send(req, HttpResponse.BodyHandlers.ofByteArray());
            if (resp.statusCode() == 200) return resp.body();
        } catch (Exception e) {
            System.err.println("downloadFichierParticipantsDemande error: " + e.getMessage());
        }
        return null;
    }

    // ── Upload / Download fichier liste des participants ─────────────────────

    /**
     * Upload du fichier liste des participants pour une candidature d'institution.
     * Formats acceptes : .docx, .xlsx, .pdf, .csv, .odt, .ods, etc.
     */
    public boolean uploadFichierParticipants(Long candidatureId,
                                             byte[] fichiersBytes,
                                             String nomFichier,
                                             String contentType,
                                             String token) {
        try {
            String boundary = "----SenExpertBoundary" + System.currentTimeMillis();
            java.io.ByteArrayOutputStream body = new java.io.ByteArrayOutputStream();
            String CRLF = "\r\n";
            body.write(("--" + boundary + CRLF).getBytes());
            body.write(("Content-Disposition: form-data; name=\"fichier\"; filename=\"" + nomFichier + "\"" + CRLF).getBytes());
            body.write(("Content-Type: " + (contentType != null ? contentType : "application/octet-stream") + CRLF).getBytes());
            body.write(CRLF.getBytes());
            body.write(fichiersBytes);
            body.write(CRLF.getBytes());
            body.write(("--" + boundary + "--" + CRLF).getBytes());
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/candidatures/" + candidatureId + "/upload-participants"))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(body.toByteArray()))
                    .build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            return resp.statusCode() == 200;
        } catch (Exception e) {
            System.err.println("uploadFichierParticipants error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Telecharge le fichier participants d'une candidature et retourne ses bytes.
     */
    public byte[] downloadFichierParticipants(Long candidatureId, String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/candidatures/" + candidatureId + "/participants-file"))
                    .header("Authorization", "Bearer " + token)
                    .GET().build();
            HttpResponse<byte[]> resp = http.send(req, HttpResponse.BodyHandlers.ofByteArray());
            if (resp.statusCode() == 200) return resp.body();
        } catch (Exception e) {
            System.err.println("downloadFichierParticipants error: " + e.getMessage());
        }
        return null;
    }

    // ── MESSAGERIE ────────────────────────────────────────────────────────────

    /** Nombre de messages non lus pour un utilisateur. */
    public int messagesNonLus(Long utilisateurId, String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/messages/non-lus/" + utilisateurId))
                    .header("Authorization", "Bearer " + token)
                    .GET().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                Map<?,?> map = mapper.readValue(resp.body(), Map.class);
                Object count = map.get("count");
                if (count instanceof Number) return ((Number) count).intValue();
            }
        } catch (Exception e) {
            System.err.println("messagesNonLus error: " + e.getMessage());
        }
        return 0;
    }

    /** Fil de conversation pour une offre et un utilisateur. */
    public List<com.senexpert.frontend.model.MessageFront> conversation(Long offreId, Long utilisateurId, String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/messages/offre/" + offreId + "/utilisateur/" + utilisateurId))
                    .header("Authorization", "Bearer " + token)
                    .GET().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                return mapper.readValue(resp.body(),
                        mapper.getTypeFactory().constructCollectionType(
                                List.class, com.senexpert.frontend.model.MessageFront.class));
            }
        } catch (Exception e) {
            System.err.println("conversation error: " + e.getMessage());
        }
        return java.util.Collections.emptyList();
    }

    /** Marquer tous les messages d'une conversation comme lus. */
    public void marquerConversationLue(Long offreId, Long utilisateurId, String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/messages/offre/" + offreId + "/utilisateur/" + utilisateurId + "/lu-tout"))
                    .header("Authorization", "Bearer " + token)
                    .PUT(HttpRequest.BodyPublishers.noBody()).build();
            http.send(req, HttpResponse.BodyHandlers.discarding());
        } catch (Exception e) {
            System.err.println("marquerConversationLue error: " + e.getMessage());
        }
    }

    /** Envoyer un message. */
    public com.senexpert.frontend.model.MessageFront envoyerMessage(
            com.senexpert.frontend.model.MessageFront msg, String token) {
        try {
            String body = mapper.writeValueAsString(msg);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/messages"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.ofString(body)).build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                return mapper.readValue(resp.body(), com.senexpert.frontend.model.MessageFront.class);
            }
        } catch (Exception e) {
            System.err.println("envoyerMessage error: " + e.getMessage());
        }
        return null;
    }

    // ── ÉVALUATION ────────────────────────────────────────────────────────────

    /** Récupère l'évaluation d'un apprenant pour une offre, ou null si inexistante. */
    public com.senexpert.frontend.model.EvaluationFront monEvaluation(
            Long utilisateurId, Long offreId, String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/evaluations/apprenant/" + utilisateurId + "/offre/" + offreId))
                    .header("Authorization", "Bearer " + token)
                    .GET().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200)
                return mapper.readValue(resp.body(), com.senexpert.frontend.model.EvaluationFront.class);
        } catch (Exception e) {
            System.err.println("monEvaluation error: " + e.getMessage());
        }
        return null;
    }

    /** Soumet (ou met à jour) une évaluation. */
    public com.senexpert.frontend.model.EvaluationFront evaluerFormation(
            com.senexpert.frontend.model.EvaluationFront ev, String token) {
        try {
            String body = mapper.writeValueAsString(ev);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/evaluations"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.ofString(body)).build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200)
                return mapper.readValue(resp.body(), com.senexpert.frontend.model.EvaluationFront.class);
        } catch (Exception e) {
            System.err.println("evaluerFormation error: " + e.getMessage());
        }
        return null;
    }

    // ── SOUMISSIONS / CORRECTIONS ──────────────────────────────────────────────

    /** Note une soumission (formateur). */
    public com.senexpert.frontend.model.SoumissionFront noterSoumission(
            Long soumissionId, double note, String commentaire, String token) {
        try {
            String url = baseUrl + "/api/elearning/soumissions/" + soumissionId
                    + "/noter?note=" + note
                    + "&commentaire=" + java.net.URLEncoder.encode(
                            commentaire != null ? commentaire : "", java.nio.charset.StandardCharsets.UTF_8);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + token)
                    .PUT(HttpRequest.BodyPublishers.noBody()).build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200)
                return mapper.readValue(resp.body(), com.senexpert.frontend.model.SoumissionFront.class);
        } catch (Exception e) {
            System.err.println("noterSoumission error: " + e.getMessage());
        }
        return null;
    }

    // ══════════════════════════════════════════════════════════════
    //  SÉANCES (emploi du temps)
    // ══════════════════════════════════════════════════════════════

    /** Séances d'un module */
    public List<com.senexpert.frontend.model.SeanceFront> seancesParModule(Long moduleId) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/seances/module/" + moduleId))
                    .GET().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200)
                return mapper.readValue(resp.body(),
                        new TypeReference<List<com.senexpert.frontend.model.SeanceFront>>() {});
        } catch (Exception e) {
            System.err.println("seancesParModule error: " + e.getMessage());
        }
        return new java.util.ArrayList<>();
    }

    /** Toutes les séances d'une offre (emploi du temps global) */
    public List<com.senexpert.frontend.model.SeanceFront> seancesParOffre(Long offreId, String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/seances/offre/" + offreId))
                    .header("Authorization", "Bearer " + (token != null ? token : ""))
                    .GET().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            System.err.println("[seancesParOffre] offreId=" + offreId + " HTTP " + resp.statusCode());
            if (resp.statusCode() == 200)
                return mapper.readValue(resp.body(),
                        new TypeReference<List<com.senexpert.frontend.model.SeanceFront>>() {});
        } catch (Exception e) {
            System.err.println("seancesParOffre error: " + e.getMessage());
        }
        return new java.util.ArrayList<>();
    }

    /** Créer une séance */
    public com.senexpert.frontend.model.SeanceFront creerSeance(
            Long moduleId, String date, String heureDebut, String heureFin,
            String salle, String lienVisio, String notes, String token) {
        try {
            ObjectNode body = mapper.createObjectNode();
            body.put("moduleId", moduleId);
            if (date != null)       body.put("date",       date);
            if (heureDebut != null) body.put("heureDebut", heureDebut);
            if (heureFin   != null) body.put("heureFin",   heureFin);
            if (salle      != null) body.put("salle",      salle);
            if (lienVisio  != null) body.put("lienVisio",  lienVisio);
            if (notes      != null) body.put("notes",      notes);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/seances"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body))).build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200)
                return mapper.readValue(resp.body(), com.senexpert.frontend.model.SeanceFront.class);
        } catch (Exception e) {
            System.err.println("creerSeance error: " + e.getMessage());
        }
        return null;
    }

    /** Modifier une séance */
    public boolean modifierSeance(Long id, Long moduleId, String date, String heureDebut,
                                   String heureFin, String salle, String lienVisio,
                                   String notes, String token) {
        try {
            ObjectNode body = mapper.createObjectNode();
            body.put("moduleId", moduleId);
            if (date != null)       body.put("date",       date);
            if (heureDebut != null) body.put("heureDebut", heureDebut);
            if (heureFin   != null) body.put("heureFin",   heureFin);
            if (salle      != null) body.put("salle",      salle);
            if (lienVisio  != null) body.put("lienVisio",  lienVisio);
            if (notes      != null) body.put("notes",      notes);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/seances/" + id))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .PUT(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body))).build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            return resp.statusCode() == 200;
        } catch (Exception e) {
            System.err.println("modifierSeance error: " + e.getMessage());
            return false;
        }
    }


    /** Supprimer une séance. */
    public boolean supprimerSeance(Long id, String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/seances/" + id))
                    .header("Authorization", "Bearer " + token)
                    .DELETE().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            return resp.statusCode() == 200 || resp.statusCode() == 204;
        } catch (Exception e) {
            System.err.println("supprimerSeance error: " + e.getMessage());
            return false;
        }
    }

    // ── PARTICIPANTS INSTITUTION ──────────────────────────────────────────────

    /** Liste les participants d'une demande. */
    public List<com.senexpert.frontend.model.ParticipantInstitutionFront> listerParticipantsDemande(
            Long demandeId, String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/demandes/" + demandeId + "/participants"))
                    .header("Authorization", "Bearer " + token)
                    .GET().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                return mapper.readValue(resp.body(),
                        mapper.getTypeFactory().constructCollectionType(
                                List.class, com.senexpert.frontend.model.ParticipantInstitutionFront.class));
            }
        } catch (Exception e) {
            System.err.println("listerParticipantsDemande error: " + e.getMessage());
        }
        return new java.util.ArrayList<>();
    }

    /** Ajoute un participant manuellement à une demande. */
    public com.senexpert.frontend.model.ParticipantInstitutionFront ajouterParticipantDemande(
            Long demandeId, com.senexpert.frontend.model.ParticipantInstitutionFront dto, String token) {
        try {
            String body = mapper.writeValueAsString(dto);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/demandes/" + demandeId + "/participants"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.ofString(body)).build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                return mapper.readValue(resp.body(),
                        com.senexpert.frontend.model.ParticipantInstitutionFront.class);
            }
        } catch (Exception e) {
            System.err.println("ajouterParticipantDemande error: " + e.getMessage());
        }
        return null;
    }

    /** Met à jour le statut d'un participant. */
    public boolean updateStatutParticipant(Long participantId, String statut, String token) {
        try {
            ObjectNode body = mapper.createObjectNode();
            body.put("statut", statut);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/participants/" + participantId + "/statut"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .PUT(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body))).build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            return resp.statusCode() == 200;
        } catch (Exception e) {
            System.err.println("updateStatutParticipant error: " + e.getMessage());
            return false;
        }
    }

    /** Supprime un participant. */
    public boolean supprimerParticipant(Long participantId, String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/participants/" + participantId))
                    .header("Authorization", "Bearer " + token)
                    .DELETE().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            return resp.statusCode() == 204 || resp.statusCode() == 200;
        } catch (Exception e) {
            System.err.println("supprimerParticipant error: " + e.getMessage());
            return false;
        }
    }

    // ── MESSAGERIE DEMANDE (agent institution ↔ admin) ───────────────────────

    /** Fil de conversation lié à une demande (agent institution). */
    public List<com.senexpert.frontend.model.MessageFront> conversationDemande(
            Long demandeId, Long utilisateurId, String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/messages/demande/" + demandeId
                            + "/utilisateur/" + utilisateurId))
                    .header("Authorization", "Bearer " + token)
                    .GET().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                return mapper.readValue(resp.body(),
                        mapper.getTypeFactory().constructCollectionType(
                                List.class, com.senexpert.frontend.model.MessageFront.class));
            }
        } catch (Exception e) {
            System.err.println("conversationDemande error: " + e.getMessage());
        }
        return new java.util.ArrayList<>();
    }

    /** Mettre à jour les participants d'une demande libre */
    public boolean mettreAJourParticipantsLibre(Long demandeId, java.util.List<Long> ids, String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/demandes-libre/" + demandeId + "/participants"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .PUT(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(ids)))
                    .build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            return resp.statusCode() == 200;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    // ── Paiement & Attestation ────────────────────────────────────────────

    /** Vérifie si une demande de formation a déjà été payée. */
    public boolean demandeEstPayee(Long demandeId, String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/paiements/demande/" + demandeId + "/statut"))
                    .header("Authorization", "Bearer " + token)
                    .GET().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = mapper.readValue(resp.body(), Map.class);
                Object val = map.get("paye");
                return Boolean.TRUE.equals(val);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    /** Enregistre un paiement pour une candidature institution. */
    public boolean enregistrerPaiementDemande(Long utilisateurId, Long demandeId,
            String titreFormation, Double montant, String methode,
            String reference, String token) {
        try {
            ObjectNode body = mapper.createObjectNode();
            body.put("utilisateurId", utilisateurId);
            body.put("demandeId", demandeId);
            body.put("offreTitre", titreFormation != null ? titreFormation : "");
            body.put("montant", montant);
            body.put("methodePaiement", methode != null ? methode : "VIREMENT");
            if (reference != null && !reference.isBlank()) body.put("reference", reference);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/paiements"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                    .build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            System.out.println("[PAIEMENT] POST /api/paiements → HTTP " + resp.statusCode() + " | body=" + resp.body());
            return resp.statusCode() == 200 || resp.statusCode() == 201;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    /** URL de téléchargement de l'attestation de participation pour une demande. */
    public String urlAttestationDemande(Long demandeId) {
        return baseUrl + "/api/attestations/demande/" + demandeId;
    }

    /** URL du certificat d'inscription (PDF) disponible après paiement confirmé. */
    public String urlCertificatInscription(Long demandeId) {
        return baseUrl + "/api/demandes/" + demandeId + "/certificat";
    }

    public String getBaseUrl() { return baseUrl; }

    /** Nombre de messages non lus pour une demande et un utilisateur. */
    public int messagesNonLusDemande(Long demandeId, Long utilisateurId, String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/messages/demande/" + demandeId
                            + "/non-lus/" + utilisateurId))
                    .header("Authorization", "Bearer " + token)
                    .GET().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                Map<?,?> map = mapper.readValue(resp.body(), Map.class);
                Object count = map.get("count");
                if (count instanceof Number) return ((Number) count).intValue();
            }
        } catch (Exception e) {
            System.err.println("messagesNonLusDemande error: " + e.getMessage());
        }
        return 0;
    }
}
