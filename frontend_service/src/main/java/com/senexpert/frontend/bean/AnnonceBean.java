package com.senexpert.frontend.bean;

import com.senexpert.frontend.client.UserServiceClient;
import com.senexpert.frontend.model.Annonce;
import com.senexpert.frontend.model.Institution;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Named
@ApplicationScoped
public class AnnonceBean implements Serializable {

    @Inject private SessionBean sessionBean;
    @Inject private UserServiceClient userClient;

    private List<Annonce> annonces = new ArrayList<>();

    // Formulaire
    private String titre;
    private String contenu;
    private String type = "INFO";
    private String cibleRole = "TOUS";
    private Long cibleInstitutionId;

    private List<Institution> institutions;

    public void creer() {
        if (titre == null || titre.isBlank() || contenu == null || contenu.isBlank()) {
            msg(false, "Titre et contenu obligatoires.");
            return;
        }
        String nomInstitution = null;
        if (cibleInstitutionId != null && institutions != null) {
            nomInstitution = institutions.stream()
                    .filter(i -> cibleInstitutionId.equals(i.getId()))
                    .map(Institution::getNom).findFirst().orElse(null);
        }
        Annonce a = new Annonce(titre, contenu, type, sessionBean.getEmail(),
                cibleRole, cibleInstitutionId, nomInstitution);
        annonces.add(0, a);
        AuditBean.log("CREATION", "ANNONCE",
                "Titre: " + titre + " | Cible: " + cibleRole
                + (cibleInstitutionId != null ? " | Institution: " + cibleInstitutionId : ""),
                sessionBean.getEmail(), "INFO");
        titre = null; contenu = null; type = "INFO"; cibleRole = "TOUS"; cibleInstitutionId = null;
        msg(true, "Annonce publiee.");
    }

    public void supprimer(Long id) {
        annonces.removeIf(a -> a.getId().equals(id));
        msg(true, "Annonce supprimee.");
        AuditBean.log("SUPPRESSION", "ANNONCE", "ID : " + id, sessionBean.getEmail(), "WARNING");
    }

    public void toggleActive(Long id) {
        annonces.stream().filter(a -> a.getId().equals(id))
                .findFirst().ifPresent(a -> a.setActive(!a.isActive()));
    }

    public List<Annonce> getAnnonces() { return annonces; }

    /** Annonces filtrées pour l'utilisateur connecté (role + institution). */
    public List<Annonce> getAnnoncesActives() {
        String role = sessionBean.getRole();
        Long institutionId = sessionBean.getInstitutionId();
        List<Annonce> visibles = new ArrayList<>();
        for (Annonce a : annonces) {
            if (a.estVisiblePour(role, institutionId)) visibles.add(a);
        }
        return visibles;
    }

    public List<Institution> getInstitutions() {
        if (institutions == null && sessionBean.getToken() != null) {
            institutions = userClient.listerInstitutions(sessionBean.getToken());
        }
        return institutions != null ? institutions : List.of();
    }

    public int getCount() { return annonces.size(); }

    private void msg(boolean ok, String text) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
            ok ? FacesMessage.SEVERITY_INFO : FacesMessage.SEVERITY_ERROR,
            ok ? "Succes" : "Erreur", text));
    }

    public String getTitre() { return titre; }
    public void setTitre(String v) { this.titre = v; }
    public String getContenu() { return contenu; }
    public void setContenu(String v) { this.contenu = v; }
    public String getType() { return type; }
    public void setType(String v) { this.type = v; }
    public String getCibleRole() { return cibleRole; }
    public void setCibleRole(String v) { this.cibleRole = v; }
    public Long getCibleInstitutionId() { return cibleInstitutionId; }
    public void setCibleInstitutionId(Long v) { this.cibleInstitutionId = v; }
}
