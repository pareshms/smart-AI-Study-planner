package service;

import model.Topic;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

/**
 * Calculates priority scores for topics using a weighted scoring formula.
 *
 * ╔══════════════════════════════════════════════════════════════════╗
 * ║                    PRIORITY ALGORITHM                           ║
 * ╠══════════════════════════════════════════════════════════════════╣
 * ║                                                                 ║
 * ║  Priority = DifficultyWeight + UrgencyWeight + EffortWeight     ║
 * ║                                                                 ║
 * ║  Where:                                                         ║
 * ║    DifficultyWeight = (difficulty / 5.0) × 35                   ║
 * ║    UrgencyWeight    = (1.0 / max(daysUntilExam, 1)) × 40       ║
 * ║    EffortWeight     = (min(hours, 10) / 10.0) × 25             ║
 * ║                                                                 ║
 * ║  Higher score = Study this topic first                          ║
 * ║                                                                 ║
 * ║  IMPORTANT: This is a RULE-BASED algorithm, NOT machine         ║
 * ║  learning. It uses a simple mathematical formula to rank         ║
 * ║  topics intelligently based on three factors.                   ║
 * ╚══════════════════════════════════════════════════════════════════╝
 *
 * OOP Concepts demonstrated:
 * - METHOD OVERLOADING: Three versions of calculatePriority() with
 *   different parameter lists.
 * - ENCAPSULATION: Weight constants are private.
 */
public class PriorityCalculator {

    // ===== Weight Constants (private — Encapsulation) =====
    // These weights determine how much each factor contributes to the score.
    // They sum to 100, making the score roughly on a 0-100 scale.

    private static final double DIFFICULTY_WEIGHT = 35.0;  // How hard the topic is
    private static final double URGENCY_WEIGHT    = 40.0;  // How close the exam is
    private static final double EFFORT_WEIGHT     = 25.0;  // How long it takes to study

    // ==========================================================
    // METHOD OVERLOADING: Three versions of calculatePriority()
    // Same method name, different parameters.
    // ==========================================================

    /**
     * VERSION 1: Calculate priority using a Topic object and exam date string.
     *
     * This is the most commonly used version. It extracts the difficulty
     * and estimated hours from the Topic object automatically.
     *
     * @param topic    The topic to calculate priority for
     * @param examDate The exam date in YYYY-MM-DD format
     * @return The priority score (higher = more urgent)
     */
    public double calculatePriority(Topic topic, String examDate) {
        long daysRemaining = calculateDaysUntilExam(examDate);
        return calculatePriority(topic.getDifficulty(), topic.getEstimatedHours(), daysRemaining);
    }

    /**
     * VERSION 2: Calculate priority using individual numeric values.
     *
     * This version is useful when you have the raw values without
     * a Topic object (e.g., for testing or custom calculations).
     *
     * @param difficulty     Difficulty level (1-5)
     * @param estimatedHours Estimated study hours
     * @param daysRemaining  Days until the exam
     * @return The priority score (higher = more urgent)
     */
    public double calculatePriority(int difficulty, double estimatedHours, long daysRemaining) {

        // Factor 1: Difficulty Score
        // Higher difficulty → higher score
        // A difficulty of 5 gives the full 35 points; difficulty of 1 gives 7 points.
        double difficultyScore = (difficulty / 5.0) * DIFFICULTY_WEIGHT;

        // Factor 2: Urgency Score
        // Fewer days remaining → higher score
        // 1 day left = 40 points; 10 days left = 4 points; 30 days left ≈ 1.3 points
        double urgencyScore = (1.0 / Math.max(daysRemaining, 1)) * URGENCY_WEIGHT;

        // Factor 3: Effort Score
        // More estimated hours → higher score (longer topics need an earlier start)
        // Capped at 10 hours to prevent extremely long topics from dominating.
        double effortScore = (Math.min(estimatedHours, 10.0) / 10.0) * EFFORT_WEIGHT;

        // Total priority = sum of all three weighted factors
        return difficultyScore + urgencyScore + effortScore;
    }

    /**
     * VERSION 3: Calculate priority using only difficulty.
     *
     * Simplified version when exam date and study hours are not available.
     * Returns only the difficulty component of the score.
     *
     * @param difficulty Difficulty level (1-5)
     * @return The difficulty-only priority score
     */
    public double calculatePriority(int difficulty) {
        return (difficulty / 5.0) * DIFFICULTY_WEIGHT;
    }

    // ==========================================================
    // Helper Method
    // ==========================================================

    /**
     * Calculates the number of days between today and the given exam date.
     *
     * @param examDate The exam date in YYYY-MM-DD format (e.g., "2026-12-15")
     * @return Number of days until the exam (minimum 0)
     */
    public long calculateDaysUntilExam(String examDate) {
        try {
            LocalDate exam = LocalDate.parse(examDate, DateTimeFormatter.ISO_LOCAL_DATE);
            LocalDate today = LocalDate.now();
            long days = ChronoUnit.DAYS.between(today, exam);
            return Math.max(days, 0); // If the exam has passed, return 0
        } catch (DateTimeParseException e) {
            // If the date format is invalid, default to 30 days
            return 30;
        }
    }
}

