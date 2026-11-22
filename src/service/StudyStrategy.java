package service;

import model.StudySession;
import model.Subject;
import model.Topic;

import java.util.List;
import java.util.Map;

/**
 * Interface for study recommendation strategies.
 *
 * OOP Concepts demonstrated:
 * - ABSTRACTION: This interface defines WHAT a strategy does, but not HOW.
 *   Any class that implements this interface must provide the actual logic.
 * - POLYMORPHISM: Different strategy implementations can be swapped at runtime.
 *   For example, we could have a DeadlineFirstStrategy, a DifficultyFirstStrategy, etc.
 *   The StudyPlanner doesn't care which one is used — it just calls generatePlan().
 *
 * Why an interface?
 * - It lets us change the recommendation algorithm without modifying other code.
 * - New strategies can be added by simply creating a new class that implements this interface.
 */
public interface StudyStrategy {

    /**
     * Generates a list of study sessions based on this strategy's logic.
     *
     * @param topics         List of pending (not completed) topics
     * @param subjectMap     Map of subject ID → Subject object (for exam dates)
     * @param availableHours How many hours the student can study today
     * @return A list of StudySession objects forming the daily study plan
     */
    List<StudySession> generatePlan(List<Topic> topics,
                                     Map<Integer, Subject> subjectMap,
                                     double availableHours);

    /**
     * Returns the name of this strategy (for display purposes).
     *
     * @return The strategy name
     */
    String getStrategyName();
}

