package model;

/**
 * Represents a topic within a subject that the student needs to study.
 *
 * OOP Concepts demonstrated:
 * - ENCAPSULATION: Private fields with public getters/setters.
 * - CONSTRUCTORS: Multiple constructors for different use cases.
 */
public class Topic {

    // ===== Encapsulation: private fields =====
    private int topicId;
    private int subjectId;
    private String topicName;
    private int difficulty;        // 1 (Very Easy) to 5 (Very Hard)
    private double estimatedHours; // Estimated time to study this topic
    private boolean isCompleted;   // Whether the student has finished studying this

    // ===== Constructors =====

    /**
     * Default constructor.
     */
    public Topic() {
    }

    /**
     * Full parameterized constructor (used when reading from database).
     *
     * @param topicId        Topic ID from database
     * @param subjectId      Subject this topic belongs to
     * @param topicName      Name of the topic
     * @param difficulty     Difficulty level (1-5)
     * @param estimatedHours Estimated hours to study
     * @param isCompleted    Whether the topic is completed
     */
    public Topic(int topicId, int subjectId, String topicName,
                 int difficulty, double estimatedHours, boolean isCompleted) {
        this.topicId = topicId;
        this.subjectId = subjectId;
        this.topicName = topicName;
        this.difficulty = difficulty;
        this.estimatedHours = estimatedHours;
        this.isCompleted = isCompleted;
    }

    /**
     * Constructor for creating a new topic (without ID, defaults to not completed).
     *
     * @param subjectId      Subject this topic belongs to
     * @param topicName      Name of the topic
     * @param difficulty     Difficulty level (1-5)
     * @param estimatedHours Estimated hours to study
     */
    public Topic(int subjectId, String topicName, int difficulty, double estimatedHours) {
        this.subjectId = subjectId;
        this.topicName = topicName;
        this.difficulty = difficulty;
        this.estimatedHours = estimatedHours;
        this.isCompleted = false; // New topics are not completed
    }

    // ===== Getters and Setters =====

    public int getTopicId() {
        return topicId;
    }

    public void setTopicId(int topicId) {
        this.topicId = topicId;
    }

    public int getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(int subjectId) {
        this.subjectId = subjectId;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public double getEstimatedHours() {
        return estimatedHours;
    }

    public void setEstimatedHours(double estimatedHours) {
        this.estimatedHours = estimatedHours;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    // ===== Helper Method =====

    /**
     * Converts the numeric difficulty level to a human-readable label.
     *
     * @return A text description of the difficulty
     */
    public String getDifficultyText() {
        switch (difficulty) {
            case 1:  return "Very Easy";
            case 2:  return "Easy";
            case 3:  return "Medium";
            case 4:  return "Hard";
            case 5:  return "Very Hard";
            default: return "Unknown";
        }
    }

    // ===== toString =====

    @Override
    public String toString() {
        return "Topic ID: " + topicId
                + " | Name: " + topicName
                + " | Difficulty: " + getDifficultyText() + " (" + difficulty + "/5)"
                + " | Hours: " + estimatedHours
                + " | Status: " + (isCompleted ? "Completed" : "Pending");
    }
}

