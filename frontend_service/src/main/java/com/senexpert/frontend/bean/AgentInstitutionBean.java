package com.senexpert.frontend.bean;

import com.senexpert.frontend.client.FormationServiceClient;
import com.senexpert.frontend.client.UserServiceClient;
import com.senexpert.frontend.model.*;
import com.senexpert.frontend.model.DemandeLibreFront;
import com.senexpert.frontend.model.ParticipantInstitutionFront;
import com.senexpert.frontend.model.MessageFront;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Named
@ViewScoped
public class AgentInstitutionBean implements Serializable {

    @Inject private UserServiceClient userClient;
    @Inject private FormationServiceClient formationClient;
    @Inject private SessionBean session;

    // Données
    private Institution institution;
    private List<Utilisateur> membres;
    private List<OffreFormation> offres;
    private List<Domaine> domaines;
    private List<DemandeFormation> mesDemandes;

    // Filtres
    private Long domaineFiltre;

    // ── Formations à la carte ─────────────────────────────
    private List<DemandeLibreFront> mesDemandesLibres;
    private String libreNomInstitution;
    private String libreDomaine;
    private String libreDescription;
    private Integer libreNbParticipants;
    private LocalDate librePeriodeDebut;
    private LocalDate librePeriodeFin;

    // ── Détail d'une demande libre (dialog) ───────────────
    private DemandeLibreFront demandeLibreDetail;
    // Formulaire ajout formation souhaitée
    private String fThematique;
    private Integer fDuree;
    private Integer fNbParticipants;
    private String fObjectifs;
    private String fNiveauRequis;

    // ── Formulaire profil ─────────────────────────────────
    private String editNom;
    private String editPrenom;
    private String editTelephone;

    // ── Formulaire mot de passe ───────────────────────────
    private String ancienMdp;
    private String nouveauMdp;
    private String confirmerMdp;

    // ── Formulaire institution ────────────────────────────
    private String instNom;
    private String instAdresse;
    private String instSecteur;
    private String instNinea;
    private String instDomainesInteret;

    // ── État UI ───────────────────────────────────────────
    private boolean profilEnEdition = false;
    private boolean mdpEnEdition    = false;
    private boolean instEnEdition   = false;

    // ── Dialog postuler ───────────────────────────────────
    private OffreFormation offreSelectionnee;
    private List<UploadedFile> documentsPostulation = new ArrayList<>();
    private List<String> nomsDocuments = new ArrayList<>();

    // ── Fichier liste des participants (institution) ───────
    private byte[]  fichierParticipantsBytes;
    private String  fichierParticipantsNom;
    private String  fichierParticipantsType;

    // ── Candidats par offre ───────────────────────────────
    private Long offreSelectionneeId;
    private String offreSelectionneetitre;
    private List<DemandeFormation> candidatsOffre = new ArrayList<>();

    // ── Suivi participants institution ────────────────────
    private DemandeFormation demandeSelectionnee;
    private List<ParticipantInstitutionFront> participants = new ArrayList<>();
    private String participantNom;
    private String participantPrenom;
    private String participantEmail;
    private String participantTelephone;

    // ── Messagerie demande ────────────────────────────────
    private List<MessageFront> messagesConversation = new ArrayList<>();
    private String messageContenu;
    // adminId fixé : l'agent envoie ses messages à l'admin (userId=1 par convention)
    // En production on récupérerait l'id admin dynamiquement
    private static final long ADMIN_DESTINATAIRE_ID = 1L;

    // ── Paiement demande ──────────────────────────────────
    private boolean demandeSelectionneePayee = false;
    private Double  paiementMontant;
    private String  paiementMethode = "VIREMENT";
    private String  paiementReference;

    // ── Participants demande libre ──────────────────────────────────────────
    private java.util.Set<Long> selectedMemberIds = new java.util.HashSet<>();

    // ── Modules temporaires (avant soumission demande à la carte) ─────────
    private java.util.List<String[]> modulesTemporaires = new java.util.ArrayList<>();
    private String tempThematique = "";
    private String tempDuree = "";

    @PostConstruct
    public void init() {
        String token = session.getToken();

        // Institution
        if (session.getInstitutionId() != null) {
            try { institution = userClient.trouverInstitutionParId(session.getInstitutionId(), token); }
            catch (Exception e) { institution = null; }
        }

        // Membres
        if (session.getInstitutionId() != null) {
            try { membres = userClient.listerMembresInstitution(session.getInstitutionId(), token); }
            catch (Exception e) { membres = new ArrayList<>(); }
        } else { membres = new ArrayList<>(); }

        // Offres & domaines
        try { offres   = formationClient.listerOffres();   } catch (Exception e) { offres   = new ArrayList<>(); }
        try { domaines = formationClient.listerDomaines(); } catch (Exception e) { domaines = new ArrayList<>(); }

        // Mes demandes
        if (session.getUtilisateurId() != null) {
            try { mesDemandes = formationClient.mesDemandes(session.getUtilisateurId(), token); }
            catch (Exception e) { mesDemandes = new ArrayList<>(); }
        } else { mesDemandes = new ArrayList<>(); }

        // Demandes à la carte — migrer les offres manquantes d'abord
        try { formationClient.migrerOffresDemandesLibres(token); } catch (Exception ignored) {}
        if (session.getUtilisateurId() != null) {
            try { mesDemandesLibres = formationClient.mesDemandesLibres(session.getUtilisateurId(), token); }
            catch (Exception e) { mesDemandesLibres = new ArrayList<>(); }
        } else { mesDemandesLibres = new ArrayList<>(); }

        // Pré-remplir formulaires
        prefillProfil();
        prefillInstitution();
        prefillLibre();
    }

