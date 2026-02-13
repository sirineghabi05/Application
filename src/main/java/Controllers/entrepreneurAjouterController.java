package Controllers;

import Models.Entrepreneur;
import Services.Entrepreneurservice;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ButtonType;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

public class entrepreneurAjouterController {

    // Déclarez les champs en utilisant EXACTEMENT les mêmes fx:id que dans le FXML
    @FXML private TextField nom;
    @FXML private TextField prenom;
    @FXML private TextField Email;
    @FXML private TextField télephone;
    @FXML private TextField Adresse;
    @FXML private PasswordField MDP;
    @FXML private TextField Dateinscriotion;

    private final Entrepreneurservice service = new Entrepreneurservice();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Initialise le contrôleur - appelé automatiquement après chargement du FXML
     */
    @FXML
    private void initialize() {
        // Configure les styles dynamiques
        configurerStylesChamps();

        // Définir la date d'aujourd'hui par défaut
        Dateinscriotion.setText(LocalDate.now().format(DATE_FORMATTER));
    }

    /**
     * Configure les styles des champs pour une meilleure UX
     */
    private void configurerStylesChamps() {
        // Ajoute des effets de focus aux champs
        TextField[] tousChamps = {nom, prenom, Email, télephone, Adresse, Dateinscriotion};

        for (TextField champ : tousChamps) {
            champ.focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    // Champ en focus
                    champ.setStyle(champ.getStyle() + "; -fx-border-color: #4A6FA5; -fx-border-width: 2;");
                } else {
                    // Champ perdu le focus
                    champ.setStyle(champ.getStyle().replace("; -fx-border-color: #4A6FA5; -fx-border-width: 2;", ""));
                }
            });
        }
    }

    /**
     * Gère l'action du bouton "Ajouter"
     */
    @FXML
    private void ajouterEntrepreneur() {
        try {
            // Validation du formulaire
            if (!validerFormulaire()) {
                return;
            }

            // Demander confirmation avant ajout
            if (!demanderConfirmationAjout()) {
                return;
            }

            // Création de l'entrepreneur
            Entrepreneur nouvelEntrepreneur = creerEntrepreneurDepuisFormulaire();

            // Appel du service
            int idEntrepreneurAjoute = service.ajouter(nouvelEntrepreneur);

            if (idEntrepreneurAjoute > 0) {
                // Afficher alerte de succès avec options
                afficherAlerteSuccesAvecOptions(idEntrepreneurAjoute, nouvelEntrepreneur);
            } else {
                afficherAlerteErreur("Erreur d'ajout", "L'ajout a échoué. Aucun ID retourné.");
            }

        } catch (DateTimeParseException e) {
            afficherAlerteDateInvalide(Dateinscriotion.getText());
            highlightChamp(Dateinscriotion, true);

        } catch (Exception e) {
            afficherAlerteErreur("Erreur lors de l'ajout",
                    "Une erreur est survenue :\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Valide tous les champs du formulaire
     */
    private boolean validerFormulaire() {
        boolean validationReussie = true;
        StringBuilder erreurs = new StringBuilder();

        // Validation du nom
        if (nom.getText() == null || nom.getText().trim().isEmpty()) {
            erreurs.append("• Le nom est obligatoire\n");
            afficherMessageErreurChamp("Le nom est obligatoire", nom);
            validationReussie = false;
        } else {
            highlightChamp(nom, false);
        }

        // Validation du prénom
        if (prenom.getText() == null || prenom.getText().trim().isEmpty()) {
            erreurs.append("• Le prénom est obligatoire\n");
            afficherMessageErreurChamp("Le prénom est obligatoire", prenom);
            validationReussie = false;
        } else {
            highlightChamp(prenom, false);
        }

        // Validation de l'email
        String email = Email.getText();
        if (email == null || email.trim().isEmpty()) {
            erreurs.append("• L'email est obligatoire\n");
            afficherMessageErreurChamp("L'email est obligatoire", Email);
            validationReussie = false;
        } else if (!estEmailValide(email)) {
            erreurs.append("• Format d'email invalide (ex: nom@domaine.com)\n");
            afficherMessageErreurChamp("Format d'email invalide", Email);
            validationReussie = false;
        } else {
            highlightChamp(Email, false);
        }

        // Validation du mot de passe
        String motDePasse = MDP.getText();
        if (motDePasse == null || motDePasse.isEmpty()) {
            erreurs.append("• Le mot de passe est obligatoire\n");
            afficherMessageErreurChamp("Le mot de passe est obligatoire", MDP);
            validationReussie = false;
        } else if (motDePasse.length() < 8) {
            erreurs.append("• Le mot de passe doit contenir au moins 8 caractères\n");
            afficherAlerteMotDePasseFaible();
            afficherMessageErreurChamp("Mot de passe trop court", MDP);
            validationReussie = false;
        } else if (!contientCaractereSpecial(motDePasse)) {
            erreurs.append("• Le mot de passe doit contenir au moins un caractère spécial (!@#$%&*)\n");
            afficherAlerteMotDePasseFaible();
            afficherMessageErreurChamp("Mot de passe trop faible", MDP);
            validationReussie = false;
        } else {
            highlightChamp(MDP, false);
        }

        // Validation du téléphone (optionnel mais format)
        String telephone = télephone.getText();
        if (telephone != null && !telephone.trim().isEmpty() && !estTelephoneValide(telephone)) {
            erreurs.append("• Format de téléphone invalide (ex: +216 12345678)\n");
            afficherMessageErreurChamp("Format de téléphone invalide", télephone);
            validationReussie = false;
        } else {
            highlightChamp(télephone, false);
        }

        // Validation de la date (optionnel mais format doit être AAAA-MM-JJ sans heure)
        String dateText = Dateinscriotion.getText();
        if (dateText != null && !dateText.trim().isEmpty()) {
            try {
                LocalDate date = LocalDate.parse(dateText, DATE_FORMATTER);
                // Vérifier que la date n'a pas d'heure
                String dateFormatee = date.format(DATE_FORMATTER);
                if (!dateText.equals(dateFormatee)) {
                    Dateinscriotion.setText(dateFormatee);
                }
                highlightChamp(Dateinscriotion, false);
            } catch (DateTimeParseException e) {
                erreurs.append("• Format de date invalide (AAAA-MM-JJ, sans heure)\n");
                afficherMessageErreurChamp("Format de date invalide (AAAA-MM-JJ)", Dateinscriotion);
                validationReussie = false;
            }
        }

        // Si erreurs, afficher alerte de validation
        if (erreurs.length() > 0) {
            afficherAlerteValidation(
                    "Formulaire incomplet ou invalide",
                    "Veuillez corriger les erreurs suivantes :\n\n" +
                            erreurs.toString() + "\n" +
                            "Les champs marqués d'un astérisque (*) sont obligatoires."
            );
            return false;
        }

        return validationReussie;
    }

    /**
     * Vérifie si un email est valide
     */
    private boolean estEmailValide(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    /**
     * Vérifie si un téléphone est valide
     */
    private boolean estTelephoneValide(String telephone) {
        // Accepte les formats internationaux et locaux
        return telephone.matches("^[+]?[0-9\\s\\-\\(\\)]{8,20}$");
    }

    /**
     * Vérifie si le mot de passe contient un caractère spécial
     */
    private boolean contientCaractereSpecial(String motDePasse) {
        return motDePasse.matches(".*[!@#$%&*].*");
    }

    /**
     * Crée un objet Entrepreneur à partir des données du formulaire
     */
    private Entrepreneur creerEntrepreneurDepuisFormulaire() {
        String dateInscription = obtenirDateInscriptionValidee();

        return new Entrepreneur(
                nom.getText().trim(),
                prenom.getText().trim(),
                Email.getText().trim(),
                MDP.getText(),
                télephone.getText().trim(),
                Adresse.getText().trim(),
                dateInscription
        );
    }

    /**
     * Obtient une date d'inscription validée
     */
    private String obtenirDateInscriptionValidee() {
        String dateText = Dateinscriotion.getText().trim();

        if (dateText.isEmpty()) {
            return LocalDate.now().format(DATE_FORMATTER);
        }

        try {
            LocalDate date = LocalDate.parse(dateText, DATE_FORMATTER);
            return date.format(DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            // En cas d'erreur, utiliser la date du jour
            return LocalDate.now().format(DATE_FORMATTER);
        }
    }

    /**
     * Demande confirmation avant l'ajout
     */
    private boolean demanderConfirmationAjout() {
        Alert confirmation = new Alert(AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmer l'ajout");
        confirmation.setHeaderText("❓ Confirmation requise");
        confirmation.setContentText(
                "Êtes-vous sûr de vouloir ajouter cet entrepreneur ?\n\n" +
                        "Détails :\n" +
                        "• Nom : " + nom.getText().trim() + "\n" +
                        "• Prénom : " + prenom.getText().trim() + "\n" +
                        "• Email : " + Email.getText().trim() + "\n\n" +
                        "Cette action est irréversible."
        );

        confirmation.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> result = confirmation.showAndWait();

        return result.isPresent() && result.get() == ButtonType.YES;
    }

    /**
     * Gère l'action du bouton "Annuler"
     */
    @FXML
    private void handleAnnuler() {
        if (champsRemplis()) {
            Alert confirmation = new Alert(AlertType.CONFIRMATION);
            confirmation.setTitle("Annuler les modifications");
            confirmation.setHeaderText("Données non sauvegardées");
            confirmation.setContentText(
                    "Vous avez des données non sauvegardées.\n" +
                            "Voulez-vous vraiment annuler et perdre ces modifications ?"
            );

            confirmation.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
            Optional<ButtonType> result = confirmation.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.YES) {
                nettoyerChamps();
                afficherAlerteInformation(
                        "Formulaire réinitialisé",
                        "Tous les champs ont été vidés.\n" +
                                "Vous pouvez commencer une nouvelle saisie."
                );
            }
        } else {
            nettoyerChamps();
        }
    }

    /**
     * Vérifie si des champs sont remplis
     */
    private boolean champsRemplis() {
        return !nom.getText().trim().isEmpty() ||
                !prenom.getText().trim().isEmpty() ||
                !Email.getText().trim().isEmpty() ||
                !MDP.getText().isEmpty() ||
                !télephone.getText().trim().isEmpty() ||
                !Adresse.getText().trim().isEmpty();
    }

    /**
     * Nettoie tous les champs du formulaire
     */
    private void nettoyerChamps() {
        nom.clear();
        prenom.clear();
        Email.clear();
        télephone.clear();
        Adresse.clear();
        MDP.clear();
        Dateinscriotion.clear();
        Dateinscriotion.setText(LocalDate.now().format(DATE_FORMATTER));

        // Retirer les highlights d'erreur
        TextField[] tousChamps = {nom, prenom, Email, télephone, Adresse, Dateinscriotion};
        for (TextField champ : tousChamps) {
            highlightChamp(champ, false);
        }
        highlightChamp(MDP, false);
    }

    /**
     * Affiche un message d'erreur pour un champ spécifique
     */
    private void afficherMessageErreurChamp(String message, TextField champ) {
        highlightChamp(champ, true);
        champ.requestFocus();
    }

    /**
     * Met en évidence un champ (erreur ou normal)
     */
    private void highlightChamp(TextField champ, boolean erreur) {
        if (erreur) {
            champ.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; " +
                    "-fx-border-color: #DC2626; -fx-border-width: 2; " +
                    "-fx-padding: 10; -fx-font-family: 'Segoe UI'; " +
                    "-fx-font-size: 13px; -fx-background-color: #FEF2F2;");
        } else {
            champ.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; " +
                    "-fx-border-color: #CBD5E0; -fx-padding: 10; " +
                    "-fx-font-family: 'Segoe UI'; -fx-font-size: 13px; " +
                    "-fx-background-color: #F8FAFC;");
        }
    }

    /**
     * Met en évidence un champ PasswordField (erreur ou normal)
     */
    private void highlightChamp(PasswordField champ, boolean erreur) {
        if (erreur) {
            champ.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; " +
                    "-fx-border-color: #DC2626; -fx-border-width: 2; " +
                    "-fx-padding: 10; -fx-font-family: 'Segoe UI'; " +
                    "-fx-font-size: 13px; -fx-background-color: #FEF2F2;");
        } else {
            champ.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; " +
                    "-fx-border-color: #CBD5E0; -fx-padding: 10; " +
                    "-fx-font-family: 'Segoe UI'; -fx-font-size: 13px; " +
                    "-fx-background-color: #F8FAFC;");
        }
    }

    /**
     * Affiche une alerte stylisée
     */
    private void afficherAlerte(AlertType type, String titre, String message) {
        Alert alerte = new Alert(type);
        alerte.setTitle(titre);
        alerte.setHeaderText(null);
        alerte.setContentText(message);

        // Personnalisation selon le type
        switch (type) {
            case INFORMATION:
                alerte.getDialogPane().setStyle("-fx-background-color: #F0F9FF;");
                break;
            case WARNING:
                alerte.getDialogPane().setStyle("-fx-background-color: #FFFBEB;");
                break;
            case ERROR:
                alerte.getDialogPane().setStyle("-fx-background-color: #FEF2F2;");
                break;
            case CONFIRMATION:
                alerte.getDialogPane().setStyle("-fx-background-color: #F8FAFC;");
                break;
        }

        alerte.showAndWait();
    }

    /**
     * Affiche un message d'information rapide
     */
    private void afficherMessageInformation(String titre, String message) {
        Alert info = new Alert(AlertType.INFORMATION);
        info.setTitle(titre);
        info.setHeaderText(null);
        info.setContentText(message);
        info.showAndWait();
    }

    /**
     * Méthode utilitaire pour définir la date d'aujourd'hui
     */
    @FXML
    private void definirDateAujourdhui() {
        Dateinscriotion.setText(LocalDate.now().format(DATE_FORMATTER));
        highlightChamp(Dateinscriotion, false);

        afficherAlerteInformation(
                "Date définie",
                "La date d'aujourd'hui a été appliquée : " + LocalDate.now().format(DATE_FORMATTER)
        );
    }

    // ============ MÉTHODES D'ALERTES AMÉLIORÉES ============

    /**
     * Affiche une alerte de succès détaillée
     */
    private void afficherAlerteSucces(String titre, String message) {
        Alert alerte = new Alert(AlertType.INFORMATION);
        alerte.setTitle(titre);
        alerte.setHeaderText("✅ Opération réussie");
        alerte.setContentText(message);
        alerte.getDialogPane().setStyle("-fx-background-color: #F0F9FF;");
        alerte.showAndWait();
    }

    /**
     * Affiche une alerte d'erreur détaillée
     */
    private void afficherAlerteErreur(String titre, String message) {
        Alert alerte = new Alert(AlertType.ERROR);
        alerte.setTitle(titre);
        alerte.setHeaderText("❌ Erreur rencontrée");
        alerte.setContentText(message);
        alerte.getDialogPane().setStyle("-fx-background-color: #FEF2F2;");
        alerte.showAndWait();
    }

    /**
     * Affiche une alerte d'information stylisée
     */
    private void afficherAlerteInformation(String titre, String message) {
        Alert alerte = new Alert(AlertType.INFORMATION);
        alerte.setTitle(titre);
        alerte.setHeaderText("ℹ️ Information");
        alerte.setContentText(message);
        alerte.getDialogPane().setStyle("-fx-background-color: #F8FAFC;");
        alerte.showAndWait();
    }

    /**
     * Affiche une alerte de validation (avertissement)
     */
    private void afficherAlerteValidation(String titre, String message) {
        Alert alerte = new Alert(AlertType.WARNING);
        alerte.setTitle(titre);
        alerte.setHeaderText("⚠️ Validation requise");
        alerte.setContentText(message);
        alerte.getDialogPane().setStyle("-fx-background-color: #FFFBEB;");
        alerte.showAndWait();
    }

    /**
     * Affiche une alerte pour l'email déjà existant
     */
    private void afficherAlerteEmailExistant(String email) {
        Alert alerte = new Alert(AlertType.WARNING);
        alerte.setTitle("Email déjà utilisé");
        alerte.setHeaderText("⚠️ Email déjà enregistré");
        alerte.setContentText(
                "L'email '" + email + "' est déjà utilisé par un autre entrepreneur.\n\n" +
                        "Veuillez utiliser une autre adresse email."
        );
        alerte.getDialogPane().setStyle("-fx-background-color: #FFFBEB;");
        alerte.showAndWait();
    }

    /**
     * Affiche une alerte pour mot de passe faible
     */
    private void afficherAlerteMotDePasseFaible() {
        Alert alerte = new Alert(AlertType.WARNING);
        alerte.setTitle("Mot de passe faible");
        alerte.setHeaderText("⚠️ Mot de passe trop simple");
        alerte.setContentText(
                "Votre mot de passe est trop faible.\n\n" +
                        "Recommandations :\n" +
                        "• Au moins 8 caractères\n" +
                        "• Une majuscule et une minuscule\n" +
                        "• Un chiffre\n" +
                        "• Un caractère spécial (!@#$%&*)\n\n" +
                        "Utilisez le bouton 'Générer MDP' pour un mot de passe sécurisé."
        );
        alerte.getDialogPane().setStyle("-fx-background-color: #FFFBEB;");
        alerte.showAndWait();
    }

    /**
     * Affiche une alerte de succès avec option de modification
     */
    private void afficherAlerteSuccesAvecOptions(int idEntrepreneur, Entrepreneur entrepreneur) {
        Alert alerteSucces = new Alert(AlertType.INFORMATION);
        alerteSucces.setTitle("Entrepreneur ajouté avec succès");
        alerteSucces.setHeaderText("✅ Nouvel entrepreneur créé !");
        alerteSucces.setContentText(
                "Entrepreneur ajouté avec l'ID : " + idEntrepreneur + "\n\n" +
                        "Détails :\n" +
                        "• Nom complet : " + entrepreneur.getNom() + " " + entrepreneur.getPrenom() + "\n" +
                        "• Email : " + entrepreneur.getEmail() + "\n" +
                        "• Date d'inscription : " + entrepreneur.getDateInscription() + "\n\n" +
                        "Que souhaitez-vous faire ?"
        );

        // Ajouter des boutons personnalisés
        ButtonType btnModifier = new ButtonType("Modifier cet entrepreneur");
        ButtonType btnNouveau = new ButtonType("Ajouter un autre");
        ButtonType btnFermer = new ButtonType("Fermer", ButtonType.CANCEL.getButtonData());

        alerteSucces.getButtonTypes().setAll(btnModifier, btnNouveau, btnFermer);

        Optional<ButtonType> result = alerteSucces.showAndWait();

        if (result.isPresent()) {
            if (result.get() == btnModifier) {
                afficherAlerteInformation("Modification",
                        "Redirection vers la modification de l'entrepreneur #" + idEntrepreneur);
                // Ici vous pourriez appeler une méthode pour ouvrir la modification
            } else if (result.get() == btnNouveau) {
                nettoyerChamps();
                afficherAlerteInformation("Prêt",
                        "Formulaire réinitialisé. Vous pouvez ajouter un nouvel entrepreneur.");
            }
        }
    }

    /**
     * Affiche une alerte pour les champs obligatoires manquants
     */
    private void afficherAlerteChampsObligatoires(String... champsManquants) {
        StringBuilder message = new StringBuilder();
        message.append("Les champs suivants sont obligatoires :\n\n");

        for (String champ : champsManquants) {
            message.append("• ").append(champ).append("\n");
        }

        message.append("\nVeuillez remplir ces champs avant de continuer.");

        afficherAlerteValidation("Champs obligatoires manquants", message.toString());
    }

    /**
     * Affiche une alerte pour la date invalide
     */
    private void afficherAlerteDateInvalide(String dateSaisie) {
        Alert alerte = new Alert(AlertType.ERROR);
        alerte.setTitle("Date invalide");
        alerte.setHeaderText("❌ Format de date incorrect");
        alerte.setContentText(
                "La date saisie '" + dateSaisie + "' est invalide.\n\n" +
                        "Format attendu : AAAA-MM-JJ\n" +
                        "Exemples valides :\n" +
                        "• " + LocalDate.now().format(DATE_FORMATTER) + "\n" +
                        "• 2024-12-31\n" +
                        "• 2025-01-15"
        );
        alerte.getDialogPane().setStyle("-fx-background-color: #FEF2F2;");
        alerte.showAndWait();
    }

    // ============ MÉTHODES DE VALIDATION APPELÉES PAR LE FXML ============

    @FXML
    private void validerPrenom() {
        String prenomText = prenom.getText().trim();
        if (prenomText.isEmpty()) {
            afficherMessageErreurChamp("Le prénom est obligatoire", prenom);
        } else if (prenomText.length() < 2) {
            afficherMessageErreurChamp("Le prénom doit avoir au moins 2 caractères", prenom);
        } else if (!prenomText.matches("^[a-zA-ZÀ-ÿ\\s'-]+$")) {
            afficherMessageErreurChamp("Le prénom ne doit contenir que des lettres", prenom);
        } else {
            highlightChamp(prenom, false);
        }
    }

    @FXML
    private void validerEmail() {
        String emailText = Email.getText().trim();
        if (emailText.isEmpty()) {
            afficherMessageErreurChamp("L'email est obligatoire", Email);
        } else if (!estEmailValide(emailText)) {
            afficherMessageErreurChamp("Format d'email invalide", Email);
        } else {
            highlightChamp(Email, false);
        }
    }

    @FXML
    private void validerTelephone() {
        String telText = télephone.getText().trim();
        if (!telText.isEmpty() && !estTelephoneValide(telText)) {
            afficherMessageErreurChamp("Format de téléphone invalide", télephone);
        } else {
            highlightChamp(télephone, false);
        }
    }

    @FXML
    private void validerMotDePasse() {
        String mdpText = MDP.getText();
        if (mdpText.isEmpty()) {
            afficherMessageErreurChamp("Le mot de passe est obligatoire", MDP);
        } else if (mdpText.length() < 8) {
            afficherMessageErreurChamp("Min 8 caractères", MDP);
        } else if (!contientCaractereSpecial(mdpText)) {
            afficherMessageErreurChamp("Besoin caractère spécial", MDP);
        } else {
            highlightChamp(MDP, false);
        }
    }

    @FXML
    private void mettreAJourStatut() {
        // Méthode appelée par le FXML - peut être implémentée
    }

    private void afficherErreur(javafx.scene.control.Label label, String message) {
        if (label != null) {
            label.setText(message);
            label.setVisible(!message.isEmpty());
        }
    }

    private void afficherAvertissement(javafx.scene.control.Label label, String message) {
        if (label != null) {
            label.setText(message);
            label.setVisible(!message.isEmpty());
        }
    }

    private void effacerErreur(javafx.scene.control.Label label) {
        if (label != null) {
            label.setText("");
            label.setVisible(false);
        }
    }
}