package com.senexpert.formationservice.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class UserServiceClient {

    private final RestTemplate restTemplate;

    @Value("${user-service.url:http://localhost:8081}")
    private String userServiceUrl;

    public UserServiceClient() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Vérifie que l'utilisateur existe et est ACTIF dans le user_service.
     * @throws RuntimeException si l'utilisateur n'existe pas ou n'est pas ACTIF
     */
    @SuppressWarnings("unchecked")
    public void verifierUtilisateurActif(Long utilisateurId) {
        try {
            // Endpoint public /statut — pas de token requis (appel service-à-service)
            java.util.Map<String, String> response = restTemplate.getForObject(
                    userServiceUrl + "/api/utilisateurs/" + utilisateurId + "/statut",
                    java.util.Map.class
            );
            if (response == null) {
                throw new RuntimeException("Utilisateur introuvable : id=" + utilisateurId);
            }
            String statut = response.get("statut");
            if (!"ACTIF".equals(statut)) {
                throw new RuntimeException("Compte non validé (statut: " + statut + ")");
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Impossible de vérifier le statut utilisateur : " + e.getMessage());
        }
    }

    /**
     * Récupère l'email d'un utilisateur pour les notifications.
     * Retourne null si introuvable ou erreur réseau.
     */
    public String getEmailUtilisateur(Long utilisateurId) {
        try {
            UtilisateurResponse r = restTemplate.getForObject(
                    userServiceUrl + "/api/utilisateurs/" + utilisateurId,
                    UtilisateurResponse.class);
            return r != null ? r.getEmail() : null;
        } catch (Exception e) { return null; }
    }

    /** Récupère prénom + nom d'un utilisateur. */
    public String getNomCompletUtilisateur(Long utilisateurId) {
        try {
            UtilisateurResponse r = restTemplate.getForObject(
                    userServiceUrl + "/api/utilisateurs/" + utilisateurId,
                    UtilisateurResponse.class);
            if (r == null) return null;
            return ((r.getPrenom() != null ? r.getPrenom() : "") + " "
                  + (r.getNom()    != null ? r.getNom()    : "")).trim();
        } catch (Exception e) { return null; }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UtilisateurResponse {
        private Long id;
        private String email;
        private String nom;
        private String prenom;
        private String statut;
        private String role;
		public Object getPrenom() {
			// TODO Auto-generated method stub
			return null;
		}
		public String getEmail() {
			// TODO Auto-generated method stub
			return null;
		}
		public Object getNom() {
			// TODO Auto-generated method stub
			return null;
		}
    }
}
