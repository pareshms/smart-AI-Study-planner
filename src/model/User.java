package model;

/**
 * Abstract base class representing a generic user in the system.
 *
 * OOP Concepts demonstrated:
 * - ABSTRACTION: This class is abstract and cannot be instantiated directly.
 *   It defines a contract (getRole) that all subclasses must implement.
 * - ENCAPSULATION: All fields are private; access is through getters/setters.
 * - INHERITANCE: This is the parent class for Student (and potentially other user types).
 */
public abstract class User {

    // ===== Encapsulation: private fields =====
    private int id;
    private String name;

    // ===== Constructors =====

    /**
     * Default constructor.
     */
    public User() {
    }

    /**
     * Parameterized constructor.
     *
     * @param id   The unique user ID
     * @param name The user's full name
     */
    public User(int id, String name) {
        this.id = id;
        this.name = name;
    }

    // ===== Getters and Setters =====

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // ===== Abstract Method =====

    /**
     * Returns the role of this user. Each subclass must provide its own role.
     * This is an example of ABSTRACTION — the method signature is defined here,
     * but the implementation is left to the subclass.
     *
     * @return The role name (e.g., "Student")
     */
    public abstract String getRole();

    // ===== Overridable Method =====

    /**
     * Displays user information. Subclasses can override this to add more details.
     * This supports POLYMORPHISM — the same method behaves differently in subclasses.
     *
     * @return A formatted string with user details
     */
    public String displayInfo() {
        return "ID: " + id + " | Name: " + name;
    }
}

