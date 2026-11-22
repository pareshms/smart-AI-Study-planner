package repository;

import database.DatabaseConnection;
import model.Topic;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles all database operations (CRUD) for the Topic entity.
 *
 * OOP Concepts demonstrated:
 * - EXCEPTION HANDLING: Every database operation is wrapped in try-catch.
 * - COLLECTIONS: Uses ArrayList to return lists of topics.
 */
public class TopicRepository {

    /**
     * Adds a new topic to the database.
     *
     * @param topic The Topic object to insert
     * @return The auto-generated topic ID, or -1 if the insert failed
     */
    public int addTopic(Topic topic) {
        String sql = "INSERT INTO topics (subject_id, topic_name, difficulty, estimated_hours, is_completed) "
                   + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, topic.getSubjectId());
            pstmt.setString(2, topic.getTopicName());
            pstmt.setInt(3, topic.getDifficulty());
            pstmt.setDouble(4, topic.getEstimatedHours());
            pstmt.setInt(5, topic.isCompleted() ? 1 : 0);
            pstmt.executeUpdate();

            ResultSet keys = pstmt.getGeneratedKeys();
            if (keys.next()) {
                return keys.getInt(1);
            }

        } catch (SQLException e) {
            System.out.println("[Error] Failed to add topic: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Retrieves a single topic by its ID.
     *
     * @param topicId The topic ID to look up
     * @return The Topic object, or null if not found
     */
    public Topic getTopic(int topicId) {
        String sql = "SELECT * FROM topics WHERE topic_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, topicId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractTopic(rs);
            }

        } catch (SQLException e) {
            System.out.println("[Error] Failed to get topic: " + e.getMessage());
        }
        return null;
    }

    /**
     * Gets all topics for a specific subject.
     *
     * @param subjectId The subject ID
     * @return A list of Topic objects for the given subject
     */
    public List<Topic> getTopicsBySubject(int subjectId) {
        List<Topic> topics = new ArrayList<>();
        String sql = "SELECT * FROM topics WHERE subject_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, subjectId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                topics.add(extractTopic(rs));
            }

        } catch (SQLException e) {
            System.out.println("[Error] Failed to get topics: " + e.getMessage());
        }
        return topics;
    }

    /**
     * Gets ALL topics for a student (across all their subjects).
     * Uses a JOIN query to link topics → subjects → student.
     *
     * @param studentId The student's ID
     * @return A list of all Topic objects for the student
     */
    public List<Topic> getAllTopicsByStudent(int studentId) {
        List<Topic> topics = new ArrayList<>();
        String sql = "SELECT t.* FROM topics t "
                   + "JOIN subjects s ON t.subject_id = s.subject_id "
                   + "WHERE s.student_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                topics.add(extractTopic(rs));
            }

        } catch (SQLException e) {
            System.out.println("[Error] Failed to get topics: " + e.getMessage());
        }
        return topics;
    }

    /**
     * Gets only PENDING (not completed) topics for a student.
     * These are the topics that still need to be studied.
     *
     * @param studentId The student's ID
     * @return A list of pending Topic objects
     */
    public List<Topic> getPendingTopicsByStudent(int studentId) {
        List<Topic> topics = new ArrayList<>();
        String sql = "SELECT t.* FROM topics t "
                   + "JOIN subjects s ON t.subject_id = s.subject_id "
                   + "WHERE s.student_id = ? AND t.is_completed = 0";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                topics.add(extractTopic(rs));
            }

        } catch (SQLException e) {
            System.out.println("[Error] Failed to get pending topics: " + e.getMessage());
        }
        return topics;
    }

    /**
     * Marks a topic as completed in the database.
     *
     * @param topicId The ID of the topic to mark as completed
     */
    public void markCompleted(int topicId) {
        String sql = "UPDATE topics SET is_completed = 1 WHERE topic_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, topicId);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("[Error] Failed to mark topic as completed: " + e.getMessage());
        }
    }

    /**
     * Deletes a topic from the database.
     *
     * @param topicId The ID of the topic to delete
     */
    public void deleteTopic(int topicId) {
        String sql = "DELETE FROM topics WHERE topic_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, topicId);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("[Error] Failed to delete topic: " + e.getMessage());
        }
    }

    /**
     * Helper method to create a Topic object from a ResultSet row.
     */
    private Topic extractTopic(ResultSet rs) throws SQLException {
        return new Topic(
                rs.getInt("topic_id"),
                rs.getInt("subject_id"),
                rs.getString("topic_name"),
                rs.getInt("difficulty"),
                rs.getDouble("estimated_hours"),
                rs.getInt("is_completed") == 1
        );
    }
}

