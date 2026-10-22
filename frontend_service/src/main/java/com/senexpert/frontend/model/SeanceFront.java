package com.senexpert.frontend.model;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Le backend retourne Seance avec un champ "module" imbriqué.
 * On stocke cet objet puis on expose moduleId / moduleTitre via getters.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SeanceFront implements Serializable {

    private Long   id;
    private String date;
    private String heureDebut;
    private String heureFin;
    private String salle;
    private String lienVisio;
    private String notes;

    /** Objet module imbriqué tel que renvoyé par le backend */
    private ModuleFront module;

    // ── Getters dérivés ─────────────────────────────────────────
    public Long   getModuleId()    { return module != null ? module.getId()   : null; }
    public String getModuleTitre() { return module != null ? module.getTitre(): null; }

    public String getCreneauFormate() {
        if (heureDebut == null) return "";
        return heureFin != null ? heureDebut + " – " + heureFin : heureDebut;
    }

    // ── Getters / Setters ────────────────────────────────────────
    public Long   getId()                  { return id; }
    public void   setId(Long id)           { this.id = id; }
    public String getDate()                { return date; }
    public void   setDate(String v)        { date = v; }
    public String getHeureDebut()          { return heureDebut; }
    public void   setHeureDebut(String v)  { heureDebut = v; }
    public String getHeureFin()            { return heureFin; }
    public void   setHeureFin(String v)    { heureFin = v; }
    public String getSalle()               { return salle; }
    public void   setSalle(String v)       { salle = v; }
    public String getLienVisio()           { return lienVisio; }
    public void   setLienVisio(String v)   { lienVisio = v; }
    public String getNotes()               { return notes; }
    public void   setNotes(String v)       { notes = v; }
    public ModuleFront getModule()         { return module; }
    public void        setModule(ModuleFront m){ module = m; }
}
