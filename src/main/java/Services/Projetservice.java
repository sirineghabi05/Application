package Services;

import Models.Projet;
import Utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Projetservice implements iservice<Projet> {

    private Connection connection;

    public Projetservice() {
        this.connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public int ajouter(Projet projet) {
        String sql = "INSERT INTO projet (nom_projet, date_creation, statut, description) VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, projet.getNom_projet());
            ps.setTimestamp(2, Timestamp.valueOf(projet.getDate_creation()));
            ps.setString(3, projet.getStatut());
            ps.setString(4, projet.getDescription());

            int affectedRows = ps.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
            return affectedRows;

        } catch (SQLException e) {
            System.err.println("Erreur ajout projet: " + e.getMessage());
            return -1;
        }
    }

    @Override
    public void supprimer(Projet projet) {
        String sql = "DELETE FROM projet WHERE id_projet = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, projet.getId_projet());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur suppression projet: " + e.getMessage());
        }
    }

    @Override
    public void modifier(Projet projet) {
        String sql = "UPDATE projet SET nom_projet=?, date_creation=?, statut=?, description=? WHERE id_projet=?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, projet.getNom_projet());
            ps.setTimestamp(2, Timestamp.valueOf(projet.getDate_creation()));
            ps.setString(3, projet.getStatut());
            ps.setString(4, projet.getDescription());
            ps.setInt(5, projet.getId_projet());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur modification projet: " + e.getMessage());
        }
    }

    @Override
    public List<Projet> recuperer() {
        String sql = "SELECT * FROM projet ORDER BY date_creation DESC";
        List<Projet> list = new ArrayList<>();

        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {

            while (rs.next()) {
                Projet p = new Projet();
                p.setId_projet(rs.getInt("id_projet"));
                p.setNom_projet(rs.getString("nom_projet"));

                Timestamp timestamp = rs.getTimestamp("date_creation");
                if (timestamp != null) {
                    p.setDate_creation(timestamp.toLocalDateTime());
                }

                p.setStatut(rs.getString("statut"));
                p.setDescription(rs.getString("description"));
                list.add(p);
            }
        } catch (SQLException e) {
            System.err.println("Erreur récupération projets: " + e.getMessage());
        }
        return list;
    }

    // =============== MÉTHODES SUPPLÉMENTAIRES ===============

    public List<Projet> rechercherParNom(String nom) {
        String sql = "SELECT * FROM projet WHERE nom_projet LIKE ?";
        List<Projet> list = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, "%" + nom + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Projet p = new Projet();
                    p.setId_projet(rs.getInt("id_projet"));
                    p.setNom_projet(rs.getString("nom_projet"));

                    Timestamp timestamp = rs.getTimestamp("date_creation");
                    if (timestamp != null) {
                        p.setDate_creation(timestamp.toLocalDateTime());
                    }

                    p.setStatut(rs.getString("statut"));
                    p.setDescription(rs.getString("description"));
                    list.add(p);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur recherche projet: " + e.getMessage());
        }
        return list;
    }

    public Projet getById(int id) {
        String sql = "SELECT * FROM projet WHERE id_projet = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Projet p = new Projet();
                    p.setId_projet(rs.getInt("id_projet"));
                    p.setNom_projet(rs.getString("nom_projet"));

                    Timestamp timestamp = rs.getTimestamp("date_creation");
                    if (timestamp != null) {
                        p.setDate_creation(timestamp.toLocalDateTime());
                    }

                    p.setStatut(rs.getString("statut"));
                    p.setDescription(rs.getString("description"));
                    return p;
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur récupération projet par ID: " + e.getMessage());
        }
        return null;
    }

    public boolean existeParNom(String nom, int idExclu) {
        String sql = "SELECT COUNT(*) FROM projet WHERE nom_projet = ? AND id_projet != ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, nom.trim());
            ps.setInt(2, idExclu);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur vérification existence: " + e.getMessage());
        }
        return false;
    }
}