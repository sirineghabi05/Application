package Controllers;

import Models.Projet;
import Services.Projetservice;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.stage.Modality;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ProjetListController {

    @FXML private ListView<Projet> projetListView;
    @FXML private TextField searchField;
    @FXML private Label totalProjetsLabel;
    @FXML private Label activeSearchLabel;
    @FXML private Label lastCreationLabel;
    @FXML private ComboBox<String> statutFilterComboBox;

    private final Projetservice service = new Projetservice();
    private ObservableList<Projet> projetList;
    private ObservableList<Projet> filteredList;

    @FXML
    private void initialize() {
        initializeStyles();
        setupStatutFilter();
        loadProjets();
        setupSearchFunctionality();
        updateStatistics();
    }

    private void initializeStyles() {
        // Style pour la barre de recherche
        searchField.setStyle("-fx-background-color: #F8FAFC; " +
                "-fx-border-color: #CBD5E0; " +
                "-fx-border-radius: 8; " +
                "-fx-background-radius: 8; " +
                "-fx-padding: 10 15; " +
                "-fx-font-size: 14px; " +
                "-fx-pref-width: 300px;");

        searchField.setPromptText("Rechercher un projet...");

        // Styles pour les labels de statistiques
        String statLabelStyle = "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2D3748;";
        totalProjetsLabel.setStyle(statLabelStyle);
        activeSearchLabel.setStyle(statLabelStyle);
        lastCreationLabel.setStyle(statLabelStyle);
    }

    private void setupStatutFilter() {
        statutFilterComboBox.setItems(FXCollections.observableArrayList(
                "Tous les statuts",
                "En cours",
                "Terminé",
                "En pause",
                "Annulé"
        ));
        statutFilterComboBox.setValue("Tous les statuts");
        statutFilterComboBox.setStyle("-fx-background-color: #F8FAFC; " +
                "-fx-border-color: #CBD5E0; " +
                "-fx-border-radius: 8; " +
                "-fx-background-radius: 8; " +
                "-fx-padding: 5 10; " +
                "-fx-font-size: 14px;");

        statutFilterComboBox.setOnAction(e -> filterProjets());
    }

    private void loadProjets() {
        try {
            List<Projet> projets = service.recuperer();
            projetList = FXCollections.observableArrayList(projets);
            filteredList = FXCollections.observableArrayList(projets);

            // Configurer la ListView avec un custom cell factory
            projetListView.setItems(filteredList);
            projetListView.setCellFactory(param -> new ListCell<Projet>() {
                @Override
                protected void updateItem(Projet projet, boolean empty) {
                    super.updateItem(projet, empty);

                    if (empty || projet == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setGraphic(createProjetCard(projet));
                    }
                }
            });

            projetListView.setStyle("-fx-background-color: transparent; -fx-padding: 10;");

        } catch (Exception e) {
            showErrorAlert("Erreur de chargement", "Impossible de charger les projets : " + e.getMessage());
            projetList = FXCollections.observableArrayList();
            filteredList = FXCollections.observableArrayList();
            projetListView.setItems(filteredList);
        }
    }

    private VBox createProjetCard(Projet projet) {
        VBox card = new VBox();
        card.setStyle("-fx-background-color: #FFFFFF; " +
                "-fx-border-color: #E2E8F0; " +
                "-fx-border-radius: 12; " +
                "-fx-background-radius: 12; " +
                "-fx-padding: 20; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 2);");
        card.setSpacing(12);
        card.setPrefWidth(600);

        // En-tête avec nom et statut
        HBox headerBox = new HBox(15);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(projet.getNom_projet());
        nameLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        nameLabel.setStyle("-fx-text-fill: #2D3748;");

        Label statutLabel = new Label(projet.getStatut());
        statutLabel.setStyle(getStatutStyle(projet.getStatut()));
        statutLabel.setPadding(new javafx.geometry.Insets(5, 12, 5, 12));

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        headerBox.getChildren().addAll(nameLabel, spacer, statutLabel);

        // Date de création
        Label dateLabel = new Label("Créé le " + formatDateTime(projet.getDate_creation()));
        dateLabel.setFont(Font.font("Segoe UI", 13));
        dateLabel.setStyle("-fx-text-fill: #718096;");

        // Description
        VBox descriptionBox = createInfoBox("DESCRIPTION", projet.getDescription(), "description");

        // ID du projet (affiché en petit)
        Label idLabel = new Label("ID: " + projet.getId_projet());
        idLabel.setFont(Font.font("Segoe UI", 11));
        idLabel.setStyle("-fx-text-fill: #A0AEC0;");

        // Boutons d'action
        HBox buttonBox = createActionButtons(projet);

        card.getChildren().addAll(headerBox, dateLabel, descriptionBox, idLabel, buttonBox);
        return card;
    }

    private String getStatutStyle(String statut) {
        switch (statut != null ? statut.toLowerCase() : "") {
            case "en cours":
                return "-fx-background-color: #FEF3C7; -fx-text-fill: #92400E; -fx-background-radius: 20; -fx-font-weight: bold; -fx-font-size: 12px;";
            case "terminé":
                return "-fx-background-color: #DEF7EC; -fx-text-fill: #03543F; -fx-background-radius: 20; -fx-font-weight: bold; -fx-font-size: 12px;";
            case "en pause":
                return "-fx-background-color: #EDF2F7; -fx-text-fill: #1A202C; -fx-background-radius: 20; -fx-font-weight: bold; -fx-font-size: 12px;";
            case "annulé":
                return "-fx-background-color: #FEE2E2; -fx-text-fill: #991B1B; -fx-background-radius: 20; -fx-font-weight: bold; -fx-font-size: 12px;";
            default:
                return "-fx-background-color: #E2E8F0; -fx-text-fill: #4A5568; -fx-background-radius: 20; -fx-font-weight: bold; -fx-font-size: 12px;";
        }
    }

    private VBox createInfoBox(String title, String content, String iconType) {
        VBox infoBox = new VBox(5);

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        titleLabel.setStyle("-fx-text-fill: #4A5568;");

        Label contentLabel = new Label(content != null ? content : "Aucune description");
        contentLabel.setFont(Font.font("Segoe UI", 14));
        contentLabel.setStyle("-fx-text-fill: #2D3748;");
        contentLabel.setWrapText(true);

        // Ajouter une icône selon le type
        String icon = getIconForType(iconType);
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-text-fill: #4A6FA5; -fx-font-size: 14px;");

        HBox contentWithIcon = new HBox(10, iconLabel, contentLabel);
        contentWithIcon.setAlignment(Pos.TOP_LEFT);

        infoBox.getChildren().addAll(titleLabel, contentWithIcon);
        return infoBox;
    }

    private String getIconForType(String type) {
        switch (type) {
            case "description": return "📋";
            default: return "•";
        }
    }

    private HBox createActionButtons(Projet projet) {
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button detailsButton = new Button("👁️ Détails");
        detailsButton.setStyle("-fx-background-color: #4A90E2; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 16; -fx-cursor: hand;");
        detailsButton.setOnAction(e -> handleShowDetails(projet));

        Button editButton = new Button("✏️ Modifier");
        editButton.setStyle("-fx-background-color: #ED8936; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 16; -fx-cursor: hand;");
        editButton.setOnAction(e -> handleEditProjet(projet));

        Button deleteButton = new Button("🗑️ Supprimer");
        deleteButton.setStyle("-fx-background-color: #E53E3E; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 16; -fx-cursor: hand;");
        deleteButton.setOnAction(e -> handleDeleteProjet(projet));

        buttonBox.getChildren().addAll(detailsButton, editButton, deleteButton);
        return buttonBox;
    }

    private void setupSearchFunctionality() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterProjets();
        });
    }

    private void filterProjets() {
        filteredList.clear();
        String searchText = searchField.getText().toLowerCase();
        String selectedStatut = statutFilterComboBox.getValue();

        for (Projet projet : projetList) {
            boolean matchesSearch = searchText.isEmpty() ||
                    projet.getNom_projet().toLowerCase().contains(searchText) ||
                    (projet.getDescription() != null && projet.getDescription().toLowerCase().contains(searchText)) ||
                    String.valueOf(projet.getId_projet()).contains(searchText);

            boolean matchesStatut = selectedStatut.equals("Tous les statuts") ||
                    (projet.getStatut() != null && projet.getStatut().equals(selectedStatut));

            if (matchesSearch && matchesStatut) {
                filteredList.add(projet);
            }
        }

        updateStatistics();
    }

    private void updateStatistics() {
        totalProjetsLabel.setText(String.valueOf(projetList.size()));
        activeSearchLabel.setText(String.valueOf(filteredList.size()));

        if (!projetList.isEmpty()) {
            String lastDate = findLastCreationDate();
            lastCreationLabel.setText(lastDate);
        } else {
            lastCreationLabel.setText("Aucun");
        }
    }

    private String findLastCreationDate() {
        LocalDateTime latestDate = null;

        for (Projet projet : projetList) {
            LocalDateTime date = projet.getDate_creation();
            if (latestDate == null || date.isAfter(latestDate)) {
                latestDate = date;
            }
        }

        if (latestDate != null) {
            DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            return latestDate.format(displayFormatter);
        }

        return "N/A";
    }

    private String formatDateTime(LocalDateTime dateTime) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm");
            return dateTime.format(formatter);
        } catch (Exception e) {
            return dateTime != null ? dateTime.toString() : "Date inconnue";
        }
    }

    @FXML
    private void handleEditProjet(Projet projet) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/modifier_projet.fxml"));
            Parent root = loader.load();

            projetModifierController modifierController = loader.getController();
            modifierController.initData(projet);

            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setTitle("Modifier Projet - " + projet.getNom_projet());
            stage.setScene(scene);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(projetListView.getScene().getWindow());
            stage.setMinWidth(700);
            stage.setMinHeight(600);
            stage.showAndWait();

            loadProjets();
            showSuccessAlert("Modification terminée",
                    "Le projet a été modifié avec succès.");

        } catch (Exception e) {
            showErrorAlert("Erreur d'ouverture",
                    "Impossible d'ouvrir l'interface de modification : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDeleteProjet(Projet projet) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmer la suppression");
        confirmation.setHeaderText("Supprimer le projet");
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer le projet \"" +
                projet.getNom_projet() + "\" ?\n\n" +
                "Cette action est irréversible !");

        confirmation.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    service.supprimer(projet);
                    projetList.remove(projet);
                    filteredList.remove(projet);
                    updateStatistics();

                    showSuccessAlert("Suppression réussie",
                            "Le projet a été supprimé avec succès.");
                } catch (Exception e) {
                    showErrorAlert("Erreur de suppression",
                            "Impossible de supprimer le projet : " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleClearSearch() {
        searchField.clear();
        statutFilterComboBox.setValue("Tous les statuts");
        filterProjets();
        showInfoAlert("Recherche effacée", "Les filtres ont été réinitialisés.");
    }

    @FXML
    private void handleExport() {
        try {
            StringBuilder csvData = new StringBuilder();
            csvData.append("ID,Nom,Date création,Statut,Description\n");

            for (Projet projet : filteredList) {
                csvData.append(projet.getId_projet()).append(",")
                        .append(escapeCSV(projet.getNom_projet())).append(",")
                        .append(projet.getDate_creation() != null ? projet.getDate_creation().toString() : "").append(",")
                        .append(escapeCSV(projet.getStatut() != null ? projet.getStatut() : "")).append(",")
                        .append(escapeCSV(projet.getDescription() != null ? projet.getDescription() : "")).append("\n");
            }

            TextArea textArea = new TextArea(csvData.toString());
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 12px;");

            Alert exportAlert = new Alert(Alert.AlertType.INFORMATION);
            exportAlert.setTitle("Exportation des données");
            exportAlert.setHeaderText("✅ Données exportées au format CSV");
            exportAlert.setContentText("Nombre de projets exportés : " + filteredList.size() + "\n\nCopiez les données ci-dessous :");
            exportAlert.getDialogPane().setExpandableContent(textArea);
            exportAlert.getDialogPane().setExpanded(true);
            exportAlert.getDialogPane().setStyle("-fx-background-color: #F0F9FF;");
            exportAlert.showAndWait();

        } catch (Exception e) {
            showErrorAlert("Erreur d'exportation", "Impossible d'exporter les données : " + e.getMessage());
        }
    }

    @FXML
    private void handleShowStatistics() {
        Alert statsAlert = new Alert(Alert.AlertType.INFORMATION);
        statsAlert.setTitle("Statistiques des projets");
        statsAlert.setHeaderText("📊 Statistiques détaillées");

        // Calculer les statistiques par statut
        int enCours = 0, termine = 0, enPause = 0, annule = 0, autres = 0;

        for (Projet p : projetList) {
            String statut = p.getStatut() != null ? p.getStatut().toLowerCase() : "";
            switch (statut) {
                case "en cours": enCours++; break;
                case "terminé": termine++; break;
                case "en pause": enPause++; break;
                case "annulé": annule++; break;
                default: autres++; break;
            }
        }

        String statsText = String.format("""
            📈 STATISTIQUES DES PROJETS
            ──────────────────────────────
            
            📊 Répartition par statut :
            
            • En cours    : %d projets (%.1f%%)
            • Terminé     : %d projets (%.1f%%)
            • En pause    : %d projets (%.1f%%)
            • Annulé      : %d projets (%.1f%%)
            • Autres      : %d projets (%.1f%%)
            
            📋 Total projets      : %d
            🔍 Projets affichés   : %d
            📅 Dernier projet     : %s
            """,
                enCours, pourcentage(enCours, projetList.size()),
                termine, pourcentage(termine, projetList.size()),
                enPause, pourcentage(enPause, projetList.size()),
                annule, pourcentage(annule, projetList.size()),
                autres, pourcentage(autres, projetList.size()),
                projetList.size(),
                filteredList.size(),
                findLastCreationDate()
        );

        statsAlert.setContentText(statsText);
        statsAlert.getDialogPane().setStyle("-fx-background-color: #F8FAFC;");
        statsAlert.showAndWait();
    }

    private double pourcentage(int valeur, int total) {
        return total > 0 ? (valeur * 100.0 / total) : 0;
    }

    private String escapeCSV(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    // ============ MÉTHODES D'ALERTE ============

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText("❌ Erreur");
        alert.setContentText(message);
        alert.getDialogPane().setStyle("-fx-background-color: #FEF2F2;");
        alert.showAndWait();
    }

    private void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText("✅ Succès");
        alert.setContentText(message);
        alert.getDialogPane().setStyle("-fx-background-color: #F0F9FF;");
        alert.showAndWait();
    }

    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText("ℹ️ Information");
        alert.setContentText(message);
        alert.getDialogPane().setStyle("-fx-background-color: #F8FAFC;");
        alert.showAndWait();
    }

    @FXML
    private void handleShowDetails(Projet projet) {
        if (projet != null) {
            String details = "ID: " + projet.getId_projet() + "\n" +
                    "Nom: " + projet.getNom_projet() + "\n" +
                    "Statut: " + projet.getStatut() + "\n" +
                    "Date création: " + (projet.getDate_creation() != null ? projet.getDate_creation() : "N/A") + "\n" +
                    "Description: " + (projet.getDescription() != null ? projet.getDescription() : "Aucune");

            TextArea textArea = new TextArea(details);
            textArea.setEditable(false);
            textArea.setWrapText(true);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Détails du projet");
            alert.setHeaderText("👁️ Détails: " + projet.getNom_projet());
            alert.getDialogPane().setContent(textArea);
            alert.getDialogPane().setStyle("-fx-background-color: #FFFFFF;");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleRefresh() {
        loadProjets();
        updateStatistics();
        showInfoAlert("Rafraîchissement", "✅ Liste des projets rafraîchie.");
    }

    @FXML
    private void handleAddNew() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ajouter_Projet.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 850, 750);
            Stage stage = new Stage();
            stage.setTitle("Ajouter un nouveau projet");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(projetListView.getScene().getWindow());

            stage.setOnHidden(e -> {
                loadProjets();
                updateStatistics();
            });

            stage.show();

        } catch (Exception e) {
            showErrorAlert("Erreur d'ouverture",
                    "Impossible d'ouvrir le formulaire d'ajout : " + e.getMessage());
            e.printStackTrace();
        }
    }
}