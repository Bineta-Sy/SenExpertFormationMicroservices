-- ============================================================
--  Seed — Documents requis par offre de formation
--  SenExpertFormation — mise à jour base existante
--
--  Usage (depuis le dossier du projet) :
--    docker exec -i postgres-formations psql -U postgres -d db_formations \
--      < seed_documents_requis.sql
-- ============================================================

-- ── 0. Etat avant mise à jour ─────────────────────────────────────────────────
SELECT id, titre, type, statut,
       CASE WHEN documents_requis IS NULL OR documents_requis = '[]'
            THEN 'VIDE' ELSE 'DEJA REMPLI' END AS etat_docs
FROM offres_formation ORDER BY id;

-- ── 1. Developpement Web Full-Stack ──────────────────────────────────────────
UPDATE offres_formation
SET documents_requis = '["CV a jour (PDF, max 3 pages)", "Diplome BAC+2 minimum en informatique ou equivalent (scan)", "Lettre de motivation (max 1 page)", "Piece d identite valide (CNI ou passeport - scan recto/verso)"]'
WHERE titre ILIKE '%Full-Stack%' OR titre ILIKE '%Full Stack%';

-- ── 2. Bases de donnees Oracle ───────────────────────────────────────────────
UPDATE offres_formation
SET documents_requis = '["CV a jour (PDF, max 3 pages)", "Diplome BAC+2 minimum en informatique, mathematiques ou equivalent (scan)", "Justificatif d experience en bases de donnees ou SQL (attestation employeur ou certificat)", "Lettre de motivation", "Piece d identite valide (CNI ou passeport - scan recto/verso)"]'
WHERE titre ILIKE '%Oracle%';

-- ── 3. Spring Boot & Microservices ───────────────────────────────────────────
UPDATE offres_formation
SET documents_requis = '["CV a jour (PDF, max 3 pages)", "Diplome BAC+2 en informatique minimum (scan)", "Justificatif d experience en developpement Java (attestation ou contrat)", "Lettre de motivation", "Piece d identite valide (CNI ou passeport - scan recto/verso)"]'
WHERE titre ILIKE '%Spring Boot%' OR titre ILIKE '%Microservices%';

-- ── 4. Cybersecurite ─────────────────────────────────────────────────────────
UPDATE offres_formation
SET documents_requis = '["CV a jour (PDF, max 3 pages)", "Diplome BAC+2 minimum en informatique ou reseaux (scan)", "Lettre de motivation precisisant votre objectif professionnel en cybersecurite", "Attestation de travail ou d etudes en cours (si applicable)", "Piece d identite valide (CNI ou passeport - scan recto/verso)", "Engagement de confidentialite (charte ethique signee - fournie par le prestataire)"]'
WHERE titre ILIKE '%Cybersecurit%';

-- ── 5. Gestion de Projet Agile / Scrum ───────────────────────────────────────
UPDATE offres_formation
SET documents_requis = '["CV a jour (PDF, max 2 pages)", "Lettre de motivation", "Attestation employeur ou justificatif de poste actuel (si en activite)", "Piece d identite valide (CNI ou passeport - scan recto/verso)"]'
WHERE titre ILIKE '%Agile%' OR titre ILIKE '%Scrum%';

-- ── 6. Finance & Comptabilite ────────────────────────────────────────────────
UPDATE offres_formation
SET documents_requis = '["CV a jour (PDF, max 3 pages)", "Diplome BAC+2 minimum en comptabilite, finance ou gestion (scan)", "Releve de notes du dernier diplome obtenu", "Lettre de motivation", "Piece d identite valide (CNI ou passeport - scan recto/verso)"]'
WHERE titre ILIKE '%Finance%' OR titre ILIKE '%Comptabilit%';

-- ── 7. Data Science & Machine Learning ───────────────────────────────────────
UPDATE offres_formation
SET documents_requis = '["CV a jour (PDF, max 3 pages)", "Diplome BAC+3 minimum en mathematiques, statistiques ou informatique (scan)", "Lettre de motivation precisisant votre projet en data science", "Portfolio ou lien vers des projets data (GitHub, Kaggle) - optionnel mais valorise", "Piece d identite valide (CNI ou passeport - scan recto/verso)"]'
WHERE titre ILIKE '%Data Science%' OR titre ILIKE '%Machine Learning%';

-- ── 8. Ressources Humaines ────────────────────────────────────────────────────
UPDATE offres_formation
SET documents_requis = '["CV a jour (PDF, max 3 pages)", "Diplome BAC+2 minimum en ressources humaines, droit ou gestion (scan)", "Lettre de motivation", "Attestation employeur si vous etes en poste RH (optionnel mais valorise)", "Piece d identite valide (CNI ou passeport - scan recto/verso)"]'
WHERE titre ILIKE '%Ressources Humaines%';

-- ── 9. Kubernetes & DevOps ────────────────────────────────────────────────────
UPDATE offres_formation
SET documents_requis = '["CV a jour (PDF, max 3 pages)", "Diplome BAC+2 minimum en informatique ou reseaux (scan)", "Justificatif d experience en administration systeme ou developpement (attestation ou contrat)", "Lettre de motivation", "Piece d identite valide (CNI ou passeport - scan recto/verso)"]'
WHERE titre ILIKE '%Kubernetes%' OR titre ILIKE '%DevOps%';

-- ── 10. Droit des Affaires ────────────────────────────────────────────────────
UPDATE offres_formation
SET documents_requis = '["CV a jour (PDF, max 3 pages)", "Diplome BAC+2 minimum en droit, economie ou gestion (scan)", "Lettre de motivation", "Piece d identite valide (CNI ou passeport - scan recto/verso)"]'
WHERE titre ILIKE '%Droit%' OR titre ILIKE '%OHADA%' OR titre ILIKE '%Juridique%';

-- ── 11. Cloud Computing (AWS / Azure) ────────────────────────────────────────
UPDATE offres_formation
SET documents_requis = '["CV a jour (PDF, max 3 pages)", "Diplome BAC+2 minimum en informatique ou reseaux (scan)", "Justificatif d experience en administration systeme ou developpement (si disponible)", "Lettre de motivation", "Piece d identite valide (CNI ou passeport - scan recto/verso)"]'
WHERE titre ILIKE '%Cloud%' OR titre ILIKE '%AWS%' OR titre ILIKE '%Azure%';

-- ── 12. Reseaux Cisco / CCNA ─────────────────────────────────────────────────
UPDATE offres_formation
SET documents_requis = '["CV a jour (PDF)", "Diplome BAC minimum ou equivalent (scan)", "Lettre de motivation", "Piece d identite valide (CNI ou passeport - scan recto/verso)"]'
WHERE titre ILIKE '%Cisco%' OR titre ILIKE '%CCNA%';

-- ── Fallback : toute offre encore sans documents ─────────────────────────────
UPDATE offres_formation
SET documents_requis = '["CV a jour (PDF, max 3 pages)", "Lettre de motivation", "Justificatif du dernier diplome obtenu (scan)", "Piece d identite valide (CNI ou passeport - scan recto/verso)"]'
WHERE documents_requis IS NULL OR documents_requis = '[]';

-- ── Verification finale ───────────────────────────────────────────────────────
SELECT
    id,
    titre,
    type,
    statut,
    jsonb_array_length(documents_requis::jsonb) AS nb_docs,
    documents_requis
FROM offres_formation
ORDER BY id;
