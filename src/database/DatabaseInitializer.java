package database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Initializes the database tables when the application starts.
 *
 * Uses "CREATE TABLE IF NOT EXISTS" so it is safe to call every time
 * the application runs — tables are only created on the first run.
 *
 * OOP Concepts demonstrated:
 * - EXCEPTION HANDLING: Catches and reports database errors gracefully.
 */
public class DatabaseInitializer {

    /**
     * Creates all required database tables if they do not already exist.
     *
     * Tables created:
     * 1. students  — stores student information
     * 2. subjects  — stores subjects linked to students
     * 3. topics    — stores topics linked to subjects
     */
    public static void initialize() {

        // SQL to create the students table
        String createStudentsTable = """
                CREATE TABLE IF NOT EXISTS students (
                    student_id       INTEGER PRIMARY KEY AUTOINCREMENT,
                    name             TEXT    NOT NULL,
                    semester         INTEGER NOT NULL,
                    daily_study_hours REAL   NOT NULL
                )
                """;

        // SQL to create the subjects table (foreign key to students)
        String createSubjectsTable = """
                CREATE TABLE IF NOT EXISTS subjects (
                    subject_id   INTEGER PRIMARY KEY AUTOINCREMENT,
                    student_id   INTEGER NOT NULL,
                    subject_name TEXT    NOT NULL,
                    exam_date    TEXT    NOT NULL,
                    FOREIGN KEY (student_id) REFERENCES students(student_id)
                )
                """;

        // SQL to create the topics table (foreign key to subjects)
        String createTopicsTable = """
                CREATE TABLE IF NOT EXISTS topics (
                    topic_id        INTEGER PRIMARY KEY AUTOINCREMENT,
                    subject_id      INTEGER NOT NULL,
                    topic_name      TEXT    NOT NULL,
                    difficulty      INTEGER NOT NULL CHECK(difficulty BETWEEN 1 AND 5),
                    estimated_hours REAL    NOT NULL,
                    is_completed    INTEGER NOT NULL DEFAULT 0,
                    FOREIGN KEY (subject_id) REFERENCES subjects(subject_id) ON DELETE CASCADE
                )
                """;

        // Execute table creation using try-with-resources
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            // Enable foreign key enforcement in SQLite
            stmt.execute("PRAGMA foreign_keys = ON");

            // Create all three tables
            stmt.execute(createStudentsTable);
            stmt.execute(createSubjectsTable);
            stmt.execute(createTopicsTable);

            System.out.println("[Database] Tables initialized successfully.");

        } catch (SQLException e) {
            System.out.println("[Database Error] Failed to initialize tables: " + e.getMessage());
        }
    }
}

