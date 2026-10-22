from reportlab.lib.pagesizes import A4
from reportlab.lib import colors
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle, PageBreak
from reportlab.lib.units import cm
from reportlab.platypus.flowables import Flowable

W, H = A4


class UseCaseDiagram(Flowable):
    def __init__(self, width=500, height=400):
        super().__init__()
        self.width = width
        self.height = height

    def draw(self):
        c = self.canv
        w, h = self.width, self.height

        c.setFillColor(colors.HexColor('#F8FAFF'))
        c.setStrokeColor(colors.HexColor('#C5D0E8'))
        c.roundRect(0, 0, w, h, 10, fill=1, stroke=1)

        c.setFont("Helvetica-Bold", 13)
        c.setFillColor(colors.HexColor('#1A237E'))
        c.drawCentredString(w/2, h - 25, "Diagramme de Cas d'Utilisation")

        # System boundary
        c.setStrokeColor(colors.HexColor('#3949AB'))
        c.setFillColor(colors.white)
        c.setLineWidth(1.5)
        c.roundRect(120, 40, w - 240, h - 80, 8, fill=1, stroke=1)
        c.setFont("Helvetica-Bold", 9)
        c.setFillColor(colors.HexColor('#3949AB'))
        c.drawCentredString(w/2, h - 52, "<<systeme>> SenExpertFormation")

        def draw_actor(x, y, label, color):
            c.setStrokeColor(color)
            c.setFillColor(color)
            c.circle(x, y + 42, 10, fill=1, stroke=0)
            c.setLineWidth(2)
            c.line(x, y + 32, x, y + 10)
            c.line(x - 14, y + 24, x + 14, y + 24)
            c.line(x, y + 10, x - 12, y - 8)
            c.line(x, y + 10, x + 12, y - 8)
            c.setFont("Helvetica-Bold", 8)
            c.setFillColor(color)
            c.drawCentredString(x, y - 14, label)

        def draw_usecase(x, y, text):
            ew, eh = 110, 26
            c.setFillColor(colors.HexColor('#E8EAF6'))
            c.setStrokeColor(colors.HexColor('#3949AB'))
            c.setLineWidth(1)
            c.ellipse(x - ew/2, y - eh/2, x + ew/2, y + eh/2, fill=1, stroke=1)
            c.setFont("Helvetica", 7.5)
            c.setFillColor(colors.HexColor('#1A237E'))
            c.drawCentredString(x, y - 3, text)

        def arrow(x1, y1, x2, y2):
            c.setStrokeColor(colors.HexColor('#78909C'))
            c.setLineWidth(0.8)
            c.line(x1, y1, x2, y2)

        draw_actor(55, 230, "Agent", colors.HexColor('#1565C0'))
        draw_actor(55, 100, "Particulier", colors.HexColor('#2E7D32'))
        draw_actor(w - 55, 180, "Administrateur", colors.HexColor('#B71C1C'))

        ucs_agent = [
            (w/2 - 10, 325, "S'inscrire"),
            (w/2 - 10, 285, "Se connecter"),
            (w/2 - 10, 245, "Creer Institution"),
            (w/2 - 10, 205, "Consulter Offres"),
            (w/2 - 10, 165, "Soumettre Demande"),
            (w/2 - 10, 125, "Voir Mes Demandes"),
        ]
        ucs_admin = [
            (w/2 + 65, 310, "Valider Comptes"),
            (w/2 + 65, 270, "Valider Institutions"),
            (w/2 + 65, 230, "Gerer Offres"),
            (w/2 + 65, 190, "Gerer Domaines"),
            (w/2 + 65, 150, "Voir Demandes"),
        ]

        for x, y, txt in ucs_agent + ucs_admin:
            draw_usecase(x, y, txt)

        for x, y, _ in ucs_agent:
            arrow(70, 262, x - 55, y)

        for x, y, _ in ucs_agent[:2]:
            arrow(70, 132, x - 55, y)
        arrow(70, 132, ucs_agent[3][0] - 55, ucs_agent[3][1])
        arrow(70, 132, ucs_agent[4][0] - 55, ucs_agent[4][1])

        for x, y, _ in ucs_admin:
            arrow(w - 70, 210, x + 55, y)

        c.setFont("Helvetica", 7)
        c.setFillColor(colors.HexColor('#546E7A'))
        c.drawString(10, 12, "Les fleches representent les associations acteur -> cas d'utilisation")


