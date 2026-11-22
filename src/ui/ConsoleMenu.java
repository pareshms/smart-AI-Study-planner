package ui;

import database.DatabaseInitializer;
import model.Student;
import model.Subject;
import model.StudySession;
import model.Topic;
import repository.StudentRepository;
import repository.SubjectRepository;
import repository.TopicRepository;
import service.PriorityCalculator;
import service.StudyPlanner;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Console-based user interface for the Smart AI Study Planner.
 *
 * This class handles all user interactions through a menu-driven interface.
 * It uses Scanner for input and delegates business logic to service/repository classes.
 *
 * OOP Concepts demonstrated:
 * - EXCEPTION HANDLING: All user inputs are validated; invalid entries don't crash the app.
 * - CLASSES AND OBJECTS: Uses objects from multiple classes to perform operations.
 * - COLLECTIONS: Works with Lists and Maps returned by repositories and services.
 */
public class ConsoleMenu {

    // ===== Private fields (Encapsulation) =====
    private Scanner scanner;
    private StudentRepository studentRepo;
    private SubjectRepository subjectRepo;
    private TopicRepository topicRepo;
    private StudyPlanner studyPlanner;
    private PriorityCalculator priorityCalculator;
    private int currentStudentId; // Tracks the currently active student

    /**
     * Constructor — initializes all dependencies.
     */
    public ConsoleMenu() {
        this.scanner = new Scanner(System.in);
        this.studentRepo = new StudentRepository();
        this.subjectRepo = new SubjectRepository();
        this.topicRepo = new TopicRepository();
        this.studyPlanner = new StudyPlanner();
        this.priorityCalculator = new PriorityCalculator();
        this.currentStudentId = -1; // No student selected yet
    }

    /**
     * Starts the application — initializes the database and runs the main loop.
     */
    public void start() {
        // Initialize database tables on startup
        DatabaseInitializer.initialize();

        // Display welcome banner
        System.out.println();
        System.out.println("========================================");
        System.out.println("       SMART AI STUDY PLANNER");
        System.out.println("========================================");
        System.out.println("  Your intelligent study companion!");
        System.out.println("========================================");
        System.out.println();

        // Main application loop
        boolean running = true;
        while (running) {
            displayMenu();
            int choice = readInt("Enter your choice: ");

            switch (choice) {
                case 1  -> registerStudent();
                case 2  -> viewStudentProfile();
                case 3  -> addSubject();
                case 4  -> viewSubjects();
                case 5  -> addTopic();
                case 6  -> viewTopics();
                case 7  -> markTopicCompleted();
                case 8  -> generateStudyPlan();
                case 9  -> viewProgress();
                case 10 -> getRecommendations();
                case 11 -> deleteSubject();
                case 12 -> {
                    System.out.println();
                    System.out.println("Thank you for using Smart AI Study Planner!");
                    System.out.println("Keep studying hard! Good luck!");
                    running = false;
                }
                default -> System.out.println("\n[!] Invalid choice. Please enter a number from 1 to 12.\n");
            }
        }

        scanner.close();
    }

    // ==========================================================
    // Menu Display
    // ==========================================================

    /**
     * Displays the main menu options.
     */
    private void displayMenu() {
        System.out.println();
        System.out.println("========================================");
        System.out.println("            MAIN MENU");
        System.out.println("========================================");
        System.out.println("  1.  Register Student");
        System.out.println("  2.  View Student Profile");
        System.out.println("  3.  Add Subject");
        System.out.println("  4.  View Subjects");
        System.out.println("  5.  Add Topic");
        System.out.println("  6.  View Topics");
        System.out.println("  7.  Mark Topic as Completed");
        System.out.println("  8.  Generate Smart Study Plan");
        System.out.println("  9.  View Study Progress");
        System.out.println("  10. Get Smart Recommendations");
        System.out.println("  11. Delete Subject");
        System.out.println("  12. Exit");
        System.out.println("========================================");
    }

    // ==========================================================
    // 1. Register Student
    // ==========================================================