    // ── Profil ────────────────────────────────────────────

    private void prefillProfil() {
        if (session.getNomComplet() != null && !session.getNomComplet().isBlank()) {
            String[] parts = session.getNomComplet().trim().split(" ", 2);
            editPrenom = parts.length > 0 ? parts[0] : "";
            editNom    = parts.length > 1 ? parts[1] : "";
        }
    }

    public void toggleProfilEdition() { profilEnEdition = !profilEnEdition; mdpEnEdition = false; }
    public void toggleMdpEdition()    { mdpEnEdition = !mdpEnEdition; profilEnEdition = false; }

    public void sauvegarderProfil() {
        if (editNom == null || editNom.isBlank() || editPrenom == null || editPrenom.isBlank()) {
            msg(false, "Nom et prénom sont obligatoires.");
            return;
        }
        boolean ok = userClient.modifierProfil(session.getUtilisateurId(),
                editNom.trim(), editPrenom.trim(), editTelephone, session.getToken());
        if (ok) {
            session.setNomComplet(editPrenom.trim() + " " + editNom.trim());
            msg(true, "Profil mis à jour avec succès.");
            profilEnEdition = false;
        } else {
            msg(false, "Échec de la mise à jour. Réessayez.");
        }
    }

    public void changerMotDePasse() {
        if (ancienMdp == null || ancienMdp.isBlank()) { msg(false, "Ancien mot de passe requis."); return; }
        if (nouveauMdp == null || nouveauMdp.length() < 6) { msg(false, "Nouveau mot de passe : 6 caractères minimum."); return; }
        if (!nouveauMdp.equals(confirmerMdp)) { msg(false, "Les mots de passe ne correspondent pas."); return; }
        boolean ok = userClient.changerMotDePasse(session.getUtilisateurId(), ancienMdp, nouveauMdp, session.getToken());
        if (ok) {
            msg(true, "Mot de passe modifié avec succès.");
            ancienMdp = null; nouveauMdp = null; confirmerMdp = null;
            mdpEnEdition = false;
        } else {
            msg(false, "Ancien mot de passe incorrect.");
        }
    }

    // ── Institution ───────────────────────────────────────

    private void prefillInstitution() {
        if (institution != null) {
            instNom            = institution.getNom();
            instAdresse        = institution.getAdresse();
            instSecteur        = institution.getSecteur();
            instNinea          = institution.getNinea();
            instDomainesInteret = institution.getDomainesInteret();
        }
    }

    public void toggleInstEdition() {
        instEnEdition = !instEnEdition;
        if (instEnEdition) prefillInstitution();
    }

    public void sauvegarderInstitution() {
        if (instNom == null || instNom.isBlank()) { msg(false, "Nom de l'institution obligatoire."); return; }
        if (institution == null) { msg(false, "Aucune institution trouvée."); return; }
        boolean ok = userClient.modifierInstitution(institution.getId(), instNom, instAdresse,
                instSecteur, instNinea, instDomainesInteret, session.getToken());
        if (ok) {
            institution.setNom(instNom.trim());
            institution.setAdresse(instAdresse);
            institution.setSecteur(instSecteur);
            institution.setNinea(instNinea);
            institution.setDomainesInteret(instDomainesInteret);
            msg(true, "Institution mise à jour.");
            instEnEdition = false;
        } else {
            msg(false, "Échec de la mise à jour de l'institution.");
        }
    }

    // ── Annuler demande classique ─────────────────────────

    public void annulerDemande(Long id) {
        boolean ok = formationClient.annulerDemande(id, session.getToken());
        if (ok) {
            msg(true, "Demande annulée.");
            mesDemandes = formationClient.mesDemandes(session.getUtilisateurId(), session.getToken());
        } else {
            msg(false, "Impossible d'annuler cette demande.");
        }
    }

    // ── Formations à la carte ─────────────────────────────

    private void prefillLibre() {
        libreNomInstitution = institution != null ? institution.getNom() : "";
    }

