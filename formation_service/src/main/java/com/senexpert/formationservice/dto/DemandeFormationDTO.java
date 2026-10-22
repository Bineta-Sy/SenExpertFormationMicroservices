package com.senexpert.formationservice.dto;

import lombok.Data;
import java.util.List;

@Data
public class DemandeFormationDTO {
    private Long utilisateurId;
    private Long offreId;
    /** Chemins des fichiers déjà uploadés via POST /api/demandes/upload */
    private List<String> documents;
	public Long getUtilisateurId() {
		return utilisateurId;
	}
	public void setUtilisateurId(Long utilisateurId) {
		this.utilisateurId = utilisateurId;
	}
	public Long getOffreId() {
		return offreId;
	}
	public void setOffreId(Long offreId) {
		this.offreId = offreId;
	}
	public List<String> getDocuments() {
		return documents;
	}
	public void setDocuments(List<String> documents) {
		this.documents = documents;
	}
    
    
}