    private void registerStudent() {
        System.out.println("\n--- Register New Student ---");

        String name = readString("Enter student name: ");
        if (name.isEmpty()) {
            System.out.println("[!] Name cannot be empty.");
            return;
        }

        int semester = readInt("Enter semester (1-8): ");
        if (semester < 1 || semester > 8) {
            System.out.println("[!] Invalid semester. Must be between 1 and 8.");
            return;
        }

        double hours = readDouble("Enter available daily study hours: ");
        if (hours <= 0 || hours > 24) {
            System.out.println("[!] Invalid study hours. Must be between 0 and 24.");
            return;
        }

        // Create a new Student object and save to database
        Student student = new Student(0, name, semester, hours);
        int id = studentRepo.addStudent(student);

        if (id > 0) {
            currentStudentId = id; // Auto-select the new student
            System.out.println("\n[+] Student registered successfully! ID: " + id);
            System.out.println("    Welcome, " + name + "!");
        } else {
            System.out.println("[!] Failed to register student.");
        }
    }

    // ==========================================================
    // 2. View Student Profile
    // ==========================================================

    private void viewStudentProfile() {
        if (!ensureStudentSelected()) return;

        Student student = studentRepo.getStudent(currentStudentId);
        if (student != null) {
            System.out.println("\n--- Student Profile ---");
            System.out.println("+-------------------------------------+");
            System.out.println("  " + student.displayInfo());
            System.out.println("+-------------------------------------+");
        } else {
            System.out.println("[!] Student not found.");
        }
    }

    // ==========================================================
    // 3. Add Subject
    // ==========================================================

    private void addSubject() {
        if (!ensureStudentSelected()) return;

        System.out.println("\n--- Add New Subject ---");

        String name = readString("Enter subject name: ");
        if (name.isEmpty()) {
            System.out.println("[!] Subject name cannot be empty.");
            return;
        }

        String examDate = readString("Enter exam date (YYYY-MM-DD): ");
        if (!isValidDate(examDate)) {
            System.out.println("[!] Invalid date format. Please use YYYY-MM-DD (e.g., 2026-12-15).");
            return;
        }

        Subject subject = new Subject(currentStudentId, name, examDate);
        int id = subjectRepo.addSubject(subject);

        if (id > 0) {
            System.out.println("[+] Subject added successfully! ID: " + id);
        } else {
            System.out.println("[!] Failed to add subject.");
        }
    }

    // ==========================================================
    // 4. View Subjects
    // ==========================================================

    private void viewSubjects() {
        if (!ensureStudentSelected()) return;

        List<Subject> subjects = subjectRepo.getSubjectsByStudent(currentStudentId);

        if (subjects.isEmpty()) {
            System.out.println("\n[!] No subjects found. Add a subject first (Option 3).");
            return;
        }

        System.out.println("\n--- Your Subjects ---");
        System.out.println("---------------------------------------------------");
        for (Subject subject : subjects) {
            long daysUntil = priorityCalculator.calculateDaysUntilExam(subject.getExamDate());
            System.out.println("  " + subject + " | Days until exam: " + daysUntil);
        }
        System.out.println("---------------------------------------------------");
    }

    // ==========================================================
    // 5. Add Topic
    // ==========================================================

    private void addTopic() {
        if (!ensureStudentSelected()) return;

        List<Subject> subjects = subjectRepo.getSubjectsByStudent(currentStudentId);
        if (subjects.isEmpty()) {
            System.out.println("\n[!] No subjects found. Add a subject first (Option 3).");
            return;
        }

        System.out.println("\n--- Add New Topic ---");
        // Show available subjects so the user can pick one
        viewSubjects();

        int subjectId = readInt("Enter Subject ID: ");

        // Verify the subject exists and belongs to the current student
        Subject subject = subjectRepo.getSubject(subjectId);
        if (subject == null || subject.getStudentId() != currentStudentId) {
            System.out.println("[!] Invalid Subject ID.");
            return;
        }

        String topicName = readString("Enter topic name: ");
        if (topicName.isEmpty()) {
            System.out.println("[!] Topic name cannot be empty.");
            return;
        }

        int difficulty = readInt("Enter difficulty (1=Very Easy, 2=Easy, 3=Medium, 4=Hard, 5=Very Hard): ");
        if (difficulty < 1 || difficulty > 5) {
            System.out.println("[!] Difficulty must be between 1 and 5.");
            return;
        }

        double hours = readDouble("Enter estimated study hours: ");
        if (hours <= 0) {
            System.out.println("[!] Study hours must be greater than 0.");
            return;
        }

        Topic topic = new Topic(subjectId, topicName, difficulty, hours);
        int id = topicRepo.addTopic(topic);

        if (id > 0) {
            System.out.println("[+] Topic added successfully! ID: " + id);
        } else {
            System.out.println("[!] Failed to add topic.");
        }
    }

