package com.senexpert.formationservice.dto;

public class QuizQuestionDTO {

    private Long   id;
    private String question;
    private String optionA, optionB, optionC, optionD;
    private String bonneReponse;
    private Integer points;
    private Integer ordre;
    private Long   coursId;

    public QuizQuestionDTO() {}

    public Long   getId()              { return id; }
    public void   setId(Long v)        { id = v; }
    public String getQuestion()        { return question; }
    public void   setQuestion(String v){ question = v; }
    public String getOptionA()         { return optionA; }
    public void   setOptionA(String v) { optionA = v; }
    public String getOptionB()         { return optionB; }
    public void   setOptionB(String v) { optionB = v; }
    public String getOptionC()         { return optionC; }
    public void   setOptionC(String v) { optionC = v; }
    public String getOptionD()         { return optionD; }
    public void   setOptionD(String v) { optionD = v; }
    public String getBonneReponse()    { return bonneReponse; }
    public void   setBonneReponse(String v){ bonneReponse = v; }
    public Integer getPoints()         { return points; }
    public void   setPoints(Integer v) { points = v; }
    public Integer getOrdre()          { return ordre; }
    public void   setOrdre(Integer v)  { ordre = v; }
    public Long   getCoursId()         { return coursId; }
    public void   setCoursId(Long v)   { coursId = v; }
}
