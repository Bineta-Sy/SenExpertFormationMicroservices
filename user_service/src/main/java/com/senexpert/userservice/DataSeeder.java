package com.senexpert.userservice;

import com.senexpert.userservice.entity.*;
import com.senexpert.userservice.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Set;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedAll(
            UtilisateurRepository utilisateurRepo,
            InstitutionRepository institutionRepo,
            BrancheRepository brancheRepo,
            PermissionRepository permissionRepo,
            ProfilRepository profilRepo,
            AuditLogRepository auditRepo,
            PasswordEncoder encoder) {

        return args -> {

            // ── 1. BRANCHES
            Branche bRH   = getOrCreateBranche(brancheRepo, "Ressources Humaines", "RH");
            Branche bIT   = getOrCreateBranche(brancheRepo, "Informatique et Systemes", "IT");
            Branche bFin  = getOrCreateBranche(brancheRepo, "Finance et Comptabilite", "FIN");
            Branche bDir  = getOrCreateBranche(brancheRepo, "Direction Generale", "DIR");
            Branche bForm = getOrCreateBranche(brancheRepo, "Formation et Competences", "FORM");

            // ── 2. PERMISSIONS ────────────────────────────────────────────────────

            // ── RH (branche 1000) ──
            Permission pGererUsers     = getOrCreatePermission(permissionRepo, "Gerer les utilisateurs",    "1001", NiveauPermission.ADMIN,   bRH);
            Permission pVoirUsers      = getOrCreatePermission(permissionRepo, "Voir les utilisateurs",     "1002", NiveauPermission.LECTURE,  bRH);
            Permission pGererInst      = getOrCreatePermission(permissionRepo, "Gerer les institutions",    "1003", NiveauPermission.ECRITURE, bRH);
            Permission pVoirInst       = getOrCreatePermission(permissionRepo, "Voir les institutions",     "1004", NiveauPermission.LECTURE,  bRH);
            Permission pGererFormateurs= getOrCreatePermission(permissionRepo, "Gerer les formateurs",      "1005", NiveauPermission.ECRITURE, bRH);
            Permission pSuspendreCompte= getOrCreatePermission(permissionRepo, "Suspendre un compte",       "1006", NiveauPermission.ADMIN,   bRH);
            Permission pImporterUsers  = getOrCreatePermission(permissionRepo, "Importer des utilisateurs", "1007", NiveauPermission.ECRITURE, bRH);
            Permission pVoirHistorique = getOrCreatePermission(permissionRepo, "Voir historique utilisateur","1008",NiveauPermission.LECTURE,  bRH);

            // ── IT (branche 2000) ──
            Permission pAudit          = getOrCreatePermission(permissionRepo, "Consulter audit",           "2001", NiveauPermission.LECTURE,  bIT);
            Permission pGererSessions  = getOrCreatePermission(permissionRepo, "Gerer les sessions",        "2002", NiveauPermission.ADMIN,   bIT);
            Permission pConfigurer2FA  = getOrCreatePermission(permissionRepo, "Configurer 2FA",            "2003", NiveauPermission.ADMIN,   bIT);
            Permission pResetMdp       = getOrCreatePermission(permissionRepo, "Reinitialiser mots de passe","2004",NiveauPermission.ECRITURE, bIT);
            Permission pGererDroits    = getOrCreatePermission(permissionRepo, "Gerer les droits d acces",  "2005", NiveauPermission.ADMIN,   bIT);

            // ── Finance (branche 3000) ──
            Permission pGererBudget    = getOrCreatePermission(permissionRepo, "Gerer le budget",           "3001", NiveauPermission.ECRITURE, bFin);
            Permission pVoirPaiements  = getOrCreatePermission(permissionRepo, "Voir les paiements",        "3002", NiveauPermission.LECTURE,  bFin);
            Permission pValiderPaiem   = getOrCreatePermission(permissionRepo, "Valider les paiements",     "3003", NiveauPermission.ECRITURE, bFin);
            Permission pExportFinancier= getOrCreatePermission(permissionRepo, "Exporter rapports financiers","3004",NiveauPermission.LECTURE, bFin);

            // ── Direction (branche 4000) ──
            Permission pExporter       = getOrCreatePermission(permissionRepo, "Exporter les rapports",     "4001", NiveauPermission.LECTURE,  bDir);
            Permission pAnalytics      = getOrCreatePermission(permissionRepo, "Voir les statistiques",     "4002", NiveauPermission.LECTURE,  bDir);
            Permission pGererAnnonces  = getOrCreatePermission(permissionRepo, "Gerer les annonces",        "4003", NiveauPermission.ECRITURE, bDir);
            Permission pAccesDashboard = getOrCreatePermission(permissionRepo, "Acceder au tableau de bord","4004", NiveauPermission.LECTURE,  bDir);

            // ── Formation (branche 5000) ──
            Permission pGererOffres    = getOrCreatePermission(permissionRepo, "Gerer les offres",          "5001", NiveauPermission.ECRITURE, bForm);
            Permission pVoirOffres     = getOrCreatePermission(permissionRepo, "Voir les offres",           "5002", NiveauPermission.LECTURE,  bForm);
            Permission pValider        = getOrCreatePermission(permissionRepo, "Valider les demandes",      "5003", NiveauPermission.ECRITURE, bForm);
            Permission pGererFormations= getOrCreatePermission(permissionRepo, "Gerer les formations",      "5004", NiveauPermission.ECRITURE, bForm);
            Permission pVoirFormations = getOrCreatePermission(permissionRepo, "Voir les formations",       "5005", NiveauPermission.LECTURE,  bForm);
            Permission pGererCours     = getOrCreatePermission(permissionRepo, "Gerer les cours",           "5006", NiveauPermission.ECRITURE, bForm);
            Permission pVoirCours      = getOrCreatePermission(permissionRepo, "Voir les cours",            "5007", NiveauPermission.LECTURE,  bForm);
            Permission pGererCandid    = getOrCreatePermission(permissionRepo, "Gerer les candidatures",    "5008", NiveauPermission.ECRITURE, bForm);
            Permission pVoirCandid     = getOrCreatePermission(permissionRepo, "Voir les candidatures",     "5009", NiveauPermission.LECTURE,  bForm);
            Permission pGererDomaines  = getOrCreatePermission(permissionRepo, "Gerer les domaines",        "5010", NiveauPermission.ECRITURE, bForm);
            Permission pCorrigerTravaux= getOrCreatePermission(permissionRepo, "Corriger les travaux",      "5011", NiveauPermission.ECRITURE, bForm);
            Permission pVoirApprenants = getOrCreatePermission(permissionRepo, "Voir les apprenants",       "5012", NiveauPermission.LECTURE,  bForm);
            Permission pGererDemandesL = getOrCreatePermission(permissionRepo, "Gerer les demandes libres", "5013", NiveauPermission.ECRITURE, bForm);
            Permission pCandidaterOffre= getOrCreatePermission(permissionRepo, "Candidater a une offre",   "5014", NiveauPermission.ECRITURE, bForm);
            Permission pSoumettreDemande=getOrCreatePermission(permissionRepo, "Soumettre une demande",    "5015", NiveauPermission.ECRITURE, bForm);

            // ── 3. PROFILS ────────────────────────────────────────────────────────
            Profil pAdmin = getOrCreateProfil(profilRepo, "Administrateur", Set.of(
                    pGererUsers, pVoirUsers, pGererInst, pVoirInst, pGererFormateurs,
                    pSuspendreCompte, pImporterUsers, pVoirHistorique,
                    pAudit, pGererSessions, pConfigurer2FA, pResetMdp, pGererDroits,
                    pGererBudget, pVoirPaiements, pValiderPaiem, pExportFinancier,
                    pExporter, pAnalytics, pGererAnnonces, pAccesDashboard,
                    pGererOffres, pVoirOffres, pValider, pGererFormations, pVoirFormations,
                    pGererCours, pVoirCours, pGererCandid, pVoirCandid,
                    pGererDomaines, pGererDemandesL));

            Profil pAgentSEF = getOrCreateProfil(profilRepo, "Agent SEF", Set.of(
                    pVoirUsers, pVoirInst, pVoirHistorique,
                    pAudit,
                    pExporter, pAnalytics, pAccesDashboard, pGererAnnonces,
                    pGererOffres, pVoirOffres, pValider, pGererFormations, pVoirFormations,
                    pGererCandid, pVoirCandid, pGererDomaines, pGererDemandesL));

            Profil pAgentInst = getOrCreateProfil(profilRepo, "Agent Institution", Set.of(
                    pVoirInst,
                    pVoirOffres, pValider, pVoirFormations, pVoirCandid, pVoirCours));

            Profil pFormateur = getOrCreateProfil(profilRepo, "Formateur", Set.of(
                    pVoirOffres, pVoirFormations,
                    pGererCours, pVoirCours, pCorrigerTravaux, pVoirApprenants));

            Profil pCandidat = getOrCreateProfil(profilRepo, "Particulier", Set.of(
                    pVoirOffres, pVoirFormations, pVoirCours, pCandidaterOffre, pSoumettreDemande));

            // ── 4. INSTITUTIONS
            Institution dakarTech  = getOrCreateInstitution(institutionRepo, "DakarTech SA",         TypeInstitution.ENTREPRISE, "Route de Ngor, Dakar",           Statut.ACTIF);
            Institution ongSenVert = getOrCreateInstitution(institutionRepo, "ONG SenVert",          TypeInstitution.ONG,        "Cite Keur Gorgui, Dakar",        Statut.ACTIF);
            Institution minEduc    = getOrCreateInstitution(institutionRepo, "Ministere Education",  TypeInstitution.MINISTERE,  "Rue Docteur Calmette, Dakar",    Statut.ACTIF);
            Institution sonatel    = getOrCreateInstitution(institutionRepo, "SONATEL",              TypeInstitution.ENTREPRISE, "Route du Front de Terre, Dakar", Statut.ACTIF);
            Institution bhs        = getOrCreateInstitution(institutionRepo, "BHS Banque",           TypeInstitution.ENTREPRISE, "Rue Amadou Assane Ndoye, Dakar", Statut.ACTIF);
            Institution minSante   = getOrCreateInstitution(institutionRepo, "Ministere Sante",      TypeInstitution.MINISTERE,  "Fann Residence, Dakar",          Statut.ACTIF);
                                     getOrCreateInstitution(institutionRepo, "Orange Senegal",       TypeInstitution.ENTREPRISE, "Rue du Docteur Theze, Dakar",    Statut.EN_ATTENTE);
                                     getOrCreateInstitution(institutionRepo, "CDE Formation",        TypeInstitution.DIRECTION,  "Point E, Dakar",                 Statut.EN_ATTENTE);

            // ── 5. UTILISATEURS
            // Admin
            creerUtilisateur(utilisateurRepo, encoder, "Khady",   "BAME",    "bame@gmail.com",           "Passer1234!", Role.ADMIN,       Statut.ACTIF,      "+221 33 800 00 00", null,       pAdmin);
            // Agents SEF
//            creerUtilisateur(utilisateurRepo, encoder, "Niang",   "Moussa",   "moussa.niang@sef.sn",          "Agent1234!", Role.AGENT,       Statut.ACTIF,      "+221 77 100 10 01", null,       pAgentSEF);
//            creerUtilisateur(utilisateurRepo, encoder, "Diallo",  "Rokhaya",  "rokhaya.diallo@sef.sn",        "Agent1234!", Role.AGENT,       Statut.ACTIF,      "+221 77 100 10 02", null,       pAgentSEF);
//            creerUtilisateur(utilisateurRepo, encoder, "Sow",     "Babacar",  "babacar.sow@sef.sn",           "Agent1234!", Role.AGENT,       Statut.ACTIF,      "+221 77 100 10 03", null,       pAgentSEF);
//            creerUtilisateur(utilisateurRepo, encoder, "Fall",    "Ndeye",    "ndeye.fall@sef.sn",            "Agent1234!", Role.AGENT,       Statut.ACTIF,      "+221 77 100 10 04", null,       pAgentSEF);
//            // Agents institution
//            creerUtilisateur(utilisateurRepo, encoder, "Diop",    "Lamine",   "lamine.diop@dakartech.sn",     "Agent1234!", Role.AGENT,       Statut.ACTIF,      "+221 76 200 20 01", dakarTech,  pAgentInst);
//            creerUtilisateur(utilisateurRepo, encoder, "Ba",      "Aissatou", "a.ba@ongsenvert.sn",           "Agent1234!", Role.AGENT,       Statut.ACTIF,      "+221 76 200 20 02", ongSenVert, pAgentInst);
//            creerUtilisateur(utilisateurRepo, encoder, "Traore",  "Cheikh",   "c.traore@education.sn",        "Agent1234!", Role.AGENT,       Statut.ACTIF,      "+221 76 200 20 03", minEduc,    pAgentInst);
//            creerUtilisateur(utilisateurRepo, encoder, "Sarr",    "Ibou",     "ibou.sarr@sonatel.sn",         "Agent1234!", Role.AGENT,       Statut.ACTIF,      "+221 76 200 20 04", sonatel,    pAgentInst);
            // Particuliers actifs
//            creerUtilisateur(utilisateurRepo, encoder, "Diallo",  "Mamadou",  "mamadou.diallo@gmail.com",     "Pass1234!",  Role.PARTICULIER, Statut.ACTIF,      "+221 77 300 30 01", null,       pCandidat);
//            creerUtilisateur(utilisateurRepo, encoder, "Seck",    "Fatou",    "fatou.seck@gmail.com",         "Pass1234!",  Role.PARTICULIER, Statut.ACTIF,      "+221 77 300 30 02", null,       pCandidat);
//            creerUtilisateur(utilisateurRepo, encoder, "Gueye",   "Lamine",   "lamine.gueye@yahoo.fr",        "Pass1234!",  Role.PARTICULIER, Statut.ACTIF,      "+221 77 300 30 03", null,       pCandidat);
//            creerUtilisateur(utilisateurRepo, encoder, "Traore",  "Awa",      "awa.traore@gmail.com",         "Pass1234!",  Role.PARTICULIER, Statut.ACTIF,      "+221 77 300 30 04", null,       pCandidat);
//            creerUtilisateur(utilisateurRepo, encoder, "Diop",    "Marieme",  "marieme.diop@gmail.com",       "Pass1234!",  Role.PARTICULIER, Statut.ACTIF,      "+221 77 300 30 05", null,       pCandidat);
//            creerUtilisateur(utilisateurRepo, encoder, "Ndiaye",  "Ibrahima", "ibrahima.ndiaye@gmail.com",    "Pass1234!",  Role.PARTICULIER, Statut.ACTIF,      "+221 77 300 30 06", null,       pCandidat);
//            creerUtilisateur(utilisateurRepo, encoder, "Badji",   "Mamadou",  "m.badji@bhs.sn",               "Pass1234!",  Role.PARTICULIER, Statut.ACTIF,      "+221 77 300 30 07", bhs,        pCandidat);
//            creerUtilisateur(utilisateurRepo, encoder, "Ndiaye",  "Seynabou", "s.ndiaye@sante.sn",            "Pass1234!",  Role.PARTICULIER, Statut.ACTIF,      "+221 77 300 30 08", minSante,   pCandidat);
//            // Particuliers en attente
//            creerUtilisateur(utilisateurRepo, encoder, "Fall",    "Ousmane",  "ousmane.fall@gmail.com",       "Pass1234!",  Role.PARTICULIER, Statut.EN_ATTENTE, "+221 78 400 40 01", null,       pCandidat);
//            creerUtilisateur(utilisateurRepo, encoder, "Ndiaye",  "Aida",     "aida.ndiaye@gmail.com",        "Pass1234!",  Role.PARTICULIER, Statut.EN_ATTENTE, "+221 78 400 40 02", null,       pCandidat);
//            creerUtilisateur(utilisateurRepo, encoder, "Mbaye",   "Cheikh",   "cheikh.mbaye@gmail.com",       "Pass1234!",  Role.PARTICULIER, Statut.EN_ATTENTE, "+221 78 400 40 03", null,       pCandidat);
//            creerUtilisateur(utilisateurRepo, encoder, "Diouf",   "Sophie",   "sophie.diouf@gmail.com",       "Pass1234!",  Role.PARTICULIER, Statut.EN_ATTENTE, "+221 78 400 40 04", null,       pCandidat);
//            // Suspendu
//            creerUtilisateur(utilisateurRepo, encoder, "Test",    "Suspendu", "test.suspendu@gmail.com",      "Pass1234!",  Role.PARTICULIER, Statut.INACTIF,    "+221 70 000 00 00", null,       pCandidat);
            // Formateur
            creerUtilisateur(utilisateurRepo, encoder, "SY",  "Bineta",   "binetasy@gmail.com",           "Passer1234!",  Role.FORMATEUR,   Statut.ACTIF,      "+221770000000",     null,       pFormateur);

            // ── 6. AUDIT LOGS
//            if (auditRepo.count() == 0) {
//                logAudit(auditRepo, "CREATION",   "UTILISATEUR", "Admin cree au demarrage",           "system",              "INFO", LocalDateTime.now().minusDays(30));
//                logAudit(auditRepo, "VALIDATION", "UTILISATEUR", "Mamadou Diallo valide",             "admin@senexpert.sn",  "INFO", LocalDateTime.now().minusDays(25));
//                logAudit(auditRepo, "VALIDATION", "INSTITUTION", "DakarTech SA validee",              "admin@senexpert.sn",  "INFO", LocalDateTime.now().minusDays(20));
//                logAudit(auditRepo, "CREATION",   "INSTITUTION", "Orange Senegal en attente",         "admin@senexpert.sn",  "INFO", LocalDateTime.now().minusDays(15));
//                logAudit(auditRepo, "SUSPENSION", "UTILISATEUR", "Compte test.suspendu suspendu",     "admin@senexpert.sn",  "WARN", LocalDateTime.now().minusDays(10));
//                logAudit(auditRepo, "VALIDATION", "UTILISATEUR", "Fatou Seck validee",                "moussa.niang@sef.sn", "INFO", LocalDateTime.now().minusDays(8));
//                logAudit(auditRepo, "VALIDATION", "UTILISATEUR", "Lamine Gueye valide",               "rokhaya.diallo@sef.sn","INFO",LocalDateTime.now().minusDays(5));
//                logAudit(auditRepo, "CONNEXION",  "SESSION",     "Connexion admin reussie",           "admin@senexpert.sn",  "INFO", LocalDateTime.now().minusDays(2));
//                logAudit(auditRepo, "RESET_MDP",  "UTILISATEUR", "Reinitialisation MDP Ibou Sarr",    "admin@senexpert.sn",  "WARN", LocalDateTime.now().minusDays(1));
//                logAudit(auditRepo, "VALIDATION", "UTILISATEUR", "Awa Traore validee",                "ndeye.fall@sef.sn",   "INFO", LocalDateTime.now().minusHours(3));
//            }

            System.out.println(">>> [DataSeeder] db_users initialise avec succes.");
//            System.out.println("    Admin     : admin@senexpert.sn / Admin1234!");
//            System.out.println("    Agent SEF : moussa.niang@sef.sn / Agent1234!");
//            System.out.println("    Candidat  : mamadou.diallo@gmail.com / Pass1234!");
        };
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Branche getOrCreateBranche(BrancheRepository repo, String nom, String code) {
        return repo.findAll().stream().filter(b -> b.getNom().equals(nom)).findFirst().orElseGet(() -> {
            Branche b = new Branche(); b.setNom(nom); b.setCode(code); return repo.save(b);
        });
    }

    private Permission getOrCreatePermission(PermissionRepository repo,
            String libelle, String code, NiveauPermission niveau, Branche branche) {
        return repo.findAll().stream().filter(p -> p.getLibelle().equals(libelle)).findFirst().orElseGet(() -> {
            Permission p = new Permission(); p.setLibelle(libelle); p.setCode(code);
            p.setNiveau(niveau); p.setBranche(branche); return repo.save(p);
        });
    }

    private Profil getOrCreateProfil(ProfilRepository repo, String nom, Set<Permission> permissions) {
        Profil p = repo.findAll().stream().filter(x -> x.getNom().equals(nom)).findFirst().orElseGet(() -> {
            Profil np = new Profil(); np.setNom(nom); return np;
        });
        p.setPermissions(permissions); // toujours mettre à jour les permissions
        return repo.save(p);
    }

    private Institution getOrCreateInstitution(InstitutionRepository repo,
            String nom, TypeInstitution type, String adresse, Statut statut) {
        return repo.findAll().stream().filter(i -> i.getNom().equals(nom)).findFirst().orElseGet(() -> {
            Institution i = new Institution(); i.setNom(nom); i.setType(type);
            i.setAdresse(adresse); i.setStatut(statut); return repo.save(i);
        });
    }

    private void creerUtilisateur(UtilisateurRepository repo, PasswordEncoder encoder,
            String nom, String prenom, String email, String mdp,
            Role role, Statut statut, String tel, Institution institution, Profil profil) {
        if (!repo.existsByEmail(email)) {
            Utilisateur u = new Utilisateur();
            u.setNom(nom); u.setPrenom(prenom); u.setEmail(email);
            u.setMotDePasse(encoder.encode(mdp)); u.setRole(role);
            u.setStatut(statut); u.setTelephone(tel);
            u.setInstitution(institution); u.setProfil(profil);
            repo.save(u);
        }
    }

    private void logAudit(AuditLogRepository repo, String action, String entite,
            String detail, String adminEmail, String niveau, LocalDateTime date) {
        AuditLog log = new AuditLog();
        log.setAction(action); log.setEntite(entite); log.setDetail(detail);
        log.setAdminEmail(adminEmail); log.setNiveau(niveau); log.setDateAction(date);
        repo.save(log);
    }
}