    // ==========================================================
    // 6. View Topics
    // ==========================================================

    private void viewTopics() {
        if (!ensureStudentSelected()) return;

        List<Subject> subjects = subjectRepo.getSubjectsByStudent(currentStudentId);

        if (subjects.isEmpty()) {
            System.out.println("\n[!] No subjects found.");
            return;
        }

        System.out.println("\n--- All Topics ---");

        for (Subject subject : subjects) {
            List<Topic> topics = topicRepo.getTopicsBySubject(subject.getSubjectId());

            System.out.println("\n  [" + subject.getSubjectName() + "] (Exam: " + subject.getExamDate() + ")");
            System.out.println("  -----------------------------------------");

            if (topics.isEmpty()) {
                System.out.println("    No topics added yet.");
            } else {
                for (Topic topic : topics) {
                    String status = topic.isCompleted() ? "[DONE]" : "[    ]";
                    System.out.println("    " + status + " " + topic);
                }
            }
        }
    }

    // ==========================================================
    // 7. Mark Topic as Completed
    // ==========================================================

    private void markTopicCompleted() {
        if (!ensureStudentSelected()) return;

        System.out.println("\n--- Mark Topic as Completed ---");
        viewTopics();

        int topicId = readInt("\nEnter Topic ID to mark as completed: ");

        Topic topic = topicRepo.getTopic(topicId);
        if (topic == null) {
            System.out.println("[!] Topic not found.");
            return;
        }

        // Verify the topic belongs to the current student
        Subject subject = subjectRepo.getSubject(topic.getSubjectId());
        if (subject == null || subject.getStudentId() != currentStudentId) {
            System.out.println("[!] This topic does not belong to you.");
            return;
        }

        if (topic.isCompleted()) {
            System.out.println("[!] This topic is already marked as completed.");
            return;
        }

        topicRepo.markCompleted(topicId);
        System.out.println("[+] Topic '" + topic.getTopicName() + "' marked as completed! Great job!");
    }

    // ==========================================================
    // 8. Generate Smart Study Plan
    // ==========================================================

    private void generateStudyPlan() {
        if (!ensureStudentSelected()) return;

        Student student = studentRepo.getStudent(currentStudentId);
        if (student == null) {
            System.out.println("[!] Student not found.");
            return;
        }

        double availableHours = student.getDailyStudyHours();

        System.out.println("\n--- Smart Study Plan Generator ---");
        System.out.println("Strategy: " + studyPlanner.getStrategyName());
        System.out.println("Available study time: " + availableHours + " hours");

        List<StudySession> sessions = studyPlanner.generateDailyPlan(currentStudentId, availableHours);

        if (sessions.isEmpty()) {
            System.out.println("\n[!] No pending topics to schedule. All caught up!");
            return;
        }

        // Display the timetable
        System.out.println();
        System.out.println("================================================================");
        System.out.println("                    TODAY'S STUDY TIMETABLE                      ");
        System.out.println("================================================================");
        System.out.printf("  %-3s  %-25s  %-18s  %-6s  %-10s%n",
                "#", "Topic", "Subject", "Hours", "Difficulty");
        System.out.println("----------------------------------------------------------------");

        double totalHours = 0;
        int index = 1;

        for (StudySession session : sessions) {
            System.out.printf("  %-3d  %-25s  %-18s  %-6.1f  %-10s%n",
                    index++,
                    truncate(session.getTopic().getTopicName(), 25),
                    truncate(session.getSubjectName(), 18),
                    session.getAllocatedHours(),
                    session.getTopic().getDifficultyText());
            totalHours += session.getAllocatedHours();
        }

        System.out.println("----------------------------------------------------------------");
        System.out.printf("  Total Study Time: %.1f / %.1f hours%n", totalHours, availableHours);
        System.out.println("================================================================");
    }

