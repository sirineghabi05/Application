package Controllers;

import Models.Entrepreneur;
import Services.Entrepreneurservice;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.stage.Modality;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.sql.SQLDataException;

public class entrepreneurLIistController {

    @FXML private ListView<Entrepreneur> entrepreneurListView;
    @FXML private TextField searchField;
    @FXML private Label totalEntrepreneursLabel;
    @FXML private Label activeSearchLabel;
    @FXML private Label lastRegistrationLabel;

    private final Entrepreneurservice service = new Entrepreneurservice();
    private ObservableList<Entrepreneur> entrepreneurList;
    private ObservableList<Entrepreneur> filteredList;

    @FXML
    private void initialize() {
        initializeStyles();
        loadEntrepreneurs();
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

        searchField.setPromptText("Rechercher un entrepreneur...");

        // Styles pour les labels de statistiques
        String statLabelStyle = "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2D3748;";
        totalEntrepreneursLabel.setStyle(statLabelStyle);
        activeSearchLabel.setStyle(statLabelStyle);
        lastRegistrationLabel.setStyle(statLabelStyle);
    }

    private void loadEntrepreneurs() {
        try {
            // Utiliser la méthode recuperer() de votre service
            List<Entrepreneur> entrepreneurs = service.recuperer();
            entrepreneurList = FXCollections.observableArrayList(entrepreneurs);
            filteredList = FXCollections.observableArrayList(entrepreneurs);

            // Configurer la ListView avec un custom cell factory
            entrepreneurListView.setItems(filteredList);
            entrepreneurListView.setCellFactory(param -> new ListCell<Entrepreneur>() {
                @Override
                protected void updateItem(Entrepreneur entrepreneur, boolean empty) {
                    super.updateItem(entrepreneur, empty);

                    if (empty || entrepreneur == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setGraphic(createEntrepreneurCard(entrepreneur));
                    }
                }
            });

            entrepreneurListView.setStyle("-fx-background-color: transparent; -fx-padding: 10;");

        } catch (SQLDataException e) {
            showErrorAlert("Erreur de chargement", "Impossible de charger les entrepreneurs : " + e.getMessage());
            entrepreneurList = FXCollections.observableArrayList();
            filteredList = FXCollections.observableArrayList();
            entrepreneurListView.setItems(filteredList);
        }
    }

    private VBox createEntrepreneurCard(Entrepreneur entrepreneur) {
        VBox card = new VBox();
        card.setStyle("-fx-background-color: #FFFFFF; " +
                "-fx-border-color: #E2E8F0; " +
                "-fx-border-radius: 12; " +
                "-fx-background-radius: 12; " +
                "-fx-padding: 20; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 2);");
        card.setSpacing(12);
        card.setPrefWidth(600);

        // Nom de l'entrepreneur
        Label nameLabel = new Label(entrepreneur.getNom() + " " + entrepreneur.getPrenom());
        nameLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        nameLabel.setStyle("-fx-text-fill: #2D3748;");

        // Date d'inscription
        Label dateLabel = new Label("Inscrit le " + formatDate(entrepreneur.getDateInscription()));
        dateLabel.setFont(Font.font("Segoe UI", 13));
        dateLabel.setStyle("-fx-text-fill: #718096;");

        // Email
        VBox emailBox = createInfoBox("EMAIL", entrepreneur.getEmail(), "mail");

        // Téléphone
        VBox phoneBox = createInfoBox("TÉLÉPHONE", entrepreneur.getTelephone(), "phone");

        // Adresse
        VBox addressBox = createInfoBox("ADRESSE", entrepreneur.getAdresse(), "location");

        // Boutons d'action
        HBox buttonBox = createActionButtons(entrepreneur);

        card.getChildren().addAll(nameLabel, dateLabel, emailBox, phoneBox, addressBox, buttonBox);
        return card;
    }

    private VBox createInfoBox(String title, String content, String iconType) {
        VBox infoBox = new VBox(5);

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        titleLabel.setStyle("-fx-text-fill: #4A5568;");

        Label contentLabel = new Label(content);
        contentLabel.setFont(Font.font("Segoe UI", 14));
        contentLabel.setStyle("-fx-text-fill: #2D3748;");
        contentLabel.setWrapText(true);

        // Ajouter une icône selon le type
        String icon = getIconForType(iconType);
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-text-fill: #4A6FA5; -fx-font-size: 14px;");

        HBox contentWithIcon = new HBox(10, iconLabel, contentLabel);
        contentWithIcon.setAlignment(Pos.CENTER_LEFT);

        infoBox.getChildren().addAll(titleLabel, contentWithIcon);
        return infoBox;
    }

