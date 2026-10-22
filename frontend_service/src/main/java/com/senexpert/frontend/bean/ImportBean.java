package com.senexpert.frontend.bean;

import com.senexpert.frontend.client.UserServiceClient;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.Part;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

/**
 * Bean pour l'import en masse d'utilisateurs via CSV.
 * Format attendu : nom;prenom;email;role;motDePasse (entête obligatoire, séparateur ;)
 */
@Named
@ViewScoped
public class ImportBean implements Serializable {

    @Inject private UserServiceClient userClient;
    @Inject private SessionBean session;

    private Part fichierCsv;
    private int nbCrees = -1;
    private int nbTotal = -1;
    private String erreurMessage;

    public void importer() {
        erreurMessage = null;
        nbCrees = -1;
        nbTotal = -1;

        if (fichierCsv == null) {
            erreurMessage = "Veuillez sélectionner un fichier CSV.";
            addMsg(false, erreurMessage);
            return;
        }

        byte[] bytes;
        try (InputStream in = fichierCsv.getInputStream();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buf = new byte[4096];
            int len;
            while ((len = in.read(buf)) != -1) out.write(buf, 0, len);
            bytes = out.toByteArray();
        } catch (IOException e) {
            erreurMessage = "Erreur lecture fichier : " + e.getMessage();
            addMsg(false, erreurMessage);
            return;
        }

        // Compter les lignes (hors entête) pour afficher le total
        String content = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
        String[] lignes = content.split("\\r?\\n");
        nbTotal = Math.max(0, lignes.length - 1); // minus header

        nbCrees = userClient.importerCSV(bytes, session.getToken());
        AuditBean.log("IMPORT_CSV", "UTILISATEUR", nbCrees + " créés / " + nbTotal, session.getEmail(), "INFO");
        addMsg(nbCrees > 0,
            nbCrees + " compte(s) créé(s) sur " + nbTotal + " ligne(s).",
            "Aucun compte créé. Vérifiez le format du fichier.");
    }

    private void addMsg(boolean ok, String msg) {
        addMsg(ok, msg, "Erreur.");
    }

    private void addMsg(boolean ok, String success, String error) {
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(ok ? FacesMessage.SEVERITY_INFO : FacesMessage.SEVERITY_WARN,
                ok ? "Succès" : "Attention", ok ? success : error));
    }

    public Part getFichierCsv() { return fichierCsv; }
    public void setFichierCsv(Part fichierCsv) { this.fichierCsv = fichierCsv; }
    public int getNbCrees() { return nbCrees; }
    public int getNbTotal() { return nbTotal; }
    public String getErreurMessage() { return erreurMessage; }
}
