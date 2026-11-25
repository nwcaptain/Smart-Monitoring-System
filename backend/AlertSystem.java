import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Evaluates sensor readings against safety thresholds and produces Alert
 * objects. Thresholds mirror the SAFE / WARNING / DANGER states used in the
 * frontend simulation.
 */
public class AlertSystem {

    /** Checks one sensor's readings and returns any alerts it triggers. */
    public List<Alert> evaluate(String sensorId, Map<String, Double> readings) {
        List<Alert> alerts = new ArrayList<>();

        for (Map.Entry<String, Double> entry : readings.entrySet()) {
            String reading = entry.getKey();
            double value = entry.getValue();

            switch (reading) {
                case "CO":
                    if (value >= 30) alerts.add(alert(sensorId, "Elevated Carbon Monoxide", value, 30, 40));
                    break;
                case "CH4":
                    if (value >= 1.5) alerts.add(alert(sensorId, "Elevated Methane (LEL)", value, 1.5, 2.5));
                    break;
                case "H2S":
                    if (value >= 4) alerts.add(alert(sensorId, "Hydrogen Sulfide Detected", value, 4, 15));
                    break;
                case "AQI":
                    if (value >= 100) alerts.add(alert(sensorId, "Poor Air Quality", value, 100, 150));
                    break;
                case "SpO2":
                    if (value <= 95) alerts.add(alert(sensorId, "Low Blood Oxygen", value, 95, 90));
                    break;
                case "HeartRate":
                    if (value >= 100) alerts.add(alert(sensorId, "Elevated Heart Rate", value, 100, 120));
                    break;
                default:
                    // Temperature / Humidity: informational only, no alert threshold
            }
        }
        return alerts;
    }

    private Alert alert(String sensorId, String message, double value, double warnAt, double criticalAt) {
        boolean critical = value >= criticalAt;
        String severity = critical ? "CRITICAL" : "WARNING";
        String action = critical
                ? "Evacuate area immediately and notify supervisor."
                : "Monitor closely; ventilate area if possible.";
        return new Alert(sensorId, message, severity, value, action);
    }
}
