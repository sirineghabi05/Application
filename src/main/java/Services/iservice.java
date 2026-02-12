package Services;

import java.sql.SQLDataException;
import java.sql.SQLException;
import java.util.List;

public interface iservice <T> {
    int ajouter (T t) throws SQLDataException, SQLException;
    void supprimer (T t) throws SQLDataException, SQLException;
    void modifier (T t) throws SQLDataException, SQLException;
    List<T> recuperer () throws SQLDataException, SQLException;
}
