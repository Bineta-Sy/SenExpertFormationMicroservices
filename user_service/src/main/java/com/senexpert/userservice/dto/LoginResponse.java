//package com.senexpert.userservice.dto;
//
//import lombok.AllArgsConstructor;
//import lombok.NoArgsConstructor; // Ajoute ça aussi, c'est mieux pour Jackson
//import lombok.Data;
//
//@Data
//@AllArgsConstructor
//@NoArgsConstructor
//public class LoginResponse {
//    private String token;
//    private String email;
//    private String role;
//    private String statut;
//    
//    // SUPPRIME TOUS LES GETTERS ET SETTERS ICI. 
//    // Lombok (@Data) s'en occupe déjà. Avoir les deux peut créer des conflits de génération.
//}

package com.senexpert.userservice.dto;

public class LoginResponse {
    private String token;
    private String email;
    private String role;
    private String statut;

    // 1. Constructeur vide (pour Jackson)
    public LoginResponse() {
    }

    // 2. Le fameux constructeur à 4 arguments attendu par le contrôleur
    public LoginResponse(String token, String email, String role, String statut) {
        this.token = token;
        this.email = email;
        this.role = role;
        this.statut = statut;
    }

    // 3. Les Getters et Setters manuels
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
}