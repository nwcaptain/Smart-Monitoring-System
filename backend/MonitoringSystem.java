import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Aggregates all hardware sensors and coordinates a scan cycle:
 * read every sensor -> validate -> store in Database -> check alerts.
 * Demonstrates ASSOCIATION: this class "has-a" list of Sensor objects and
 * works with them polymorphically through the Sensor base type.
 */
public class MonitoringSystem {

    private final List<Sensor> sensors;
    private final Database database;
    private final AlertSystem alertSystem;

    public MonitoringSystem(List<Sensor> sensors, Database database, AlertSystem alertSystem) {
        this.sensors = sensors;
        this.database = database;
        this.alertSystem = alertSystem;
    }

    /**
     * Reads every sensor once, validates, stores the readings, evaluates
     * alerts, and returns everything gathered this cycle so the Dashboard
     * can display it.
     */
    public Map<String, Map<String, Double>> scanAll() {
        Map<String, Map<String, Double>> allReadings = new LinkedHashMap<>();

        for (Sensor sensor : sensors) {
            Map<String, Double> readings = sensor.readValues(); // polymorphic call
            if (!validate(readings)) {
                continue;
            }
            allReadings.put(sensor.getSensorId(), readings);

            database.storeReadings(sensor.getSensorId(), readings);

            List<Alert> alerts = alertSystem.evaluate(sensor.getSensorId(), readings);
            if (!alerts.isEmpty()) {
                database.storeAlerts(alerts);
                for (Alert a : alerts) {
                    System.out.println("  !! ALERT: " + a);
                }
            }
        }
        return allReadings;
    }

    /** Basic sanity check on readings (no NaN/negative-impossible values). */
    private boolean validate(Map<String, Double> readings) {
        for (Double v : readings.values()) {
            if (v == null || v.isNaN()) {
                return false;
            }
        }
        return true;
    }
}
