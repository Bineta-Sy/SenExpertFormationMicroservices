package com.senexpert.userservice.service;

import com.senexpert.userservice.entity.NiveauPermission;
import com.senexpert.userservice.entity.Permission;
import com.senexpert.userservice.exception.ResourceNotFoundException;
import com.senexpert.userservice.repository.PermissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PermissionService {

    @Autowired private PermissionRepository permissionRepository;
    @Autowired private BrancheService brancheService;

    public Permission creer(String libelle, String code, String niveau, Long brancheId) {
        Permission p = new Permission();
        p.setLibelle(libelle);
        p.setCode(code);
        p.setNiveau(NiveauPermission.valueOf(niveau));
        p.setBranche(brancheService.trouverParId(brancheId));
        return permissionRepository.save(p);
    }

    public List<Permission> listerTous() {
        return permissionRepository.findAll();
    }

    public List<Permission> rechercher(String filtre) {
        return permissionRepository.findByCodeContainingIgnoreCaseOrLibelleContainingIgnoreCase(filtre, filtre);
    }

    public Permission trouverParId(Long id) {
        return permissionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Permission non trouvée : id=" + id));
    }

    public void supprimer(Long id) {
        permissionRepository.delete(trouverParId(id));
    }
}
