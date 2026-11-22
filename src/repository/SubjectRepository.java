package repository;

import database.DatabaseConnection;
import model.Subject;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles all database operations (CRUD) for the Subject entity.
 *
 * OOP Concepts demonstrated:
 * - EXCEPTION HANDLING: Every database operation is wrapped in try-catch.
 * - COLLECTIONS: Uses ArrayList to return lists of subjects.
 */
public class SubjectRepository {

    /**
     * Adds a new subject to the database.
     *
     * @param subject The Subject object to insert
     * @return The auto-generated subject ID, or -1 if the insert failed
     */
    public int addSubject(Subject subject) {
        String sql = "INSERT INTO subjects (student_id, subject_name, exam_date) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, subject.getStudentId());
            pstmt.setString(2, subject.getSubjectName());
            pstmt.setString(3, subject.getExamDate());
            pstmt.executeUpdate();

            ResultSet keys = pstmt.getGeneratedKeys();
            if (keys.next()) {
                return keys.getInt(1);
            }

        } catch (SQLException e) {
            System.out.println("[Error] Failed to add subject: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Retrieves a subject by its ID.
     *
     * @param subjectId The subject ID to look up
     * @return The Subject object, or null if not found
     */
    public Subject getSubject(int subjectId) {
        String sql = "SELECT * FROM subjects WHERE subject_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, subjectId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractSubject(rs);
            }

        } catch (SQLException e) {
            System.out.println("[Error] Failed to get subject: " + e.getMessage());
        }
        return null;
    }

    /**
     * Gets all subjects belonging to a specific student.
     *
     * @param studentId The student's ID
     * @return A list of Subject objects for the given student
     */
    public List<Subject> getSubjectsByStudent(int studentId) {
        List<Subject> subjects = new ArrayList<>();
        String sql = "SELECT * FROM subjects WHERE student_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                subjects.add(extractSubject(rs));
            }

        } catch (SQLException e) {
            System.out.println("[Error] Failed to get subjects: " + e.getMessage());
        }
        return subjects;
    }

    /**
     * Updates an existing subject's name and exam date.
     *
     * @param subject The Subject object with updated values
     */
    public void updateSubject(Subject subject) {
        String sql = "UPDATE subjects SET subject_name = ?, exam_date = ? WHERE subject_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, subject.getSubjectName());
            pstmt.setString(2, subject.getExamDate());
            pstmt.setInt(3, subject.getSubjectId());
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("[Error] Failed to update subject: " + e.getMessage());
        }
    }

    /**
     * Deletes a subject and ALL its related topics.
     * Uses a database transaction to ensure both deletions succeed or both fail.
     *
     * @param subjectId The ID of the subject to delete
     */
    public void deleteSubject(int subjectId) {
        // Delete topics first (child records), then the subject (parent record)
        String deleteTopics = "DELETE FROM topics WHERE subject_id = ?";
        String deleteSubject = "DELETE FROM subjects WHERE subject_id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {

            // Start a transaction — both deletes must succeed together
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt1 = conn.prepareStatement(deleteTopics);
                 PreparedStatement pstmt2 = conn.prepareStatement(deleteSubject)) {

                // Delete all topics under this subject
                pstmt1.setInt(1, subjectId);
                pstmt1.executeUpdate();

                // Delete the subject itself
                pstmt2.setInt(1, subjectId);
                pstmt2.executeUpdate();

                // Commit the transaction
                conn.commit();

            } catch (SQLException e) {
                // If anything goes wrong, rollback both operations
                conn.rollback();
                throw e;
            }

        } catch (SQLException e) {
            System.out.println("[Error] Failed to delete subject: " + e.getMessage());
        }
    }

    /**
     * Helper method to create a Subject object from a ResultSet row.
     */
    private Subject extractSubject(ResultSet rs) throws SQLException {
        return new Subject(
                rs.getInt("subject_id"),
                rs.getInt("student_id"),
                rs.getString("subject_name"),
                rs.getString("exam_date")
        );
    }
}