class ClassDiagram(Flowable):
    def __init__(self, width=500, height=460):
        super().__init__()
        self.width = width
        self.height = height

    def draw(self):
        c = self.canv
        w, h = self.width, self.height

        c.setFillColor(colors.HexColor('#F8FAFF'))
        c.setStrokeColor(colors.HexColor('#C5D0E8'))
        c.roundRect(0, 0, w, h, 10, fill=1, stroke=1)

        c.setFont("Helvetica-Bold", 13)
        c.setFillColor(colors.HexColor('#1A237E'))
        c.drawCentredString(w/2, h - 25, "Diagramme de Classes")

        def draw_class(x, y, name, attrs, stereotype=None, color=colors.HexColor('#E8EAF6')):
            bw = 148
            lh = 14
            hh = 28 if not stereotype else 40
            bh = len(attrs) * lh + 8
            total = hh + bh

            c.setFillColor(colors.HexColor('#B0BEC5'))
            c.setStrokeColor(colors.transparent)
            c.roundRect(x + 3, y - total - 3, bw, total, 4, fill=1, stroke=0)

            c.setFillColor(color)
            c.setStrokeColor(colors.HexColor('#3949AB'))
            c.setLineWidth(1.2)
            c.roundRect(x, y - total, bw, total, 4, fill=1, stroke=1)

            c.setFillColor(colors.HexColor('#3949AB'))
            c.roundRect(x, y - hh, bw, hh, 4, fill=1, stroke=0)
            c.rect(x, y - hh, bw, hh/2, fill=1, stroke=0)

            if stereotype:
                c.setFont("Helvetica-Oblique", 7)
                c.setFillColor(colors.HexColor('#B3C5FF'))
                c.drawCentredString(x + bw/2, y - 14, '<<' + stereotype + '>>')
                c.setFont("Helvetica-Bold", 9)
                c.setFillColor(colors.white)
                c.drawCentredString(x + bw/2, y - 29, name)
            else:
                c.setFont("Helvetica-Bold", 9)
                c.setFillColor(colors.white)
                c.drawCentredString(x + bw/2, y - 18, name)

            c.setStrokeColor(colors.HexColor('#3949AB'))
            c.line(x, y - hh, x + bw, y - hh)

            c.setFont("Helvetica", 7.5)
            for i, attr in enumerate(attrs):
                c.setFillColor(colors.HexColor('#1A237E') if attr.startswith('+') else colors.HexColor('#37474F'))
                c.drawString(x + 8, y - hh - (i + 1) * lh + 3, attr)

        def rel(x1, y1, x2, y2, label=""):
            c.setStrokeColor(colors.HexColor('#546E7A'))
            c.setLineWidth(1)
            c.line(x1, y1, x2, y2)
            if label:
                c.setFont("Helvetica-Oblique", 7)
                c.setFillColor(colors.HexColor('#546E7A'))
                c.drawCentredString((x1+x2)/2, (y1+y2)/2 + 4, label)

        draw_class(15, h-45, "Utilisateur", [
            "- id: Long", "- nom: String", "- prenom: String",
            "- email: String", "- motDePasse: String",
            "+ role: Role", "+ statut: Statut",
        ], color=colors.HexColor('#E3F2FD'))

        draw_class(195, h-45, "Institution", [
            "- id: Long", "- nom: String",
            "+ type: TypeInstitution",
            "- adresse: String", "+ statut: Statut",
        ], color=colors.HexColor('#E8F5E9'))

        draw_class(15, h-230, "Role", [
            "ADMIN", "AGENT", "PARTICULIER",
        ], stereotype="enumeration", color=colors.HexColor('#FFF3E0'))

        draw_class(195, h-230, "Statut", [
            "ACTIF", "INACTIF", "EN_ATTENTE",
        ], stereotype="enumeration", color=colors.HexColor('#FFF3E0'))

        draw_class(355, h-45, "OffreFormation", [
            "- id: Long", "- titre: String",
            "- description: String", "- duree: String",
            "+ type: TypeFormation",
            "- dateDebut: LocalDate", "- dateFin: LocalDate",
        ], color=colors.HexColor('#F3E5F5'))

        draw_class(355, h-265, "Domaine", [
            "- id: Long", "- nom: String", "- description: String",
        ], color=colors.HexColor('#E0F7FA'))

        draw_class(185, h-360, "DemandeFormation", [
            "- id: Long", "- dateCreation: LocalDate",
            "+ statut: StatutDemande", "- commentaire: String",
        ], color=colors.HexColor('#FCE4EC'))

        draw_class(15, h-380, "StatutDemande", [
            "EN_ATTENTE", "VALIDEE", "REFUSEE",
        ], stereotype="enumeration", color=colors.HexColor('#FFF3E0'))

        draw_class(355, h-385, "TypeFormation", [
            "EN_LIGNE", "PRESENTIEL", "A_LA_CARTE",
        ], stereotype="enumeration", color=colors.HexColor('#FFF3E0'))

        rel(163, h-115, 195, h-115, "appartient a  N:1")
        rel(89, h-230, 89, h-200)
        rel(269, h-230, 269, h-200)
        rel(429, h-265, 429, h-222)
        rel(185, h-385, 89, h-278, "faite par")
        rel(333, h-385, 429, h-380, "concerne")
        rel(89, h-380, 89, h-354)
        rel(429, h-385, 429, h-378)


