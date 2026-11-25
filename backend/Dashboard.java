import java.util.Map;

/** Displays the logged-in user's dashboard with the latest sensor readings. */
public class Dashboard {

    public void display(User user, Map<String, Map<String, Double>> allReadings) {
        System.out.println();
        System.out.println("=====================================================");
        System.out.println(" SMART WEATHER & HARMFUL GAS MONITORING - Supervisor: " + user.getName());
        System.out.println("=====================================================");

        for (Map.Entry<String, Map<String, Double>> sensorEntry : allReadings.entrySet()) {
            String sensorId = sensorEntry.getKey();
            for (Map.Entry<String, Double> reading : sensorEntry.getValue().entrySet()) {
                System.out.printf("  [%-9s] %-12s : %.2f%n", sensorId, reading.getKey(), reading.getValue());
            }
        }
        System.out.println("SYSTEM STATUS: ACTIVE");
        System.out.println("=====================================================");
    }
}
