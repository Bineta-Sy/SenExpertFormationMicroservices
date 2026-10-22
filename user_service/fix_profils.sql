-- =============================================================
-- FIX : Fusionner "Candidat" et "Particulier"
-- Renomme le profil "Candidat" en "Particulier"
-- et supprime le profil "Particulier" vide (orphelin)
-- =============================================================

-- Étape 1 : Supprimer le profil "Particulier" vide (aucun user ne l'utilise)
-- D'abord supprimer ses permissions liées (table pivot)
DELETE FROM profil_permissions
WHERE profil_id = (SELECT id FROM profils WHERE nom = 'Particulier');

-- Supprimer le profil vide
DELETE FROM profils WHERE nom = 'Particulier';

-- Étape 2 : Renommer "Candidat" → "Particulier"
UPDATE profils SET nom = 'Particulier' WHERE nom = 'Candidat';