class ArchDiagram(Flowable):
    def __init__(self, width=500, height=450):
        super().__init__()
        self.width = width
        self.height = height

    def draw(self):
        c = self.canv
        w, h = self.width, self.height

        c.setFillColor(colors.HexColor('#F8FAFF'))
        c.setStrokeColor(colors.HexColor('#C5D0E8'))
        c.roundRect(0, 0, w, h, 10, fill=1, stroke=1)

        c.setFont("Helvetica-Bold", 13)
        c.setFillColor(colors.HexColor('#1A237E'))
        c.drawCentredString(w/2, h - 25, "Architecture Applicative N-Tiers")

        def layer(y, lh, label, bg, border):
            c.setFillColor(bg)
            c.setStrokeColor(border)
            c.setLineWidth(1.5)
            c.roundRect(10, y, w - 20, lh, 6, fill=1, stroke=1)
            c.setFont("Helvetica-Bold", 8)
            c.setFillColor(border)
            c.drawString(18, y + lh - 13, label)

        def box(x, y, bw, bh, label, sub, bg, border):
            c.setFillColor(bg)
            c.setStrokeColor(border)
            c.setLineWidth(1)
            c.roundRect(x, y, bw, bh, 5, fill=1, stroke=1)
            c.setFont("Helvetica-Bold", 8)
            c.setFillColor(border)
            c.drawCentredString(x + bw/2, y + bh - 14, label)
            if sub:
                c.setFont("Helvetica", 7)
                c.setFillColor(colors.HexColor('#546E7A'))
                c.drawCentredString(x + bw/2, y + 6, sub)

        layer(h - 80,  45, "Couche Presentation  (Tier 1)", colors.HexColor('#E3F2FD'), colors.HexColor('#1565C0'))
        layer(h - 170, 80, "Couche Metier / API  (Tier 2)", colors.HexColor('#E8F5E9'), colors.HexColor('#2E7D32'))
        layer(h - 265, 85, "Couche Donnees  (Tier 3)", colors.HexColor('#FFF8E1'), colors.HexColor('#F57F17'))
        layer(h - 355, 80, "Couche Infrastructure / DevOps", colors.HexColor('#FCE4EC'), colors.HexColor('#880E4F'))
        layer(h - 425, 60, "Couche Securite (transversale)", colors.HexColor('#EDE7F6'), colors.HexColor('#4A148C'))

        box(25,  h-72, 130, 32, "PrimeFaces / JSF", "WildFly :8080", colors.HexColor('#BBDEFB'), colors.HexColor('#1565C0'))
        box(170, h-72, 130, 32, "Pages XHTML", "login / offres / admin", colors.HexColor('#BBDEFB'), colors.HexColor('#1565C0'))
        box(315, h-72, 150, 32, "CDI Beans", "AuthBean, OffreBean...", colors.HexColor('#BBDEFB'), colors.HexColor('#1565C0'))

        box(25,  h-162, 130, 38, "User Service (MS-A)", "Spring Boot :8081", colors.HexColor('#C8E6C9'), colors.HexColor('#2E7D32'))
        box(170, h-162, 130, 38, "Formation Service (MS-B)", "Spring Boot :8082", colors.HexColor('#C8E6C9'), colors.HexColor('#2E7D32'))
        box(315, h-162, 150, 38, "REST API (JSON)", "Spring MVC / Security", colors.HexColor('#C8E6C9'), colors.HexColor('#2E7D32'))

        box(25,  h-257, 130, 40, "PostgreSQL db_users", "port 5433", colors.HexColor('#FFF9C4'), colors.HexColor('#F57F17'))
        box(170, h-257, 130, 40, "PostgreSQL db_formations", "port 5434", colors.HexColor('#FFF9C4'), colors.HexColor('#F57F17'))
        box(315, h-257, 150, 40, "Hibernate / JPA", "ddl-auto=update", colors.HexColor('#FFF9C4'), colors.HexColor('#F57F17'))

        box(25,  h-347, 95, 38, "Docker", "Containers", colors.HexColor('#F8BBD9'), colors.HexColor('#880E4F'))
        box(130, h-347, 110, 38, "Docker Compose", "Orchestration locale", colors.HexColor('#F8BBD9'), colors.HexColor('#880E4F'))
        box(250, h-347, 95, 38, "Kubernetes", "k8s deploy", colors.HexColor('#F8BBD9'), colors.HexColor('#880E4F'))
        box(355, h-347, 110, 38, "Dockerfile x3", "user/formation/front", colors.HexColor('#F8BBD9'), colors.HexColor('#880E4F'))

        box(25,  h-418, 110, 38, "Spring Security", "JWT Auth", colors.HexColor('#D1C4E9'), colors.HexColor('#4A148C'))
        box(145, h-418, 100, 38, "JwtFilter", "Token validation", colors.HexColor('#D1C4E9'), colors.HexColor('#4A148C'))
        box(255, h-418, 100, 38, "BCrypt", "Password hash", colors.HexColor('#D1C4E9'), colors.HexColor('#4A148C'))
        box(365, h-418, 100, 38, "RBAC", "ADMIN/AGENT/PART.", colors.HexColor('#D1C4E9'), colors.HexColor('#4A148C'))

        for x, y1, y2, lbl in [(90, h-80, h-90, "HTTP"), (235, h-80, h-90, "HTTP"),
                                (90, h-170, h-180, "JDBC"), (235, h-170, h-180, "JDBC")]:
            c.setStrokeColor(colors.HexColor('#90A4AE'))
            c.setLineWidth(1)
            c.setDash(4, 3)
            c.line(x, y1, x, y2)
            c.setDash()
            c.setFont("Helvetica", 6.5)
            c.setFillColor(colors.HexColor('#546E7A'))
            c.drawString(x + 3, (y1+y2)/2, lbl)


