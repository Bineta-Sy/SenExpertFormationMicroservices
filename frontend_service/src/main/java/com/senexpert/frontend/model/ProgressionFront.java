package com.senexpert.frontend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProgressionFront {

    private Long    id;
    private Long    utilisateurId;
    private Long    coursId;
    private String  coursTitre;
    private String  coursType;
    private boolean complete;
    private String  dateAcces;
    private String  dateCompletion;
    private Integer scoreQuiz;

    public Long    getId()                { return id; }
    public void    setId(Long v)          { id = v; }
    public Long    getUtilisateurId()     { return utilisateurId; }
    public void    setUtilisateurId(Long v){ utilisateurId = v; }
    public Long    getCoursId()           { return coursId; }
    public void    setCoursId(Long v)     { coursId = v; }
    public String  getCoursTitre()        { return coursTitre; }
    public void    setCoursTitre(String v){ coursTitre = v; }
    public String  getCoursType()         { return coursType; }
    public void    setCoursType(String v) { coursType = v; }
    public boolean isComplete()           { return complete; }
    public void    setComplete(boolean v) { complete = v; }
    public String  getDateAcces()         { return dateAcces; }
    public void    setDateAcces(String v) { dateAcces = v; }
    public String  getDateCompletion()    { return dateCompletion; }
    public void    setDateCompletion(String v){ dateCompletion = v; }
    public Integer getScoreQuiz()         { return scoreQuiz; }
    public void    setScoreQuiz(Integer v){ scoreQuiz = v; }

    public String getScoreLabel() {
        return scoreQuiz != null ? scoreQuiz + "%" : "—";
    }
}
