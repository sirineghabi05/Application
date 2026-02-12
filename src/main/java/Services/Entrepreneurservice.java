package Services;

import Models.Entrepreneur;
import Utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Entrepreneurservice implements iservice<Entrepreneur> {

    private Connection connection;

    public Entrepreneurservice() {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public int ajouter(Entrepreneur entrepreneur) throws SQLDataException {
        String sql = "INSERT INTO entrepreneur (nom, prénom, email, motDePasse, télephone, adresse, datediscription) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, entrepreneur.getNom());
            ps.setString(2, entrepreneur.getPrenom());
            ps.setString(3, entrepreneur.getEmail());
            ps.setString(4, entrepreneur.getMotDePasse());
            ps.setString(5, entrepreneur.getTelephone());
            ps.setString(6, entrepreneur.getAdresse());
            ps.setString(7, entrepreneur.getDateInscription());

            int rowsAffected = ps.executeUpdate();

            // Récupérer l'ID généré
            if (rowsAffected > 0) {
                ResultSet generatedKeys = ps.getGeneratedKeys();
                if (generatedKeys.next()) {
                    entrepreneur.setId(generatedKeys.getInt(1));
                }
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout de l'entrepreneur : " + e.getMessage());
            throw new SQLDataException("Impossible d'ajouter l'entrepreneur : " + e.getMessage());
        }
        return 0;
    }

    @Override
    public void supprimer(Entrepreneur entrepreneur) throws SQLDataException {
        String sql = "DELETE FROM entrepreneur WHERE id = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, entrepreneur.getId());
            int rowsAffected = ps.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLDataException("Aucun entrepreneur trouvé avec l'ID : " + entrepreneur.getId());
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de l'entrepreneur : " + e.getMessage());
            throw new SQLDataException("Impossible de supprimer l'entrepreneur : " + e.getMessage());
        }
    }

    @Override
    public void modifier(Entrepreneur entrepreneur) throws SQLDataException {
        String sql = "UPDATE entrepreneur SET nom=?, prénom=?, email=?, motDePasse=?, télephone=?, adresse=?, datediscription=? WHERE id =?";

        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, entrepreneur.getNom());
            ps.setString(2, entrepreneur.getPrenom());
            ps.setString(3, entrepreneur.getEmail());
            ps.setString(4, entrepreneur.getMotDePasse());
            ps.setString(5, entrepreneur.getTelephone());
            ps.setString(6, entrepreneur.getAdresse());
            ps.setString(7, entrepreneur.getDateInscription());
            ps.setInt(8, entrepreneur.getId());

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLDataException("Aucun entrepreneur trouvé avec l'ID : " + entrepreneur.getId());
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la modification de l'entrepreneur : " + e.getMessage());
            throw new SQLDataException("Impossible de modifier l'entrepreneur : " + e.getMessage());
        }
    }

    @Override
    public List<Entrepreneur> recuperer() throws SQLDataException {
        String sql = "SELECT * FROM entrepreneur ORDER BY nom ASC";
        List<Entrepreneur> list = new ArrayList<>();

        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(sql);

            while (rs.next()) {
                Entrepreneur e = creerEntrepreneurDepuisResultSet(rs);
                list.add(e);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des entrepreneurs : " + e.getMessage());
            throw new SQLDataException("Impossible de récupérer les entrepreneurs : " + e.getMessage());
        }
        return list;
    }

    /**
     * Récupère un entrepreneur par son ID
     */
    public Entrepreneur recuperer(int idEntrepreneur) throws SQLDataException {
        String sql = "SELECT * FROM entrepreneur WHERE id = ?";

        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, idEntrepreneur);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return creerEntrepreneurDepuisResultSet(rs);
            } else {
                throw new SQLDataException("Aucun entrepreneur trouvé avec l'ID : " + idEntrepreneur);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de l'entrepreneur : " + e.getMessage());
            throw new SQLDataException("Impossible de récupérer l'entrepreneur : " + e.getMessage());
        }
    }

    /**
     * Récupère un entrepreneur par son email
     */
    public Entrepreneur recupererParEmail(String email) throws SQLDataException {
        String sql = "SELECT * FROM entrepreneur WHERE email = ?";

        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return creerEntrepreneurDepuisResultSet(rs);
            } else {
                return null;
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche par email : " + e.getMessage());
            throw new SQLDataException("Impossible de rechercher l'entrepreneur : " + e.getMessage());
        }
    }

    /**
     * Recherche des entrepreneurs par nom ou prénom
     */
    public List<Entrepreneur> rechercher(String recherche) throws SQLDataException {
        String sql = "SELECT * FROM entrepreneur WHERE nom LIKE ? OR prénom LIKE ? OR email LIKE ? ORDER BY nom ASC";
        List<Entrepreneur> list = new ArrayList<>();

        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            String recherchePattern = "%" + recherche + "%";
            ps.setString(1, recherchePattern);
            ps.setString(2, recherchePattern);
            ps.setString(3, recherchePattern);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Entrepreneur e = creerEntrepreneurDepuisResultSet(rs);
                list.add(e);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche des entrepreneurs : " + e.getMessage());
            throw new SQLDataException("Impossible de rechercher les entrepreneurs : " + e.getMessage());
        }
        return list;
    }

    /**
     * Vérifie si un email existe déjà
     */
    public boolean emailExiste(String email) throws SQLDataException {
        String sql = "SELECT COUNT(*) FROM entrepreneur WHERE email = ?";

        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification de l'email : " + e.getMessage());
            throw new SQLDataException("Impossible de vérifier l'email : " + e.getMessage());
        }
        return false;
    }

    /**
     * Récupère le nombre total d'entrepreneurs
     */
    public int getNombreEntrepreneurs() throws SQLDataException {
        String sql = "SELECT COUNT(*) FROM entrepreneur";

        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(sql);

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors du comptage des entrepreneurs : " + e.getMessage());
            throw new SQLDataException("Impossible de compter les entrepreneurs : " + e.getMessage());
        }
        return 0;
    }

    /**
     * Récupère les entrepreneurs avec pagination
     */
    public List<Entrepreneur> recupererAvecPagination(int page, int taillePage) throws SQLDataException {
        String sql = "SELECT * FROM entrepreneur ORDER BY nom ASC LIMIT ? OFFSET ?";
        List<Entrepreneur> list = new ArrayList<>();

        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, taillePage);
            ps.setInt(2, (page - 1) * taillePage);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Entrepreneur e = creerEntrepreneurDepuisResultSet(rs);
                list.add(e);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la pagination des entrepreneurs : " + e.getMessage());
            throw new SQLDataException("Impossible de récupérer les entrepreneurs : " + e.getMessage());
        }
        return list;
    }

    /**
     * Récupère les entrepreneurs actifs (inscrits récemment)
     */
    public List<Entrepreneur> recupererEntrepreneursActifs(int limite) throws SQLDataException {
        String sql = "SELECT * FROM entrepreneur ORDER BY datediscription DESC LIMIT ?";
        List<Entrepreneur> list = new ArrayList<>();

        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, limite);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Entrepreneur e = creerEntrepreneurDepuisResultSet(rs);
                list.add(e);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des entrepreneurs actifs : " + e.getMessage());
            throw new SQLDataException("Impossible de récupérer les entrepreneurs actifs : " + e.getMessage());
        }
        return list;
    }

    /**
     * Récupère les statistiques des entrepreneurs
     */
    public int getNombreProjetsParEntrepreneur(int idEntrepreneur) throws SQLDataException {
        // Cette méthode suppose que vous avez une table 'projet' avec une clé étrangère 'entrepreneur_id'
        String sql = "SELECT COUNT(*) FROM projet WHERE entrepreneur_id = ?";

        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, idEntrepreneur);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            // Si la table n'existe pas, retourner 0
            System.err.println("Table projet non trouvée ou erreur : " + e.getMessage());
            return 0; // Retourne 0 par défaut pour la démo
        }
        return 0;
    }

    /**
     * Méthode utilitaire pour créer un Entrepreneur à partir d'un ResultSet
     */
    private Entrepreneur creerEntrepreneurDepuisResultSet(ResultSet rs) throws SQLException {
        Entrepreneur e = new Entrepreneur();
        e.setId(rs.getInt("id"));
        e.setNom(rs.getString("nom"));
        e.setPrenom(rs.getString("prénom"));
        e.setEmail(rs.getString("email"));
        e.setMotDePasse(rs.getString("motDePasse"));
        e.setTelephone(rs.getString("télephone"));
        e.setAdresse(rs.getString("adresse"));
        e.setDateInscription(rs.getString("datediscription"));
        return e;
    }

    /**
     * Méthode pour obtenir un entrepreneur par son ID (alias de recuperer)
     * Compatible avec le contrôleur
     */
    public Entrepreneur getEntrepreneurById(int id) {
        try {
            return recuperer(id);
        } catch (SQLDataException e) {
            System.err.println("Erreur dans getEntrepreneurById: " + e.getMessage());
            return null;
        }
    }

    /**
     * Authentifie un entrepreneur avec email et mot de passe
     */
    public Entrepreneur authentifier(String email, String motDePasse) throws SQLDataException {
        String sql = "SELECT * FROM entrepreneur WHERE email = ? AND motDePasse = ?";

        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, email);
            ps.setString(2, motDePasse);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return creerEntrepreneurDepuisResultSet(rs);
            } else {
                return null;
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'authentification : " + e.getMessage());
            throw new SQLDataException("Impossible d'authentifier l'entrepreneur : " + e.getMessage());
        }
    }

    /**
     * Change le mot de passe d'un entrepreneur
     */
    public void changerMotDePasse(int idEntrepreneur, String nouveauMotDePasse) throws SQLDataException {
        String sql = "UPDATE entrepreneur SET motDePasse = ? WHERE id = ?";

        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, nouveauMotDePasse);
            ps.setInt(2, idEntrepreneur);

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLDataException("Aucun entrepreneur trouvé avec l'ID : " + idEntrepreneur);
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors du changement de mot de passe : " + e.getMessage());
            throw new SQLDataException("Impossible de changer le mot de passe : " + e.getMessage());
        }
    }

    /**
     * Met à jour le statut d'un entrepreneur
     */
    public void mettreAJourStatut(int idEntrepreneur, String statut) throws SQLDataException {
        // Cette méthode suppose que vous avez une colonne 'statut' dans votre table
        String sql = "UPDATE entrepreneur SET statut = ? WHERE id = ?";

        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, statut);
            ps.setInt(2, idEntrepreneur);

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLDataException("Aucun entrepreneur trouvé avec l'ID : " + idEntrepreneur);
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du statut : " + e.getMessage());
            // Si la colonne n'existe pas, on ignore l'erreur
        }
    }
}