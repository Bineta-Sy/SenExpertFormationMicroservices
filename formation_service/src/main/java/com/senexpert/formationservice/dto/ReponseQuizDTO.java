package com.senexpert.formationservice.dto;

/** Réponse d'un apprenant à une question de quiz. */
public class ReponseQuizDTO {

    private Long   questionId;
    private String reponse; // A, B, C ou D

    public ReponseQuizDTO() {}

    public Long   getQuestionId()        { return questionId; }
    public void   setQuestionId(Long v)  { questionId = v; }
    public String getReponse()           { return reponse; }
    public void   setReponse(String v)   { reponse = v; }
}