# ── Build PDF ─────────────────────────────────────────────────────────────────
output_path = "/sessions/compassionate-gifted-tesla/mnt/formation_service/UML_SenExpertFormation.pdf"

doc = SimpleDocTemplate(output_path, pagesize=A4,
    leftMargin=1.5*cm, rightMargin=1.5*cm,
    topMargin=1.5*cm, bottomMargin=1.5*cm)

styles = getSampleStyleSheet()
title_style = ParagraphStyle('T', parent=styles['Title'], fontSize=18,
    textColor=colors.HexColor('#1A237E'), spaceAfter=6)
sub_style = ParagraphStyle('S', parent=styles['Normal'], fontSize=10,
    textColor=colors.HexColor('#546E7A'), spaceAfter=20, alignment=1)
section_style = ParagraphStyle('H', parent=styles['Heading2'], fontSize=12,
    textColor=colors.HexColor('#1A237E'), spaceBefore=12, spaceAfter=6)

story = []

# Cover
story.append(Spacer(1, 2*cm))
story.append(Paragraph("SenExpertFormation", title_style))
story.append(Paragraph("Conception UML et Architecture Applicative", sub_style))
story.append(Spacer(1, 0.5*cm))

info = [
    ["TP de JEE — Developpement Entreprise de Haut Niveau", ""],
    ["Technologies", "SpringBoot, MicroServices, PostgreSQL, Docker, Kubernetes, PrimeFaces, WildFly, SpringSecurity"],
    ["Encadrant", "M. NDONG"],
    ["Presentation", "Vendredi 05 Juin 2026, 08h-11h"],
]
t = Table(info, colWidths=[5.5*cm, 11.5*cm])
t.setStyle(TableStyle([
    ('BACKGROUND', (0,0), (-1,0), colors.HexColor('#1A237E')),
    ('TEXTCOLOR', (0,0), (-1,0), colors.white),
    ('SPAN', (0,0), (-1,0)), ('ALIGN', (0,0), (-1,0), 'CENTER'),
    ('FONTNAME', (0,0), (-1,0), 'Helvetica-Bold'), ('FONTSIZE', (0,0), (-1,0), 11),
    ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.HexColor('#F0F4FF'), colors.white]),
    ('FONTNAME', (0,1), (0,-1), 'Helvetica-Bold'), ('FONTSIZE', (0,1), (-1,-1), 9),
    ('GRID', (0,0), (-1,-1), 0.5, colors.HexColor('#C5D0E8')),
    ('PADDING', (0,0), (-1,-1), 8), ('VALIGN', (0,0), (-1,-1), 'MIDDLE'),
]))
story.append(t)
story.append(Spacer(1, 1*cm))

