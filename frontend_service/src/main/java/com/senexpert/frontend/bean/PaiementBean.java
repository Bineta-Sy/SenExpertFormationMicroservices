package com.senexpert.frontend.bean;

import com.senexpert.frontend.client.FormationServiceClient;
import com.senexpert.frontend.model.DemandeFormation;
import com.senexpert.frontend.model.OffreFormation;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.UUID;

/**
 * Gère le flux de paiement avant soumission d'une candidature.
 * SessionScoped pour survivre à la redirection vers la page de paiement.
 */
@Named
@SessionScoped
public class PaiementBean implements Serializable {

    @Inject private SessionBean session;
    @Inject private FormationServiceClient formationClient;

    // Offre en cours de paiement
    private Long offreId;
    private String offreTitre;
    private Double montant;
    private String domaine;

    /**
     * ID de la demande existante (non-null quand on paie depuis le dashboard
     * pour une demande déjà VALIDEE — null quand on crée une nouvelle candidature).
     */
    private Long demandeId;

    // Formulaire paiement
    private String methodePaiement = "WAVE"; // WAVE | ORANGE_MONEY | CARTE
    private String telephone;
    private String numeroCarte;
    private String expiration;
    private String cvv;
    private String nomCarte;

    // Résultat
    private String referenceTransaction;
    private boolean paiementEffectue = false;

    /**
     * Initialise le paiement pour une offre et redirige vers la page de paiement.
     * Appelé depuis le bouton "Candidater" si l'offre est payante.
     */
    /** Nouveau paiement pour une offre sans demande préexistante (depuis le catalogue). */
    public String initierPaiement(OffreFormation offre) {
        if (!session.isConnecte()) {
            return "/login?faces-redirect=true";
        }
        this.offreId     = offre.getId();
        this.offreTitre  = offre.getTitre();
        this.montant     = offre.getCout() != null ? offre.getCout() : 0.0;
        this.domaine     = offre.getDomaine() != null ? offre.getDomaine().getLibelle() : "";
        this.demandeId   = null; // nouvelle candidature
        this.paiementEffectue     = false;
        this.referenceTransaction = null;
        this.telephone   = null;
        this.numeroCarte = null;
        return "/paiement?faces-redirect=true";
    }

    /**
     * Paiement pour une demande déjà existante et validée (depuis le dashboard).
     * Ne crée PAS de nouvelle demande — se contente d'enregistrer le paiement.
     */
    public String initierPaiementDemande(DemandeFormation demande) {
        if (!session.isConnecte()) {
            return "/login?faces-redirect=true";
        }
        OffreFormation offre = demande.getOffre();
        this.demandeId   = demande.getId();
        this.offreId     = offre != null ? offre.getId() : null;
        this.offreTitre  = offre != null ? offre.getTitre() : "Formation";
        this.montant     = offre != null && offre.getCout() != null ? offre.getCout() : 0.0;
        this.domaine     = offre != null && offre.getDomaine() != null ? offre.getDomaine().getLibelle() : "";
        this.paiementEffectue     = false;
        this.referenceTransaction = null;
        this.telephone   = null;
        this.numeroCarte = null;
        return "/paiement?faces-redirect=true";
    }

    /**
     * Traite le paiement et crée la candidature si succès.
     * En production : appeler ici l'API CinetPay / Wave / Orange Money.
     */
    public String traiterPaiement() {
        // ── Validation ──────────────────────────────────────────────────────
        if (!validerFormulaire()) return null;

        // ── Simulation gateway (remplacer par vrai appel API en prod) ────────
        boolean paiementOk = simulerPaiementGateway();

        if (!paiementOk) {
            msg(false, "Échec du paiement. Vérifiez vos informations et réessayez.");
            return null;
        }

        this.referenceTransaction = "SEF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        if (demandeId != null) {
            // ── Paiement pour une demande existante (validée depuis le dashboard) ──
            // Pas de création de nouvelle demande — on enregistre juste le paiement
            formationClient.enregistrerPaiementDemande(
                session.getUtilisateurId(), demandeId, offreTitre,
                montant, methodePaiement, referenceTransaction, session.getToken());
        } else {
            // ── Nouvelle candidature : créer la demande puis enregistrer le paiement ──
            String erreur = formationClient.soumettreDemande(
                    session.getUtilisateurId(), offreId, null, session.getToken());

            if (erreur != null) {
                msg(false, "Paiement reçu, mais candidature refusée : " + erreur
                         + ". Contactez l'administration pour un remboursement.");
                return null;
            }

            formationClient.enregistrerPaiement(
                session.getUtilisateurId(), offreId, offreTitre,
                montant, methodePaiement, referenceTransaction, session.getToken());
        }

