import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Simple in-memory "database" for storing sensor readings and alerts.
 * Swap the internals for a real JDBC/ORM implementation later without
 * changing the rest of the system (ABSTRACTION of the storage layer).
 */
public class Database {

    /** One stored reading snapshot (sensorId, reading name, value, time). */
    public static class ReadingRecord {
        public final String sensorId;
        public final String reading;
        public final double value;
        public final LocalDateTime timestamp;

        public ReadingRecord(String sensorId, String reading, double value) {
            this.sensorId = sensorId;
            this.reading = reading;
            this.value = value;
            this.timestamp = LocalDateTime.now();
        }
    }

    private final List<ReadingRecord> readingLog = new ArrayList<>();
    private final List<Alert> alertLog = new ArrayList<>();

    public void storeReadings(String sensorId, Map<String, Double> readings) {
        for (Map.Entry<String, Double> entry : readings.entrySet()) {
            readingLog.add(new ReadingRecord(sensorId, entry.getKey(), entry.getValue()));
        }
    }

    public void storeAlerts(List<Alert> alerts) {
        alertLog.addAll(alerts);
    }

    public List<ReadingRecord> getReadingLog() {
        return readingLog;
    }

    public List<Alert> getAlertLog() {
        return alertLog;
    }
}
