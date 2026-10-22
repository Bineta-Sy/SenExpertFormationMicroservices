package com.senexpert.userservice.service;

import com.senexpert.userservice.dto.RegisterRequest;
import com.senexpert.userservice.entity.Institution;
import com.senexpert.userservice.entity.Profil;
import com.senexpert.userservice.entity.Role;
import com.senexpert.userservice.entity.Statut;
import com.senexpert.userservice.entity.TypeInstitution;
import com.senexpert.userservice.entity.Utilisateur;
import com.senexpert.userservice.exception.BusinessException;
import com.senexpert.userservice.exception.ResourceNotFoundException;
import com.senexpert.userservice.entity.Permission;
import com.senexpert.userservice.repository.InstitutionRepository;
import com.senexpert.userservice.repository.PermissionRepository;
import com.senexpert.userservice.repository.ProfilRepository;
import com.senexpert.userservice.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UtilisateurService {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private InstitutionRepository institutionRepository;

    @Autowired
    private ProfilRepository profilRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Utilisateur inscrire(RegisterRequest request) {
        if (utilisateurRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email déjà utilisé : " + request.getEmail());
        }

        // Sécurité : interdire l'auto-attribution du rôle ADMIN
        Role role;
        try {
            role = Role.valueOf(request.getRole());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Rôle invalide : " + request.getRole());
        }
        if (role == Role.ADMIN) {
            throw new BusinessException("Vous ne pouvez pas vous inscrire avec le rôle ADMIN");
        }

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setNom(request.getNom());
        utilisateur.setPrenom(request.getPrenom());
        utilisateur.setEmail(request.getEmail());
        utilisateur.setMotDePasse(passwordEncoder.encode(request.getMotDePasse()));
        utilisateur.setRole(role);
        utilisateur.setStatut(Statut.EN_ATTENTE);

        // Champs communs
        if (request.getTelephone() != null && !request.getTelephone().isBlank())
            utilisateur.setTelephone(request.getTelephone().trim());

        // Champs spécifiques PARTICULIER
        if (role == Role.PARTICULIER) {
            if (request.getProfession() != null && !request.getProfession().isBlank())
                utilisateur.setProfession(request.getProfession().trim());
            if (request.getNumeroCni() != null && !request.getNumeroCni().isBlank())
                utilisateur.setNumeroCni(request.getNumeroCni().trim());
        }

        // Champs spécifiques AGENT
        if (role == Role.AGENT) {
            if (request.getNumeroInstitution() != null && !request.getNumeroInstitution().isBlank())
                utilisateur.setNumeroInstitution(request.getNumeroInstitution().trim());

            // Créer l'institution si les champs essentiels sont fournis
            if (request.getNomInstitution() != null && !request.getNomInstitution().isBlank()) {
                Institution inst = new Institution();
                inst.setNom(request.getNomInstitution().trim());
                if (request.getTypeInstitution() != null && !request.getTypeInstitution().isBlank()) {
                    try { inst.setType(TypeInstitution.valueOf(request.getTypeInstitution().trim())); }
                    catch (IllegalArgumentException ignored) { inst.setType(TypeInstitution.ENTREPRISE); }
                }
                if (request.getSecteurInstitution() != null && !request.getSecteurInstitution().isBlank())
                    inst.setSecteur(request.getSecteurInstitution().trim());
                if (request.getNumeroInstitution() != null && !request.getNumeroInstitution().isBlank())
                    inst.setNinea(request.getNumeroInstitution().trim());
                inst.setStatut(Statut.EN_ATTENTE);
                Institution saved = institutionRepository.save(inst);
                utilisateur.setInstitution(saved);
            }
        }

        // Liaison optionnelle à une institution existante
        if (request.getInstitutionId() != null) {
            Institution institution = institutionRepository.findById(request.getInstitutionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Institution non trouvée : id=" + request.getInstitutionId()));
            utilisateur.setInstitution(institution);
        }

        return utilisateurRepository.save(utilisateur);
    }

    public List<Utilisateur> listerTous() {
        return utilisateurRepository.findAll();
    }

    public Utilisateur valider(Long id) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé : id=" + id));
        utilisateur.setStatut(Statut.ACTIF);
        return utilisateurRepository.save(utilisateur);
    }

    public Utilisateur trouverParId(Long id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé : id=" + id));
    }

    public Utilisateur trouverParEmail(String email) {
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé : " + email));
    }

    /** Change le rôle d'un utilisateur (Admin uniquement) */
    public Utilisateur changerRole(Long id, String nouveauRole) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé : id=" + id));
        try {
            utilisateur.setRole(Role.valueOf(nouveauRole));
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Rôle invalide : " + nouveauRole);
        }
        return utilisateurRepository.save(utilisateur);
    }

    /** Désactive un compte utilisateur */
    public Utilisateur desactiver(Long id) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé : id=" + id));
        if (utilisateur.getRole() == Role.ADMIN) {
            throw new BusinessException("Impossible de désactiver un compte ADMIN.");
        }
        utilisateur.setStatut(Statut.INACTIF);
        return utilisateurRepository.save(utilisateur);
    }

    /** Réactive un compte utilisateur */
    public Utilisateur reactiver(Long id) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé : id=" + id));
        utilisateur.setStatut(Statut.ACTIF);
        return utilisateurRepository.save(utilisateur);
    }

    /** Supprime un compte utilisateur */
    public void supprimer(Long id) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé : id=" + id));
        if (utilisateur.getRole() == Role.ADMIN) {
            throw new BusinessException("Impossible de supprimer un compte ADMIN.");
        }
        utilisateurRepository.delete(utilisateur);
    }

    /** Recherche des utilisateurs par nom, prénom ou email */
    public List<Utilisateur> rechercher(String q) {
        return utilisateurRepository.rechercher(q);
    }

    /** Assigne un profil à un utilisateur */
    public Utilisateur assignerProfil(Long id, Long profilId) {
        Utilisateur utilisateur = trouverParId(id);
        Profil profil = profilRepository.findById(profilId)
            .orElseThrow(() -> new ResourceNotFoundException("Profil non trouvé : id=" + profilId));
        utilisateur.setProfil(profil);
        return utilisateurRepository.save(utilisateur);
    }

    /**
     * Réinitialise le mot de passe d'un utilisateur (Admin).
     * Retourne le nouveau mot de passe temporaire en clair pour que l'admin puisse le communiquer.
     */
    public String reinitialiserMotDePasse(Long id) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé : id=" + id));
        String tempPassword = "Temp@" + id + "!" + (int)(Math.random() * 9000 + 1000);
        utilisateur.setMotDePasse(passwordEncoder.encode(tempPassword));
        utilisateurRepository.save(utilisateur);
        return tempPassword;
    }

    /**
     * Import en masse d'utilisateurs depuis une liste de RegisterRequest.
     * Retourne le nombre de comptes créés avec succès.
     */
    public int importerEnMasse(List<RegisterRequest> requests) {
        int count = 0;
        for (RegisterRequest req : requests) {
            try {
                if (!utilisateurRepository.existsByEmail(req.getEmail())) {
                    Role role;
                    try { role = Role.valueOf(req.getRole()); } catch (Exception e) { role = Role.PARTICULIER; }
                    if (role == Role.ADMIN) role = Role.PARTICULIER;
                    Utilisateur u = new Utilisateur();
                    u.setNom(req.getNom());
                    u.setPrenom(req.getPrenom());
                    u.setEmail(req.getEmail());
                    u.setMotDePasse(passwordEncoder.encode(
                        req.getMotDePasse() != null ? req.getMotDePasse() : "Changeme123!"));
                    u.setRole(role);
                    u.setStatut(Statut.ACTIF);
                    utilisateurRepository.save(u);
                    count++;
                }
            } catch (Exception ignored) {}
        }
        return count;
    }

    /**
     * Modifie le profil d'un utilisateur (nom, prénom, téléphone).
     * Utilisé par l'agent institution depuis son dashboard.
     */
    public Utilisateur modifierProfil(Long id, String nom, String prenom, String telephone) {
        Utilisateur u = utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé : id=" + id));
        if (nom != null && !nom.isBlank()) u.setNom(nom.trim());
        if (prenom != null && !prenom.isBlank()) u.setPrenom(prenom.trim());
        if (telephone != null) u.setTelephone(telephone.trim());
        return utilisateurRepository.save(u);
    }

    /**
     * Changement de mot de passe par l'utilisateur lui-même.
     * Vérifie l'ancien mot de passe avant de mettre à jour.
     */
    public void changerMotDePasse(Long id, String ancienMdp, String nouveauMdp) {
        Utilisateur u = utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé : id=" + id));
        if (!passwordEncoder.matches(ancienMdp, u.getMotDePasse())) {
            throw new BusinessException("Ancien mot de passe incorrect.");
        }
        if (nouveauMdp == null || nouveauMdp.length() < 6) {
            throw new BusinessException("Le nouveau mot de passe doit contenir au moins 6 caractères.");
        }
        u.setMotDePasse(passwordEncoder.encode(nouveauMdp));
        utilisateurRepository.save(u);
    }

    /** Retourne les IDs des permissions directes d'un utilisateur */
    public java.util.List<Long> getPermissionsDirectesIds(Long userId) {
        Utilisateur u = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé : id=" + userId));
        return u.getPermissionsDirectes().stream()
                .map(Permission::getId)
                .collect(java.util.stream.Collectors.toList());
    }

    /** Retourne les codes de permissions effectives (profil + directes) sans doublon */
    public java.util.List<String> getPermissionsEffectivesCodes(Long userId) {
        Utilisateur u = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé : id=" + userId));
        java.util.Set<String> codes = new java.util.LinkedHashSet<>();
        // Permissions du profil
        if (u.getProfil() != null && u.getProfil().getPermissions() != null) {
            u.getProfil().getPermissions().stream()
                    .map(Permission::getCode)
                    .forEach(codes::add);
        }
        // Permissions directes (override / ajout)
        if (u.getPermissionsDirectes() != null) {
            u.getPermissionsDirectes().stream()
                    .map(Permission::getCode)
                    .forEach(codes::add);
        }
        return new java.util.ArrayList<>(codes);
    }

    /** Remplace les permissions directes d'un utilisateur */
    public void setPermissionsDirectes(Long userId, java.util.List<Long> permissionIds) {
        Utilisateur u = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé : id=" + userId));
        java.util.Set<Permission> perms = new java.util.HashSet<>(
                permissionRepository.findAllById(permissionIds != null ? permissionIds : java.util.Collections.emptyList()));
        u.setPermissionsDirectes(perms);
        utilisateurRepository.save(u);
    }

    /** Crée un compte ADMIN (réservé à un admin existant) */
    public Utilisateur creerAdmin(RegisterRequest request) {
        if (utilisateurRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email déjà utilisé : " + request.getEmail());
        }
        Utilisateur admin = new Utilisateur();
        admin.setNom(request.getNom());
        admin.setPrenom(request.getPrenom());
        admin.setEmail(request.getEmail());
        admin.setMotDePasse(passwordEncoder.encode(request.getMotDePasse()));
        admin.setRole(Role.ADMIN);
        admin.setStatut(Statut.ACTIF);
        return utilisateurRepository.save(admin);
    }
}