toc = [["#", "Document", "Page"],
       ["1", "Diagramme de Cas d'Utilisation", "2"],
       ["2", "Diagramme de Classes", "3"],
       ["3", "Architecture Applicative N-Tiers", "4"]]
t2 = Table(toc, colWidths=[1*cm, 11*cm, 2*cm])
t2.setStyle(TableStyle([
    ('BACKGROUND', (0,0), (-1,0), colors.HexColor('#3949AB')),
    ('TEXTCOLOR', (0,0), (-1,0), colors.white),
    ('FONTNAME', (0,0), (-1,0), 'Helvetica-Bold'), ('FONTSIZE', (0,0), (-1,-1), 9),
    ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.HexColor('#E8EAF6'), colors.white]),
    ('GRID', (0,0), (-1,-1), 0.5, colors.HexColor('#C5D0E8')),
    ('ALIGN', (0,0), (-1,-1), 'CENTER'), ('ALIGN', (1,1), (1,-1), 'LEFT'),
    ('PADDING', (0,0), (-1,-1), 7),
]))
story.append(Paragraph("Table des matieres", section_style))
story.append(t2)
story.append(PageBreak())

# Page 2 — Use Case
story.append(Paragraph("1. Diagramme de Cas d'Utilisation", section_style))
story.append(Paragraph(
    "Les trois acteurs interagissent avec la plateforme SenExpertFormation : "
    "l'Agent represente une institution, le Particulier est un individu, "
    "et l'Administrateur gere la plateforme.", styles['Normal']))
story.append(Spacer(1, 0.3*cm))
story.append(UseCaseDiagram(width=500, height=390))
story.append(Spacer(1, 0.3*cm))
actors = [
    ["Acteur", "Role", "Cas d'utilisation principaux"],
    ["Agent", "Representant d'une institution", "S'inscrire, Se connecter, Creer Institution, Soumettre Demande"],
    ["Particulier", "Individu cherchant une formation", "S'inscrire, Se connecter, Consulter Offres, Soumettre Demande"],
    ["Administrateur", "Gestionnaire plateforme", "Valider Comptes, Valider Institutions, Gerer Offres, Voir Demandes"],
]
ta = Table(actors, colWidths=[3*cm, 4.5*cm, 9.5*cm])
ta.setStyle(TableStyle([
    ('BACKGROUND', (0,0), (-1,0), colors.HexColor('#3949AB')),
    ('TEXTCOLOR', (0,0), (-1,0), colors.white), ('FONTNAME', (0,0), (-1,0), 'Helvetica-Bold'),
    ('FONTSIZE', (0,0), (-1,-1), 8),
    ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.HexColor('#E8EAF6'), colors.white]),
    ('GRID', (0,0), (-1,-1), 0.5, colors.HexColor('#C5D0E8')),
    ('PADDING', (0,0), (-1,-1), 6), ('VALIGN', (0,0), (-1,-1), 'MIDDLE'),
]))
story.append(ta)
story.append(PageBreak())

