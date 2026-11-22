package service;

import model.StudySession;
import model.Subject;
import model.Topic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Default study strategy implementation that prioritizes topics by their
 * calculated priority score (difficulty + urgency + effort).
 *
 * OOP Concepts demonstrated:
 * - POLYMORPHISM: This class implements the StudyStrategy interface.
 *   The StudyPlanner can use this strategy or any other implementation
 *   of StudyStrategy — the code works the same either way.
 *
 * How this strategy works:
 * 1. Calculate a priority score for each pending topic.
 * 2. Sort topics by priority (highest first).
 * 3. Allocate study time starting from the highest-priority topic.
 * 4. Stop when daily available hours are used up.
 */
public class DefaultStudyStrategy implements StudyStrategy {

    private PriorityCalculator priorityCalculator;

    /**
     * Constructor — creates the priority calculator used for scoring.
     */
    public DefaultStudyStrategy() {
        this.priorityCalculator = new PriorityCalculator();
    }

    /**
     * Generates a daily study plan sorted by priority score.
     *
     * @param topics         Pending topics to schedule
     * @param subjectMap     Map of subject ID → Subject (for exam dates)
     * @param availableHours Daily study hours available
     * @return Ordered list of study sessions fitting within available hours
     */
    @Override
    public List<StudySession> generatePlan(List<Topic> topics,
                                            Map<Integer, Subject> subjectMap,
                                            double availableHours) {

        List<StudySession> sessions = new ArrayList<>();

        // Create a copy of the list so we don't modify the original
        List<Topic> sortedTopics = new ArrayList<>(topics);

        // Sort topics by priority score in DESCENDING order (highest first)
        sortedTopics.sort((t1, t2) -> {
            Subject s1 = subjectMap.get(t1.getSubjectId());
            Subject s2 = subjectMap.get(t2.getSubjectId());

            // Get exam dates (empty string as fallback)
            String examDate1 = (s1 != null) ? s1.getExamDate() : "";
            String examDate2 = (s2 != null) ? s2.getExamDate() : "";

            // Calculate priority for both topics
            double p1 = priorityCalculator.calculatePriority(t1, examDate1);
            double p2 = priorityCalculator.calculatePriority(t2, examDate2);

            // Sort descending (higher priority first)
            return Double.compare(p2, p1);
        });

        // Allocate study time to topics based on priority order
        double remainingHours = availableHours;

        for (Topic topic : sortedTopics) {
            if (remainingHours <= 0) {
                break; // No more time available
            }

            // Get the subject name for display
            Subject subject = subjectMap.get(topic.getSubjectId());
            String subjectName = (subject != null) ? subject.getSubjectName() : "Unknown";

            // Allocate either the full estimated time or whatever time is left
            double allocatedTime = Math.min(topic.getEstimatedHours(), remainingHours);

            // Create a study session and add it to the plan
            sessions.add(new StudySession(topic, subjectName, allocatedTime));
            remainingHours -= allocatedTime;
        }

        return sessions;
    }

    /**
     * Returns the name of this strategy.
     */
    @Override
    public String getStrategyName() {
        return "Default Priority-Based Strategy";
    }
}