    public void soumettreDemandeLibre() {
        if (libreDomaine == null || libreDomaine.isBlank()) {
            msg(false, "Veuillez préciser le domaine de formation."); return;
        }
        if (libreDescription == null || libreDescription.isBlank()) {
            msg(false, "Veuillez décrire votre besoin."); return;
        }
        LocalDate today = LocalDate.now();
        if (librePeriodeDebut != null && librePeriodeDebut.isBefore(today)) {
            msg(false, "La date de début ne peut pas être dans le passé."); return;
        }
        if (librePeriodeFin != null && librePeriodeFin.isBefore(today)) {
            msg(false, "La date de fin ne peut pas être dans le passé."); return;
        }
        if (librePeriodeDebut != null && librePeriodeFin != null && librePeriodeFin.isBefore(librePeriodeDebut)) {
            msg(false, "La date de fin doit être après la date de début."); return;
        }
        com.senexpert.frontend.model.DemandeLibreFront created = formationClient.creerDemandeLibre(
                session.getUtilisateurId(),
                libreNomInstitution,
                libreDomaine,
                libreDescription,
                libreNbParticipants,
                librePeriodeDebut != null ? librePeriodeDebut.toString() : null,
                librePeriodeFin != null ? librePeriodeFin.toString() : null,
                session.getToken());
        if (created != null) {
            // Post modules souhaitées
            for (String[] mod : modulesTemporaires) {
                try {
                    Integer dureeInt = mod[1] != null && !mod[1].isBlank() ? Integer.parseInt(mod[1].trim()) : null;
                    formationClient.ajouterFormationSouhaitee(created.getId(), mod[0], dureeInt, null, null, null, session.getToken());
                } catch (Exception ignored) {}
            }
            msg(true, "Demande de formation à la carte soumise avec succès !");
            mesDemandesLibres = formationClient.mesDemandesLibres(session.getUtilisateurId(), session.getToken());
            // Reset form
            libreDomaine = null; libreDescription = null;
            libreNbParticipants = null; librePeriodeDebut = null; librePeriodeFin = null;
            modulesTemporaires.clear(); tempThematique = ""; tempDuree = "";
        } else {
            msg(false, "Erreur lors de la soumission de la demande.");
        }
    }

    public void toggleMembreLibre(Long membreId) {
        if (selectedMemberIds.contains(membreId)) selectedMemberIds.remove(membreId);
        else selectedMemberIds.add(membreId);
    }

    public boolean isMembreLibreSelectionne(Long membreId) {
        return selectedMemberIds.contains(membreId);
    }

    public void sauvegarderParticipantsLibre() {
        if (demandeLibreDetail == null) return;
        boolean ok = formationClient.mettreAJourParticipantsLibre(
                demandeLibreDetail.getId(),
                new java.util.ArrayList<>(selectedMemberIds),
                session.getToken());
        if (ok) {
            demandeLibreDetail.setParticipantIds(new java.util.ArrayList<>(selectedMemberIds));
            msg(true, "Participants mis à jour.");
            mesDemandesLibres = formationClient.mesDemandesLibres(session.getUtilisateurId(), session.getToken());
        } else {
            msg(false, "Erreur lors de la mise à jour des participants.");
        }
    }

    public long getNbDemandesLibresEnAttente() {
        if (mesDemandesLibres == null) return 0;
        return mesDemandesLibres.stream()
               .filter(d -> "EN_ATTENTE".equals(d.getStatut())).count();
    }

    public java.util.Set<Long> getSelectedMemberIds() { return selectedMemberIds; }

    public void ajouterModuleTemp() {
        if (tempThematique == null || tempThematique.isBlank()) {
            msg(false, "Veuillez saisir la thématique du module."); return;
        }
        modulesTemporaires.add(new String[]{tempThematique.trim(), tempDuree != null ? tempDuree.trim() : ""});
        tempThematique = ""; tempDuree = "";
    }

    public void supprimerModuleTemp(int index) {
        if (index >= 0 && index < modulesTemporaires.size())
            modulesTemporaires.remove(index);
    }

    public void annulerDemandeLibre(Long id) {
        boolean ok = formationClient.annulerDemandeLibre(id, session.getToken());
        if (ok) {
            msg(true, "Demande à la carte annulée.");
            mesDemandesLibres = formationClient.mesDemandesLibres(session.getUtilisateurId(), session.getToken());
        } else {
            msg(false, "Impossible d'annuler cette demande.");
        }
    }

    public int getNbDemandesLibres() { return mesDemandesLibres == null ? 0 : mesDemandesLibres.size(); }

    public void rechargerDemandesLibres() {
        String token = session.getToken();
        try { formationClient.migrerOffresDemandesLibres(token); } catch (Exception ignored) {}
        try { mesDemandesLibres = formationClient.mesDemandesLibres(session.getUtilisateurId(), token); }
        catch (Exception e) { mesDemandesLibres = new java.util.ArrayList<>(); }
    }

    // ── Détail demande libre + gestion formations souhaitées ─────────

    public void ouvrirDetailDemandeLibre(DemandeLibreFront demande) {
        this.demandeLibreDetail = demande;
        selectedMemberIds.clear();
        if (demande != null && demande.getParticipantIds() != null)
            selectedMemberIds.addAll(demande.getParticipantIds());
        rafraichirFormationsSouhaitees();
        resetFormulaireFormation();
        chargerProposition();
    }

    private void rafraichirFormationsSouhaitees() {
        if (demandeLibreDetail == null) return;
        List<FormationSouhaitee> fs = formationClient.listerFormationsSouhaitees(
                demandeLibreDetail.getId(), session.getToken());
        demandeLibreDetail.setFormationsSouhaitees(fs);
        // Synchronise dans la liste principale
        if (mesDemandesLibres != null) {
            mesDemandesLibres.stream()
                    .filter(d -> d.getId().equals(demandeLibreDetail.getId()))
                    .findFirst().ifPresent(d -> d.setFormationsSouhaitees(fs));
        }
    }