# Page 3 — Classes
story.append(Paragraph("2. Diagramme de Classes", section_style))
story.append(Paragraph(
    "Entites reparties entre User Service (Utilisateur, Institution) et "
    "Formation Service (OffreFormation, Domaine, DemandeFormation), chacun avec sa propre base PostgreSQL.",
    styles['Normal']))
story.append(Spacer(1, 0.3*cm))
story.append(ClassDiagram(width=500, height=450))
story.append(Spacer(1, 0.3*cm))
ms = [
    ["Microservice", "Entites", "Base de donnees"],
    ["User Service (MS-A)", "Utilisateur, Institution, Role, Statut", "db_users (PostgreSQL :5433)"],
    ["Formation Service (MS-B)", "OffreFormation, Domaine, DemandeFormation, TypeFormation, StatutDemande", "db_formations (PostgreSQL :5434)"],
]
tm = Table(ms, colWidths=[4.5*cm, 7.5*cm, 5*cm])
tm.setStyle(TableStyle([
    ('BACKGROUND', (0,0), (-1,0), colors.HexColor('#3949AB')),
    ('TEXTCOLOR', (0,0), (-1,0), colors.white), ('FONTNAME', (0,0), (-1,0), 'Helvetica-Bold'),
    ('FONTSIZE', (0,0), (-1,-1), 8),
    ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.HexColor('#E8EAF6'), colors.white]),
    ('GRID', (0,0), (-1,-1), 0.5, colors.HexColor('#C5D0E8')),
    ('PADDING', (0,0), (-1,-1), 6), ('VALIGN', (0,0), (-1,-1), 'MIDDLE'),
]))
story.append(tm)
story.append(PageBreak())

# Page 4 — Architecture
story.append(Paragraph("3. Architecture Applicative N-Tiers", section_style))
story.append(Paragraph(
    "Schema N-tiers de la plateforme : chaque couche indique le role et l'emplacement de chaque outil. "
    "La securite JWT est transversale a toutes les couches.",
    styles['Normal']))
story.append(Spacer(1, 0.3*cm))
story.append(ArchDiagram(width=500, height=440))
story.append(Spacer(1, 0.3*cm))
tech = [
    ["Couche", "Technologie", "Role"],
    ["Presentation (Tier 1)", "PrimeFaces 13 / JSF / WildFly 32", "Interface web — port 8080"],
    ["Metier / API (Tier 2)", "Spring Boot 3 / Spring MVC", "Logique metier, API REST — 8081, 8082"],
    ["Donnees (Tier 3)", "PostgreSQL 15 / Hibernate / JPA", "Persistance — db_users, db_formations"],
    ["Infrastructure", "Docker, Docker Compose, Kubernetes", "Containerisation et orchestration"],
    ["Securite (transversale)", "Spring Security, JWT, BCrypt, RBAC", "Auth stateless, autorisation par role"],
]
tt = Table(tech, colWidths=[4.5*cm, 6.5*cm, 6*cm])
tt.setStyle(TableStyle([
    ('BACKGROUND', (0,0), (-1,0), colors.HexColor('#3949AB')),
    ('TEXTCOLOR', (0,0), (-1,0), colors.white), ('FONTNAME', (0,0), (-1,0), 'Helvetica-Bold'),
    ('FONTSIZE', (0,0), (-1,-1), 8),
    ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.HexColor('#E8EAF6'), colors.white]),
    ('GRID', (0,0), (-1,-1), 0.5, colors.HexColor('#C5D0E8')),
    ('PADDING', (0,0), (-1,-1), 6), ('VALIGN', (0,0), (-1,-1), 'MIDDLE'),
]))
story.append(tt)

doc.build(story)
print("PDF genere :", output_path)
