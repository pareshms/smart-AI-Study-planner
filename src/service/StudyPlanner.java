package service;

import model.StudySession;
import model.Subject;
import model.Topic;
import repository.SubjectRepository;
import repository.TopicRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Core service class that coordinates study planning and recommendations.
 *
 * This class uses a StudyStrategy (interface) to generate study plans.
 * The strategy can be changed at runtime — this is POLYMORPHISM in action.
 *
 * OOP Concepts demonstrated:
 * - POLYMORPHISM: Uses the StudyStrategy interface; different strategies
 *   can be plugged in without changing this class.
 * - COLLECTIONS: Uses ArrayList, HashMap, LinkedHashMap.
 * - CONSTRUCTORS: Default constructor and parameterized constructor.
 * - EXCEPTION HANDLING: Handles null values and empty lists gracefully.
 */
public class StudyPlanner {

    // ===== Encapsulation: private fields =====
    private StudyStrategy strategy;             // The current study strategy
    private TopicRepository topicRepository;     // For fetching topics
    private SubjectRepository subjectRepository; // For fetching subjects
    private PriorityCalculator priorityCalculator;

    // ===== Constructors =====

    /**
     * Default constructor — uses the DefaultStudyStrategy.
     */
    public StudyPlanner() {
        this.strategy = new DefaultStudyStrategy();
        this.topicRepository = new TopicRepository();
        this.subjectRepository = new SubjectRepository();
        this.priorityCalculator = new PriorityCalculator();
    }

    /**
     * Parameterized constructor — accepts a custom strategy.
     * This demonstrates POLYMORPHISM: any class implementing StudyStrategy works here.
     *
     * @param strategy The study strategy to use
     */
    public StudyPlanner(StudyStrategy strategy) {
        this.strategy = strategy;
        this.topicRepository = new TopicRepository();
        this.subjectRepository = new SubjectRepository();
        this.priorityCalculator = new PriorityCalculator();
    }

    /**
     * Changes the study strategy at runtime.
     * This is the Strategy Pattern — a classic example of POLYMORPHISM.
     *
     * @param strategy The new study strategy to use
     */
    public void setStrategy(StudyStrategy strategy) {
        this.strategy = strategy;
    }

    // ==========================================================
    // Study Plan Generation
    // ==========================================================

    /**
     * Generates a daily study plan for the given student.
     *
     * Steps:
     * 1. Fetch all pending (not completed) topics for the student.
     * 2. Build a map of subject IDs to Subject objects (for exam dates).
     * 3. Delegate to the current strategy to generate the plan.
     *
     * @param studentId      The student's ID
     * @param availableHours Hours available for study today
     * @return A list of StudySession objects forming the daily plan
     */
    public List<StudySession> generateDailyPlan(int studentId, double availableHours) {
        // Get all pending topics for this student
        List<Topic> pendingTopics = topicRepository.getPendingTopicsByStudent(studentId);

        if (pendingTopics.isEmpty()) {
            return new ArrayList<>(); // No topics to schedule
        }

        // Build a subject map for quick lookup
        Map<Integer, Subject> subjectMap = buildSubjectMap(studentId);

        // Use the strategy to generate the plan (POLYMORPHISM)
        return strategy.generatePlan(pendingTopics, subjectMap, availableHours);
    }

    // ==========================================================
    // Smart Recommendations
    // ==========================================================

    /**
     * Gets the single most important topic to study next.
     * Finds the pending topic with the highest priority score.
     *
     * @param studentId The student's ID
     * @return The highest-priority pending Topic, or null if all are completed
     */
    public Topic getTopRecommendation(int studentId) {
        List<Topic> pendingTopics = topicRepository.getPendingTopicsByStudent(studentId);
        Map<Integer, Subject> subjectMap = buildSubjectMap(studentId);

        if (pendingTopics.isEmpty()) {
            return null; // All topics completed!
        }

        Topic bestTopic = null;
        double highestPriority = -1;

        // Find the topic with the highest priority score
        for (Topic topic : pendingTopics) {
            Subject subject = subjectMap.get(topic.getSubjectId());
            String examDate = (subject != null) ? subject.getExamDate() : "";
            double priority = priorityCalculator.calculatePriority(topic, examDate);

            if (priority > highestPriority) {
                highestPriority = priority;
                bestTopic = topic;
            }
        }

        return bestTopic;
    }

    /**
     * Gets completed topics that are HARD (difficulty >= 4).
     * These topics should be revised because they are likely to be forgotten.
     *
     * @param studentId The student's ID
     * @return A list of hard, completed topics recommended for revision
     */
    public List<Topic> getRevisionRecommendations(int studentId) {
        List<Topic> allTopics = topicRepository.getAllTopicsByStudent(studentId);
        List<Topic> revisionTopics = new ArrayList<>();

        for (Topic topic : allTopics) {
            // Recommend revision for topics that are:
            // 1. Already completed (so the student has studied them before)
            // 2. Difficulty is 4 or 5 (Hard or Very Hard)
            if (topic.isCompleted() && topic.getDifficulty() >= 4) {
                revisionTopics.add(topic);
            }
        }

        return revisionTopics;
    }

    /**
     * Gets subjects where the completion percentage is below 50%.
     * These subjects need more attention.
     *
     * @param studentId The student's ID
     * @return A map of Subject → completion percentage for low-completion subjects
     */
    public Map<Subject, Double> getLowCompletionSubjects(int studentId) {
        Map<Subject, Double> lowCompletion = new LinkedHashMap<>();
        List<Subject> subjects = subjectRepository.getSubjectsByStudent(studentId);

        for (Subject subject : subjects) {
            List<Topic> topics = topicRepository.getTopicsBySubject(subject.getSubjectId());

            if (topics.isEmpty()) {
                continue; // Skip subjects with no topics
            }

            // Count completed topics
            long completed = 0;
            for (Topic t : topics) {
                if (t.isCompleted()) {
                    completed++;
                }
            }

            double percentage = (completed * 100.0) / topics.size();

            // Add to result if completion is below 50%
            if (percentage < 50.0) {
                lowCompletion.put(subject, percentage);
            }
        }

        return lowCompletion;
    }

    // ==========================================================
    // Helper Methods
    // ==========================================================

    /**
     * Builds a map of subject ID → Subject for quick lookups.
     * This avoids repeated database queries when processing topics.
     *
     * @param studentId The student's ID
     * @return A HashMap mapping subject IDs to Subject objects
     */
    private Map<Integer, Subject> buildSubjectMap(int studentId) {
        Map<Integer, Subject> subjectMap = new HashMap<>();
        List<Subject> subjects = subjectRepository.getSubjectsByStudent(studentId);

        for (Subject subject : subjects) {
            subjectMap.put(subject.getSubjectId(), subject);
        }

        return subjectMap;
    }

    /**
     * Returns the name of the current strategy.
     *
     * @return The strategy name string
     */
    public String getStrategyName() {
        return strategy.getStrategyName();
    }
}