    private String getIconForType(String type) {
        switch (type) {
            case "mail": return "✉️";
            case "phone": return "📞";
            case "location": return "📍";
            default: return "•";
        }
    }

    private HBox createActionButtons(Entrepreneur entrepreneur) {
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        // Bouton Modifier
        Button editButton = new Button("Modifier");
        editButton.setStyle("-fx-background-color: #4A6FA5; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 8; " +
                "-fx-padding: 8 16; " +
                "-fx-cursor: hand;");
        editButton.setOnAction(e -> handleEditEntrepreneur(entrepreneur));

        // Bouton Supprimer
        Button deleteButton = new Button("Supprimer");
        deleteButton.setStyle("-fx-background-color: #E53E3E; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 8; " +
                "-fx-padding: 8 16; " +
                "-fx-cursor: hand;");
        deleteButton.setOnAction(e -> handleDeleteEntrepreneur(entrepreneur));

        buttonBox.getChildren().addAll(editButton, deleteButton);
        return buttonBox;
    }

    private void setupSearchFunctionality() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterEntrepreneurs(newValue);
        });
    }

    private void filterEntrepreneurs(String searchText) {
        filteredList.clear();

        if (searchText == null || searchText.trim().isEmpty()) {
            filteredList.addAll(entrepreneurList);
        } else {
            String searchLower = searchText.toLowerCase();
            for (Entrepreneur entrepreneur : entrepreneurList) {
                if (entrepreneur.getNom().toLowerCase().contains(searchLower) ||
                        entrepreneur.getPrenom().toLowerCase().contains(searchLower) ||
                        entrepreneur.getEmail().toLowerCase().contains(searchLower) ||
                        (entrepreneur.getTelephone() != null && entrepreneur.getTelephone().toLowerCase().contains(searchLower)) ||
                        (entrepreneur.getAdresse() != null && entrepreneur.getAdresse().toLowerCase().contains(searchLower))) {
                    filteredList.add(entrepreneur);
                }
            }
        }

        updateStatistics();
    }

    private void updateStatistics() {
        totalEntrepreneursLabel.setText(String.valueOf(entrepreneurList.size()));
        activeSearchLabel.setText(String.valueOf(filteredList.size()));

        if (!entrepreneurList.isEmpty()) {
            String lastDate = findLastRegistrationDate();
            lastRegistrationLabel.setText(lastDate);
        } else {
            lastRegistrationLabel.setText("Aucune");
        }
    }

    private String findLastRegistrationDate() {
        LocalDate latestDate = null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (Entrepreneur entrepreneur : entrepreneurList) {
            try {
                LocalDate date = LocalDate.parse(entrepreneur.getDateInscription(), formatter);
                if (latestDate == null || date.isAfter(latestDate)) {
                    latestDate = date;
                }
            } catch (Exception e) {
                // Gérer l'erreur de parsing de date
            }
        }

        if (latestDate != null) {
            DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            return latestDate.format(displayFormatter);
        }

        return "N/A";
    }

    private String formatDate(String dateString) {
        try {
            LocalDate date = LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            return date.format(formatter);
        } catch (Exception e) {
            return dateString;
        }
    }

    @FXML
    private void handleEditEntrepreneur(Entrepreneur entrepreneur) {
        try {
            // 1. Charger le FXML de modification
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/modifier_Entrepreneur.fxml"));
            Parent root = loader.load();

            // 2. Récupérer le contrôleur de modification
            entrepreneurModifierController modifierController = loader.getController();

            // 3. Passer l'entrepreneur au contrôleur de modification
            modifierController.chargerEntrepreneur(entrepreneur);

            // 4. Créer une nouvelle scène
            Scene scene = new Scene(root);

            // 5. Créer une nouvelle fenêtre modale
            Stage stage = new Stage();
            stage.setTitle("Modifier Entrepreneur - " + entrepreneur.getNom() + " " + entrepreneur.getPrenom());
            stage.setScene(scene);

            // 6. Configurer comme fenêtre modale (bloque la fenêtre parent)
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(entrepreneurListView.getScene().getWindow());

            // 7. Définir la taille minimale
            stage.setMinWidth(800);
            stage.setMinHeight(600);

            // 8. Afficher et attendre la fermeture
            stage.showAndWait();

            // 9. Rafraîchir la liste après modification
            loadEntrepreneurs();
            showSuccessAlert("Modification terminée",
                    "Les modifications de l'entrepreneur ont été enregistrées.\n" +
                            "La liste a été mise à jour.");

        } catch (Exception e) {
            showErrorAlert("Erreur d'ouverture",
                    "Impossible d'ouvrir l'interface de modification : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDeleteEntrepreneur(Entrepreneur entrepreneur) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmer la suppression");
        confirmation.setHeaderText("Supprimer l'entrepreneur");
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer " +
                entrepreneur.getNom() + " " + entrepreneur.getPrenom() + " ?\n\n" +
                "Cette action est irréversible !");

        confirmation.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    // Utiliser la méthode supprimer() de votre service
                    service.supprimer(entrepreneur);
                    entrepreneurList.remove(entrepreneur);
                    filteredList.remove(entrepreneur);
                    updateStatistics();

                    showSuccessAlert("Suppression réussie",
                            "L'entrepreneur a été supprimé avec succès.\n" +
                                    "Nom : " + entrepreneur.getNom() + " " + entrepreneur.getPrenom());
                } catch (SQLDataException e) {
                    showErrorAlert("Erreur de suppression",
                            "Impossible de supprimer l'entrepreneur : " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleRefresh() {
        loadEntrepreneurs();
        updateStatistics();
        showInfoAlert("Rafraîchissement",
                "✅ Liste des entrepreneurs rafraîchie.\n" +
                        "Nombre d'entrepreneurs : " + entrepreneurList.size());
    }

    @FXML
    private void handleAddNew() {
        try {
            // Charger le FXML d'ajout
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ajouter_Entrepreneur.fxml"));
            Parent root = loader.load();

            // Créer une nouvelle scène
            Scene scene = new Scene(root);

            // Créer une nouvelle fenêtre modale
            Stage stage = new Stage();
            stage.setTitle("Ajouter un nouvel entrepreneur");
            stage.setScene(scene);

            // Configurer comme fenêtre modale
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(entrepreneurListView.getScene().getWindow());

            // Définir la taille
            stage.setMinWidth(700);
            stage.setMinHeight(650);

            // Afficher et attendre la fermeture
            stage.showAndWait();

            // Rafraîchir la liste après ajout
            loadEntrepreneurs();

        } catch (Exception e) {
            showErrorAlert("Erreur d'ouverture",
                    "Impossible d'ouvrir le formulaire d'ajout : " + e.getMessage());
        }
    }

    @FXML
    private void handleExport() {
        try {
            // Exporter les données au format CSV
            StringBuilder csvData = new StringBuilder();
            csvData.append("ID,Nom,Prénom,Email,Téléphone,Adresse,Date d'inscription\n");

            for (Entrepreneur entrepreneur : entrepreneurList) {
                csvData.append(entrepreneur.getId()).append(",")
                        .append(entrepreneur.getNom()).append(",")
                        .append(entrepreneur.getPrenom()).append(",")
                        .append(entrepreneur.getEmail()).append(",")
                        .append(entrepreneur.getTelephone() != null ? entrepreneur.getTelephone() : "").append(",")
                        .append(entrepreneur.getAdresse() != null ? entrepreneur.getAdresse() : "").append(",")
                        .append(formatDate(entrepreneur.getDateInscription())).append("\n");
            }

            // Créer une alerte pour afficher les données exportées
            TextArea textArea = new TextArea(csvData.toString());
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 12px;");

            Alert exportAlert = new Alert(Alert.AlertType.INFORMATION);
            exportAlert.setTitle("Exportation des données");
            exportAlert.setHeaderText("✅ Données exportées au format CSV");
            exportAlert.setContentText("Nombre d'entrepreneurs exportés : " + entrepreneurList.size() + "\n\nCopiez les données ci-dessous :");
            exportAlert.getDialogPane().setExpandableContent(textArea);
            exportAlert.getDialogPane().setExpanded(true);
            exportAlert.getDialogPane().setStyle("-fx-background-color: #F0F9FF;");
            exportAlert.showAndWait();

        } catch (Exception e) {
            showErrorAlert("Erreur d'exportation", "Impossible d'exporter les données : " + e.getMessage());
        }
    }

    @FXML
    private void handleClearSearch() {
        searchField.clear();
        filterEntrepreneurs("");
        showInfoAlert("Recherche effacée", "La recherche a été réinitialisée.");
    }

    @FXML
    private void handleAdvancedSearch() {
        String searchText = searchField.getText().trim();

        if (searchText.isEmpty()) {
            // Si vide, charger tous les entrepreneurs
            loadEntrepreneurs();
            showInfoAlert("Recherche avancée", "Affichage de tous les entrepreneurs.");
        } else {
            try {
                // Utiliser la méthode rechercher() du service
                List<Entrepreneur> resultats = service.rechercher(searchText);
                filteredList.clear();
                filteredList.addAll(resultats);
                entrepreneurListView.setItems(filteredList);
                updateStatistics();

                showInfoAlert("Résultats de recherche",
                        "Nombre de résultats pour \"" + searchText + "\" : " + filteredList.size());
            } catch (SQLDataException e) {
                showErrorAlert("Erreur de recherche", "Impossible d'effectuer la recherche : " + e.getMessage());
            }
        }
    }

    // Méthodes utilitaires pour afficher des alertes stylisées
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

    // Méthode pour obtenir le nombre total d'entrepreneurs depuis la base de données
    @FXML
    private void updateTotalCountFromDatabase() {
        try {
            int totalCount = service.getNombreEntrepreneurs();
            totalEntrepreneursLabel.setText(String.valueOf(totalCount));
            showInfoAlert("Mise à jour statistiques",
                    "Nombre total d'entrepreneurs en base de données : " + totalCount);
        } catch (SQLDataException e) {
            totalEntrepreneursLabel.setText("Erreur");
            showErrorAlert("Erreur statistique", "Impossible de récupérer le nombre total d'entrepreneurs");
        }
    }

    @FXML
    private void handleShowStatistics() {
        Alert statsAlert = new Alert(Alert.AlertType.INFORMATION);
        statsAlert.setTitle("Statistiques des entrepreneurs");
        statsAlert.setHeaderText("📊 Statistiques détaillées");

        // Calculer quelques statistiques
        int total = entrepreneurList.size();
        int avecEmail = 0;
        int avecTelephone = 0;
        int avecAdresse = 0;

        for (Entrepreneur e : entrepreneurList) {
            if (e.getEmail() != null && !e.getEmail().trim().isEmpty()) avecEmail++;
            if (e.getTelephone() != null && !e.getTelephone().trim().isEmpty()) avecTelephone++;
            if (e.getAdresse() != null && !e.getAdresse().trim().isEmpty()) avecAdresse++;
        }

        String statsText = "📈 Statistiques générales :\n" +
                "──────────────────────────────\n" +
                "• Total entrepreneurs : " + total + "\n" +
                "• Avec email : " + avecEmail + " (" + (total > 0 ? (avecEmail * 100 / total) : 0) + "%)\n" +
                "• Avec téléphone : " + avecTelephone + " (" + (total > 0 ? (avecTelephone * 100 / total) : 0) + "%)\n" +
                "• Avec adresse : " + avecAdresse + " (" + (total > 0 ? (avecAdresse * 100 / total) : 0) + "%)\n\n" +
                "📅 Dernière inscription : " + findLastRegistrationDate() + "\n" +
                "🔍 Résultats filtrés : " + filteredList.size() + " sur " + total;

        statsAlert.setContentText(statsText);
        statsAlert.getDialogPane().setStyle("-fx-background-color: #F8FAFC;");
        statsAlert.showAndWait();
    }

    @FXML
    private void handleExportToClipboard() {
        if (entrepreneurList.isEmpty()) {
            showErrorAlert("Export impossible", "Aucun entrepreneur à exporter.");
            return;
        }

        try {
            StringBuilder clipboardData = new StringBuilder();
            clipboardData.append("=== LISTE DES ENTREPRENEURS ===\n\n");

            for (Entrepreneur entrepreneur : filteredList) {
                clipboardData.append("📋 ").append(entrepreneur.getNom()).append(" ").append(entrepreneur.getPrenom()).append("\n");
                clipboardData.append("   ✉️ Email : ").append(entrepreneur.getEmail()).append("\n");
                if (entrepreneur.getTelephone() != null && !entrepreneur.getTelephone().trim().isEmpty()) {
                    clipboardData.append("   📞 Téléphone : ").append(entrepreneur.getTelephone()).append("\n");
                }
                if (entrepreneur.getAdresse() != null && !entrepreneur.getAdresse().trim().isEmpty()) {
                    clipboardData.append("   📍 Adresse : ").append(entrepreneur.getAdresse()).append("\n");
                }
                clipboardData.append("   📅 Inscrit le : ").append(formatDate(entrepreneur.getDateInscription())).append("\n");
                clipboardData.append("─────────────────────────────────\n");
            }

            // Copier dans le presse-papier
            ClipboardContent content = new ClipboardContent();
            content.putString(clipboardData.toString());
            Clipboard.getSystemClipboard().setContent(content);

            showSuccessAlert("Copie réussie",
                    "Les données de " + filteredList.size() + " entrepreneur(s) ont été copiées dans le presse-papier.");

        } catch (Exception e) {
            showErrorAlert("Erreur de copie", "Impossible de copier les données : " + e.getMessage());
        }
    }
}