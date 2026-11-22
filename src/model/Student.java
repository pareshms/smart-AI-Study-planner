package model;

/**
 * Represents a student in the system. Extends the abstract User class.
 *
 * OOP Concepts demonstrated:
 * - INHERITANCE: Student inherits id and name from User.
 * - POLYMORPHISM: Overrides getRole() and displayInfo() from User.
 * - ENCAPSULATION: Private fields with public getters/setters.
 * - CONSTRUCTORS: Both default and parameterized constructors.
 */
public class Student extends User {

    // ===== Encapsulation: private fields =====
    private int semester;
    private double dailyStudyHours;

    // ===== Constructors =====

    /**
     * Default constructor. Calls the parent class default constructor.
     */
    public Student() {
        super();
    }

    /**
     * Parameterized constructor.
     *
     * @param id              Student ID (from database)
     * @param name            Student's full name
     * @param semester         Current semester (1-8)
     * @param dailyStudyHours Hours available for study each day
     */
    public Student(int id, String name, int semester, double dailyStudyHours) {
        super(id, name); // Call parent constructor — Inheritance
        this.semester = semester;
        this.dailyStudyHours = dailyStudyHours;
    }

    // ===== Getters and Setters =====

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        this.semester = semester;
    }

    public double getDailyStudyHours() {
        return dailyStudyHours;
    }

    public void setDailyStudyHours(double dailyStudyHours) {
        this.dailyStudyHours = dailyStudyHours;
    }

    // ===== Polymorphism: Overriding abstract method =====

    /**
     * Returns the role of this user.
     * This overrides the abstract method defined in User — POLYMORPHISM.
     */
    @Override
    public String getRole() {
        return "Student";
    }

    // ===== Polymorphism: Overriding displayInfo =====

    /**
     * Returns detailed student information.
     * Extends the parent's displayInfo() with additional fields — POLYMORPHISM.
     */
    @Override
    public String displayInfo() {
        return super.displayInfo()
                + " | Role: " + getRole()
                + " | Semester: " + semester
                + " | Daily Study Hours: " + dailyStudyHours;
    }
}