    public void ajouterFormationSouhaitee() {
        if (demandeLibreDetail == null) return;
        if (fThematique == null || fThematique.isBlank()) {
            msg(false, "La thématique est obligatoire."); return;
        }
        FormationSouhaitee f = formationClient.ajouterFormationSouhaitee(
                demandeLibreDetail.getId(), fThematique, fDuree, fNbParticipants,
                fObjectifs, fNiveauRequis, session.getToken());
        if (f != null) {
            msg(true, "Formation ajoutée.");
            rafraichirFormationsSouhaitees();
            resetFormulaireFormation();
        } else {
            msg(false, "Impossible d'ajouter la formation.");
        }
    }

    public void supprimerFormationSouhaitee(Long formationId) {
        if (demandeLibreDetail == null) return;
        boolean ok = formationClient.supprimerFormationSouhaitee(
                demandeLibreDetail.getId(), formationId, session.getToken());
        if (ok) {
            msg(true, "Formation supprimée.");
            rafraichirFormationsSouhaitees();
        } else {
            msg(false, "Impossible de supprimer cette formation.");
        }
    }

    private void resetFormulaireFormation() {
        fThematique = null; fDuree = null; fNbParticipants = null;
        fObjectifs = null; fNiveauRequis = null;
    }

    // ── Getters/Setters formations souhaitées ─────────────
    public DemandeLibreFront getDemandeLibreDetail() { return demandeLibreDetail; }
    public String getfThematique() { return fThematique; }
    public void setfThematique(String v) { this.fThematique = v; }
    public Integer getfDuree() { return fDuree; }
    public void setfDuree(Integer v) { this.fDuree = v; }
    public Integer getfNbParticipants() { return fNbParticipants; }
    public void setfNbParticipants(Integer v) { this.fNbParticipants = v; }
    public String getfObjectifs() { return fObjectifs; }
    public void setfObjectifs(String v) { this.fObjectifs = v; }
    public String getfNiveauRequis() { return fNiveauRequis; }
    public void setfNiveauRequis(String v) { this.fNiveauRequis = v; }

    // ── Candidature ───────────────────────────────────────

    public void filtrerParDomaine() {
        offres = domaineFiltre == null
                ? formationClient.listerOffres()
                : formationClient.listerOffresParDomaine(domaineFiltre);
    }

    /** Ouvre le dialog de postulation et initialise l'état */
    public void ouvrirDialogPostuler(OffreFormation offre) {
        this.offreSelectionnee = offre;
        this.documentsPostulation = new ArrayList<>();
        this.nomsDocuments = new ArrayList<>();
        this.fichierParticipantsBytes = null;
        this.fichierParticipantsNom = null;
        this.fichierParticipantsType = null;
    }

    /** Reçoit un fichier uploadé (p:fileUpload auto="true") */
    public void handleDocumentUpload(FileUploadEvent event) {
        UploadedFile fichier = event.getFile();
        if (fichier != null && fichier.getSize() > 0) {
            documentsPostulation.add(fichier);
            nomsDocuments.add(fichier.getFileName());
        }
    }

    /** Reçoit le fichier liste des participants (UploadedFile auto="true") */
    public void handleParticipantsUpload(org.primefaces.event.FileUploadEvent event) {
        org.primefaces.model.file.UploadedFile f = event.getFile();
        if (f != null && f.getSize() > 0) {
            try {
                fichierParticipantsBytes = f.getContent();
                fichierParticipantsNom   = f.getFileName();
                fichierParticipantsType  = f.getContentType();
            } catch (Exception e) {
                System.err.println("handleParticipantsUpload error: " + e.getMessage());
            }
        }
    }

    /** Retire un document de la liste avant soumission */
    public void supprimerDocument(int index) {
        if (index >= 0 && index < documentsPostulation.size()) {
            documentsPostulation.remove(index);
            nomsDocuments.remove(index);
        }
    }

    /** Soumet la demande : upload des fichiers puis POST /api/demandes */
    public void postulerAvecDocuments() {
        if (!session.isConnecte()) { msg(false, "Connexion requise."); return; }
        if (offreSelectionnee == null) { msg(false, "Aucune offre sélectionnée."); return; }

        List<String> chemins = new ArrayList<>();
        if (!documentsPostulation.isEmpty()) {
            chemins = formationClient.uploadDocumentsDemande(
                    session.getUtilisateurId(), documentsPostulation, session.getToken());
        }

        // Soumettre la demande et récupérer l'ID pour l'upload participants
        Long demandeId = formationClient.soumettreDemandeReturnId(
                session.getUtilisateurId(), offreSelectionnee.getId(), chemins, session.getToken());

        if (demandeId != null) {
            // Upload fichier participants si fourni
            if (fichierParticipantsBytes != null && fichierParticipantsBytes.length > 0) {
                boolean ok = formationClient.uploadFichierParticipantsDemande(
                        demandeId, fichierParticipantsBytes,
                        fichierParticipantsNom, fichierParticipantsType,
                        session.getToken());
                if (ok) msg(true, "Candidature soumise avec liste des participants. En attente de validation.");
                else    msg(true, "Candidature soumise. Le fichier participants n'a pas pu être uploadé.");
            } else {
                msg(true, "Candidature soumise avec succès ! En attente de validation.");
            }
            mesDemandes = formationClient.mesDemandes(session.getUtilisateurId(), session.getToken());
            offreSelectionnee = null;
            documentsPostulation = new ArrayList<>();
            nomsDocuments = new ArrayList<>();
            fichierParticipantsBytes = null;
            fichierParticipantsNom   = null;
            fichierParticipantsType  = null;
        } else {
            msg(false, "Impossible de soumettre la candidature (erreur réseau ou doublon).");
        }
    }

