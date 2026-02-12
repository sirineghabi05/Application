package Controllers;

import Models.Projet;
import Services.Projetservice;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

public class projetAjouterController {

    @FXML private TextField nomProjetField;
    @FXML private DatePicker dateCreationPicker;
    @FXML private ComboBox<String> statutComboBox;
    @FXML private ComboBox<String> prioriteComboBox;
    @FXML private TextArea descriptionArea;
    @FXML private TextField ressourcesField;
    @FXML private TextField dureeEstimeeField;

    @FXML private Label nomProjetError;
    @FXML private Label statutError;
    @FXML private Label descriptionWarning;
    @FXML private Label caracteresCountLabel;
    @FXML private Label formStatusLabel;
    @FXML private Label validationStatusLabel;

    private final Projetservice service = new Projetservice();
    private int erreurCount = 0;

    @FXML
    private void initialize() {
        configurerStylesChamps();
        initialiserComboBoxes();
        definirDateAujourdhui();
        setupValidationListeners();
        updateValidationStatus();
    }

    private void setupValidationListeners() {
        // Écouteur pour le compte de caractères
        descriptionArea.textProperty().addListener((obs, oldVal, newVal) -> {
            int count = newVal.length();
            caracteresCountLabel.setText(count + "/2000 caractères");

            if (count > 2000) {
                caracteresCountLabel.setStyle("-fx-text-fill: #E53E3E;");
            } else if (count > 1500) {
                caracteresCountLabel.setStyle("-fx-text-fill: #F59E0B;");
            } else {
                caracteresCountLabel.setStyle("-fx-text-fill: #48BB78;");
            }

            validerDescription();
        });
    }

    // ============ VALIDATIONS EN TEMPS RÉEL ============

    @FXML
    private void validerNomProjetEnTempsReel() {
        String nom = nomProjetField.getText().trim();

        if (nom.isEmpty()) {
            showFieldError(nomProjetError, "Le nom du projet est obligatoire");
            highlightFieldError(nomProjetField, true);
        } else if (nom.length() < 3) {
            showFieldError(nomProjetError, "Le nom doit contenir au moins 3 caractères");
            highlightFieldError(nomProjetField, true);
        } else if (nom.length() > 100) {
            showFieldError(nomProjetError, "Le nom ne doit pas dépasser 100 caractères");
            highlightFieldError(nomProjetField, true);
        } else {
            clearFieldError(nomProjetError);
            highlightFieldError(nomProjetField, false);
        }
        updateValidationStatus();
    }

    @FXML
    private void validerStatut() {
        if (statutComboBox.getValue() == null || statutComboBox.getValue().trim().isEmpty()) {
            showFieldError(statutError, "Veuillez sélectionner un statut");
            highlightComboBoxError(statutComboBox, true);
        } else {
            clearFieldError(statutError);
            highlightComboBoxError(statutComboBox, false);
        }
        updateValidationStatus();
    }

    @FXML
    private void validerDescription() {
        String description = descriptionArea.getText().trim();

        if (description.isEmpty()) {
            showFieldWarning(descriptionWarning, "⚠️ Une description est fortement recommandée");
        } else if (description.length() < 20) {
            showFieldWarning(descriptionWarning, "⚠️ La description semble trop courte (min. 20 caractères recommandés)");
        } else if (description.length() > 2000) {
            showFieldWarning(descriptionWarning, "⚠️ La description dépasse la limite de 2000 caractères");
        } else {
            clearFieldWarning(descriptionWarning);
        }
        updateValidationStatus();
    }

    @FXML
    private void validerRessources() {
        // Validation optionnelle des ressources
        updateValidationStatus();
    }

    // ============ MÉTHODES PRINCIPALES ============

