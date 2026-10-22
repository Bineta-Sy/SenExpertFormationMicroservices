package com.senexpert.userservice.repository;

import com.senexpert.userservice.entity.AuditLog;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /** Les N entrées les plus récentes, triées par date décroissante */
    @Query("SELECT a FROM AuditLog a ORDER BY a.dateAction DESC")
    List<AuditLog> findTopByOrderByDateActionDesc(PageRequest pageRequest);

    List<AuditLog> findByAdminEmailOrderByDateActionDesc(String adminEmail);

    List<AuditLog> findByNiveauOrderByDateActionDesc(String niveau);
}