    public int getNbDocumentsPostulation() {
        return documentsPostulation == null ? 0 : documentsPostulation.size();
    }

    // ── Détail demande : participants / messages / paiement ───────────────────

    /** Ouvre le panneau de détail d'une demande (participants, messages, paiement). */
    public void ouvrirDetailDemande(DemandeFormation demande) {
        this.demandeSelectionnee = demande;
        if (demande == null) return;
        Long id = demande.getId();
        String token = session.getToken();
        try { participants = formationClient.listerParticipantsDemande(id, token); }
        catch (Exception e) { participants = new ArrayList<>(); }
        try { messagesConversation = formationClient.conversationDemande(id, session.getUtilisateurId(), token); }
        catch (Exception e) { messagesConversation = new ArrayList<>(); }
        try { demandeSelectionneePayee = formationClient.demandeEstPayee(id, token); }
        catch (Exception e) { demandeSelectionneePayee = false; }
        // Reset formulaires
        participantNom = null; participantPrenom = null;
        participantEmail = null; participantTelephone = null;
        messageContenu = null;
        paiementReference = null;
        paiementMethode = "VIREMENT";
        // Auto-fill montant depuis l'offre
        paiementMontant = (demande.getOffre() != null && demande.getOffre().getCout() != null && demande.getOffre().getCout() > 0)
                ? demande.getOffre().getCout() : null;
    }

    // ── Participants ──────────────────────────────────────

    public void ajouterParticipant() {
        if (demandeSelectionnee == null) return;
        if (participantNom == null || participantNom.isBlank()
                || participantPrenom == null || participantPrenom.isBlank()) {
            msg(false, "Nom et prénom sont obligatoires."); return;
        }
        ParticipantInstitutionFront dto = new ParticipantInstitutionFront();
        dto.setNom(participantNom.trim());
        dto.setPrenom(participantPrenom.trim());
        dto.setEmail(participantEmail);
        dto.setTelephone(participantTelephone);
        ParticipantInstitutionFront result = formationClient.ajouterParticipantDemande(
                demandeSelectionnee.getId(), dto, session.getToken());
        if (result != null) {
            participants.add(result);
            participantNom = null; participantPrenom = null;
            participantEmail = null; participantTelephone = null;
            msg(true, "Participant ajouté.");
        } else {
            msg(false, "Erreur lors de l'ajout du participant.");
        }
    }

    public void updateStatutParticipant(Long participantId, String statut) {
        boolean ok = formationClient.updateStatutParticipant(participantId, statut, session.getToken());
        if (ok) {
            participants.stream().filter(p -> participantId.equals(p.getId()))
                    .findFirst().ifPresent(p -> p.setStatut(statut));
            msg(true, "Statut mis à jour.");
        } else {
            msg(false, "Impossible de mettre à jour le statut.");
        }
    }

    public void supprimerParticipant(Long participantId) {
        boolean ok = formationClient.supprimerParticipant(participantId, session.getToken());
        if (ok) {
            participants.removeIf(p -> participantId.equals(p.getId()));
            msg(true, "Participant retiré.");
        } else {
            msg(false, "Impossible de supprimer le participant.");
        }
    }

    public int getNbParticipants() { return participants == null ? 0 : participants.size(); }

    // ── Messagerie demande ────────────────────────────────

    public void envoyerMessageDemande() {
        if (demandeSelectionnee == null) return;
        if (messageContenu == null || messageContenu.isBlank()) {
            msg(false, "Le message ne peut pas être vide."); return;
        }
        MessageFront m = new MessageFront();
        m.setDemandeId(demandeSelectionnee.getId());
        m.setExpediteurId(session.getUtilisateurId());
        m.setExpediteurRole("AGENT_INSTITUTION");
        m.setExpediteurNom(session.getNomComplet());
        m.setDestinataireId(ADMIN_DESTINATAIRE_ID);
        m.setContenu(messageContenu.trim());
        MessageFront envoyé = formationClient.envoyerMessage(m, session.getToken());
        if (envoyé != null) {
            messagesConversation.add(envoyé);
            messageContenu = null;
        } else {
            msg(false, "Erreur lors de l'envoi du message.");
        }
    }

    public int getNbMessagesNonLusDemande() {
        if (demandeSelectionnee == null) return 0;
        return formationClient.messagesNonLusDemande(
                demandeSelectionnee.getId(), session.getUtilisateurId(), session.getToken());
    }

    // ── Paiement demande ──────────────────────────────────

    public void enregistrerPaiementDemande() {
        if (demandeSelectionnee == null) return;
        // Montant : auto-rempli depuis offre, ou 0 si formation gratuite
        double montant = (paiementMontant != null && paiementMontant > 0) ? paiementMontant : 0.0;
        String titre = demandeSelectionnee.getOffre() != null
                ? demandeSelectionnee.getOffre().getTitre() : "Formation";
        boolean ok = formationClient.enregistrerPaiementDemande(
                session.getUtilisateurId(), demandeSelectionnee.getId(),
                titre, montant, paiementMethode, paiementReference,
                session.getToken());
        if (ok) {
            demandeSelectionneePayee = true;
            // Mettre à jour le statut local directement (objet partagé avec mesDemandes)
            if (demandeSelectionnee != null) {
                demandeSelectionnee.setStatut("CONFIRMEE");
            }
            msg(true, "Paiement confirmé ! Vous pouvez télécharger votre certificat d'inscription.");
            paiementReference = null;
        } else {
            msg(false, "Erreur lors de l'enregistrement du paiement.");
        }
    }

