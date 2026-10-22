package com.senexpert.frontend.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.senexpert.frontend.model.*;
import com.senexpert.frontend.model.Institution;
import com.senexpert.frontend.model.Utilisateur;
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
public class UserServiceClient {

    @Inject
    private ServletContext ctx;

    private String baseUrl;
    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        // Priorité : variable d'environnement (Docker/K8s) > context-param (web.xml) > défaut
        String envUrl = System.getenv("USER_SERVICE_URL");
        if (envUrl != null && !envUrl.isBlank()) {
            baseUrl = envUrl;
        } else {
            baseUrl = ctx.getInitParameter("USER_SERVICE_URL");
            if (baseUrl == null) baseUrl = "http://localhost:8081";
        }
    }

    /** Retourne le token JWT ou null si échec */
    public String login(String email, String motDePasse) {
        try {
            ObjectNode body = mapper.createObjectNode();
            body.put("email", email);
            body.put("motDePasse", motDePasse);

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/auth/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                    .build();

            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                Map<?, ?> map = mapper.readValue(resp.body(), Map.class);
                // Cas 2FA : le serveur retourne { requiresTwoFactor: true, email: "..." }
                if (Boolean.TRUE.equals(map.get("requiresTwoFactor"))) {
                    return "REQUIRES_2FA:" + map.get("email");
                }
                return (String) map.get("token");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /** Inscription d'un nouvel utilisateur avec champs additionnels selon le rôle */
    public boolean register(String nom, String prenom, String email, String motDePasse,
                            String role, String telephone,
                            String profession, String numeroCni,
                            String numeroInstitution,
                            String nomInstitution, String typeInstitution, String secteurInstitution) {
        try {
            ObjectNode body = mapper.createObjectNode();
            body.put("nom", nom);
            body.put("prenom", prenom);
            body.put("email", email);
            body.put("motDePasse", motDePasse);
            body.put("role", role);
            if (telephone != null && !telephone.isBlank())
                body.put("telephone", telephone);
            if ("PARTICULIER".equals(role)) {
                if (profession != null && !profession.isBlank())
                    body.put("profession", profession);
                if (numeroCni != null && !numeroCni.isBlank())
                    body.put("numeroCni", numeroCni);
            }
            if ("AGENT".equals(role)) {
                if (numeroInstitution  != null && !numeroInstitution.isBlank())
                    body.put("numeroInstitution",  numeroInstitution);
                if (nomInstitution     != null && !nomInstitution.isBlank())
                    body.put("nomInstitution",     nomInstitution);
                if (typeInstitution    != null && !typeInstitution.isBlank())
                    body.put("typeInstitution",    typeInstitution);
                if (secteurInstitution != null && !secteurInstitution.isBlank())
                    body.put("secteurInstitution", secteurInstitution);
            }

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/utilisateurs/register"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                    .build();

            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            return resp.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Retourne l'utilisateur actuellement connecté (via son token JWT) */
    public Utilisateur moi(String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/utilisateurs/me"))
                    .header("Authorization", "Bearer " + token)
                    .GET().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                return mapper.readValue(resp.body(), Utilisateur.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /** Liste tous les utilisateurs (Admin) */
    public List<Utilisateur> listerUtilisateurs(String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/utilisateurs"))
                    .header("Authorization", "Bearer " + token)
                    .GET().build();

            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                return mapper.readValue(resp.body(), new TypeReference<List<Utilisateur>>() {});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return List.of();
    }

    /** Récupère un utilisateur par son id */
    public Utilisateur trouverParId(Long id, String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/utilisateurs/" + id))
                    .header("Authorization", "Bearer " + token)
                    .GET().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                return mapper.readValue(resp.body(), Utilisateur.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /** Valide un compte utilisateur (Admin) */
    public boolean validerUtilisateur(Long id, String token) {
        return put(baseUrl + "/api/utilisateurs/valider/" + id, token);
    }

    /** Récupère une institution par son ID */
    public Institution trouverInstitutionParId(Long id, String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/institutions/" + id))
                    .header("Authorization", "Bearer " + token)
                    .GET().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                return mapper.readValue(resp.body(), Institution.class);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    /** Liste les utilisateurs d'une institution */
    public List<Utilisateur> listerMembresInstitution(Long institutionId, String token) {
        try {
            List<Utilisateur> tous = listerUtilisateurs(token);
            return tous.stream()
                    .filter(u -> u.getInstitution() != null && institutionId.equals(u.getInstitution().getId()))
                    .collect(java.util.stream.Collectors.toList());
        } catch (Exception e) { e.printStackTrace(); }
        return List.of();
    }

    /** Liste toutes les institutions (Admin) */
    public List<Institution> listerInstitutions(String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/institutions"))
                    .header("Authorization", "Bearer " + token)
                    .GET().build();

            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                return mapper.readValue(resp.body(), new TypeReference<List<Institution>>() {});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return List.of();
    }

    /** Crée une institution (Agent connecté) */
    public Institution creerInstitution(String nom, String type, String adresse,
                                        String secteur, String ninea, String token) {
        try {
            ObjectNode body = mapper.createObjectNode();
            body.put("nom", nom);
            body.put("type", type);
            if (adresse != null && !adresse.isBlank()) body.put("adresse", adresse);
            if (secteur != null && !secteur.isBlank()) body.put("secteur", secteur);
            if (ninea   != null && !ninea.isBlank())   body.put("ninea",   ninea);

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/institutions"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                    .build();

            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                return mapper.readValue(resp.body(), Institution.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /** Valide une institution (Admin) */
    public boolean validerInstitution(Long id, String token) {
        return put(baseUrl + "/api/institutions/valider/" + id, token);
    }

    /** Désactive une institution (Admin) */
    public boolean desactiverInstitution(Long id, String token) {
        return put(baseUrl + "/api/institutions/desactiver/" + id, token);
    }

    /** Réactive une institution (Admin) */
    public boolean reactiverInstitution(Long id, String token) {
        return put(baseUrl + "/api/institutions/reactiver/" + id, token);
    }

    /** Supprime une institution (Admin) */
    public boolean supprimerInstitution(Long id, String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/institutions/" + id))
                    .header("Authorization", "Bearer " + token)
                    .DELETE().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            return resp.statusCode() < 300;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    /** Change le rôle d'un utilisateur (Admin) */
    public boolean changerRole(Long id, String role, String token) {
        return put(baseUrl + "/api/utilisateurs/role/" + id + "?role=" + role, token);
    }

    /** Désactive un compte utilisateur (Admin) */
    public boolean desactiverUtilisateur(Long id, String token) {
        return put(baseUrl + "/api/utilisateurs/desactiver/" + id, token);
    }

    /** Réactive un compte utilisateur (Admin) */
    public boolean reactiverUtilisateur(Long id, String token) {
        return put(baseUrl + "/api/utilisateurs/reactiver/" + id, token);
    }

    /** Modifie les informations d'un utilisateur (nom, prenom, telephone) */
    public boolean modifierUtilisateur(Long id, String nom, String prenom, String telephone, String token) {
        try {
            String url = baseUrl + "/api/utilisateurs/modifier-profil/" + id
                    + "?nom=" + java.net.URLEncoder.encode(nom != null ? nom : "", java.nio.charset.StandardCharsets.UTF_8)
                    + "&prenom=" + java.net.URLEncoder.encode(prenom != null ? prenom : "", java.nio.charset.StandardCharsets.UTF_8)
                    + "&telephone=" + java.net.URLEncoder.encode(telephone != null ? telephone : "", java.nio.charset.StandardCharsets.UTF_8);
            return put(url, token);
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    /** Supprime un compte utilisateur (Admin) */
    public boolean supprimerUtilisateur(Long id, String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/utilisateurs/" + id))
                    .header("Authorization", "Bearer " + token)
                    .DELETE().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            return resp.statusCode() == 204 || resp.statusCode() == 200;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    /** Recherche utilisateurs */
    public List<Utilisateur> rechercherUtilisateurs(String q, String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/utilisateurs/recherche?q=" + java.net.URLEncoder.encode(q, java.nio.charset.StandardCharsets.UTF_8)))
                    .header("Authorization", "Bearer " + token).GET().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200)
                return mapper.readValue(resp.body(), new TypeReference<List<Utilisateur>>() {});
        } catch (Exception e) { e.printStackTrace(); }
        return List.of();
    }

    /** Assigne un profil */
    public boolean assignerProfil(Long userId, Long profilId, String token) {
        return put(baseUrl + "/api/utilisateurs/profil/" + userId + "?profilId=" + profilId, token);
    }

    /** Liste tous les profils */
    public List<ProfilFront> listerProfils(String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/profils"))
                    .header("Authorization", "Bearer " + token).GET().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200)
                return mapper.readValue(resp.body(), new TypeReference<List<ProfilFront>>() {});
        } catch (Exception e) { e.printStackTrace(); }
        return List.of();
    }

    public boolean creerProfil(String nom, String token) {
        try {
            ObjectNode body = mapper.createObjectNode(); body.put("nom", nom);
            HttpRequest req = HttpRequest.newBuilder().uri(URI.create(baseUrl + "/api/profils"))
                    .header("Content-Type", "application/json").header("Authorization", "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body))).build();
            return http.send(req, HttpResponse.BodyHandlers.ofString()).statusCode() == 200;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public boolean supprimerProfil(Long id, String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder().uri(URI.create(baseUrl + "/api/profils/" + id))
                    .header("Authorization", "Bearer " + token).DELETE().build();
            return http.send(req, HttpResponse.BodyHandlers.ofString()).statusCode() == 204;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public boolean ajouterPermissionAuProfil(Long profilId, Long permId, String token) {
        return put(baseUrl + "/api/profils/" + profilId + "/permissions/" + permId, token);
    }

    public boolean retirerPermissionDuProfil(Long profilId, Long permId, String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/profils/" + profilId + "/permissions/" + permId))
                    .header("Authorization", "Bearer " + token).DELETE().build();
            return http.send(req, HttpResponse.BodyHandlers.ofString()).statusCode() == 200;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public List<BrancheFront> listerBranches(String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder().uri(URI.create(baseUrl + "/api/branches"))
                    .header("Authorization", "Bearer " + token).GET().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200)
                return mapper.readValue(resp.body(), new TypeReference<List<BrancheFront>>() {});
        } catch (Exception e) { e.printStackTrace(); }
        return List.of();
    }

    public boolean creerBranche(String nom, String code, String token) {
        try {
            ObjectNode body = mapper.createObjectNode(); body.put("nom", nom); body.put("code", code);
            HttpRequest req = HttpRequest.newBuilder().uri(URI.create(baseUrl + "/api/branches"))
                    .header("Content-Type", "application/json").header("Authorization", "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body))).build();
            return http.send(req, HttpResponse.BodyHandlers.ofString()).statusCode() == 200;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public boolean modifierBranche(Long id, String nom, String code, String token) {
        try {
            ObjectNode body = mapper.createObjectNode(); body.put("nom", nom); body.put("code", code);
            HttpRequest req = HttpRequest.newBuilder().uri(URI.create(baseUrl + "/api/branches/" + id))
                    .header("Content-Type", "application/json").header("Authorization", "Bearer " + token)
                    .PUT(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body))).build();
            return http.send(req, HttpResponse.BodyHandlers.ofString()).statusCode() == 200;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public boolean supprimerBranche(Long id, String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder().uri(URI.create(baseUrl + "/api/branches/" + id))
                    .header("Authorization", "Bearer " + token).DELETE().build();
            return http.send(req, HttpResponse.BodyHandlers.ofString()).statusCode() == 204;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public List<PermissionFront> listerPermissions(String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder().uri(URI.create(baseUrl + "/api/permissions"))
                    .header("Authorization", "Bearer " + token).GET().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200)
                return mapper.readValue(resp.body(), new TypeReference<List<PermissionFront>>() {});
        } catch (Exception e) { e.printStackTrace(); }
        return List.of();
    }

    public boolean creerPermission(String libelle, String code, String niveau, Long brancheId, String token) {
        try {
            ObjectNode body = mapper.createObjectNode();
            body.put("libelle", libelle); body.put("code", code);
            body.put("niveau", niveau); body.put("brancheId", brancheId);
            HttpRequest req = HttpRequest.newBuilder().uri(URI.create(baseUrl + "/api/permissions"))
                    .header("Content-Type", "application/json").header("Authorization", "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body))).build();
            return http.send(req, HttpResponse.BodyHandlers.ofString()).statusCode() == 200;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public boolean supprimerPermission(Long id, String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder().uri(URI.create(baseUrl + "/api/permissions/" + id))
                    .header("Authorization", "Bearer " + token).DELETE().build();
            return http.send(req, HttpResponse.BodyHandlers.ofString()).statusCode() == 204;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public boolean creerAdmin(String nom, String prenom, String email, String motDePasse, String token) {
        try {
            ObjectNode body = mapper.createObjectNode();
            body.put("nom", nom); body.put("prenom", prenom);
            body.put("email", email); body.put("motDePasse", motDePasse); body.put("role", "ADMIN");
            HttpRequest req = HttpRequest.newBuilder().uri(URI.create(baseUrl + "/api/utilisateurs/admin"))
                    .header("Content-Type", "application/json").header("Authorization", "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body))).build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            return resp.statusCode() == 200 || resp.statusCode() == 201;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    /** Reset mot de passe (Admin) - retourne mot de passe temporaire */
    public String reinitialiserMotDePasse(Long id, String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/utilisateurs/reset-password/" + id))
                    .header("Authorization", "Bearer " + token)
                    .PUT(HttpRequest.BodyPublishers.noBody()).build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                java.util.Map<?, ?> map = mapper.readValue(resp.body(), java.util.Map.class);
                return (String) map.get("motDePasseTemporaire");
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    /** Import CSV en masse */
    public int importerCSV(byte[] csvBytes, String token) {
        try {
            String boundary = "----FormBoundary" + System.currentTimeMillis();
            String hdr = "--" + boundary + "\r\nContent-Disposition: form-data; name=\"file\"; filename=\"import.csv\"\r\nContent-Type: text/csv\r\n\r\n";
            String ftr = "\r\n--" + boundary + "--\r\n";
            byte[] h = hdr.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            byte[] f = ftr.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            byte[] body = new byte[h.length + csvBytes.length + f.length];
            System.arraycopy(h, 0, body, 0, h.length);
            System.arraycopy(csvBytes, 0, body, h.length, csvBytes.length);
            System.arraycopy(f, 0, body, h.length + csvBytes.length, f.length);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/utilisateurs/import-csv"))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(body)).build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                java.util.Map<?, ?> map = mapper.readValue(resp.body(), java.util.Map.class);
                return ((Number) map.get("crees")).intValue();
            }
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    // ─── Audit ───────────────────────────────────────────────────────────────

    /** Persiste une entree d'audit en base via user_service */
    public void logAudit(String action, String entite, String detail, String adminEmail, String niveau, String token) {
        try {
            ObjectNode body = mapper.createObjectNode();
            body.put("action", action); body.put("entite", entite);
            body.put("detail", detail); body.put("adminEmail", adminEmail);
            body.put("niveau", niveau);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/audit"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + (token != null ? token : ""))
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body))).build();
            http.send(req, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) { /* audit ne doit pas bloquer */ }
    }

    /** Liste les entrees d'audit depuis la base */
    public List<java.util.Map<String, Object>> listerAudit(int limit, String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/audit?limit=" + limit))
                    .header("Authorization", "Bearer " + token).GET().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200)
                return mapper.readValue(resp.body(), new TypeReference<List<java.util.Map<String, Object>>>() {});
        } catch (Exception e) { e.printStackTrace(); }
        return List.of();
    }

    // ─── Expiration comptes inactifs ──────────────────────────────────────────

    /** Declenche l'expiration manuelle des comptes inactifs depuis N jours */
    public int expirerComptesInactifs(int joursInactivite, String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/utilisateurs/expirer-inactifs?jours=" + joursInactivite))
                    .header("Authorization", "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.noBody()).build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                java.util.Map<?, ?> map = mapper.readValue(resp.body(), java.util.Map.class);
                return ((Number) map.get("expires")).intValue();
            }
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    // ─── 2FA ─────────────────────────────────────────────────────────────────

    /** Initialise le 2FA pour un admin - retourne { secret, otpauthUrl } */
    public java.util.Map<String, String> setup2FA(Long userId, String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/auth/2fa/setup/" + userId))
                    .header("Authorization", "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.noBody()).build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200)
                return mapper.readValue(resp.body(), new TypeReference<java.util.Map<String, String>>() {});
        } catch (Exception e) { e.printStackTrace(); }
        return java.util.Map.of();
    }

    /** Verifie un code TOTP - retourne { valid: true/false, token: jwt } */
    public java.util.Map<String, Object> verifier2FA(String email, String code) {
        try {
            ObjectNode body = mapper.createObjectNode();
            body.put("email", email); body.put("code", code);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/auth/2fa/verify"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body))).build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200)
                return mapper.readValue(resp.body(), new TypeReference<java.util.Map<String, Object>>() {});
        } catch (Exception e) { e.printStackTrace(); }
        return java.util.Map.of("valid", false);
    }

    /** Desactive le 2FA pour un admin */
    public boolean disable2FA(Long userId, String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/auth/2fa/disable/" + userId))
                    .header("Authorization", "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.noBody()).build();
            return http.send(req, HttpResponse.BodyHandlers.ofString()).statusCode() == 200;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    /** Modifie le profil d'un utilisateur (nom, prénom, téléphone) */
    public boolean modifierProfil(Long id, String nom, String prenom, String telephone, String token) {
        try {
            String url = baseUrl + "/api/utilisateurs/modifier-profil/" + id
                    + "?nom="       + java.net.URLEncoder.encode(nom       != null ? nom       : "", "UTF-8")
                    + "&prenom="    + java.net.URLEncoder.encode(prenom    != null ? prenom    : "", "UTF-8")
                    + "&telephone=" + java.net.URLEncoder.encode(telephone != null ? telephone : "", "UTF-8");
            HttpRequest req = HttpRequest.newBuilder().uri(java.net.URI.create(url))
                    .header("Authorization", "Bearer " + token)
                    .PUT(HttpRequest.BodyPublishers.noBody()).build();
            return http.send(req, HttpResponse.BodyHandlers.ofString()).statusCode() == 200;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    /** Changement de mot de passe (auto) */
    public boolean changerMotDePasse(Long id, String ancienMdp, String nouveauMdp, String token) {
        try {
            String url = baseUrl + "/api/utilisateurs/changer-mdp/" + id
                    + "?ancienMdp=" + java.net.URLEncoder.encode(ancienMdp,  "UTF-8")
                    + "&nouveauMdp=" + java.net.URLEncoder.encode(nouveauMdp, "UTF-8");
            HttpRequest req = HttpRequest.newBuilder().uri(java.net.URI.create(url))
                    .header("Authorization", "Bearer " + token)
                    .PUT(HttpRequest.BodyPublishers.noBody()).build();
            return http.send(req, HttpResponse.BodyHandlers.ofString()).statusCode() == 200;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    /** Modifie les infos d'une institution */
    public boolean modifierInstitution(Long id, String nom, String adresse,
                                        String secteur, String ninea, String domainesInteret,
                                        String token) {
        try {
            String url = baseUrl + "/api/institutions/" + id
                    + "?nom="             + java.net.URLEncoder.encode(nom             != null ? nom             : "", "UTF-8")
                    + "&adresse="         + java.net.URLEncoder.encode(adresse         != null ? adresse         : "", "UTF-8")
                    + "&secteur="         + java.net.URLEncoder.encode(secteur         != null ? secteur         : "", "UTF-8")
                    + "&ninea="           + java.net.URLEncoder.encode(ninea           != null ? ninea           : "", "UTF-8")
                    + "&domainesInteret=" + java.net.URLEncoder.encode(domainesInteret != null ? domainesInteret : "", "UTF-8");
            HttpRequest req = HttpRequest.newBuilder().uri(java.net.URI.create(url))
                    .header("Authorization", "Bearer " + token)
                    .PUT(HttpRequest.BodyPublishers.noBody()).build();
            return http.send(req, HttpResponse.BodyHandlers.ofString()).statusCode() == 200;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    /** GET /api/utilisateurs/{id}/permissions/effectives — codes fusionnés (profil + directes) */
    public java.util.List<String> getPermissionsEffectives(Long userId, String token) {
        try {
            String url = baseUrl + "/api/utilisateurs/" + userId + "/permissions/effectives";
            HttpRequest req = HttpRequest.newBuilder().uri(java.net.URI.create(url))
                    .header("Authorization", "Bearer " + token)
                    .GET().build();
            var resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                return mapper.readValue(resp.body(), new TypeReference<java.util.List<String>>() {});
            }
        } catch (Exception e) { e.printStackTrace(); }
        return new java.util.ArrayList<>();
    }

    /** GET /api/utilisateurs/{id}/permissions — IDs des permissions directes */
    public java.util.List<Long> getPermissionsUtilisateur(Long userId, String token) {
        try {
            String url = baseUrl + "/api/utilisateurs/" + userId + "/permissions";
            HttpRequest req = HttpRequest.newBuilder().uri(java.net.URI.create(url))
                    .header("Authorization", "Bearer " + token)
                    .GET().build();
            var resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                return mapper.readValue(resp.body(), new TypeReference<java.util.List<Long>>() {});
            }
        } catch (Exception e) { e.printStackTrace(); }
        return new java.util.ArrayList<>();
    }

    /** Helper : PUT sans body, retourne true si 200 */
    private boolean put(String url, String token) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + token)
                    .PUT(HttpRequest.BodyPublishers.noBody()).build();
            return http.send(req, HttpResponse.BodyHandlers.ofString()).statusCode() == 200;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    /** PUT /api/utilisateurs/{id}/permissions — remplace les permissions directes */
    public boolean assignerPermissionsDirectes(Long userId, java.util.List<Long> ids, String token) {
        try {
            String body = mapper.writeValueAsString(ids != null ? ids : java.util.Collections.emptyList());
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(java.net.URI.create(baseUrl + "/api/utilisateurs/" + userId + "/permissions"))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(body)).build();
            return http.send(req, HttpResponse.BodyHandlers.ofString()).statusCode() == 200;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }
}