        this.paiementEffectue = true;

        return "/paiement-succes?faces-redirect=true";
    }

    /**
     * Candidature gratuite : soumettre directement sans paiement.
     */
    public String soumettreSansPaiement(OffreFormation offre) {
        if (!session.isConnecte()) return "/login?faces-redirect=true";
        String erreur = formationClient.soumettreDemande(
                session.getUtilisateurId(), offre.getId(), null, session.getToken());
        if (erreur == null) {
            this.offreTitre           = offre.getTitre();
            this.montant              = 0.0;
            this.paiementEffectue     = true;
            this.referenceTransaction = "SEF-GRATUIT-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
            return "/paiement-succes?faces-redirect=true";
        }
        // Retourner un message d'erreur sur la page courante
        msg(false, erreur);
        return null;
    }

    // ── Validation formulaire ────────────────────────────────────────────────

    private boolean validerFormulaire() {
        switch (methodePaiement) {
            case "WAVE", "ORANGE_MONEY" -> {
                if (telephone == null || telephone.isBlank()) {
                    msg(false, "Veuillez saisir votre numéro de téléphone.");
                    return false;
                }
                if (!telephone.matches("\\+?[0-9]{9,15}")) {
                    msg(false, "Numéro de téléphone invalide.");
                    return false;
                }
            }
            case "CARTE" -> {
                if (numeroCarte == null || numeroCarte.isBlank()) {
                    msg(false, "Numéro de carte requis."); return false;
                }
                if (expiration == null || expiration.isBlank()) {
                    msg(false, "Date d'expiration requise."); return false;
                }
                if (cvv == null || cvv.isBlank()) {
                    msg(false, "CVV requis."); return false;
                }
                if (nomCarte == null || nomCarte.isBlank()) {
                    msg(false, "Nom sur la carte requis."); return false;
                }
            }
        }
        return true;
    }

    /**
     * Simulation de passerelle de paiement.
     * À remplacer par un vrai appel API (CinetPay, Wave Business, etc.).
     *
     * CinetPay example :
     *   POST https://api-checkout.cinetpay.com/v2/payment
     *   { "apikey": "...", "site_id": "...", "transaction_id": "...",
     *     "amount": montant, "currency": "XOF", "description": offreTitre,
     *     "return_url": "...", "notify_url": "..." }
     */
    private boolean simulerPaiementGateway() {
        // Simulation : refus si numéro fictif "0000000"
        if (telephone != null && telephone.contains("0000000")) return false;
        if (numeroCarte != null && numeroCarte.startsWith("0000")) return false;
        // Sinon : accepté (simulé)
        try { Thread.sleep(800); } catch (InterruptedException ignored) {}
        return true;
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void msg(boolean ok, String text) {
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(ok ? FacesMessage.SEVERITY_INFO : FacesMessage.SEVERITY_ERROR,
                ok ? "Succès" : "Erreur", text));
    }

    public String getMontantFormate() {
        if (montant == null || montant <= 0) return "Gratuit";
        return String.format("%,.0f FCFA", montant).replace(",", " ");
    }

    // ── Getters / Setters ────────────────────────────────────────────────────

    public Long getOffreId() { return offreId; }
    public String getOffreTitre() { return offreTitre; }
    public Double getMontant() { return montant; }
    public String getDomaine() { return domaine; }
    public String getMethodePaiement() { return methodePaiement; }
    public void setMethodePaiement(String m) { this.methodePaiement = m; }
    public String getTelephone() { return telephone; }
    public void setTelephone(String t) { this.telephone = t; }
    public String getNumeroCarte() { return numeroCarte; }
    public void setNumeroCarte(String n) { this.numeroCarte = n; }
    public String getExpiration() { return expiration; }
    public void setExpiration(String e) { this.expiration = e; }
    public String getCvv() { return cvv; }
    public void setCvv(String c) { this.cvv = c; }
    public String getNomCarte() { return nomCarte; }
    public void setNomCarte(String n) { this.nomCarte = n; }
    public String getReferenceTransaction() { return referenceTransaction; }
    public boolean isPaiementEffectue() { return paiementEffectue; }
}
