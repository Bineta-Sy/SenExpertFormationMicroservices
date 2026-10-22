package com.senexpert.userservice.service;

import com.senexpert.userservice.entity.Permission;
import com.senexpert.userservice.entity.Profil;
import com.senexpert.userservice.exception.ResourceNotFoundException;
import com.senexpert.userservice.repository.ProfilRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProfilService {

    @Autowired private ProfilRepository profilRepository;
    @Autowired private PermissionService permissionService;

    public Profil creer(String nom) {
        Profil p = new Profil();
        p.setNom(nom);
        return profilRepository.save(p);
    }

    public List<Profil> listerTous() {
        return profilRepository.findAll();
    }

    public Profil trouverParId(Long id) {
        return profilRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Profil non trouvé : id=" + id));
    }

    public Profil ajouterPermission(Long profilId, Long permissionId) {
        Profil profil = trouverParId(profilId);
        Permission permission = permissionService.trouverParId(permissionId);
        profil.getPermissions().add(permission);
        return profilRepository.save(profil);
    }

    public Profil retirerPermission(Long profilId, Long permissionId) {
        Profil profil = trouverParId(profilId);
        profil.getPermissions().removeIf(p -> p.getId().equals(permissionId));
        return profilRepository.save(profil);
    }

    public void supprimer(Long id) {
        profilRepository.delete(trouverParId(id));
    }
}