    // ==========================================================
    // 9. View Study Progress
    // ==========================================================

    private void viewProgress() {
        if (!ensureStudentSelected()) return;

        System.out.println("\n--- Study Progress ---");

        List<Topic> allTopics = topicRepo.getAllTopicsByStudent(currentStudentId);

        if (allTopics.isEmpty()) {
            System.out.println("[!] No topics added yet. Add subjects and topics first.");
            return;
        }

        // Calculate overall progress
        int total = allTopics.size();
        int completed = 0;
        for (Topic t : allTopics) {
            if (t.isCompleted()) completed++;
        }
        int pending = total - completed;
        double percentage = (completed * 100.0) / total;

        // Display overall progress
        System.out.println("+-------------------------------------+");
        System.out.println("  Total Topics    : " + total);
        System.out.println("  Completed       : " + completed);
        System.out.println("  Pending         : " + pending);
        System.out.printf("  Overall Progress: %.1f%%%n", percentage);
        System.out.println("  " + generateProgressBar(percentage));
        System.out.println("+-------------------------------------+");

        // Display subject-wise progress
        System.out.println("\n--- Subject-wise Progress ---");
        List<Subject> subjects = subjectRepo.getSubjectsByStudent(currentStudentId);

        for (Subject subject : subjects) {
            List<Topic> topics = topicRepo.getTopicsBySubject(subject.getSubjectId());
            if (topics.isEmpty()) continue;

            int subTotal = topics.size();
            int subCompleted = 0;
            for (Topic t : topics) {
                if (t.isCompleted()) subCompleted++;
            }
            double subPercentage = (subCompleted * 100.0) / subTotal;

            System.out.printf("  %-20s : %d/%d (%.1f%%) %s%n",
                    subject.getSubjectName(), subCompleted, subTotal,
                    subPercentage, generateProgressBar(subPercentage));
        }
    }

    // ==========================================================
    // 10. Get Smart Recommendations
    // ==========================================================

    private void getRecommendations() {
        if (!ensureStudentSelected()) return;

        System.out.println();
        System.out.println("========================================");
        System.out.println("       SMART RECOMMENDATIONS");
        System.out.println("========================================");

        // --- Next Topic to Study ---
        System.out.println("\n>> Next Topic to Study:");
        Topic topTopic = studyPlanner.getTopRecommendation(currentStudentId);
        if (topTopic != null) {
            Subject subject = subjectRepo.getSubject(topTopic.getSubjectId());
            String subjectName = (subject != null) ? subject.getSubjectName() : "Unknown";
            System.out.println("   -> " + topTopic.getTopicName()
                    + " (" + subjectName + ") - " + topTopic.getDifficultyText());
            if (subject != null) {
                long days = priorityCalculator.calculateDaysUntilExam(subject.getExamDate());
                System.out.println("   -> Exam in " + days + " days | Estimated: "
                        + topTopic.getEstimatedHours() + " hours");
            }
        } else {
            System.out.println("   -> All topics completed! No pending recommendations.");
        }

        // --- Revision Recommendations ---
        System.out.println("\n>> Revision Recommendations (Completed Hard Topics):");
        List<Topic> revisionTopics = studyPlanner.getRevisionRecommendations(currentStudentId);
        if (revisionTopics.isEmpty()) {
            System.out.println("   -> No revision recommendations at this time.");
        } else {
            for (Topic topic : revisionTopics) {
                System.out.println("   -> Revise: " + topic.getTopicName()
                        + " (Difficulty: " + topic.getDifficultyText() + ")");
            }
        }

        // --- Low Completion Subjects ---
        System.out.println("\n>> Subjects with Low Completion (< 50%):");
        Map<Subject, Double> lowCompletion = studyPlanner.getLowCompletionSubjects(currentStudentId);
        if (lowCompletion.isEmpty()) {
            System.out.println("   -> All subjects are progressing well! Keep it up!");
        } else {
            for (Map.Entry<Subject, Double> entry : lowCompletion.entrySet()) {
                System.out.printf("   -> %s: %.1f%% completed%n",
                        entry.getKey().getSubjectName(), entry.getValue());
            }
        }

        System.out.println();
        System.out.println("========================================");
    }

