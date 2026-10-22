package com.senexpert.formationservice.entity;

public enum StatutSoumission {
    EN_ATTENTE,   // soumis mais pas encore corrigé
    CORRIGE,      // note attribuée
    RENDU_TARD    // soumis après la deadline
}
