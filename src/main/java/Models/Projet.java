package Models;


import java.time.LocalDateTime;

public class Projet {

    private int id_projet;
    private String nom_projet;
    private LocalDateTime date_creation;
    private String statut;
    private String description;


    public Projet() {
    }


    public Projet(int id_projet, String nom_projet,
                  LocalDateTime date_creation,
                  String statut, String description) {
        this.id_projet = id_projet;
        this.nom_projet = nom_projet;
        this.date_creation = date_creation;
        this.statut = statut;
        this.description = description;
    }


    public Projet(String nom_projet,
                  LocalDateTime date_creation,
                  String statut, String description) {
        this.nom_projet = nom_projet;
        this.date_creation = date_creation;
        this.statut = statut;
        this.description = description;
    }


    public int getId_projet() {
        return id_projet;
    }

    public void setId_projet(int id_projet) {
        this.id_projet = id_projet;
    }

    public String getNom_projet() {
        return nom_projet;
    }

    public void setNom_projet(String nom_projet) {
        this.nom_projet = nom_projet;
    }

    public LocalDateTime getDate_creation() {
        return date_creation;
    }

    public void setDate_creation(LocalDateTime date_creation) {
        this.date_creation = date_creation;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Projet{" +
                "id_projet=" + id_projet +
                ", nom_projet='" + nom_projet + '\'' +
                ", date_creation=" + date_creation +
                ", statut='" + statut + '\'' +
                ", description='" + description + '\'' +
                '}' + '\n';
    }

}