    // ==========================================================
    // 11. Delete Subject
    // ==========================================================

    private void deleteSubject() {
        if (!ensureStudentSelected()) return;

        System.out.println("\n--- Delete Subject ---");
        viewSubjects();

        int subjectId = readInt("\nEnter Subject ID to delete: ");

        Subject subject = subjectRepo.getSubject(subjectId);
        if (subject == null || subject.getStudentId() != currentStudentId) {
            System.out.println("[!] Invalid Subject ID.");
            return;
        }

        String confirm = readString("Are you sure you want to delete '"
                + subject.getSubjectName() + "' and all its topics? (yes/no): ");

        if (confirm.equalsIgnoreCase("yes")) {
            subjectRepo.deleteSubject(subjectId);
            System.out.println("[+] Subject '" + subject.getSubjectName() + "' and its topics deleted.");
        } else {
            System.out.println("[!] Deletion cancelled.");
        }
    }

    // ==========================================================
    // Helper Methods
    // ==========================================================

    /**
     * Ensures a student is selected before performing an operation.
     * If no student is registered, prompts the user to register first.
     * If only one student exists, auto-selects them.
     * If multiple students exist, asks the user to choose.
     *
     * @return true if a valid student is selected, false otherwise
     */
    private boolean ensureStudentSelected() {
        if (currentStudentId > 0) {
            return true;
        }

        // Check if any students exist in the database
        List<Student> students = studentRepo.getAllStudents();

        if (students.isEmpty()) {
            System.out.println("\n[!] No student registered. Please register first (Option 1).");
            return false;
        }

        // If only one student, auto-select them
        if (students.size() == 1) {
            currentStudentId = students.get(0).getId();
            return true;
        }

        // Multiple students — let the user choose
        System.out.println("\nRegistered students:");
        for (Student s : students) {
            System.out.println("  ID: " + s.getId() + " | Name: " + s.getName());
        }
        currentStudentId = readInt("Enter Student ID to use: ");

        Student selected = studentRepo.getStudent(currentStudentId);
        if (selected == null) {
            System.out.println("[!] Invalid Student ID.");
            currentStudentId = -1;
            return false;
        }

        return true;
    }

    /**
     * Reads an integer from the user with error handling.
     *
     * @param prompt The prompt message to display
     * @return The parsed integer, or -1 if parsing failed
     */
    private int readInt(String prompt) {
        System.out.print(prompt);
        try {
            String input = scanner.nextLine().trim();
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("[!] Invalid number. Please enter a valid integer.");
            return -1;
        }
    }

    /**
     * Reads a double from the user with error handling.
     *
     * @param prompt The prompt message to display
     * @return The parsed double, or -1.0 if parsing failed
     */
    private double readDouble(String prompt) {
        System.out.print(prompt);
        try {
            String input = scanner.nextLine().trim();
            return Double.parseDouble(input);
        } catch (NumberFormatException e) {
            System.out.println("[!] Invalid number. Please enter a valid number.");
            return -1;
        }
    }

    /**
     * Reads a string from the user.
     *
     * @param prompt The prompt message to display
     * @return The trimmed input string
     */
    private String readString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    /**
     * Validates that a string is a valid date in YYYY-MM-DD format.
     *
     * @param date The date string to validate
     * @return true if the date is valid, false otherwise
     */
    private boolean isValidDate(String date) {
        try {
            java.time.LocalDate.parse(date, java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Generates a text-based progress bar.
     * Example: [########............] 40.0%
     *
     * @param percentage The completion percentage (0-100)
     * @return A string representing the progress bar
     */
    private String generateProgressBar(double percentage) {
        int filled = (int) (percentage / 5);  // Each '#' represents 5%
        int empty = 20 - filled;              // Total bar length is 20

        StringBuilder bar = new StringBuilder("[");
        for (int i = 0; i < filled; i++) bar.append("#");
        for (int i = 0; i < empty; i++) bar.append(".");
        bar.append("]");

        return bar.toString();
    }

    /**
     * Truncates a string if it exceeds the maximum length.
     *
     * @param text      The original text
     * @param maxLength The maximum allowed length
     * @return The truncated string with "..." suffix, or the original if short enough
     */
    private String truncate(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }
}

