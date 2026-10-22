package com.senexpert.formationservice.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Health check Kubernetes
                .requestMatchers("/actuator/health").permitAll()
                // Endpoint admin (tous statuts) : réservé aux rôles authentifiés
                .requestMatchers(HttpMethod.GET, "/api/offres/admin").hasAnyRole("ADMIN", "FORMATEUR", "AGENT")
                // Lecture publique des offres (DISPONIBLE seulement) et domaines
                .requestMatchers(HttpMethod.GET, "/api/offres/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/domaines/**").permitAll()
                // Création d'offres et domaines : ADMIN seulement
                .requestMatchers(HttpMethod.POST, "/api/offres/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/domaines/**").hasRole("ADMIN")
                // Upload documents pour une demande : tout utilisateur connecté
                .requestMatchers(HttpMethod.POST, "/api/demandes/upload").authenticated()
                // Validation/rejet des demandes : ADMIN seulement
                .requestMatchers("/api/demandes/valider/**").hasRole("ADMIN")
                .requestMatchers("/api/demandes/rejeter/**").hasRole("ADMIN")
                // Demandes libre : admin pour valider/rejeter/lister-tout
                .requestMatchers("/api/demandes-libre/valider/**").hasRole("ADMIN")
                .requestMatchers("/api/demandes-libre/rejeter/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/demandes-libre").hasRole("ADMIN")
                // E-learning : lecture publique (cours publiés via ?admin=false)
                .requestMatchers(HttpMethod.GET, "/api/elearning/cours/offre/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/elearning/ping").permitAll()
                // Formateurs : lecture publique, écriture ADMIN ou AGENT
                .requestMatchers(HttpMethod.GET, "/api/formateurs/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/formateurs/**").hasAnyRole("ADMIN", "AGENT")
                .requestMatchers(HttpMethod.PUT, "/api/formateurs/**").hasAnyRole("ADMIN", "AGENT")
                .requestMatchers(HttpMethod.DELETE, "/api/formateurs/**").hasAnyRole("ADMIN", "AGENT")
                // Reste : authentifié
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
