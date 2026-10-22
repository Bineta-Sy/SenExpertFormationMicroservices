package com.senexpert.frontend.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AuditEntry implements Serializable {
    private static long counter = 1;
    private Long id;
    private String action;
    private String entite;
    private String detail;
    private String adminEmail;
    private String date;
    private String niveau; // INFO, WARNING, DANGER

    public AuditEntry() {}

    public AuditEntry(String action, String entite, String detail, String adminEmail, String niveau) {
        this.id = counter++;
        this.action = action;
        this.entite = entite;
        this.detail = detail;
        this.adminEmail = adminEmail;
        this.date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        this.niveau = niveau;
    }

    public Long getId() { return id; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getEntite() { return entite; }
    public void setEntite(String entite) { this.entite = entite; }
    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
    public String getAdminEmail() { return adminEmail; }
    public void setAdminEmail(String adminEmail) { this.adminEmail = adminEmail; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getNiveau() { return niveau; }
    public void setNiveau(String niveau) { this.niveau = niveau; }
}
