package Controllers;

import Models.Entrepreneur;
import Services.Entrepreneurservice;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Label;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

public class entrepreneurModifierController {

    // Champs du formulaire
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private TextField telephoneField;
    @FXML private TextField adresseField;
    @FXML private PasswordField mdpField;
    @FXML private TextField dateField;

    // Labels pour les informations
    @FXML private Label idLabel;
    @FXML private Label dateCreationLabel;
    @FXML private Label statutLabel;
    @FXML private Label projetsLabel;

    // Boutons supplémentaires
    @FXML private Button btnGenererMdp;
    @FXML private Button btnVoirMdp;
    @FXML private Button btnDateAujourdhui;
    @FXML private Button btnReinitialiserDate;

    private final Entrepreneurservice service = new Entrepreneurservice();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private int idEntrepreneur = 0;
    private Entrepreneur entrepreneurOriginal;
    private boolean motDePasseVisible = false;
    private String ancienMotDePasse = "";

    /**
     * Initialisation du contrôleur
     */
    @FXML
    private void initialize() {
        configurerStylesChamps();
        configurerPlaceholders();
        configurerListeners();
    }

    /**
     * Configure les styles des champs
     */
    private void configurerStylesChamps() {
        TextField[] tousChamps = {nomField, prenomField, emailField, telephoneField, adresseField, dateField};
        for (TextField champ : tousChamps) {
            champ.focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    champ.setStyle(champ.getStyle() + "; -fx-border-color: #4A6FA5; -fx-border-width: 2;");
                } else {
                    champ.setStyle(champ.getStyle().replace("; -fx-border-color: #4A6FA5; -fx-border-width: 2;", ""));
                }
            });
        }
    }

    /**
     * Configure les placeholders
     */
    private void configurerPlaceholders() {
        mdpField.setPromptText("Laisser vide pour conserver l'ancien");
        dateField.setPromptText("AAAA-MM-JJ");
    }

    /**
     * Configure les écouteurs d'événements
     */
    private void configurerListeners() {
        // Détecter les modifications pour mettre à jour le statut
        nomField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.equals(oldVal)) {
                mettreAJourStatutModification();
            }
        });

        emailField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.equals(oldVal)) {
                mettreAJourStatutModification();
            }
        });
    }

    /**
     * Charge un entrepreneur dans le formulaire pour modification
     */
    public void chargerEntrepreneur(Entrepreneur entrepreneur) {
        if (entrepreneur == null) {
            afficherAlerteErreur("Erreur de chargement", "Aucun entrepreneur à modifier");
            return;
        }

        this.idEntrepreneur = entrepreneur.getId();
        this.entrepreneurOriginal = entrepreneur;
        this.ancienMotDePasse = entrepreneur.getMotDePasse();

        // Remplir les champs
        nomField.setText(entrepreneur.getNom());
        prenomField.setText(entrepreneur.getPrenom());
        emailField.setText(entrepreneur.getEmail());
        telephoneField.setText(entrepreneur.getTelephone());
        adresseField.setText(entrepreneur.getAdresse());

        // Remplir le mot de passe avec la valeur existante
        mdpField.setText(entrepreneur.getMotDePasse());

        // Formater la date sans heure (AAAA-MM-JJ)
        String dateFormatee = formateDateSansHeure(entrepreneur.getDateInscription());
        dateField.setText(dateFormatee);

        // Mettre à jour les labels
        mettreAJourLabels(entrepreneur);

        afficherAlerteInformation(
                "Entrepreneur chargé",
                "Entrepreneur #" + idEntrepreneur + " chargé pour modification.\n" +
                        "Vous pouvez maintenant modifier ses informations."
        );
    }

    /**
     * Formate une date pour supprimer l'heure et garder uniquement AAAA-MM-JJ
     */
    private String formateDateSansHeure(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return "";
        }

        try {
            // Essayer d'abord avec le format DateTimeFormatter
            LocalDate date = LocalDate.parse(dateString, DATE_FORMATTER);
            return date.format(DATE_FORMATTER);
        } catch (DateTimeParseException e1) {
            try {
                // Si le format a une heure, on la retire
                String[] parts = dateString.split(" ");
                LocalDate date = LocalDate.parse(parts[0], DATE_FORMATTER);
                return date.format(DATE_FORMATTER);
            } catch (Exception e2) {
                // Retourner tel quel si impossible à parser
                return dateString;
            }
        }
    }

    /**
     * Met à jour les labels d'information
     */
    private void mettreAJourLabels(Entrepreneur entrepreneur) {
        if (idLabel != null) {
            idLabel.setText("ID: #" + String.format("%05d", entrepreneur.getId()));
        }

        if (dateCreationLabel != null) {
            try {
                LocalDate date = LocalDate.parse(entrepreneur.getDateInscription(), DATE_FORMATTER);
                dateCreationLabel.setText(date.format(DISPLAY_FORMATTER));
            } catch (Exception e) {
                dateCreationLabel.setText(entrepreneur.getDateInscription());
            }
        }

        if (statutLabel != null) {
            statutLabel.setText("✓ Actif");
            statutLabel.setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold;");
        }

        // Mettre à jour le label projets (exemple)
        if (projetsLabel != null) {
            projetsLabel.setText("3 projets"); // À remplacer par une valeur réelle
        }
    }

    /**
     * Met à jour le statut de modification
     */
    private void mettreAJourStatutModification() {
        // Vous pouvez ajouter une indication visuelle si des modifications ont été faites
    }

    /**
     * Gère la modification de l'entrepreneur
     */
    @FXML
    private void modifierEntrepreneur() {
        // Vérifier qu'un entrepreneur est chargé
        if (idEntrepreneur == 0) {
            afficherAlerteErreur(
                    "Aucun entrepreneur sélectionné",
                    "Veuillez d'abord charger un entrepreneur à modifier."
            );
            return;
        }

        // Validation du formulaire
        if (!validerFormulaireModification()) {
            return;
        }

        // Demander confirmation
        if (!demanderConfirmationModification()) {
            return;
        }

        try {
            // Créer l'entrepreneur modifié
            Entrepreneur entrepreneurModifie = creerEntrepreneurModifie();

            // Appeler le service
            service.modifier(entrepreneurModifie);

            // Mettre à jour l'original
            this.entrepreneurOriginal = entrepreneurModifie;

            // Afficher message de succès
            afficherAlerteSucces(
                    "Modification réussie",
                    "✅ L'entrepreneur a été modifié avec succès !\n\n" +
                            "Informations mises à jour :\n" +
                            "• Nom complet : " + entrepreneurModifie.getNom() + " " + entrepreneurModifie.getPrenom() + "\n" +
                            "• Email : " + entrepreneurModifie.getEmail() + "\n" +
                            "• Téléphone : " + (entrepreneurModifie.getTelephone().isEmpty() ? "Non renseigné" : entrepreneurModifie.getTelephone()) + "\n" +
                            "• Date de modification : " + LocalDate.now().format(DISPLAY_FORMATTER)
            );

            // Mettre à jour les labels
            mettreAJourLabels(entrepreneurModifie);

        } catch (DateTimeParseException e) {
            afficherAlerteErreur(
                    "Format de date incorrect",
                    "Le format de date doit être : AAAA-MM-JJ\n" +
                            "Exemple : " + LocalDate.now().format(DATE_FORMATTER)
            );
            highlightChamp(dateField, true);

        } catch (Exception e) {
            afficherAlerteErreur(
                    "Erreur de modification",
                    "Une erreur est survenue lors de la modification :\n" +
                            e.getMessage() + "\n\n" +
                            "Veuillez vérifier les données et réessayer."
            );
            e.printStackTrace();
        }
    }

    /**
     * Valide le formulaire de modification
     */
    private boolean validerFormulaireModification() {
        StringBuilder erreurs = new StringBuilder();

        // Validation du nom
        if (nomField.getText() == null || nomField.getText().trim().isEmpty()) {
            erreurs.append("• Le nom est obligatoire\n");
            highlightChamp(nomField, true);
        } else {
            highlightChamp(nomField, false);
        }

        // Validation du prénom
        if (prenomField.getText() == null || prenomField.getText().trim().isEmpty()) {
            erreurs.append("• Le prénom est obligatoire\n");
            highlightChamp(prenomField, true);
        } else {
            highlightChamp(prenomField, false);
        }

        // Validation de l'email
        String email = emailField.getText();
        if (email == null || email.trim().isEmpty()) {
            erreurs.append("• L'email est obligatoire\n");
            highlightChamp(emailField, true);
        } else if (!estEmailValide(email)) {
            erreurs.append("• Format d'email invalide (ex: nom@domaine.com)\n");
            highlightChamp(emailField, true);
        } else {
            highlightChamp(emailField, false);
        }

        // Validation du téléphone
        String telephone = telephoneField.getText();
        if (telephone != null && !telephone.trim().isEmpty() && !estTelephoneValide(telephone)) {
            erreurs.append("• Format de téléphone invalide (ex: +216 12 345 678)\n");
            highlightChamp(telephoneField, true);
        } else {
            highlightChamp(telephoneField, false);
        }

        // Validation de la date
        String dateText = dateField.getText();
        if (dateText != null && !dateText.trim().isEmpty()) {
            try {
                LocalDate date = LocalDate.parse(dateText, DATE_FORMATTER);
                if (date.isAfter(LocalDate.now())) {
                    erreurs.append("• La date ne peut pas être dans le futur\n");
                    highlightChamp(dateField, true);
                } else {
                    highlightChamp(dateField, false);
                }
            } catch (DateTimeParseException e) {
                erreurs.append("• Format de date invalide (AAAA-MM-JJ)\n");
                highlightChamp(dateField, true);
            }
        }

        // Si erreurs, afficher alerte
        if (erreurs.length() > 0) {
            afficherAlerteValidation(
                    "Formulaire incomplet ou invalide",
                    "Veuillez corriger les erreurs suivantes :\n\n" +
                            erreurs.toString() + "\n" +
                            "Les champs marqués d'un astérisque (*) sont obligatoires."
            );
            return false;
        }

        return true;
    }

    /**
     * Vérifie si un email est valide
     */
    private boolean estEmailValide(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    /**
     * Vérifie si un téléphone est valide
     */
    private boolean estTelephoneValide(String telephone) {
        return telephone.matches("^[+]?[0-9\\s\\-\\(\\)]{8,20}$");
    }

    /**
     * Crée un entrepreneur modifié à partir des champs
     */
    private Entrepreneur creerEntrepreneurModifie() {
        String motDePasse = mdpField.getText().isEmpty() ? ancienMotDePasse : mdpField.getText();
        String dateInscription = obtenirDateInscriptionValidee();

        return new Entrepreneur(
                idEntrepreneur,
                nomField.getText().trim(),
                prenomField.getText().trim(),
                emailField.getText().trim(),
                motDePasse,
                telephoneField.getText().trim(),
                adresseField.getText().trim(),
                dateInscription
        );
    }

    /**
     * Obtient une date d'inscription validée (format AAAA-MM-JJ, sans heure)
     */
    private String obtenirDateInscriptionValidee() {
        String dateText = dateField.getText().trim();

        if (dateText.isEmpty()) {
            return entrepreneurOriginal != null ?
                    entrepreneurOriginal.getDateInscription() :
                    LocalDate.now().format(DATE_FORMATTER);
        }

        try {
            // Parser et reformater pour supprimer l'heure si présente
            LocalDate date = LocalDate.parse(dateText, DATE_FORMATTER);
            String dateFormatee = date.format(DATE_FORMATTER);

            // S'assurer que c'est au format AAAA-MM-JJ sans heure
            if (!dateFormatee.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
                return entrepreneurOriginal != null ?
                        entrepreneurOriginal.getDateInscription() :
                        LocalDate.now().format(DATE_FORMATTER);
            }

            return dateFormatee;
        } catch (DateTimeParseException e) {
            // Essayer de nettoyer la date si elle contient une heure
            try {
                String[] parts = dateText.split(" ");
                LocalDate date = LocalDate.parse(parts[0], DATE_FORMATTER);
                return date.format(DATE_FORMATTER);
            } catch (Exception ex) {
                return entrepreneurOriginal != null ?
                        entrepreneurOriginal.getDateInscription() :
                        LocalDate.now().format(DATE_FORMATTER);
            }
        }
    }

    /**
     * Demande confirmation avant modification
     */
    private boolean demanderConfirmationModification() {
        Alert confirmation = new Alert(AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmer la modification");
        confirmation.setHeaderText("Enregistrer les modifications ?");
        confirmation.setContentText(
                "Êtes-vous sûr de vouloir modifier cet entrepreneur ?\n\n" +
                        "Les modifications suivantes seront appliquées :\n" +
                        "• Nom : " + (nomField.getText().equals(entrepreneurOriginal.getNom()) ? "Non modifié" : "Modifié") + "\n" +
                        "• Email : " + (emailField.getText().equals(entrepreneurOriginal.getEmail()) ? "Non modifié" : "Modifié") + "\n" +
                        "• Téléphone : " + (telephoneField.getText().equals(entrepreneurOriginal.getTelephone()) ? "Non modifié" : "Modifié") + "\n\n" +
                        "Cette action est définitive."
        );

        confirmation.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
        Optional<ButtonType> result = confirmation.showAndWait();

        return result.isPresent() && result.get() == ButtonType.YES;
    }

    /**
     * Gère l'annulation
     */
    @FXML
    private void handleAnnuler() {
        if (champsModifies()) {
            Alert confirmation = new Alert(AlertType.CONFIRMATION);
            confirmation.setTitle("Annuler les modifications");
            confirmation.setHeaderText("Modifications non sauvegardées");
            confirmation.setContentText(
                    "Vous avez des modifications non sauvegardées.\n" +
                            "Voulez-vous vraiment annuler et perdre ces modifications ?"
            );

            confirmation.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
            Optional<ButtonType> result = confirmation.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.YES) {
                fermerFenetre();
            }
        } else {
            fermerFenetre();
        }
    }

    /**
     * Vérifie si des champs ont été modifiés
     */
    private boolean champsModifies() {
        if (entrepreneurOriginal == null) return false;

        return !nomField.getText().equals(entrepreneurOriginal.getNom()) ||
                !prenomField.getText().equals(entrepreneurOriginal.getPrenom()) ||
                !emailField.getText().equals(entrepreneurOriginal.getEmail()) ||
                !telephoneField.getText().equals(entrepreneurOriginal.getTelephone()) ||
                !adresseField.getText().equals(entrepreneurOriginal.getAdresse()) ||
                (!mdpField.getText().isEmpty() && !mdpField.getText().equals(ancienMotDePasse)) ||
                !dateField.getText().equals(entrepreneurOriginal.getDateInscription());
    }

    /**
     * Réinitialise le formulaire
     */
    @FXML
    private void reinitialiserFormulaire() {
        if (entrepreneurOriginal != null) {
            chargerEntrepreneur(entrepreneurOriginal);
            afficherAlerteInformation(
                    "Formulaire réinitialisé",
                    "Tous les champs ont été réinitialisés aux valeurs d'origine."
            );
        } else {
            afficherAlerteErreur(
                    "Impossible de réinitialiser",
                    "Aucun entrepreneur chargé pour réinitialisation."
            );
        }
    }

    /**
     * Génère un mot de passe sécurisé
     */
    @FXML
    private void genererMotDePasse() {
        String motDePasseGenere = genererMotDePasseSecurise();
        mdpField.setText(motDePasseGenere);

        afficherAlerteInformation(
                "Mot de passe généré",
                "Un mot de passe sécurisé a été généré.\n\n" +
                        "Nouveau mot de passe : " + motDePasseGenere + "\n\n" +
                        "Conseil : Notez ce mot de passe dans un endroit sécurisé."
        );
    }

    /**
     * Affiche/Masque le mot de passe
     */
    @FXML
    private void afficherMotDePasse() {
        // Cette fonctionnalité affiche le mot de passe en clair
        if (!mdpField.getText().isEmpty()) {
            afficherAlerteInformation(
                    "Mot de passe actuel",
                    "Mot de passe saisi : " + mdpField.getText()
            );
        } else {
            afficherAlerteInformation(
                    "Mot de passe",
                    "Aucun mot de passe saisi. Le mot de passe original sera conservé."
            );
        }
    }

    /**
     * Affiche/Masque le mot de passe
     */
    @FXML
    private void voirMotDePasse() {
        // Cette fonctionnalité nécessite un TextField supplémentaire pour afficher le mot de passe en clair
        // Pour l'instant, on affiche juste une alerte avec le mot de passe
        if (!mdpField.getText().isEmpty()) {
            afficherAlerteInformation(
                    "Mot de passe actuel",
                    "Mot de passe saisi : " + mdpField.getText()
            );
        } else {
            afficherAlerteInformation(
                    "Mot de passe",
                    "Aucun mot de passe saisi. Le mot de passe original sera conservé."
            );
        }
    }

    /**
     * Définit la date d'aujourd'hui
     */
    @FXML
    private void definirDateAujourdhui() {
        dateField.setText(LocalDate.now().format(DATE_FORMATTER));
        highlightChamp(dateField, false);
    }

    /**
     * Réinitialise la date à la date d'origine
     */
    @FXML
    private void reinitialiserDate() {
        if (entrepreneurOriginal != null) {
            dateField.setText(entrepreneurOriginal.getDateInscription());
            highlightChamp(dateField, false);
        }
    }

    /**
     * Génère un mot de passe sécurisé
     */
    private String genererMotDePasseSecurise() {
        String majuscules = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String minuscules = "abcdefghijklmnopqrstuvwxyz";
        String chiffres = "0123456789";
        String caracteresSpeciaux = "!@#$%&*";

        StringBuilder motDePasse = new StringBuilder();

        // Assurer au moins un de chaque type
        motDePasse.append(majuscules.charAt((int)(Math.random() * majuscules.length())));
        motDePasse.append(minuscules.charAt((int)(Math.random() * minuscules.length())));
        motDePasse.append(chiffres.charAt((int)(Math.random() * chiffres.length())));
        motDePasse.append(caracteresSpeciaux.charAt((int)(Math.random() * caracteresSpeciaux.length())));

        // Ajouter 4 caractères aléatoires
        String tousCaracteres = majuscules + minuscules + chiffres + caracteresSpeciaux;
        for (int i = 0; i < 4; i++) {
            motDePasse.append(tousCaracteres.charAt((int)(Math.random() * tousCaracteres.length())));
        }

        return melangerChaine(motDePasse.toString());
    }

    /**
     * Mélange les caractères d'une chaîne
     */
    private String melangerChaine(String chaine) {
        char[] caracteres = chaine.toCharArray();
        for (int i = caracteres.length - 1; i > 0; i--) {
            int j = (int)(Math.random() * (i + 1));
            char temp = caracteres[i];
            caracteres[i] = caracteres[j];
            caracteres[j] = temp;
        }
        return new String(caracteres);
    }

    /**
     * Met en évidence un champ
     */
    private void highlightChamp(TextField champ, boolean erreur) {
        if (erreur) {
            champ.setStyle("-fx-background-radius: 10; -fx-border-radius: 10; " +
                    "-fx-border-color: #DC2626; -fx-border-width: 2; " +
                    "-fx-padding: 12 15; -fx-font-family: 'Segoe UI'; " +
                    "-fx-font-size: 14px; -fx-background-color: #FEF2F2;");
        } else {
            champ.setStyle("-fx-background-radius: 10; -fx-border-radius: 10; " +
                    "-fx-border-color: #E2E8F0; -fx-padding: 12 15; " +
                    "-fx-font-family: 'Segoe UI'; -fx-font-size: 14px; " +
                    "-fx-background-color: #F8FAFC;");
        }
    }

    /**
     * Ferme la fenêtre
     */
    private void fermerFenetre() {
        Stage stage = (Stage) nomField.getScene().getWindow();
        stage.close();
    }

    // ============ MÉTHODES D'ALERTES ============

    private void afficherAlerteSucces(String titre, String message) {
        Alert alerte = new Alert(AlertType.INFORMATION);
        alerte.setTitle(titre);
        alerte.setHeaderText("✅ Opération réussie");
        alerte.setContentText(message);
        alerte.getDialogPane().setStyle("-fx-background-color: #F0F9FF;");
        alerte.showAndWait();
    }

    private void afficherAlerteErreur(String titre, String message) {
        Alert alerte = new Alert(AlertType.ERROR);
        alerte.setTitle(titre);
        alerte.setHeaderText("❌ Erreur rencontrée");
        alerte.setContentText(message);
        alerte.getDialogPane().setStyle("-fx-background-color: #FEF2F2;");
        alerte.showAndWait();
    }

    private void afficherAlerteInformation(String titre, String message) {
        Alert alerte = new Alert(AlertType.INFORMATION);
        alerte.setTitle(titre);
        alerte.setHeaderText("ℹ️ Information");
        alerte.setContentText(message);
        alerte.getDialogPane().setStyle("-fx-background-color: #F8FAFC;");
        alerte.showAndWait();
    }

    private void afficherAlerteValidation(String titre, String message) {
        Alert alerte = new Alert(AlertType.WARNING);
        alerte.setTitle(titre);
        alerte.setHeaderText("⚠️ Validation requise");
        alerte.setContentText(message);
        alerte.getDialogPane().setStyle("-fx-background-color: #FFFBEB;");
        alerte.showAndWait();
    }
}
