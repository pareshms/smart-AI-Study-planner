package repository;

import database.DatabaseConnection;
import model.Student;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles all database operations (CRUD) for the Student entity.
 *
 * OOP Concepts demonstrated:
 * - EXCEPTION HANDLING: Every database operation is wrapped in try-catch.
 * - COLLECTIONS: Uses ArrayList to store and return lists of students.
 * - METHOD OVERLOADING: getStudent(int id) vs getStudent(String name).
 * - ENCAPSULATION: Database logic is encapsulated within this class.
 */
public class StudentRepository {

    /**
     * Adds a new student to the database.
     *
     * @param student The Student object to insert
     * @return The auto-generated student ID, or -1 if the insert failed
     */
    public int addStudent(Student student) {
        String sql = "INSERT INTO students (name, semester, daily_study_hours) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, student.getName());
            pstmt.setInt(2, student.getSemester());
            pstmt.setDouble(3, student.getDailyStudyHours());
            pstmt.executeUpdate();

            // Retrieve the auto-generated ID
            ResultSet keys = pstmt.getGeneratedKeys();
            if (keys.next()) {
                return keys.getInt(1);
            }

        } catch (SQLException e) {
            System.out.println("[Error] Failed to add student: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Retrieves a student by their ID.
     *
     * METHOD OVERLOADING: This method has the same name as getStudent(String)
     * but accepts a different parameter type (int vs String).
     *
     * @param id The student ID to look up
     * @return The Student object, or null if not found
     */
    public Student getStudent(int id) {
        String sql = "SELECT * FROM students WHERE student_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractStudent(rs);
            }

        } catch (SQLException e) {
            System.out.println("[Error] Failed to get student: " + e.getMessage());
        }
        return null;
    }

    /**
     * Retrieves a student by their name.
     *
     * METHOD OVERLOADING: This method has the same name as getStudent(int)
     * but accepts a different parameter type (String vs int).
     *
     * @param name The student name to look up
     * @return The Student object, or null if not found
     */
    public Student getStudent(String name) {
        String sql = "SELECT * FROM students WHERE name = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractStudent(rs);
            }

        } catch (SQLException e) {
            System.out.println("[Error] Failed to get student: " + e.getMessage());
        }
        return null;
    }

    /**
     * Updates an existing student's details in the database.
     *
     * @param student The Student object with updated values
     */
    public void updateStudent(Student student) {
        String sql = "UPDATE students SET name = ?, semester = ?, daily_study_hours = ? WHERE student_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, student.getName());
            pstmt.setInt(2, student.getSemester());
            pstmt.setDouble(3, student.getDailyStudyHours());
            pstmt.setInt(4, student.getId());
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("[Error] Failed to update student: " + e.getMessage());
        }
    }

    /**
     * Gets a list of all registered students.
     * Uses ArrayList — demonstrates COLLECTIONS.
     *
     * @return A list of all Student objects in the database
     */
    public List<Student> getAllStudents() {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT * FROM students";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                students.add(extractStudent(rs));
            }

        } catch (SQLException e) {
            System.out.println("[Error] Failed to get students: " + e.getMessage());
        }
        return students;
    }

    /**
     * Helper method to create a Student object from a ResultSet row.
     *
     * @param rs The ResultSet positioned at the current row
     * @return A new Student object populated with database values
     * @throws SQLException If a database column cannot be read
     */
    private Student extractStudent(ResultSet rs) throws SQLException {
        return new Student(
                rs.getInt("student_id"),
                rs.getString("name"),
                rs.getInt("semester"),
                rs.getDouble("daily_study_hours")
        );
    }
}

