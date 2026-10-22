package com.senexpert.formationservice.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "progression_apprenants",
       uniqueConstraints = @UniqueConstraint(columnNames = {"utilisateur_id", "cours_id"}))
@Data
public class ProgressionApprenant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long utilisateurId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cours_id")
    private CoursEnLigne cours;

    private boolean complete = false;

    private LocalDateTime dateAcces;

    private LocalDateTime dateCompletion;

    /** Score en % (null si le cours n'est pas un QUIZ). */
    private Integer scoreQuiz;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getUtilisateurId() {
		return utilisateurId;
	}

	public void setUtilisateurId(Long utilisateurId) {
		this.utilisateurId = utilisateurId;
	}

	public CoursEnLigne getCours() {
		return cours;
	}

	public void setCours(CoursEnLigne cours) {
		this.cours = cours;
	}

	public boolean isComplete() {
		return complete;
	}

	public void setComplete(boolean complete) {
		this.complete = complete;
	}

	public LocalDateTime getDateAcces() {
		return dateAcces;
	}

	public void setDateAcces(LocalDateTime dateAcces) {
		this.dateAcces = dateAcces;
	}

	public LocalDateTime getDateCompletion() {
		return dateCompletion;
	}

	public void setDateCompletion(LocalDateTime dateCompletion) {
		this.dateCompletion = dateCompletion;
	}

	public Integer getScoreQuiz() {
		return scoreQuiz;
	}

	public void setScoreQuiz(Integer scoreQuiz) {
		this.scoreQuiz = scoreQuiz;
	}
    
    
}
