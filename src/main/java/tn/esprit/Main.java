package tn.esprit;



import Services.Entrepreneurservice;
import Models.Entrepreneur;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;


import java.sql.SQLException;


import java.util.List;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        // Chargement du fichier FXML
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/list_Projet.fxml"));

        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Gestion des Projets - InnoStart");
         stage.setScene(scene);
        stage.show();
    }
    public static void main(String[] args) {

        // 🔹 Création du service
        /*Entrepreneurservice service = new Entrepreneurservice();

        try {
            // 1️⃣ CREATE : Ajouter des entrepreneurs
            Entrepreneur e1 = new Entrepreneur("fakher", "Ben Salah", "ali@mail.com", "12345", "12345678", "Tunis", "2026-02-08");
            Entrepreneur e2 = new Entrepreneur("Sara", "Trabelsi", "sara@mail.com", "54321", "87654321", "Sfax", "2026-02-08");

            service.ajouter(e1);
            service.ajouter(e2);

            System.out.println("✅ Entrepreneurs ajoutés avec succès !\n");

            // 2️⃣ READ : Récupérer tous les entrepreneurs
            List<Entrepreneur> entrepreneurs = service.recuperer();
            System.out.println("📋 Liste des entrepreneurs :");
            for (Entrepreneur e : entrepreneurs) {
                System.out.println(e);
            }

            // 3️⃣ UPDATE : Modifier un entrepreneur
            if (!entrepreneurs.isEmpty()) {
                Entrepreneur first = entrepreneurs.get(0);
                first.setNom("Ali-Updated");
                first.setTelephone("111222333");
                service.modifier(first);
                System.out.println("\n✏️ Entrepreneur modifié : " + first.getId());
            }

            // 4️⃣ READ après modification
            entrepreneurs = service.recuperer();
            System.out.println("\n📋 Liste après modification :");
            for (Entrepreneur e : entrepreneurs) {
                System.out.println(e);
            }

            // 5️⃣ DELETE : Supprimer un entrepreneur
            if (!entrepreneurs.isEmpty()) {
                Entrepreneur last = entrepreneurs.get(entrepreneurs.size() - 1);
                service.supprimer(last);
                System.out.println("\n🗑️ Entrepreneur supprimé : " + last.getId());
            }

            // 6️⃣ READ final
            entrepreneurs = service.recuperer();
            System.out.println("\n📋 Liste finale des entrepreneurs :");
            for (Entrepreneur e : entrepreneurs) {
                System.out.println(e);
            }

        } catch (SQLException ex) {
            System.out.println("Erreur SQL : " + ex.getMessage());
        } catch (Exception ex) {
            System.out.println("Erreur : " + ex.getMessage());
        }*/

        launch();


    }
}











