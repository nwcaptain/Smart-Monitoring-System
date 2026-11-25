import java.util.HashMap;
import java.util.Map;

/** Carbon monoxide sensor (INHERITANCE from Sensor). */
public class MQ7Sensor extends Sensor {

    public MQ7Sensor() {
        super("MQ-7", "Carbon Monoxide");
    }

    @Override
    public Map<String, Double> readValues() {
        Map<String, Double> readings = new HashMap<>();
        readings.put("CO", simulate(8, 42)); // ppm
        return readings;
    }
}
