package managers.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Clasa este responsabilă de a crea conexiunea spre baza de date a aplicației
 */
public class ConexiuneDB {

    private final Connection SQLConnection;

    /**
     * Constructorul clasei {@link ConexiuneDB}
     * @throws SQLException arnuncă excepție în caz că intervine vreo eroare în timpul creării conexiunii cu baza de date
     */
    public ConexiuneDB() throws SQLException {
        /* TODO: Trebuie să schimbi credențialele aici  */
        String databaseUrl = "jdbc:postgresql://localhost:5432/ProiectP3";
        String databaseUsername = "postgres";
        String databasePassword = "dd221101";
        SQLConnection = DriverManager.getConnection(databaseUrl, databaseUsername, databasePassword);
    }

    /**
     * Getter-ul ce returnează obiectul de tip {@link Connection}
     * @return returnează {@link #SQLConnection}
     */
    public Connection getSQLConnection() {
        return SQLConnection;
    }

}
