# Smart AI Study Planner 🎓

A complete, working academic mini-project built with **Java 17, SQLite (JDBC), and Layered Architecture**, featuring an offline rule-based priority recommendation engine and an interactive web dashboard.

---

## ⚡ Quick Start (Run in 5 Seconds)

### Option 1: Just Double-Click (Easiest)
1. Clone this repository or download the ZIP.
2. **Double-click `run.bat`**.
3. Your web browser will open automatically at:
   👉 **http://localhost:8080**

### Option 2: Run via Terminal
```bash
git clone https://github.com/pareshms/smart-AI-Study-planner.git
cd smart-AI-Study-planner
.\run.bat
```

### Option 3: Terminal / Console Mode
If you want to run the classic console-based text menu:
```bash
.\run.bat --console
```

---

## 🏗️ Project Architecture & OOP Concepts

The project demonstrates 9 core Object-Oriented Programming (OOP) concepts:

| OOP Concept | Implementation in Code |
| :--- | :--- |
| **Encapsulation** | Private fields with public getters and setters in `Student`, `Subject`, `Topic`, `StudySession` |
| **Inheritance** | `Student` extends abstract `User` class |
| **Polymorphism** | `DefaultStudyStrategy` implements `StudyStrategy` interface (Strategy Pattern) |
| **Abstraction** | `User` abstract class and `StudyStrategy` interface |
| **Classes & Objects** | Separate domain entities for all real-world concepts |
| **Constructors** | Default and overloaded parameterized constructors |
| **Method Overloading**| `PriorityCalculator.calculatePriority()` with multiple signatures |
| **Exception Handling**| `try-with-resources` for automatic JDBC resource management and numeric parsing |
| **Collections** | `ArrayList`, `HashMap`, and `LinkedHashMap` for structured in-memory data |

---

## 🧠 Offline Priority Algorithm

The recommendation engine uses a deterministic weighted mathematical formula:

$$\text{Priority Score} = \left(\frac{\text{Difficulty}}{5.0} \times 35\right) + \left(\frac{1.0}{\max(\text{DaysToExam}, 1)} \times 40\right) + \left(\frac{\min(\text{Hours}, 10.0)}{10.0} \times 25\right)$$

- **Difficulty (35% weight)**: Harder topics get higher attention.
- **Urgency (40% weight)**: Topics with impending exam dates receive top scheduling priority.
- **Duration (25% weight)**: Longer topics are scheduled earlier to ensure sufficient coverage.

---

## 📁 Repository Structure

```
SmartAIStudyPlanner/
├── database/        # studyplanner.db (SQLite database)
├── lib/             # sqlite-jdbc driver
├── out/             # Pre-compiled .class files (runs without manual compilation)
├── src/             # Java source code
│   ├── database/    # DatabaseConnection, DatabaseInitializer
│   ├── model/       # User, Student, Subject, Topic, StudySession
│   ├── repository/  # StudentRepository, SubjectRepository, TopicRepository
│   ├── server/      # Embedded WebServer
│   ├── service/     # StudyPlanner, PriorityCalculator, StudyStrategy
│   └── ui/          # ConsoleMenu (Terminal UI fallback)
├── web/             # Modern Web UI (HTML5, CSS, Canvas Graphs)
├── HOW-TO-RUN.txt   # Setup instructions
├── pom.xml          # Maven configuration
└── run.bat          # 1-click Windows launcher
```

---

## 💻 Tech Stack
- **Language**: Java 17+
- **Database**: SQLite 3 (via JDBC)
- **Frontend**: Vanilla HTML5, CSS3, JavaScript (Native Canvas Graphs, zero external CDN dependencies)
- **Server**: Java built-in `HttpServer` (zero external framework overhead)