    /** URL du certificat d'inscription (PDF) — disponible après paiement confirmé. */
    public String getUrlCertificatInscription() {
        if (demandeSelectionnee == null) return "#";
        return formationClient.urlCertificatInscription(demandeSelectionnee.getId());
    }

    public String getUrlAttestation() {
        if (demandeSelectionnee == null) return "#";
        return formationClient.urlAttestationDemande(demandeSelectionnee.getId());
    }

    // ── Stats ─────────────────────────────────────────────

    public int  getNbDemandes()          { return mesDemandes == null ? 0 : mesDemandes.size(); }
    public int  getNbOffres()            { return offres == null ? 0 : offres.size(); }
    public int  getNbMembres()           { return membres == null ? 0 : membres.size(); }
    public long getNbDemandesValidees()  { return count("CONFIRMEE"); } // CONFIRMEE = validée + payée
    public long getNbDemandesEnAttente() { return count("EN_ATTENTE"); }
    public long getNbDemandesRejetees()  { return count("REJETEE"); }

    /** Nombre de formations souhaitées sur la demande libre en cours de consultation. */
    public int getNbFormationsSouhaitees() {
        if (demandeLibreDetail == null || demandeLibreDetail.getFormationsSouhaitees() == null) return 0;
        return demandeLibreDetail.getFormationsSouhaitees().size();
    }

    public long getNbFormationsSurMesure() {
        if (mesDemandesLibres == null) return 0;
        return mesDemandesLibres.stream()
                .filter(d -> "VALIDEE".equals(d.getStatut()))
                .count();
    }

    public List<DemandeLibreFront> getFormationsSurMesure() {
        if (mesDemandesLibres == null) return java.util.Collections.emptyList();
        return mesDemandesLibres.stream()
                .filter(d -> "VALIDEE".equals(d.getStatut()))
                .collect(java.util.stream.Collectors.toList());
    }

    /** Demandes en cours de traitement (EN_ATTENTE ou PROPOSITION_RECUE uniquement) */
    public List<DemandeLibreFront> getMesDemandesLibresEnCours() {
        if (mesDemandesLibres == null) return java.util.Collections.emptyList();
        return mesDemandesLibres.stream()
                .filter(d -> "EN_ATTENTE".equals(d.getStatut()) || "PROPOSITION_RECUE".equals(d.getStatut()))
                .collect(java.util.stream.Collectors.toList());
    }

    public boolean isHasDemandesEnCours() {
        return !getMesDemandesLibresEnCours().isEmpty();
    }

    private long count(String statut) {
        if (mesDemandes == null) return 0;
        return mesDemandes.stream().filter(d -> statut.equals(d.getStatut())).count();
    }

    public List<DemandeFormation> getDemandesRecentes() {
        if (mesDemandes == null || mesDemandes.isEmpty()) return List.of();
        return mesDemandes.subList(0, Math.min(4, mesDemandes.size()));
    }

    /** Demandes VALIDEE à traiter (paiement, attestation…) — max 3. */
    public List<DemandeFormation> getDemandesAFaire() {
        if (mesDemandes == null) return List.of();
        return mesDemandes.stream()
                .filter(d -> "VALIDEE".equals(d.getStatut()))
                .limit(3)
                .collect(java.util.stream.Collectors.toList());
    }

    /** Candidatures définitivement confirmées (paiement effectué). */
    public List<DemandeFormation> getMesFormationsValidees() {
        if (mesDemandes == null) return java.util.Collections.emptyList();
        return mesDemandes.stream()
                .filter(d -> "CONFIRMEE".equals(d.getStatut()))
                .collect(java.util.stream.Collectors.toList());
    }

    /** Candidatures acceptées par l'admin mais en attente de paiement. */
    public List<DemandeFormation> getDemandesEnAttentePaiement() {
        if (mesDemandes == null) return java.util.Collections.emptyList();
        return mesDemandes.stream()
                .filter(d -> "VALIDEE".equals(d.getStatut()))
                .collect(java.util.stream.Collectors.toList());
    }

    public long getNbDemandesEnAttentePaiement() {
        if (mesDemandes == null) return 0;
        return mesDemandes.stream().filter(d -> "VALIDEE".equals(d.getStatut())).count();
    }

    public long getNbDemandesConfirmees() {
        if (mesDemandes == null) return 0;
        return mesDemandes.stream().filter(d -> "CONFIRMEE".equals(d.getStatut())).count();
    }

    /** Nombre total de formations obtenues (CONFIRMEE + demandes à la carte VALIDEE). */
    public long getNbFormationsObtenues() {
        return getNbDemandesConfirmees() + getNbFormationsSurMesure();
    }

    public List<OffreFormation> getOffresTop() {
        if (offres == null || offres.isEmpty()) return List.of();
        return offres.subList(0, Math.min(6, offres.size()));
    }

