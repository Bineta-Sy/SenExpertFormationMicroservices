package com.senexpert.formationservice.controller;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;
import com.senexpert.formationservice.entity.DemandeFormation;
import com.senexpert.formationservice.entity.ParticipantInstitution;
import com.senexpert.formationservice.repository.DemandeFormationRepository;
import com.senexpert.formationservice.repository.ParticipantInstitutionRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

import java.awt.Color;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/demandes")
public class AttestationController {

    private static final DateTimeFormatter FMT_DATE = DateTimeFormatter.ofPattern("dd MMMM yyyy",
            java.util.Locale.FRENCH);

    private final DemandeFormationRepository demandeRepo;
    private final ParticipantInstitutionRepository participantRepo;

    public AttestationController(DemandeFormationRepository demandeRepo,
                                 ParticipantInstitutionRepository participantRepo) {
        this.demandeRepo = demandeRepo;
        this.participantRepo = participantRepo;
    }

    /**
     * Génère un certificat d'inscription PDF après confirmation du paiement.
     * GET /api/candidatures/{id}/certificat  (alias pratique)
     */
    @GetMapping("/{id}/certificat")
    public void genererCertificat(@PathVariable Long id,
                                  HttpServletResponse response) throws IOException {
        DemandeFormation demande = demandeRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Demande introuvable : " + id));

        String titre   = demande.getOffre() != null ? demande.getOffre().getTitre() : "Formation";
        String dateDoc = LocalDate.now().format(FMT_DATE);
        String dateDebut = (demande.getOffre() != null && demande.getOffre().getDateDebut() != null)
                ? demande.getOffre().getDateDebut().toString() : "—";
        String dateFin   = (demande.getOffre() != null && demande.getOffre().getDateFin() != null)
                ? demande.getOffre().getDateFin().toString() : "—";
        Double cout = (demande.getOffre() != null) ? demande.getOffre().getCout() : null;
        String montant = (cout != null && cout > 0)
                ? String.format("%,.0f FCFA", cout).replace(",", " ") : "Gratuit";

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"certificat-inscription-" + id + ".pdf\"");

        Color navy = new Color(11, 37, 69);
        Color gold = new Color(217, 164, 65);
        Color light = new Color(240, 244, 255);

