package Controllers;

import Models.Projet;
import Services.Projetservice;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class projetModifierController {

    @FXML private Label projetIdLabel;
    @FXML private TextField nomProjetField;
    @FXML private DatePicker dateCreationPicker;
    @FXML private ComboBox<String> statutComboBox;
    @FXML private DatePicker dateDebutPicker;
    @FXML private DatePicker dateFinPicker;
    @FXML private TextArea descriptionArea;
    @FXML private Label caracteresCountLabel;
    @FXML private Label nomProjetError;
    @FXML private Label statutError;
    @FXML private Label dateCreationOriginaleLabel;
    @FXML private Label dateModificationLabel;
    @FXML private Label modeEditionLabel;
    @FXML private Label modificationsLabel;

    private Projet projetActuel;
    private String descriptionOriginale;
    private Projetservice projetService;
    private boolean modificationsEffectuees = false;

    public projetModifierController() {
        projetService = new Projetservice();
    }

    @FXML
    public void initialize() {
        // Initialisation des ComboBox
        statutComboBox.setItems(FXCollections.observableArrayList(
                "En cours", "Planifié", "Terminé", "En pause", "Annulé"
        ));

        // Limite de caractères pour la description
        descriptionArea.textProperty().addListener((obs, oldText, newText) -> {
            if (newText.length() > 1000) {
                descriptionArea.setText(oldText);
            }
            caracteresCountLabel.setText(newText.length() + "/1000 caractères");

            if (!newText.equals(descriptionOriginale)) {
                modificationsEffectuees = true;
                modificationsLabel.setText("Non enregistrées");
                modificationsLabel.setStyle("-fx-text-fill: #ED8936;");
            }
        });

        // Écouteurs pour détecter les modifications
        nomProjetField.textProperty().addListener((obs, old, nw) -> marquerModification());
        dateCreationPicker.valueProperty().addListener((obs, old, nw) -> marquerModification());
        statutComboBox.valueProperty().addListener((obs, old, nw) -> marquerModification());
        dateDebutPicker.valueProperty().addListener((obs, old, nw) -> marquerModification());
        dateFinPicker.valueProperty().addListener((obs, old, nw) -> marquerModification());
    }

    /**
     * Initialise les données du projet depuis la liste
     */
    public void initData(Projet projet) {
        this.projetActuel = projet;
        chargerDonneesProjet();
    }

    /**
     * Charge les données du projet dans les champs du formulaire
     */
    private void chargerDonneesProjet() {
        if (projetActuel != null) {
            // ID du projet
            projetIdLabel.setText("ID: #" + projetActuel.getId_projet());

            // Nom du projet
            nomProjetField.setText(projetActuel.getNom_projet());

            // Date de création
            if (projetActuel.getDate_creation() != null) {
                dateCreationPicker.setValue(projetActuel.getDate_creation().toLocalDate());
                dateCreationOriginaleLabel.setText(
                        projetActuel.getDate_creation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                );
            }

            // Statut
            statutComboBox.setValue(projetActuel.getStatut());

            // Description
            if (projetActuel.getDescription() != null) {
                descriptionArea.setText(projetActuel.getDescription());
                descriptionOriginale = projetActuel.getDescription();
            }

            // Date de modification
            dateModificationLabel.setText(
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
            );

            modificationsEffectuees = false;
            modificationsLabel.setText("À jour");
            modificationsLabel.setStyle("-fx-text-fill: #48BB78;");
        }
    }

    private void marquerModification() {
        if (!modificationsEffectuees) {
            modificationsEffectuees = true;
            modificationsLabel.setText("Non enregistrées");
            modificationsLabel.setStyle("-fx-text-fill: #ED8936;");
        }
    }

    // ============ MÉTHODES DE MODIFICATION DE DATE ============

    @FXML
    private void definirDateAujourdhui() {
        dateCreationPicker.setValue(LocalDate.now());
    }

    @FXML
    private void definirDatePlus7() {
        dateCreationPicker.setValue(LocalDate.now().plusDays(7));
    }

    // ============ MÉTHODES DE GESTION DE LA DESCRIPTION ============

    @FXML
    private void restaurerDescriptionOriginale() {
        if (descriptionOriginale != null) {
            descriptionArea.setText(descriptionOriginale);
        }
    }

    @FXML
    private void genererDescription() {
        String nomProjet = nomProjetField.getText();
        if (nomProjet != null && !nomProjet.isEmpty()) {
            String modeleDescription = "Projet " + nomProjet + " - Objectifs :\n\n" +
                    "1. \n2. \n3. \n\n" +
                    "Livrables : \n\n" +
                    "Échéances : ";
            descriptionArea.setText(modeleDescription);
        }
    }

    // ============ MÉTHODES DE VISUALISATION ============

    @FXML
    private void afficherHistorique() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Historique du projet");
        alert.setHeaderText("📜 Modifications précédentes");
        alert.setContentText("Fonctionnalité d'historique à implémenter.");
        alert.showAndWait();
    }

    @FXML
    private void dupliquerProjet() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Dupliquer le projet");
        alert.setHeaderText("Créer une copie du projet ?");
        alert.setContentText("Voulez-vous créer une copie de ce projet ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                Projet nouveauProjet = new Projet(
                        nomProjetField.getText() + " (copie)",
                        LocalDateTime.now(),
                        "Planifié",
                        descriptionArea.getText()
                );

                projetService.ajouter(nouveauProjet);
                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("Succès");
                success.setHeaderText("📋 Projet dupliqué");
                success.setContentText("Une copie du projet a été créée avec succès.");
                success.showAndWait();
            }
        });
    }

    @FXML
    private void afficherApercu() {
        String apercu = "=== APERÇU DU PROJET ===\n\n" +
                "ID: " + projetActuel.getId_projet() + "\n" +
                "Nom: " + nomProjetField.getText() + "\n" +
                "Statut: " + statutComboBox.getValue() + "\n" +
                "Date création: " + dateCreationPicker.getValue() + "\n" +
                "Description: " + descriptionArea.getText();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Aperçu du projet");
        alert.setHeaderText("👁️ Détails du projet");

        TextArea textArea = new TextArea(apercu);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefSize(500, 300);

        alert.getDialogPane().setContent(textArea);
        alert.showAndWait();
    }

    // ============ MÉTHODES DE VALIDATION ET VÉRIFICATION ============

    @FXML
    private void verifierProjet() {
        boolean isValid = validateInputs();
        if (isValid) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Vérification réussie");
            alert.setHeaderText("✔️ Vérification du projet");
            alert.setContentText("Toutes les informations sont valides.");
            alert.showAndWait();
        }
    }

    private boolean validateInputs() {
        boolean isValid = true;

        // Validation du nom
        String nom = nomProjetField.getText();
        if (nom == null || nom.trim().isEmpty()) {
            nomProjetError.setText("Le nom du projet est obligatoire");
            nomProjetError.setVisible(true);
            nomProjetField.setStyle("-fx-border-color: #E53E3E;");
            isValid = false;
        } else if (nom.length() > 100) {
            nomProjetError.setText("Le nom ne doit pas dépasser 100 caractères");
            nomProjetError.setVisible(true);
            nomProjetField.setStyle("-fx-border-color: #E53E3E;");
            isValid = false;
        } else {
            nomProjetError.setVisible(false);
            nomProjetField.setStyle("-fx-border-color: #CBD5E0;");
        }

        // Validation du statut
        if (statutComboBox.getValue() == null || statutComboBox.getValue().isEmpty()) {
            statutError.setText("Le statut est obligatoire");
            statutError.setVisible(true);
            statutComboBox.setStyle("-fx-border-color: #E53E3E;");
            isValid = false;
        } else {
            statutError.setVisible(false);
            statutComboBox.setStyle("-fx-border-color: #CBD5E0;");
        }

        return isValid;
    }

    // ============ MÉTHODES DE MODIFICATION ET ENREGISTREMENT ============

    @FXML
    private void modifierProjet() {
        if (!validateInputs()) {
            return;
        }

        // Mise à jour des données du projet
        projetActuel.setNom_projet(nomProjetField.getText().trim());

        if (dateCreationPicker.getValue() != null) {
            projetActuel.setDate_creation(dateCreationPicker.getValue().atStartOfDay());
        }

        projetActuel.setStatut(statutComboBox.getValue());
        projetActuel.setDescription(descriptionArea.getText().trim());

        // Appel au service pour modifier
        projetService.modifier(projetActuel);

        // Confirmation
        Alert success = new Alert(Alert.AlertType.INFORMATION);
        success.setTitle("Succès");
        success.setHeaderText("✅ Projet modifié");
        success.setContentText("Le projet '" + projetActuel.getNom_projet() + "' a été modifié avec succès !");
        success.showAndWait();

        modificationsEffectuees = false;
        modificationsLabel.setText("Enregistrées");
        modificationsLabel.setStyle("-fx-text-fill: #48BB78;");
        dateModificationLabel.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

        fermerFenetre();
    }

    @FXML
    private void reinitialiserFormulaire() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Réinitialisation");
        alert.setHeaderText("Réinitialiser le formulaire");
        alert.setContentText("Voulez-vous annuler toutes vos modifications ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                chargerDonneesProjet();
            }
        });
    }

    // ============ MÉTHODES DE NAVIGATION ============

    @FXML
    private void handleAnnuler() {
        if (modificationsEffectuees) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText("Modifications non enregistrées");
            alert.setContentText("Voulez-vous vraiment quitter sans enregistrer ?");

            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    fermerFenetre();
                }
            });
        } else {
            fermerFenetre();
        }
    }

    private void fermerFenetre() {
        Stage stage = (Stage) nomProjetField.getScene().getWindow();
        stage.close();
    }
}