    public void voirCandidatsOffre(Long offreId, String titre) {
        this.offreSelectionneeId    = offreId;
        this.offreSelectionneetitre = titre;
        try {
            candidatsOffre = formationClient.demandesParOffre(offreId, session.getToken());
        } catch (Exception e) {
            candidatsOffre = new ArrayList<>();
        }
    }

    public int getNbCandidatsOffre() {
        return candidatsOffre == null ? 0 : candidatsOffre.size();
    }

    public long getNbCandidatsEnAttente() {
        if (candidatsOffre == null) return 0;
        return candidatsOffre.stream().filter(d -> "EN_ATTENTE".equals(d.getStatut())).count();
    }

    public long getNbCandidatsValides() {
        if (candidatsOffre == null) return 0;
        return candidatsOffre.stream().filter(d -> "VALIDEE".equals(d.getStatut())).count();
    }

    /** L'agent peut agir uniquement si son compte ET son institution sont ACTIF */
    public boolean isActionsDisponibles() {
        return "ACTIF".equals(session.getStatut())
            && institution != null
            && "ACTIF".equals(institution.getStatut());
    }

    /** Vrai si compte en attente OU institution en attente */
    public boolean isEnAttente() {
        return "EN_ATTENTE".equals(session.getStatut())
            || (institution != null && "EN_ATTENTE".equals(institution.getStatut()));
    }

    public boolean isInstitutionActive()   { return institution != null && "ACTIF".equals(institution.getStatut()); }
    public boolean isInstitutionEnAttente(){ return institution != null && "EN_ATTENTE".equals(institution.getStatut()); }

    public String getStatutInstitution() {
        if (institution == null) return "Non rattaché";
        String s = institution.getStatut();
        if ("ACTIF".equals(s))      return "Active";
        if ("EN_ATTENTE".equals(s)) return "En attente de validation";
        if ("INACTIF".equals(s))    return "Suspendue";
        return s != null ? s : "Inconnu";
    }

    // ── Helper ────────────────────────────────────────────

