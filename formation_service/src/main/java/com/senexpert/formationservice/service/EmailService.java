package com.senexpert.formationservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

/**
 * Service d'envoi d'emails automatiques.
 * Activé seulement si app.mail.enabled=true (variable d'env MAIL_ENABLED).
 * Tous les envois sont asynchrones (@Async) pour ne pas bloquer les traitements admin.
 */
@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${app.mail.from:SenExpertFormation <noreply@senexpert.sn>}")
    private String mailFrom;

    // ─── Candidatures ─────────────────────────────────────────────────────────

    @Async
    public void envoyerCandidatureAcceptee(String destinataire, String nomCandidat, String titrOffre) {
        if (!mailEnabled || destinataire == null) return;
        String sujet = "Votre candidature a été acceptée — " + titrOffre;
        String corps = construireHtml(
            "Félicitations, " + nomCandidat + " !",
            "Votre candidature pour la formation <strong>« " + titrOffre + " »</strong> a été <span style='color:#059669;font-weight:700;'>acceptée</span>.",
            "Vous pouvez vous connecter à la plateforme SenExpertFormation pour consulter les détails et les prochaines étapes.",
            "#059669", "ACCEPTÉE"
        );
        envoyer(destinataire, sujet, corps);
    }

    @Async
    public void envoyerCandidatureRefusee(String destinataire, String nomCandidat, String titreOffre) {
        if (!mailEnabled || destinataire == null) return;
        String sujet = "Résultat de votre candidature — " + titreOffre;
        String corps = construireHtml(
            "Bonjour " + nomCandidat + ",",
            "Après examen de votre dossier, votre candidature pour la formation <strong>« " + titreOffre + " »</strong> n'a pas été retenue.",
            "Nous vous encourageons à consulter les autres offres de formation disponibles sur la plateforme. Notre équipe reste disponible pour toute question.",
            "#DC2626", "NON RETENUE"
        );
        envoyer(destinataire, sujet, corps);
    }

    // ─── Demandes ─────────────────────────────────────────────────────────────

    @Async
    public void envoyerDemandeValidee(String destinataire, String nomCandidat, String titreOffre) {
        if (!mailEnabled || destinataire == null) return;
        String sujet = "Votre demande de formation a été validée — " + titreOffre;
        String corps = construireHtml(
            "Bonne nouvelle, " + nomCandidat + " !",
            "Votre demande pour la formation <strong>« " + titreOffre + " »</strong> a été <span style='color:#059669;font-weight:700;'>validée</span> par l'administration.",
            "Vous pouvez désormais finaliser votre inscription et accéder aux ressources de la formation depuis votre espace personnel.",
            "#059669", "VALIDÉE"
        );
        envoyer(destinataire, sujet, corps);
    }

    @Async
    public void envoyerDemandeRejetee(String destinataire, String nomCandidat, String titreOffre) {
        if (!mailEnabled || destinataire == null) return;
        String sujet = "Résultat de votre demande de formation — " + titreOffre;
        String corps = construireHtml(
            "Bonjour " + nomCandidat + ",",
            "Votre demande pour la formation <strong>« " + titreOffre + " »</strong> a été <span style='color:#DC2626;font-weight:700;'>rejetée</span> par l'administration.",
            "Pour toute question ou pour soumettre une nouvelle demande, connectez-vous à votre espace sur la plateforme SenExpertFormation.",
            "#DC2626", "REJETÉE"
        );
        envoyer(destinataire, sujet, corps);
    }

    // ─── Envoi effectif ───────────────────────────────────────────────────────

    private void envoyer(String destinataire, String sujet, String corpsHtml) {
        if (mailSender == null) {
            System.out.println("[EMAIL SIMULÉ] À: " + destinataire + " | Sujet: " + sujet);
            return;
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(mailFrom);
            helper.setTo(destinataire);
            helper.setSubject(sujet);
            helper.setText(corpsHtml, true);
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("[EmailService] Échec envoi à " + destinataire + " : " + e.getMessage());
        }
    }

    // ─── Template HTML ────────────────────────────────────────────────────────

    private String construireHtml(String titre, String corps, String action, String couleur, String badge) {
        return "<!DOCTYPE html><html><body style='margin:0;padding:0;background:#f1f5f9;font-family:Arial,sans-serif;'>"
            + "<table width='100%' cellpadding='0' cellspacing='0'><tr><td align='center' style='padding:40px 20px;'>"
            + "<table width='560' cellpadding='0' cellspacing='0' style='background:white;border-radius:16px;overflow:hidden;box-shadow:0 4px 20px rgba(0,0,0,.08);'>"
            // Header
            + "<tr><td style='background:linear-gradient(135deg,#1e1b4b,#3730a3);padding:32px 40px;text-align:center;'>"
            + "<h1 style='color:white;margin:0;font-size:22px;letter-spacing:-0.5px;'>SenExpert<span style='color:#a5b4fc;'>Formation</span></h1>"
            + "<p style='color:rgba(255,255,255,0.6);margin:6px 0 0;font-size:13px;'>Plateforme nationale de formation professionnelle</p>"
            + "</td></tr>"
            // Badge statut
            + "<tr><td style='padding:28px 40px 0;text-align:center;'>"
            + "<span style='display:inline-block;background:" + couleur + ";color:white;border-radius:100px;padding:6px 20px;font-size:12px;font-weight:700;letter-spacing:1px;'>"
            + badge + "</span>"
            + "</td></tr>"
            // Corps
            + "<tr><td style='padding:24px 40px;'>"
            + "<h2 style='color:#1e1b4b;font-size:18px;margin:0 0 12px;'>" + titre + "</h2>"
            + "<p style='color:#475569;font-size:14px;line-height:1.6;margin:0 0 16px;'>" + corps + "</p>"
            + "<p style='color:#475569;font-size:14px;line-height:1.6;margin:0;'>" + action + "</p>"
            + "</td></tr>"
            // CTA
            + "<tr><td style='padding:0 40px 28px;text-align:center;'>"
            + "<a href='#' style='display:inline-block;background:#3730a3;color:white;text-decoration:none;border-radius:10px;padding:12px 28px;font-size:14px;font-weight:600;'>Accéder à mon espace</a>"
            + "</td></tr>"
            // Footer
            + "<tr><td style='background:#f8fafc;padding:20px 40px;text-align:center;border-top:1px solid #e2e8f0;'>"
            + "<p style='color:#94a3b8;font-size:12px;margin:0;'>© SenExpertFormation · Dakar, Sénégal<br/>"
            + "Cet email a été envoyé automatiquement, merci de ne pas y répondre.</p>"
            + "</td></tr>"
            + "</table></td></tr></table></body></html>";
    }
}
