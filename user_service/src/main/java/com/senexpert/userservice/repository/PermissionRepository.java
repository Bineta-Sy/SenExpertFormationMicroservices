package com.senexpert.userservice.repository;

import com.senexpert.userservice.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
    List<Permission> findByCodeContainingIgnoreCaseOrLibelleContainingIgnoreCase(String code, String libelle);
}
