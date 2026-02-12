package Services;

import Models.Entrepreneur;
import org.junit.jupiter.api.*;

import java.sql.SQLDataException;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EntrepreneurserviceTest {

    static Entrepreneurservice service;
    static Entrepreneur entrepreneurTest;

    @BeforeAll
    static void setup() {
        service = new Entrepreneurservice();

        entrepreneurTest = new Entrepreneur(
                "TestNom",
                "TestPrenom",
                "test@email.com",
                "123456",
                "99999999",
                "Tunis",
                "2025-02-09"
        );
    }

    // =======================
    // TEST CREATE
    // =======================
    @Test
    @Order(1)
    void testAjouter() throws SQLDataException {
        service.ajouter(entrepreneurTest);

        List<Entrepreneur> list = service.recuperer();
        Assertions.assertFalse(list.isEmpty(), "La liste ne doit pas être vide après ajout");
    }

    // =======================
    // TEST READ
    // =======================
    @Test
    @Order(2)
    void testRecuperer() throws SQLDataException {
        List<Entrepreneur> list = service.recuperer();
        Assertions.assertNotNull(list);
        Assertions.assertTrue(list.size() > 0, "Il doit y avoir au moins un entrepreneur");
    }

    // =======================
    // TEST UPDATE
    // =======================
    @Test
    @Order(3)
    void testModifier() throws SQLDataException {
        List<Entrepreneur> list = service.recuperer();
        Entrepreneur e = list.get(list.size() - 1);

        e.setNom("NomModifie");
        e.setPrenom("PrenomModifie");

        service.modifier(e);

        List<Entrepreneur> updatedList = service.recuperer();
        Entrepreneur updated = updatedList.get(updatedList.size() - 1);

        Assertions.assertEquals("NomModifie", updated.getNom());
        Assertions.assertEquals("PrenomModifie", updated.getPrenom());
    }

    // =======================
    // TEST DELETE
    // =======================
    @Test
    @Order(4)
    void testSupprimer() throws SQLDataException {
        List<Entrepreneur> list = service.recuperer();
        int sizeBefore = list.size();

        Entrepreneur e = list.get(list.size() - 1);
        service.supprimer(e);

        List<Entrepreneur> listAfter = service.recuperer();
        int sizeAfter = listAfter.size();

        Assertions.assertEquals(sizeBefore - 1, sizeAfter);
    }
}
