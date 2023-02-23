package managers.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.logging.Logger;

import static java.sql.DriverManager.getConnection;
import static java.util.Optional.ofNullable;

@SuppressWarnings("FieldCanBeLocal")
public class DatabaseManager {
    private Connection databaseConnection;
    private final String DATABASE_URL = "jdbc:postgresql://localhost:5432/ProiectP3";
    private final String USERNAME = "postgres";
//    private final String PASSWORD = "dd221101";
    private final String PASSWORD = "dd221101";
    private final static Logger LOGGER = Logger.getLogger(DatabaseManager.class.getName());

    public DatabaseManager() {
        try {
            databaseConnection = getConnection(DATABASE_URL, USERNAME, PASSWORD);
        } catch (SQLException sqlException) {
            LOGGER.severe(sqlException.getMessage());
        }
    }

    public Optional<Connection> getDatabaseConnection() {
        return ofNullable(databaseConnection);
    }
}
