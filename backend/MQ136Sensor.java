import java.util.HashMap;
import java.util.Map;

/** Hydrogen sulfide (H2S) sensor (INHERITANCE from Sensor). */
public class MQ136Sensor extends Sensor {

    public MQ136Sensor() {
        super("MQ-136", "Hydrogen Sulfide");
    }

    @Override
    public Map<String, Double> readValues() {
        Map<String, Double> readings = new HashMap<>();
        readings.put("H2S", simulate(1.2, 22.0)); // ppm
        return readings;
    }
}
