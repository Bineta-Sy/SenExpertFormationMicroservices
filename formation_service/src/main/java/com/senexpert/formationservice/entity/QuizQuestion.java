package com.senexpert.formationservice.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "quiz_questions")
@Data
public class QuizQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 1000)
    private String question;

    @Column(length = 500)
    private String optionA;
    @Column(length = 500)
    private String optionB;
    @Column(length = 500)
    private String optionC;
    @Column(length = 500)
    private String optionD;

    /** Bonne réponse : A, B, C ou D */
    private String bonneReponse;

    private Integer points = 1;

    private Integer ordre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cours_id")
 
    private CoursEnLigne cours;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public String getOptionA() {
		return optionA;
	}

	public void setOptionA(String optionA) {
		this.optionA = optionA;
	}

	public String getOptionB() {
		return optionB;
	}

	public void setOptionB(String optionB) {
		this.optionB = optionB;
	}

	public String getOptionC() {
		return optionC;
	}

	public void setOptionC(String optionC) {
		this.optionC = optionC;
	}

	public String getOptionD() {
		return optionD;
	}

	public void setOptionD(String optionD) {
		this.optionD = optionD;
	}

	public String getBonneReponse() {
		return bonneReponse;
	}

	public void setBonneReponse(String bonneReponse) {
		this.bonneReponse = bonneReponse;
	}

	public Integer getPoints() {
		return points;
	}

	public void setPoints(Integer points) {
		this.points = points;
	}

	public Integer getOrdre() {
		return ordre;
	}

	public void setOrdre(Integer ordre) {
		this.ordre = ordre;
	}

	public CoursEnLigne getCours() {
		return cours;
	}

	public void setCours(CoursEnLigne cours) {
		this.cours = cours;
	}
    
}
