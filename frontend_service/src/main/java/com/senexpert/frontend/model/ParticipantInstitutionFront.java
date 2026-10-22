package com.senexpert.frontend.model;

public class ParticipantInstitutionFront {
    private Long   id;
    private Long   demandeId;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String statut;
    private String dateInscription;

    public Long   getId()               { return id; }
    public void   setId(Long v)         { id = v; }
    public Long   getDemandeId()        { return demandeId; }
    public void   setDemandeId(Long v)  { demandeId = v; }
    public String getNom()              { return nom; }
    public void   setNom(String v)      { nom = v; }
    public String getPrenom()           { return prenom; }
    public void   setPrenom(String v)   { prenom = v; }
    public String getEmail()            { return email; }
    public void   setEmail(String v)    { email = v; }
    public String getTelephone()        { return telephone; }
    public void   setTelephone(String v){ telephone = v; }
    public String getStatut()           { return statut; }
    public void   setStatut(String v)   { statut = v; }
    public String getDateInscription()  { return dateInscription; }
    public void   setDateInscription(String v){ dateInscription = v; }
}
