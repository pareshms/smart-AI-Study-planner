package model;

/**
 * Represents an academic subject that a student is preparing for.
 *
 * OOP Concepts demonstrated:
 * - ENCAPSULATION: Private fields with public getters/setters.
 * - CONSTRUCTORS: Multiple constructors (default, full, without-ID).
 */
public class Subject {

    // ===== Encapsulation: private fields =====
    private int subjectId;
    private int studentId;
    private String subjectName;
    private String examDate; // Format: YYYY-MM-DD (e.g., "2026-12-15")

    // ===== Constructors =====

    /**
     * Default constructor.
     */
    public Subject() {
    }

    /**
     * Full parameterized constructor (used when reading from database).
     *
     * @param subjectId   Subject ID from the database
     * @param studentId   ID of the student this subject belongs to
     * @param subjectName Name of the subject
     * @param examDate    Exam date in YYYY-MM-DD format
     */
    public Subject(int subjectId, int studentId, String subjectName, String examDate) {
        this.subjectId = subjectId;
        this.studentId = studentId;
        this.subjectName = subjectName;
        this.examDate = examDate;
    }

    /**
     * Constructor without ID (used when creating a new subject before database insert).
     *
     * @param studentId   ID of the student
     * @param subjectName Name of the subject
     * @param examDate    Exam date in YYYY-MM-DD format
     */
    public Subject(int studentId, String subjectName, String examDate) {
        this.studentId = studentId;
        this.subjectName = subjectName;
        this.examDate = examDate;
    }

    // ===== Getters and Setters =====

    public int getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(int subjectId) {
        this.subjectId = subjectId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getExamDate() {
        return examDate;
    }

    public void setExamDate(String examDate) {
        this.examDate = examDate;
    }

    // ===== toString =====

    @Override
    public String toString() {
        return "Subject ID: " + subjectId
                + " | Name: " + subjectName
                + " | Exam Date: " + examDate;
    }
}