    private void msg(boolean ok, String text) {
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(ok ? FacesMessage.SEVERITY_INFO : FacesMessage.SEVERITY_ERROR,
                ok ? "Succès" : "Erreur", text));
    }

    // ── Getters / Setters ─────────────────────────────────

    public Institution getInstitution() { return institution; }
    public List<Utilisateur> getMembres() { return membres; }
    public List<OffreFormation> getOffres() { return offres; }
    public List<Domaine> getDomaines() { return domaines; }
    public List<DemandeFormation> getMesDemandes() { return mesDemandes; }
    public Long getDomaineFiltre() { return domaineFiltre; }
    public void setDomaineFiltre(Long v) { this.domaineFiltre = v; }

    public String getEditNom() { return editNom; }
    public void setEditNom(String v) { this.editNom = v; }
    public String getEditPrenom() { return editPrenom; }
    public void setEditPrenom(String v) { this.editPrenom = v; }
    public String getEditTelephone() { return editTelephone; }
    public void setEditTelephone(String v) { this.editTelephone = v; }

    public String getAncienMdp() { return ancienMdp; }
    public void setAncienMdp(String v) { this.ancienMdp = v; }
    public String getNouveauMdp() { return nouveauMdp; }
    public void setNouveauMdp(String v) { this.nouveauMdp = v; }
    public String getConfirmerMdp() { return confirmerMdp; }
    public void setConfirmerMdp(String v) { this.confirmerMdp = v; }

    public String getInstNom() { return instNom; }
    public void setInstNom(String v) { this.instNom = v; }
    public String getInstAdresse() { return instAdresse; }
    public void setInstAdresse(String v) { this.instAdresse = v; }
    public String getInstSecteur() { return instSecteur; }
    public void setInstSecteur(String v) { this.instSecteur = v; }
    public String getInstNinea() { return instNinea; }
    public void setInstNinea(String v) { this.instNinea = v; }
    public String getInstDomainesInteret() { return instDomainesInteret; }
    public void setInstDomainesInteret(String v) { this.instDomainesInteret = v; }

    public boolean isProfilEnEdition() { return profilEnEdition; }

    public void setProfilEnEdition(boolean v) { this.profilEnEdition = v; }
    public boolean isMdpEnEdition()   { return mdpEnEdition; }
    public void setMdpEnEdition(boolean v) { this.mdpEnEdition = v; }
    public boolean isInstEnEdition()  { return instEnEdition; }
    public void setInstEnEdition(boolean v) { this.instEnEdition = v; }

    public OffreFormation getOffreSelectionnee() { return offreSelectionnee; }
    public void setOffreSelectionnee(OffreFormation v) { this.offreSelectionnee = v; }

    public Long getOffreSelectionneeId() { return offreSelectionneeId; }
    public String getOffreSelectionneetitre() { return offreSelectionneetitre; }
    public List<DemandeFormation> getCandidatsOffre()   { return candidatsOffre; }

    public String getParticipantNom() { return participantNom; }
    public void setParticipantNom(String v) { this.participantNom = v; }
    public String getParticipantPrenom() { return participantPrenom; }
    public void setParticipantPrenom(String v) { this.participantPrenom = v; }
    public String getParticipantEmail() { return participantEmail; }
    public void setParticipantEmail(String v) { this.participantEmail = v; }
    public String getParticipantTelephone() { return participantTelephone; }
    public void setParticipantTelephone(String v) { this.participantTelephone = v; }

    public List<ParticipantInstitutionFront> getParticipants() { return participants; }
    public DemandeFormation getDemandeSelectionnee()           { return demandeSelectionnee; }
    public boolean isDemandeSelectionneePayee()                { return demandeSelectionneePayee; }

    public List<MessageFront> getMessagesConversation() { return messagesConversation; }
    public String getMessageContenu() { return messageContenu; }
    public void setMessageContenu(String v) { this.messageContenu = v; }

    public List<DemandeLibreFront> getMesDemandesLibres() { return mesDemandesLibres; }
    public String getLibreNomInstitution() { return libreNomInstitution; }
    public void setLibreNomInstitution(String v) { this.libreNomInstitution = v; }
    public String getLibreDomaine() { return libreDomaine; }
    public void setLibreDomaine(String v) { this.libreDomaine = v; }
    public String getLibreDescription() { return libreDescription; }
    public void setLibreDescription(String v) { this.libreDescription = v; }
    public Integer getLibreNbParticipants() { return libreNbParticipants; }
    public void setLibreNbParticipants(Integer v) { this.libreNbParticipants = v; }
    public java.time.LocalDate getLibrePeriodeDebut() { return librePeriodeDebut; }
    public void setLibrePeriodeDebut(java.time.LocalDate v) { this.librePeriodeDebut = v; }
    public java.time.LocalDate getLibrePeriodeFin() { return librePeriodeFin; }
    public void setLibrePeriodeFin(java.time.LocalDate v) { this.librePeriodeFin = v; }
    public java.time.LocalDate getToday() { return java.time.LocalDate.now(); }

    // ── Getters/Setters modules temporaires ────────────────────────────────
    public java.util.List<String[]> getModulesTemporaires() { return modulesTemporaires; }
    public String getTempThematique() { return tempThematique; }
    public void setTempThematique(String v) { this.tempThematique = v; }
    public String getTempDuree() { return tempDuree; }
    public void setTempDuree(String v) { this.tempDuree = v; }

    // ── Getters/Setters paiement ──────────────────────────────────────────
    public Double getPaiementMontant() { return paiementMontant; }
    public void setPaiementMontant(Double v) { this.paiementMontant = v; }
    public String getPaiementMethode() { return paiementMethode; }
    public void setPaiementMethode(String v) { this.paiementMethode = v; }
    public String getPaiementReference() { return paiementReference; }
    public void setPaiementReference(String v) { this.paiementReference = v; }

    // ── Proposition de formation (demande à la carte) ──────────────────────

    private com.senexpert.frontend.model.PropositionFormationFront propositionCourante;
    private String motifRefusProposition;

    /** Charge la proposition pour la demande en cours de consultation */
    public void chargerProposition() {
        propositionCourante = null;
        if (demandeLibreDetail == null) return;
        try {
            propositionCourante = formationClient.derniereProposition(
                    demandeLibreDetail.getId(), session.getToken());
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void accepterPropositionCourante() {
        if (propositionCourante == null) return;
        boolean ok = formationClient.accepterProposition(propositionCourante.getId(), session.getToken());
        if (ok) {
            msg(true, "Proposition acceptée ! Procédez au paiement.");
            propositionCourante.setStatut("ACCEPTEE");
        } else {
            msg(false, "Échec de l'acceptation.");
        }
    }

    public void refuserPropositionCourante() {
        if (propositionCourante == null) return;
        boolean ok = formationClient.refuserProposition(propositionCourante.getId(), motifRefusProposition, session.getToken());
        if (ok) {
            msg(true, "Proposition refusée.");
            propositionCourante.setStatut("REFUSEE");
            motifRefusProposition = null;
        } else {
            msg(false, "Échec du refus.");
        }
    }

    public void payerProposition() {
        if (propositionCourante == null || paiementMethode == null || paiementMethode.isBlank()) {
            msg(false, "Méthode de paiement requise.");
            return;
        }
        if (paiementReference == null || paiementReference.isBlank()) {
            msg(false, "Référence de transaction requise.");
            return;
        }
        String titreFormation = demandeLibreDetail.getDomaine() != null
                ? "Formation à la carte : " + demandeLibreDetail.getDomaine()
                : "Formation à la carte";
        boolean ok = formationClient.enregistrerPaiementDemande(
                session.getUtilisateurId(),
                demandeLibreDetail.getId(),
                titreFormation,
                propositionCourante.getMontant(),
                paiementMethode,
                paiementReference,
                session.getToken());
        if (ok) {
            msg(true, "Paiement enregistré avec succès !");
            paiementMethode = null;
            paiementReference = null;
        } else {
            msg(false, "Échec de l'enregistrement du paiement.");
        }
    }

    public com.senexpert.frontend.model.PropositionFormationFront getPropositionCourante() { return propositionCourante; }
    public void setPropositionCourante(com.senexpert.frontend.model.PropositionFormationFront v) { this.propositionCourante = v; }
    public String getMotifRefusProposition() { return motifRefusProposition; }
    public void setMotifRefusProposition(String v) { this.motifRefusProposition = v; }
}
