import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Entry point. Implements the pseudocode exactly:
 *   START -> (optional REGISTER) -> LOGIN -> Authentication.login()
 *   -> IF correct: DISPLAY Dashboard, INITIALIZE sensors,
 *      WHILE active: READ -> VALIDATE -> STORE -> DISPLAY
 *   ELSE: DISPLAY "Invalid username or password" -> STOP
 */
public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Authentication auth = new Authentication();

        // ---- REGISTER (optional) ----
        System.out.print("Register a new account first? (y/n): ");
        if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
            System.out.print("Full name: ");
            String name = scanner.nextLine();
            System.out.print("Username: ");
            String username = scanner.nextLine();
            System.out.print("Password: ");
            String password = scanner.nextLine();
            System.out.print("Confirm password: ");
            String confirm = scanner.nextLine();
            auth.register(name, username, password, confirm);
        }

        // ---- LOGIN ----
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        User loggedInUser = auth.login(username, password);

        if (loggedInUser == null) {
            System.out.println("Invalid username or password.");
            return; // STOP
        }

        // ---- DASHBOARD + HARDWARE ----
        Dashboard dashboard = new Dashboard();

        List<Sensor> sensors = Arrays.asList(
                new MQ135Sensor(),
                new MQ7Sensor(),
                new MQ4Sensor(),
                new MQ136Sensor(),
                new DHT11Sensor(),
                new MAX30102Sensor()
        );

        Database database = new Database();
        AlertSystem alertSystem = new AlertSystem();
        MonitoringSystem monitoringSystem = new MonitoringSystem(sensors, database, alertSystem);

        // ---- WHILE system is active ----
        int cycles = 5; // demo: run 5 scan cycles instead of forever
        for (int i = 0; i < cycles; i++) {
            Map<String, Map<String, Double>> readings = monitoringSystem.scanAll();
            dashboard.display(loggedInUser, readings);

            try {
                Thread.sleep(1000); // simulate a real-time interval between reads
            } catch (InterruptedException ignored) {
            }
        }

        System.out.println("Session ended. Total readings stored: " + database.getReadingLog().size());
        System.out.println("Total alerts logged: " + database.getAlertLog().size());
        scanner.close();
    }
}
