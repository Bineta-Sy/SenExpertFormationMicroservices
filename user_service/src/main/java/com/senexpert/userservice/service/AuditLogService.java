package com.senexpert.userservice.service;

import com.senexpert.userservice.entity.AuditLog;
import com.senexpert.userservice.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    public AuditLog enregistrer(String action, String entite, String detail,
                                 String adminEmail, String niveau) {
        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setEntite(entite);
        log.setDetail(detail != null && detail.length() > 500 ? detail.substring(0, 497) + "..." : detail);
        log.setAdminEmail(adminEmail);
        log.setNiveau(niveau);
        return auditLogRepository.save(log);
    }

    /** Retourne les N entrées les plus récentes (défaut 200) */
    public List<AuditLog> listerRecents(int limit) {
        return auditLogRepository.findTopByOrderByDateActionDesc(PageRequest.of(0, limit));
    }

    public List<AuditLog> listerParAdmin(String adminEmail) {
        return auditLogRepository.findByAdminEmailOrderByDateActionDesc(adminEmail);
    }

    public List<AuditLog> listerParNiveau(String niveau) {
        return auditLogRepository.findByNiveauOrderByDateActionDesc(niveau);
    }

    public long compter() {
        return auditLogRepository.count();
    }
}
