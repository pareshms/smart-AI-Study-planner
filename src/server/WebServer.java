package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import database.DatabaseInitializer;
import model.Student;
import model.StudySession;
import model.Subject;
import model.Topic;
import repository.StudentRepository;
import repository.SubjectRepository;
import repository.TopicRepository;
import service.PriorityCalculator;
import service.StudyPlanner;

import java.awt.Desktop;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

/**
 * Embedded HTTP Web Server for the Smart AI Study Planner.
 *
 * Uses Java's built-in HttpServer (no external dependencies required).
 * Provides REST API endpoints and serves a modern responsive web application.
 */
public class WebServer {

    private static final int PORT = 8080;
    private static StudentRepository studentRepo = new StudentRepository();
    private static SubjectRepository subjectRepo = new SubjectRepository();
    private static TopicRepository topicRepo = new TopicRepository();
    private static StudyPlanner studyPlanner = new StudyPlanner();
    private static PriorityCalculator priorityCalc = new PriorityCalculator();

    public static void start() throws IOException {
        DatabaseInitializer.initialize();

        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // API Handlers
        server.createContext("/api/student", new StudentHandler());
        server.createContext("/api/subjects", new SubjectsHandler());
        server.createContext("/api/topics", new TopicsHandler());
        server.createContext("/api/topics/complete", new CompleteTopicHandler());
        server.createContext("/api/plan", new PlanHandler());
        server.createContext("/api/progress", new ProgressHandler());
        server.createContext("/api/recommendations", new RecommendationsHandler());

        // Static Web UI Handler
        server.createContext("/", new StaticFileHandler());

        server.setExecutor(null); // default executor
        server.start();

        System.out.println("==================================================");
        System.out.println("  Smart AI Study Planner - Web Server Started!");
        System.out.println("  URL: http://localhost:" + PORT);
        System.out.println("==================================================");
        System.out.println("  Press Ctrl+C in this window to stop the server.");

        // Try to automatically open default browser
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI("http://localhost:" + PORT));
            }
        } catch (Exception ignored) {
            // Browser opening fallback
        }

        // Keep the main thread alive so server doesn't exit
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            System.out.println("Server stopped.");
        }
    }

    // ==========================================
    // HTTP Handlers
    // ==========================================

    static class StudentHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 204, "");
                return;
            }

            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                List<Student> students = studentRepo.getAllStudents();
                if (students.isEmpty()) {
                    sendJson(exchange, 200, "{\"registered\":false}");
                } else {
                    Student s = students.get(0);
                    String json = String.format(Locale.US,
                            "{\"registered\":true,\"id\":%d,\"name\":\"%s\",\"semester\":%d,\"dailyStudyHours\":%.1f}",
                            s.getId(), escapeJson(s.getName()), s.getSemester(), s.getDailyStudyHours());
                    sendJson(exchange, 200, json);
                }
            } else if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> params = parseBody(exchange);
                String name = params.getOrDefault("name", "").trim();
                int semester = Integer.parseInt(params.getOrDefault("semester", "1"));
                double hours = Double.parseDouble(params.getOrDefault("dailyStudyHours", "4.0"));

                List<Student> existing = studentRepo.getAllStudents();
                int id;
                if (existing.isEmpty()) {
                    Student student = new Student(0, name, semester, hours);
                    id = studentRepo.addStudent(student);
                } else {
                    Student student = existing.get(0);
                    student.setName(name);
                    student.setSemester(semester);
                    student.setDailyStudyHours(hours);
                    studentRepo.updateStudent(student);
                    id = student.getId();
                }

                sendJson(exchange, 200, "{\"success\":true,\"id\":" + id + "}");
            }
        }
    }

    static class SubjectsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 204, "");
                return;
            }

            List<Student> students = studentRepo.getAllStudents();
            if (students.isEmpty()) {
                sendJson(exchange, 200, "[]");
                return;
            }
            int studentId = students.get(0).getId();

            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                List<Subject> subjects = subjectRepo.getSubjectsByStudent(studentId);
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < subjects.size(); i++) {
                    Subject s = subjects.get(i);
                    long days = priorityCalc.calculateDaysUntilExam(s.getExamDate());
                    sb.append(String.format(Locale.US,
                            "{\"id\":%d,\"name\":\"%s\",\"examDate\":\"%s\",\"daysUntil\":%d}",
                            s.getSubjectId(), escapeJson(s.getSubjectName()), s.getExamDate(), days));
                    if (i < subjects.size() - 1) sb.append(",");
                }
                sb.append("]");
                sendJson(exchange, 200, sb.toString());

            } else if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> params = parseBody(exchange);
                String name = params.getOrDefault("name", "").trim();
                String examDate = params.getOrDefault("examDate", "").trim();

                Subject subject = new Subject(studentId, name, examDate);
                int id = subjectRepo.addSubject(subject);
                sendJson(exchange, 200, "{\"success\":true,\"id\":" + id + "}");

            } else if ("DELETE".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> q = parseQuery(exchange.getRequestURI().getQuery());
                int subjectId = Integer.parseInt(q.getOrDefault("id", "0"));
                if (subjectId > 0) {
                    subjectRepo.deleteSubject(subjectId);
                }
                sendJson(exchange, 200, "{\"success\":true}");
            }
        }
    }

    static class TopicsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 204, "");
                return;
            }

            List<Student> students = studentRepo.getAllStudents();
            if (students.isEmpty()) {
                sendJson(exchange, 200, "[]");
                return;
            }
            int studentId = students.get(0).getId();

            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                List<Topic> topics = topicRepo.getAllTopicsByStudent(studentId);
                List<Subject> subjects = subjectRepo.getSubjectsByStudent(studentId);
                Map<Integer, String> subNames = new HashMap<>();
                for (Subject s : subjects) subNames.put(s.getSubjectId(), s.getSubjectName());

                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < topics.size(); i++) {
                    Topic t = topics.get(i);
                    String sName = subNames.getOrDefault(t.getSubjectId(), "Unknown");
                    sb.append(String.format(Locale.US,
                            "{\"id\":%d,\"subjectId\":%d,\"subjectName\":\"%s\",\"name\":\"%s\",\"difficulty\":%d,\"difficultyText\":\"%s\",\"hours\":%.1f,\"isCompleted\":%b}",
                            t.getTopicId(), t.getSubjectId(), escapeJson(sName), escapeJson(t.getTopicName()),
                            t.getDifficulty(), t.getDifficultyText(), t.getEstimatedHours(), t.isCompleted()));
                    if (i < topics.size() - 1) sb.append(",");
                }
                sb.append("]");
                sendJson(exchange, 200, sb.toString());

            } else if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> params = parseBody(exchange);
                int subjectId = Integer.parseInt(params.getOrDefault("subjectId", "0"));
                String name = params.getOrDefault("name", "").trim();
                int difficulty = Integer.parseInt(params.getOrDefault("difficulty", "3"));
                double hours = Double.parseDouble(params.getOrDefault("hours", "1.0"));

                Topic topic = new Topic(subjectId, name, difficulty, hours);
                int id = topicRepo.addTopic(topic);
                sendJson(exchange, 200, "{\"success\":true,\"id\":" + id + "}");

            } else if ("DELETE".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> q = parseQuery(exchange.getRequestURI().getQuery());
                int topicId = Integer.parseInt(q.getOrDefault("id", "0"));
                if (topicId > 0) {
                    topicRepo.deleteTopic(topicId);
                }
                sendJson(exchange, 200, "{\"success\":true}");
            }
        }
    }

    static class CompleteTopicHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 204, "");
                return;
            }
            Map<String, String> q = parseQuery(exchange.getRequestURI().getQuery());
            int topicId = Integer.parseInt(q.getOrDefault("id", "0"));
            if (topicId > 0) {
                topicRepo.markCompleted(topicId);
            }
            sendJson(exchange, 200, "{\"success\":true}");
        }
    }

    static class PlanHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 204, "");
                return;
            }

            List<Student> students = studentRepo.getAllStudents();
            if (students.isEmpty()) {
                sendJson(exchange, 200, "[]");
                return;
            }
            Student student = students.get(0);

            Map<String, String> q = parseQuery(exchange.getRequestURI().getQuery());
            double hours = q.containsKey("hours") ? Double.parseDouble(q.get("hours")) : student.getDailyStudyHours();

            List<StudySession> sessions = studyPlanner.generateDailyPlan(student.getId(), hours);
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < sessions.size(); i++) {
                StudySession s = sessions.get(i);
                sb.append(String.format(Locale.US,
                        "{\"topicId\":%d,\"topicName\":\"%s\",\"subjectName\":\"%s\",\"allocatedHours\":%.1f,\"difficulty\":%d,\"difficultyText\":\"%s\"}",
                        s.getTopic().getTopicId(), escapeJson(s.getTopic().getTopicName()),
                        escapeJson(s.getSubjectName()), s.getAllocatedHours(),
                        s.getTopic().getDifficulty(), s.getTopic().getDifficultyText()));
                if (i < sessions.size() - 1) sb.append(",");
            }
            sb.append("]");
            sendJson(exchange, 200, sb.toString());
        }
    }

    static class ProgressHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 204, "");
                return;
            }

            List<Student> students = studentRepo.getAllStudents();
            if (students.isEmpty()) {
                sendJson(exchange, 200, "{\"total\":0,\"completed\":0,\"pending\":0,\"percentage\":0.0,\"subjects\":[]}");
                return;
            }
            int studentId = students.get(0).getId();

            List<Topic> allTopics = topicRepo.getAllTopicsByStudent(studentId);
            int total = allTopics.size();
            int completed = 0;
            for (Topic t : allTopics) if (t.isCompleted()) completed++;
            int pending = total - completed;
            double percentage = total == 0 ? 0 : (completed * 100.0) / total;

            List<Subject> subjects = subjectRepo.getSubjectsByStudent(studentId);
            StringBuilder subJson = new StringBuilder("[");
            for (int i = 0; i < subjects.size(); i++) {
                Subject s = subjects.get(i);
                List<Topic> sTopics = topicRepo.getTopicsBySubject(s.getSubjectId());
                int sTot = sTopics.size();
                int sComp = 0;
                for (Topic t : sTopics) if (t.isCompleted()) sComp++;
                double sPct = sTot == 0 ? 0 : (sComp * 100.0) / sTot;

                subJson.append(String.format(Locale.US,
                        "{\"subjectId\":%d,\"name\":\"%s\",\"total\":%d,\"completed\":%d,\"percentage\":%.1f}",
                        s.getSubjectId(), escapeJson(s.getSubjectName()), sTot, sComp, sPct));
                if (i < subjects.size() - 1) subJson.append(",");
            }
            subJson.append("]");

            String json = String.format(Locale.US,
                    "{\"total\":%d,\"completed\":%d,\"pending\":%d,\"percentage\":%.1f,\"subjects\":%s}",
                    total, completed, pending, percentage, subJson.toString());
            sendJson(exchange, 200, json);
        }
    }

    static class RecommendationsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 204, "");
                return;
            }

            List<Student> students = studentRepo.getAllStudents();
            if (students.isEmpty()) {
                sendJson(exchange, 200, "{}");
                return;
            }
            int studentId = students.get(0).getId();

            // 1. Top recommendation
            Topic top = studyPlanner.getTopRecommendation(studentId);
            String topJson = "null";
            if (top != null) {
                Subject s = subjectRepo.getSubject(top.getSubjectId());
                String sName = (s != null) ? s.getSubjectName() : "Unknown";
                long days = (s != null) ? priorityCalc.calculateDaysUntilExam(s.getExamDate()) : 0;
                topJson = String.format(Locale.US,
                        "{\"topicName\":\"%s\",\"subjectName\":\"%s\",\"difficultyText\":\"%s\",\"estimatedHours\":%.1f,\"daysUntilExam\":%d}",
                        escapeJson(top.getTopicName()), escapeJson(sName), top.getDifficultyText(), top.getEstimatedHours(), days);
            }

            // 2. Revision topics
            List<Topic> revision = studyPlanner.getRevisionRecommendations(studentId);
            StringBuilder revJson = new StringBuilder("[");
            for (int i = 0; i < revision.size(); i++) {
                Topic t = revision.get(i);
                Subject s = subjectRepo.getSubject(t.getSubjectId());
                String sName = (s != null) ? s.getSubjectName() : "Unknown";
                revJson.append(String.format(Locale.US,
                        "{\"topicName\":\"%s\",\"subjectName\":\"%s\",\"difficultyText\":\"%s\"}",
                        escapeJson(t.getTopicName()), escapeJson(sName), t.getDifficultyText()));
                if (i < revision.size() - 1) revJson.append(",");
            }
            revJson.append("]");

            // 3. Low completion subjects
            Map<Subject, Double> lowComp = studyPlanner.getLowCompletionSubjects(studentId);
            StringBuilder lowJson = new StringBuilder("[");
            int count = 0;
            for (Map.Entry<Subject, Double> e : lowComp.entrySet()) {
                lowJson.append(String.format(Locale.US,
                        "{\"subjectName\":\"%s\",\"percentage\":%.1f}",
                        escapeJson(e.getKey().getSubjectName()), e.getValue()));
                if (++count < lowComp.size()) lowJson.append(",");
            }
            lowJson.append("]");

            String json = String.format(Locale.US,
                    "{\"topRecommendation\":%s,\"revisionTopics\":%s,\"lowCompletion\":%s}",
                    topJson, revJson.toString(), lowJson.toString());
            sendJson(exchange, 200, json);
        }
    }

    // Static Web UI Handler
    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            File webDir = new File("web");
            File file = new File(webDir, "index.html");

            if (!file.exists()) {
                sendResponse(exchange, 404, "404 Not Found");
                return;
            }

            byte[] bytes = Files.readAllBytes(file.toPath());
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(bytes);
            os.close();
        }
    }

    // ==========================================
    // Utility Methods
    // ==========================================

    private static void sendJson(HttpExchange exchange, int code, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(code, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    private static void sendResponse(HttpExchange exchange, int code, String text) throws IOException {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(code, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    private static void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }

    private static Map<String, String> parseBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> map = new HashMap<>();

        if (body.startsWith("{")) {
            // Basic JSON parse
            body = body.trim().replaceAll("[{}\"]", "");
            String[] pairs = body.split(",");
            for (String pair : pairs) {
                String[] kv = pair.split(":", 2);
                if (kv.length == 2) {
                    map.put(kv[0].trim(), kv[1].trim());
                }
            }
        } else {
            // Form encoded
            map = parseQuery(body);
        }
        return map;
    }

    private static Map<String, String> parseQuery(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isEmpty()) return map;

        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=", 2);
            String k = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
            String v = kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "";
            map.put(k, v);
        }
        return map;
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }
}
