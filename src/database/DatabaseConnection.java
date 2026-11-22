package database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Manages the SQLite database connection.
 *
 * This class provides a static method to get a database connection.
 * It automatically creates the database directory and file if they don't exist.
 *
 * OOP Concepts demonstrated:
 * - ENCAPSULATION: Database URL is private; access is through a public method.
 * - EXCEPTION HANDLING: SQLException is properly thrown/handled.
 */
public class DatabaseConnection {

    // ===== Encapsulation: private constants =====
    private static final String DB_FOLDER = "database";
    private static final String DB_URL = "jdbc:sqlite:" + DB_FOLDER + "/studyplanner.db";

    /**
     * Returns a new connection to the SQLite database.
     * Creates the database directory if it does not exist.
     *
     * IMPORTANT: Each caller is responsible for closing this connection
     * (preferably using try-with-resources).
     *
     * @return A Connection object to the SQLite database
     * @throws SQLException If the connection cannot be established
     */
    public static Connection getConnection() throws SQLException {
        // Create the database directory if it doesn't exist
        File dbDir = new File(DB_FOLDER);
        if (!dbDir.exists()) {
            dbDir.mkdirs();
        }

        // DriverManager automatically loads the SQLite JDBC driver
        return DriverManager.getConnection(DB_URL);
    }
}

