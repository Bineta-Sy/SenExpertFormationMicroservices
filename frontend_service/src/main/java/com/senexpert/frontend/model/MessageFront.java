package com.senexpert.frontend.model;

public class MessageFront {

    private Long   id;
    private Long   offreId;
    private Long   demandeId;
    private Long   expediteurId;
    private String expediteurRole;
    private String expediteurNom;
    private Long   destinataireId;
    private String contenu;
    private String dateEnvoi;
    private boolean lu;

    public Long    getId()               { return id; }
    public void    setId(Long v)         { id = v; }
    public Long    getOffreId()          { return offreId; }
    public void    setOffreId(Long v)    { offreId = v; }
    public Long    getDemandeId()        { return demandeId; }
    public void    setDemandeId(Long v)  { demandeId = v; }
    public Long    getExpediteurId()     { return expediteurId; }
    public void    setExpediteurId(Long v){ expediteurId = v; }
    public String  getExpediteurRole()   { return expediteurRole; }
    public void    setExpediteurRole(String v){ expediteurRole = v; }
    public String  getExpediteurNom()    { return expediteurNom; }
    public void    setExpediteurNom(String v){ expediteurNom = v; }
    public Long    getDestinataireId()   { return destinataireId; }
    public void    setDestinataireId(Long v){ destinataireId = v; }
    public String  getContenu()          { return contenu; }
    public void    setContenu(String v)  { contenu = v; }
    public String  getDateEnvoi()        { return dateEnvoi; }
    public void    setDateEnvoi(String v){ dateEnvoi = v; }
    public boolean isLu()                { return lu; }
    public void    setLu(boolean v)      { lu = v; }

    /** Vrai si ce message est de l'apprenant (pour aligner à droite dans le fil). */
    public boolean isDeApprenant()       { return "APPRENANT".equals(expediteurRole); }
}
