import java.util.HashMap;
import java.util.Map;

/** Methane sensor (INHERITANCE from Sensor). */
public class MQ4Sensor extends Sensor {

    public MQ4Sensor() {
        super("MQ-4", "Methane");
    }

    @Override
    public Map<String, Double> readValues() {
        Map<String, Double> readings = new HashMap<>();
        readings.put("CH4", simulate(0.4, 2.9)); // % LEL
        return readings;
    }
}
