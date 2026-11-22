import server.WebServer;
import ui.ConsoleMenu;

/**
 * Main Entry Point for Smart AI Study Planner.
 *
 * Launches the modern web application interface by default.
 * If '--console' argument is provided, runs the terminal console menu.
 */
public class Main {

    public static void main(String[] args) {
        if (args.length > 0 && "--console".equalsIgnoreCase(args[0])) {
            ConsoleMenu menu = new ConsoleMenu();
            menu.start();
        } else {
            try {
                WebServer.start();
            } catch (Exception e) {
                System.out.println("[Error] Failed to start Web UI: " + e.getMessage());
                System.out.println("Falling back to console interface...");
                ConsoleMenu menu = new ConsoleMenu();
                menu.start();
            }
        }
    }
}
