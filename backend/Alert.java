import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * A single alert entry, matching the Alerts History table in the frontend
 * (sensor, alert, severity, value, date, time, action).
 */
public class Alert {

    private final String sensor;
    private final String message;
    private final String severity; // "WARNING" or "CRITICAL"
    private final double value;
    private final LocalDateTime timestamp;
    private final String recommendedAction;

    public Alert(String sensor, String message, String severity, double value, String recommendedAction) {
        this.sensor = sensor;
        this.message = message;
        this.severity = severity;
        this.value = value;
        this.timestamp = LocalDateTime.now();
        this.recommendedAction = recommendedAction;
    }

    public String getSensor() { return sensor; }
    public String getMessage() { return message; }
    public String getSeverity() { return severity; }
    public double getValue() { return value; }
    public String getDate() { return timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")); }
    public String getTime() { return timestamp.format(DateTimeFormatter.ofPattern("HH:mm:ss")); }
    public String getRecommendedAction() { return recommendedAction; }

    @Override
    public String toString() {
        return String.format("[%s][%s] %s (%s) = %.2f -> %s",
                getDate(), getTime(), sensor, severity, value, message);
    }
}