        Font fTitle  = new Font(Font.HELVETICA, 22, Font.BOLD, navy);
        Font fSub    = new Font(Font.HELVETICA, 10, Font.NORMAL, new Color(200, 210, 230));
        Font fLabel  = new Font(Font.HELVETICA, 10, Font.BOLD, navy);
        Font fValue  = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.DARK_GRAY);
        Font fFooter = new Font(Font.HELVETICA, 8, Font.ITALIC, Color.GRAY);
        Font fCertif = new Font(Font.HELVETICA, 12, Font.NORMAL, Color.DARK_GRAY);

        Document doc = new Document(PageSize.A4, 60, 60, 80, 80);
        PdfWriter.getInstance(doc, response.getOutputStream());
        doc.open();

        // En-tête
        PdfPTable header = new PdfPTable(1);
        header.setWidthPercentage(100);
        PdfPCell hCell = new PdfPCell();
        hCell.setBackgroundColor(navy); hCell.setPadding(20); hCell.setBorder(Rectangle.NO_BORDER);
        Paragraph hPara = new Paragraph("SÉNEXPERT", fTitle);
        hPara.setAlignment(Element.ALIGN_CENTER);
        hCell.addElement(hPara);
        Paragraph hSub = new Paragraph("Certificat d'inscription à une formation", fSub);
        hSub.setAlignment(Element.ALIGN_CENTER);
        hCell.addElement(hSub);
        header.addCell(hCell);
        doc.add(header);
        doc.add(Chunk.NEWLINE);

        // Barre dorée
        PdfPTable bar = new PdfPTable(1);
        bar.setWidthPercentage(100);
        PdfPCell barCell = new PdfPCell(new Phrase(" "));
        barCell.setBackgroundColor(gold); barCell.setBorder(Rectangle.NO_BORDER); barCell.setFixedHeight(4);
        bar.addCell(barCell);
        doc.add(bar);
        doc.add(Chunk.NEWLINE);

        // Texte de certification
        Paragraph certif = new Paragraph(
            "Ce certificat atteste que l'institution enregistrée sous la demande n° " + id +
            " est officiellement inscrite à la formation ci-dessous, le paiement ayant été confirmé.",
            fCertif);
        certif.setSpacingAfter(16);
        doc.add(certif);

        // Infos
        PdfPTable info = new PdfPTable(2);
        info.setWidthPercentage(100);
        info.setWidths(new float[]{1.2f, 2f});
        info.setSpacingBefore(8);
        addInfoRow(info, "Formation :",     titre,    fLabel, fValue, light);
        addInfoRow(info, "N° Demande :",    "#" + id, fLabel, fValue, Color.WHITE);
        addInfoRow(info, "Date de début :", dateDebut, fLabel, fValue, light);
        addInfoRow(info, "Date de fin :",   dateFin,   fLabel, fValue, Color.WHITE);
        addInfoRow(info, "Montant réglé :", montant,   fLabel, fValue, light);
        addInfoRow(info, "Statut :",        "CONFIRMÉE — Inscription validée", fLabel,
                   new Font(Font.HELVETICA, 10, Font.BOLD, new Color(22, 101, 52)), Color.WHITE);
        addInfoRow(info, "Délivré le :",    dateDoc,   fLabel, fValue, light);
        doc.add(info);

        doc.add(Chunk.NEWLINE);
        doc.add(Chunk.NEWLINE);

        // Signature fictive
        PdfPTable sig = new PdfPTable(2);
        sig.setWidthPercentage(100);
        sig.setWidths(new float[]{1f, 1f});
        PdfPCell sc1 = new PdfPCell();
        sc1.setBorder(Rectangle.NO_BORDER); sc1.setPadding(8);
        sc1.addElement(new Phrase("La Direction de SÉNEXPERT",
                new Font(Font.HELVETICA, 10, Font.BOLD, navy)));
        sig.addCell(sc1);
        PdfPCell sc2 = new PdfPCell();
        sc2.setBorder(Rectangle.NO_BORDER); sc2.setPadding(8);
        sc2.setHorizontalAlignment(Element.ALIGN_RIGHT);
        sc2.addElement(new Phrase("Cachet et signature", fValue));
        sig.addCell(sc2);
        doc.add(sig);

        // Pied
        doc.add(Chunk.NEWLINE);
        Paragraph footer = new Paragraph(
            "Document généré automatiquement le " + dateDoc + " — SÉNEXPERT. " +
            "Ce certificat est valable uniquement en présence du justificatif de paiement.",
            fFooter);
        footer.setAlignment(Element.ALIGN_CENTER);
        doc.add(footer);
        doc.close();
    }

    /**
     * Génère une attestation de participation PDF pour une demande d'institution.
     * GET /api/demandes/{id}/attestation
     */
    @GetMapping("/{id}/attestation")
    public void genererAttestation(@PathVariable Long id,
                                   HttpServletResponse response) throws IOException {

        DemandeFormation demande = demandeRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Demande introuvable : " + id));

        List<ParticipantInstitution> participants = participantRepo.findByDemandeIdOrderByNomAsc(id);

        String titre = demande.getOffre() != null ? demande.getOffre().getTitre() : "Formation";
        String dateDoc = LocalDate.now().format(FMT_DATE);

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"attestation-demande-" + id + ".pdf\"");

        Document doc = new Document(PageSize.A4, 60, 60, 80, 80);
        PdfWriter.getInstance(doc, response.getOutputStream());
        doc.open();

        // ── Couleurs thème ──
        Color navy  = new Color(11, 37, 69);   // #0B2545
        Color gold  = new Color(217, 164, 65);  // #D9A441
        Color light = new Color(240, 244, 255);

        // ── Polices ──
        Font fTitle  = new Font(Font.HELVETICA, 20, Font.BOLD, navy);
        Font fSub    = new Font(Font.HELVETICA, 11, Font.NORMAL, Color.DARK_GRAY);
        Font fLabel  = new Font(Font.HELVETICA, 10, Font.BOLD, navy);
        Font fValue  = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.DARK_GRAY);
        Font fHead   = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
        Font fCell   = new Font(Font.HELVETICA,  9, Font.NORMAL, Color.DARK_GRAY);

        // ── En-tête ──
        PdfPTable header = new PdfPTable(1);
        header.setWidthPercentage(100);
        PdfPCell hCell = new PdfPCell();
        hCell.setBackgroundColor(navy);
        hCell.setPadding(16);
        hCell.setBorder(Rectangle.NO_BORDER);
        Paragraph hPara = new Paragraph("SÉNEXPERT — Attestation de participation", fTitle);
        hPara.setAlignment(Element.ALIGN_CENTER);
        hCell.addElement(hPara);
        Paragraph hSub = new Paragraph("Document officiel de suivi de formation", fSub);
        hSub.setAlignment(Element.ALIGN_CENTER);
        Font fSubW = new Font(Font.HELVETICA, 11, Font.NORMAL, new Color(200, 210, 230));
        hSub.setFont(fSubW);
        hCell.addElement(hSub);
        header.addCell(hCell);
        doc.add(header);
        doc.add(Chunk.NEWLINE);

        // ── Infos de la formation ──
        PdfPTable info = new PdfPTable(2);
        info.setWidthPercentage(100);
        info.setWidths(new float[]{1f, 2f});
        info.setSpacingBefore(10);

        addInfoRow(info, "Formation :", titre,      fLabel, fValue, light);
        addInfoRow(info, "N° Demande :", "#" + id,  fLabel, fValue, Color.WHITE);
        addInfoRow(info, "Date :", dateDoc,          fLabel, fValue, light);
        addInfoRow(info, "Statut :", demande.getStatut() != null ? demande.getStatut().name() : "—",
                   fLabel, fValue, Color.WHITE);
        doc.add(info);
        doc.add(Chunk.NEWLINE);

        // ── Titre liste participants ──
        Paragraph secTitle = new Paragraph("Liste des participants", fLabel);
        secTitle.setSpacingBefore(8);
        secTitle.setSpacingAfter(6);
        doc.add(secTitle);

        if (participants.isEmpty()) {
            Paragraph vide = new Paragraph("Aucun participant enregistré.", fValue);
            vide.setSpacingAfter(10);
            doc.add(vide);
        } else {
            // ── Tableau participants ──
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{0.5f, 1.5f, 1.5f, 2f, 1f});

            String[] cols = {"#", "Nom", "Prénom", "Email", "Statut"};
            for (String col : cols) {
                PdfPCell c = new PdfPCell(new Phrase(col, fHead));
                c.setBackgroundColor(navy);
                c.setPadding(7);
                c.setBorderColor(navy);
                table.addCell(c);
            }

            int i = 1;
            for (ParticipantInstitution p : participants) {
                Color bg = (i % 2 == 0) ? light : Color.WHITE;
                addTableCell(table, String.valueOf(i++), fCell, bg);
                addTableCell(table, nvl(p.getNom()),      fCell, bg);
                addTableCell(table, nvl(p.getPrenom()),   fCell, bg);
                addTableCell(table, nvl(p.getEmail()),    fCell, bg);
                Color sc = statutColor(p.getStatut());
                Font fStat = new Font(Font.HELVETICA, 9, Font.BOLD, sc);
                addTableCell(table, nvl(p.getStatut()),   fStat, bg);
            }
            doc.add(table);
        }

        // ── Pied de page ──
        doc.add(Chunk.NEWLINE);
        Paragraph footer = new Paragraph(
            "Ce document a été généré automatiquement par le système SÉNEXPERT le " + dateDoc + ".",
            new Font(Font.HELVETICA, 8, Font.ITALIC, Color.GRAY));
        footer.setAlignment(Element.ALIGN_CENTER);
        doc.add(footer);

        doc.close();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void addInfoRow(PdfPTable t, String label, String value,
                            Font fL, Font fV, Color bg) {
        PdfPCell lc = new PdfPCell(new Phrase(label, fL));
        lc.setBackgroundColor(bg); lc.setPadding(6); lc.setBorder(Rectangle.BOX);
        t.addCell(lc);
        PdfPCell vc = new PdfPCell(new Phrase(value, fV));
        vc.setBackgroundColor(bg); vc.setPadding(6); vc.setBorder(Rectangle.BOX);
        t.addCell(vc);
    }

    private void addTableCell(PdfPTable t, String text, Font f, Color bg) {
        PdfPCell c = new PdfPCell(new Phrase(text, f));
        c.setBackgroundColor(bg);
        c.setPadding(6);
        t.addCell(c);
    }

    private Color statutColor(String statut) {
        if (statut == null) return Color.GRAY;
        return switch (statut) {
            case "TERMINE"  -> new Color(22, 101, 52);   // vert
            case "EN_COURS" -> new Color(120, 53, 15);   // orange
            case "ABSENT"   -> new Color(153, 27, 27);   // rouge
            default         -> new Color(30, 58, 138);   // bleu (INSCRIT)
        };
    }

    private String nvl(String s) { return s != null ? s : "—"; }
}
