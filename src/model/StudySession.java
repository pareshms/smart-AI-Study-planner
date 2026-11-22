package model;

/**
 * Represents a single study session in the generated timetable.
 * A study session pairs a Topic with the allocated study time.
 *
 * OOP Concepts demonstrated:
 * - CLASSES AND OBJECTS: This class models a real-world concept (a study session).
 * - ENCAPSULATION: Private fields with public getters/setters.
 */
public class StudySession {

    // ===== Encapsulation: private fields =====
    private Topic topic;           // The topic to study
    private String subjectName;    // Name of the subject (for display purposes)
    private double allocatedHours; // How many hours are allocated for this session

    // ===== Constructors =====

    /**
     * Default constructor.
     */
    public StudySession() {
    }

    /**
     * Parameterized constructor.
     *
     * @param topic          The topic to study in this session
     * @param subjectName    Name of the subject the topic belongs to
     * @param allocatedHours Hours allocated for this study session
     */
    public StudySession(Topic topic, String subjectName, double allocatedHours) {
        this.topic = topic;
        this.subjectName = subjectName;
        this.allocatedHours = allocatedHours;
    }

    // ===== Getters and Setters =====

    public Topic getTopic() {
        return topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public double getAllocatedHours() {
        return allocatedHours;
    }

    public void setAllocatedHours(double allocatedHours) {
        this.allocatedHours = allocatedHours;
    }

    // ===== toString =====

    @Override
    public String toString() {
        return String.format("%-25s | %-20s | %.1f hrs | Difficulty: %s",
                topic.getTopicName(), subjectName, allocatedHours, topic.getDifficultyText());
    }
}

