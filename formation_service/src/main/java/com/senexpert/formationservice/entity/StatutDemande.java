package com.senexpert.formationservice.entity;

public enum StatutDemande {
    EN_ATTENTE,
    PROPOSITION_RECUE,
    VALIDEE,       // acceptée par l'admin — paiement requis
    CONFIRMEE,     // paiement reçu — formation définitivement confirmée
    REJETEE
}