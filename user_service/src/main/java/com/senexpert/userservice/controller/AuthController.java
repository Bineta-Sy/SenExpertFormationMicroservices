//package com.senexpert.userservice.controller;
//
//import com.senexpert.userservice.dto.LoginRequest;
//import com.senexpert.userservice.dto.LoginResponse;
//import com.senexpert.userservice.entity.Role;
//import com.senexpert.userservice.entity.Utilisateur;
//import com.senexpert.userservice.repository.UtilisateurRepository;
//import com.senexpert.userservice.security.JwtUtil;
//import com.senexpert.userservice.service.TotpService;
//import com.senexpert.userservice.service.UtilisateurService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.BadCredentialsException;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.web.bind.annotation.*;
//
//import java.time.LocalDateTime;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/auth")
//public class AuthController {
//
//    @Autowired private AuthenticationManager authenticationManager;
//    @Autowired private JwtUtil jwtUtil;
//    @Autowired private UtilisateurService utilisateurService;
//    @Autowired private UtilisateurRepository utilisateurRepository;
//    @Autowired private TotpService totpService;
//
//    @PostMapping("/login")
//    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
//        try {
//            authenticationManager.authenticate(
//                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getMotDePasse())
//            );
//        } catch (BadCredentialsException e) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Email ou mot de passe incorrect");
//        }
//
//        Utilisateur utilisateur = utilisateurService.trouverParEmail(request.getEmail());
//
//        if (!utilisateur.getStatut().name().equals("ACTIF")) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN)
//                    .body("Compte non encore valide par l'administrateur");
//        }
//
//        // Mettre a jour la derniere connexion
//        utilisateur.setDerniereConnexion(LocalDateTime.now());
//        utilisateurRepository.save(utilisateur);
//
//        // Si admin avec 2FA active : retourner requiresTwoFactor sans JWT
//        if (utilisateur.getRole() == Role.ADMIN && utilisateur.isTotpActif()) {
//            return ResponseEntity.ok(Map.of(
//                "requiresTwoFactor", true,
//                "email", utilisateur.getEmail()
//            ));
//        }
//
//        String token = jwtUtil.generateToken(utilisateur.getEmail(), utilisateur.getRole().name());
//        return ResponseEntity.ok(new LoginResponse());
//    }
//
//    // ─── 2FA endpoints ────────────────────────────────────────────────────────
//
//    /** Initialise le 2FA : genere un secret TOTP et retourne l'URL otpauth */
//    @PostMapping("/2fa/setup/{id}")
//    public ResponseEntity<Map<String, String>> setup2FA(@PathVariable Long id) {
//        Utilisateur u = utilisateurService.trouverParId(id);
//        String secret = totpService.genererSecret();
//        u.setTotpSecret(secret);
//        u.setTotpActif(false); // actif seulement apres verification
//        utilisateurRepository.save(u);
//        String otpauthUrl = totpService.construireOtpauthUrl(secret, u.getEmail());
//        return ResponseEntity.ok(Map.of("secret", secret, "otpauthUrl", otpauthUrl));
//    }
//
//    /**
//     * Verifie le code TOTP lors du login (2eme etape).
//     * Body : { email, code }
//     * Retourne le JWT si code valide.
//     */
//    @PostMapping("/2fa/verify")
//    public ResponseEntity<?> verify2FA(@RequestBody Map<String, String> body) {
//        String email = body.get("email");
//        String code  = body.get("code");
//        if (email == null || code == null)
//            return ResponseEntity.badRequest().body("email et code requis");
//
//        Utilisateur u = utilisateurService.trouverParEmail(email);
//        if (u.getTotpSecret() == null)
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("2FA non configure");
//
//        if (!totpService.verifierCode(u.getTotpSecret(), code)) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Code invalide");
//        }
//
//        // Activer le 2FA si ce n'est pas encore fait (premier verify apres setup)
//        if (!u.isTotpActif()) {
//            u.setTotpActif(true);
//            utilisateurRepository.save(u);
//        }
//
//        String token = jwtUtil.generateToken(u.getEmail(), u.getRole().name());
//        return ResponseEntity.ok(Map.of("valid", true, "token", token,
//                "email", u.getEmail(), "role", u.getRole().name()));
//    }
//
//    /** Desactive le 2FA pour un admin */
//    @PostMapping("/2fa/disable/{id}")
//    public ResponseEntity<Map<String, Object>> disable2FA(@PathVariable Long id) {
//        Utilisateur u = utilisateurService.trouverParId(id);
//        u.setTotpSecret(null);
//        u.setTotpActif(false);
//        utilisateurRepository.save(u);
//        return ResponseEntity.ok(Map.of("disabled", true));
//    }
//}


package com.senexpert.userservice.controller;

import com.senexpert.userservice.dto.LoginRequest;
import com.senexpert.userservice.dto.LoginResponse;
import com.senexpert.userservice.entity.Utilisateur;
import com.senexpert.userservice.repository.UtilisateurRepository;
import com.senexpert.userservice.security.JwtUtil;
import com.senexpert.userservice.service.UtilisateurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private UtilisateurService utilisateurService;
    @Autowired private UtilisateurRepository utilisateurRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getMotDePasse())
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Email ou mot de passe incorrect");
        }

        Utilisateur utilisateur = utilisateurService.trouverParEmail(request.getEmail());

        if (!utilisateur.getStatut().name().equals("ACTIF")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Compte non encore valide par l'administrateur");
        }

        // Mettre à jour la dernière connexion
        utilisateur.setDerniereConnexion(LocalDateTime.now());
        utilisateurRepository.save(utilisateur);

        // Génération directe du token JWT pour tout le monde (Admin y compris)
        String token = jwtUtil.generateToken(utilisateur.getEmail(), utilisateur.getRole().name());
        
        return ResponseEntity.ok(new LoginResponse(
            token, 
            utilisateur.getEmail(), 
            utilisateur.getRole().name(), 
            utilisateur.getStatut().name()
        ));
    }
}