-- ================================================================
-- FIX COMPLET : Alignement profils ↔ rôles
-- DB: db_users  |  Conteneur: postgres-users
-- ================================================================

-- 1. État avant correction (diagnostic)
SELECT p.nom AS profil, u.role, COUNT(u.id) AS nb_users
FROM profils p
LEFT JOIN utilisateurs u ON u.profil_id = p.id
GROUP BY p.nom, u.role
ORDER BY p.nom, u.role;

-- 2. Tous les PARTICULIER → profil "Particulier"
UPDATE utilisateurs
SET profil_id = (SELECT id FROM profils WHERE nom = 'Particulier')
WHERE role = 'PARTICULIER';

-- 3. Tous les FORMATEUR → profil "Formateur"
UPDATE utilisateurs
SET profil_id = (SELECT id FROM profils WHERE nom = 'Formateur')
WHERE role = 'FORMATEUR';

-- 4. Tous les ADMIN → profil "Administrateur"
UPDATE utilisateurs
SET profil_id = (SELECT id FROM profils WHERE nom = 'Administrateur')
WHERE role = 'ADMIN';

-- 5. AGENT sans institution → profil "Agent SEF"
UPDATE utilisateurs
SET profil_id = (SELECT id FROM profils WHERE nom = 'Agent SEF')
WHERE role = 'AGENT'
  AND institution_id IS NULL;

-- 6. AGENT avec institution → profil "Agent Institution"
UPDATE utilisateurs
SET profil_id = (SELECT id FROM profils WHERE nom = 'Agent Institution')
WHERE role = 'AGENT'
  AND institution_id IS NOT NULL;

-- 7. État après correction (vérification)
SELECT p.nom AS profil, u.role, COUNT(u.id) AS nb_users
FROM profils p
LEFT JOIN utilisateurs u ON u.profil_id = p.id
GROUP BY p.nom, u.role
ORDER BY p.nom, u.role;
