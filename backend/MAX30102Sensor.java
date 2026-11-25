import java.util.HashMap;
import java.util.Map;

/** Heart rate + SpO2 sensor (INHERITANCE; returns two readings). */
public class MAX30102Sensor extends Sensor {

    public MAX30102Sensor() {
        super("MAX30102", "Heart Rate & SpO2");
    }

    @Override
    public Map<String, Double> readValues() {
        Map<String, Double> readings = new HashMap<>();
        readings.put("HeartRate", simulate(70, 115)); // BPM
        readings.put("SpO2", simulate(93, 100));       // %
        return readings;
    }
}