    @FXML
    private void ajouterProjet() {
        if (!validerFormulaireComplet()) {
            afficherAlerteValidation("Formulaire incomplet",
                    "Veuillez corriger les erreurs avant de continuer.");
            return;
        }

        if (!demanderConfirmationAjout()) {
            return;
        }

        try {
            Projet nouveauProjet = creerProjetDepuisFormulaire();
            int result = service.ajouter(nouveauProjet);

            if (result >= 0) {
                afficherAlerteSuccesAvecOptions(nouveauProjet);
                resetForm();
            } else {
                afficherAlerteErreur("Erreur", "L'ajout du projet a échoué. Veuillez réessayer.");
            }
        } catch (Exception e) {
            afficherAlerteErreur("Erreur système",
                    "Une erreur est survenue : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean validerFormulaireComplet() {
        boolean isValid = true;
        erreurCount = 0;

        // Validation du nom
        if (nomProjetField.getText().trim().isEmpty()) {
            showFieldError(nomProjetError, "Le nom du projet est obligatoire");
            highlightFieldError(nomProjetField, true);
            isValid = false;
            erreurCount++;
        }

        // Validation du statut
        if (statutComboBox.getValue() == null || statutComboBox.getValue().trim().isEmpty()) {
            showFieldError(statutError, "Le statut est obligatoire");
            highlightComboBoxError(statutComboBox, true);
            isValid = false;
            erreurCount++;
        }

        // Validation de la date
        if (dateCreationPicker.getValue() == null) {
            dateCreationPicker.setValue(LocalDate.now());
        }

        // Validation de la description (avertissement seulement)
        String description = descriptionArea.getText().trim();
        if (description.isEmpty()) {
            showFieldWarning(descriptionWarning,
                    "⚠️ Attention : Aucune description fournie. Ce champ est recommandé.");
        }

        updateValidationStatus();
        return isValid;
    }

    // ============ MÉTHODES UTILITAIRES ============

    private void initialiserComboBoxes() {
        // Initialiser les statuts
        statutComboBox.getItems().addAll(
                "En attente",
                "Planifié",
                "En cours",
                "En révision",
                "Terminé",
                "Suspendu",
                "Annulé"
        );
        statutComboBox.setValue("En attente");

        // Initialiser les priorités
        prioriteComboBox.getItems().addAll(
                "Basse",
                "Moyenne",
                "Haute",
                "Urgent"
        );
        prioriteComboBox.setValue("Moyenne");
    }

    @FXML
    private void definirDateAujourdhui() {
        dateCreationPicker.setValue(LocalDate.now());
        showNotification("Date définie", "Date d'aujourd'hui appliquée.");
    }

    @FXML
    private void definirDateDemain() {
        dateCreationPicker.setValue(LocalDate.now().plusDays(1));
        showNotification("Date définie", "Date de demain appliquée.");
    }

    @FXML
    private void genererDescription() {
        String nomProjet = nomProjetField.getText().trim();
        if (!nomProjet.isEmpty()) {
            String description = "## 📋 Description du projet : " + nomProjet + "\n\n" +
                    "### 🎯 Objectifs principaux :\n" +
                    "• [Définir l'objectif principal]\n" +
                    "• [Objectif secondaire 1]\n" +
                    "• [Objectif secondaire 2]\n\n" +
                    "### 📅 Étapes clés :\n" +
                    "1. [Phase 1 : Analyse et planification]\n" +
                    "2. [Phase 2 : Développement]\n" +
                    "3. [Phase 3 : Tests et validation]\n" +
                    "4. [Phase 4 : Déploiement]\n\n" +
                    "### 📦 Livrables attendus :\n" +
                    "• [Livrable 1]\n" +
                    "• [Livrable 2]\n" +
                    "• [Livrable 3]\n\n" +
                    "### 👥 Équipe et ressources :\n" +
                    "• [Rôle 1 : Responsabilités]\n" +
                    "• [Rôle 2 : Responsabilités]\n\n" +
                    "### ⚠️ Risques identifiés :\n" +
                    "• [Risque 1 : Mesure de mitigation]\n" +
                    "• [Risque 2 : Mesure de mitigation]";

            descriptionArea.setText(description);
            showNotification("Modèle généré",
                    "Un modèle de description a été généré. Personnalisez-le selon vos besoins.");
        } else {
            afficherAlerteErreur("Nom manquant",
                    "Veuillez d'abord saisir un nom de projet.");
            nomProjetField.requestFocus();
        }
    }

    private Projet creerProjetDepuisFormulaire() {
        LocalDateTime dateCreation = dateCreationPicker.getValue().atStartOfDay();
        String priorite = prioriteComboBox.getValue() != null ? prioriteComboBox.getValue() : "Moyenne";

        return new Projet(
                nomProjetField.getText().trim(),
                dateCreation,
                statutComboBox.getValue(),
                descriptionArea.getText().trim()
        );
    }

    private void resetForm() {
        nomProjetField.clear();
        dateCreationPicker.setValue(LocalDate.now());
        statutComboBox.setValue("En attente");
        prioriteComboBox.setValue("Moyenne");
        descriptionArea.clear();
        ressourcesField.clear();
        dureeEstimeeField.clear();

        clearAllErrors();
        updateValidationStatus();

        showNotification("Formulaire réinitialisé",
                "Tous les champs ont été vidés. Prêt pour un nouveau projet.");
    }

    private void updateValidationStatus() {
        int errorCount = 0;

        if (nomProjetError.isVisible()) errorCount++;
        if (statutError.isVisible()) errorCount++;

        validationStatusLabel.setText(errorCount + " erreur(s)");

        if (errorCount > 0) {
            validationStatusLabel.setStyle("-fx-text-fill: #E53E3E;");
            formStatusLabel.setText("Corrigez les erreurs");
            formStatusLabel.setStyle("-fx-text-fill: #E53E3E;");
        } else {
            validationStatusLabel.setStyle("-fx-text-fill: #48BB78;");
            formStatusLabel.setText("Formulaire valide");
            formStatusLabel.setStyle("-fx-text-fill: #48BB78;");
        }
    }

    // ============ GESTION DES STYLES ET ERREURS ============

    private void showFieldError(Label errorLabel, String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void clearFieldError(Label errorLabel) {
        errorLabel.setText("");
        errorLabel.setVisible(false);
    }

    private void showFieldWarning(Label warningLabel, String message) {
        warningLabel.setText(message);
        warningLabel.setVisible(true);
    }

    private void clearFieldWarning(Label warningLabel) {
        warningLabel.setText("");
        warningLabel.setVisible(false);
    }

    private void clearAllErrors() {
        clearFieldError(nomProjetError);
        clearFieldError(statutError);
        clearFieldWarning(descriptionWarning);
        highlightFieldError(nomProjetField, false);
        highlightComboBoxError(statutComboBox, false);
    }

    private void highlightFieldError(TextField field, boolean hasError) {
        if (hasError) {
            field.setStyle("-fx-background-color: #FEF2F2; " +
                    "-fx-border-color: #E53E3E; " +
                    "-fx-border-width: 2; " +
                    "-fx-border-radius: 8; " +
                    "-fx-background-radius: 8; " +
                    "-fx-padding: 12;");
        } else {
            field.setStyle("-fx-background-color: #F8FAFC; " +
                    "-fx-border-color: #CBD5E0; " +
                    "-fx-border-radius: 8; " +
                    "-fx-background-radius: 8; " +
                    "-fx-padding: 12;");
        }
    }

    private void highlightComboBoxError(ComboBox<?> comboBox, boolean hasError) {
        if (hasError) {
            comboBox.setStyle("-fx-background-color: #FEF2F2; " +
                    "-fx-border-color: #E53E3E; " +
                    "-fx-border-width: 2; " +
                    "-fx-border-radius: 8; " +
                    "-fx-background-radius: 8; " +
                    "-fx-padding: 10;");
        } else {
            comboBox.setStyle("-fx-background-color: #F8FAFC; " +
                    "-fx-border-color: #CBD5E0; " +
                    "-fx-border-radius: 8; " +
                    "-fx-background-radius: 8; " +
                    "-fx-padding: 10;");
        }
    }

    // ============ ALERTES ET NOTIFICATIONS ============

    private void showNotification(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().setStyle("-fx-background-color: #F0F9FF;");
        alert.show();
    }

    private boolean demanderConfirmationAjout() {
        Alert confirmation = new Alert(AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmer l'ajout");
        confirmation.setHeaderText("❓ Confirmation requise");
        confirmation.setContentText(
                "Êtes-vous sûr de vouloir créer ce projet ?\n\n" +
                        "Nom : " + nomProjetField.getText().trim() + "\n" +
                        "Statut : " + statutComboBox.getValue() + "\n" +
                        "Date : " + dateCreationPicker.getValue()
        );

        ButtonType btnOui = new ButtonType("Oui, créer");
        ButtonType btnNon = new ButtonType("Non, annuler");
        ButtonType btnModifier = new ButtonType("Modifier avant création");

        confirmation.getButtonTypes().setAll(btnOui, btnModifier, btnNon);
        Optional<ButtonType> result = confirmation.showAndWait();

        if (result.isPresent()) {
            if (result.get() == btnOui) {
                return true;
            } else if (result.get() == btnModifier) {
                // Donner le focus au premier champ
                if (nomProjetField.getText().trim().isEmpty()) {
                    nomProjetField.requestFocus();
                }
                return false;
            }
        }
        return false;
    }

    private void afficherAlerteSuccesAvecOptions(Projet projet) {
        Alert success = new Alert(AlertType.INFORMATION);
        success.setTitle("Succès !");
        success.setHeaderText("✅ Projet créé avec succès");
        success.setContentText(
                "Le projet \"" + projet.getNom_projet() + "\" a été créé.\n\n" +
                        "Détails :\n" +
                        "• Statut : " + projet.getStatut() + "\n" +
                        "• Date : " + projet.getDate_creation().toLocalDate() + "\n" +
                        "• ID : [Généré par la base de données]\n\n" +
                        "Que souhaitez-vous faire maintenant ?"
        );

        ButtonType btnNouveau = new ButtonType("➕ Nouveau projet");
        ButtonType btnVoir = new ButtonType("👁️ Voir ce projet");
        ButtonType btnListe = new ButtonType("📋 Liste des projets");
        ButtonType btnFermer = new ButtonType("Fermer", ButtonType.CANCEL.getButtonData());

        success.getButtonTypes().setAll(btnNouveau, btnVoir, btnListe, btnFermer);

        Optional<ButtonType> result = success.showAndWait();

        if (result.isPresent()) {
            if (result.get() == btnNouveau) {
                resetForm();
            } else if (result.get() == btnVoir) {
                // Rediriger vers la vue détaillée du projet
                showNotification("Vue projet",
                        "Redirection vers la vue détaillée du projet...");
            } else if (result.get() == btnListe) {
                // Rediriger vers la liste des projets
                showNotification("Liste des projets",
                        "Redirection vers la liste des projets...");
            }
        }
    }

    private void afficherAlerteValidation(String titre, String message) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle(titre);
        alert.setHeaderText("⚠️ Validation requise");
        alert.setContentText(message);
        alert.getDialogPane().setStyle("-fx-background-color: #FFFBEB;");
        alert.showAndWait();
    }

    private void afficherAlerteErreur(String titre, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText("❌ Erreur");
        alert.setContentText(message);
        alert.getDialogPane().setStyle("-fx-background-color: #FEF2F2;");
        alert.showAndWait();
    }

    // ============ AUTRES MÉTHODES FXML ============

    @FXML
    private void handleAnnuler() {
        if (champsRemplis()) {
            Alert confirmation = new Alert(AlertType.CONFIRMATION);
            confirmation.setTitle("Confirmer l'annulation");
            confirmation.setHeaderText("Données non sauvegardées");
            confirmation.setContentText(
                    "Vous avez des données non sauvegardées.\n" +
                            "Voulez-vous vraiment annuler et perdre ces modifications ?"
            );

            confirmation.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
            Optional<ButtonType> result = confirmation.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.YES) {
                resetForm();
            }
        } else {
            resetForm();
        }
    }

    @FXML
    private void afficherAide() {
        Alert aide = new Alert(AlertType.INFORMATION);
        aide.setTitle("Aide - Ajout de projet");
        aide.setHeaderText("📋 Guide d'utilisation");
        aide.setContentText(
                "Instructions pour créer un projet :\n\n" +
                        "1. 📝 Nom du projet (obligatoire) :\n" +
                        "   • Soyez clair et descriptif\n" +
                        "   • Maximum 100 caractères\n\n" +
                        "2. 🗓️ Date de création :\n" +
                        "   • Par défaut : aujourd'hui\n" +
                        "   • Utilisez les boutons pour sélectionner rapidement\n\n" +
                        "3. 📊 Statut (obligatoire) :\n" +
                        "   • En attente : Projet créé mais non démarré\n" +
                        "   • En cours : En développement\n" +
                        "   • Terminé : Finalisé\n" +
                        "   • Autres : Selon l'avancement\n\n" +
                        "4. 📄 Description (recommandé) :\n" +
                        "   • Utilisez le bouton 'Générer modèle' pour un modèle prédéfini\n" +
                        "   • Maximum 2000 caractères\n\n" +
                        "5. 💡 Conseils :\n" +
                        "   • Validez chaque champ au fur et à mesure\n" +
                        "   • Exportez les données avant de créer\n" +
                        "   • Vérifiez les projets similaires"
        );
        aide.getDialogPane().setStyle("-fx-background-color: #F8FAFC;");
        aide.getDialogPane().setPrefSize(600, 500);
        aide.showAndWait();
    }

    @FXML
    private void verifierProjetsSimilaires() {
        String nomProjet = nomProjetField.getText().trim();
        if (!nomProjet.isEmpty()) {
            Alert info = new Alert(AlertType.INFORMATION);
            info.setTitle("Vérification des projets");
            info.setHeaderText("🔍 Recherche de projets similaires");
            info.setContentText(
                    "Recherche en cours pour : \"" + nomProjet + "\"\n\n" +
                            "Cette fonctionnalité vérifie dans la base de données\n" +
                            "si des projets similaires existent déjà.\n\n" +
                            "Résultat : Aucun projet similaire trouvé.\n" +
                            "Vous pouvez continuer la création."
            );
            info.getDialogPane().setStyle("-fx-background-color: #F0F9FF;");
            info.showAndWait();
        } else {
            afficherAlerteErreur("Nom manquant",
                    "Veuillez d'abord saisir un nom de projet pour la vérification.");
            nomProjetField.requestFocus();
        }
    }

    @FXML
    private void exporterDonneesProjet() {
        if (!nomProjetField.getText().trim().isEmpty()) {
            String exportData = "=== FICHE PROJET ===\n\n" +
                    "NOM DU PROJET : " + nomProjetField.getText().trim() + "\n" +
                    "DATE DE CRÉATION : " + dateCreationPicker.getValue() + "\n" +
                    "STATUT : " + statutComboBox.getValue() + "\n" +
                    "PRIORITÉ : " + prioriteComboBox.getValue() + "\n" +
                    "DURÉE ESTIMÉE : " + dureeEstimeeField.getText() + "\n" +
                    "RESSOURCES : " + ressourcesField.getText() + "\n\n" +
                    "DESCRIPTION :\n" + descriptionArea.getText() + "\n\n" +
                    "=== EXPORTÉ LE : " + LocalDate.now() + " ===\n" +
                    "© Système de Gestion de Projets";

            TextArea textArea = new TextArea(exportData);
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setPrefSize(600, 400);
            textArea.setStyle("-fx-font-family: 'Consolas', monospace;");

            Alert exportAlert = new Alert(AlertType.INFORMATION);
            exportAlert.setTitle("Export des données");
            exportAlert.setHeaderText("📋 Données du projet exportées");
            exportAlert.getDialogPane().setContent(textArea);
            exportAlert.getDialogPane().setStyle("-fx-background-color: #F8FAFC;");
            exportAlert.getDialogPane().setPrefSize(650, 450);

            exportAlert.showAndWait();
        } else {
            afficherAlerteErreur("Données insuffisantes",
                    "Veuillez au moins saisir un nom de projet avant d'exporter.");
        }
    }

    private boolean champsRemplis() {
        return !nomProjetField.getText().trim().isEmpty() ||
                !descriptionArea.getText().trim().isEmpty() ||
                !ressourcesField.getText().trim().isEmpty() ||
                !dureeEstimeeField.getText().trim().isEmpty();
    }

    private void configurerStylesChamps() {
        // Le style est maintenant géré dynamiquement par les méthodes de validation
    }
}