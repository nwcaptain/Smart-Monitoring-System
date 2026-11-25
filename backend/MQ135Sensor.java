import java.util.HashMap;
import java.util.Map;

/** Air quality sensor (INHERITANCE from Sensor, POLYMORPHIC readValues()). */
public class MQ135Sensor extends Sensor {

    public MQ135Sensor() {
        super("MQ-135", "Air Quality");
    }

    @Override
    public Map<String, Double> readValues() {
        Map<String, Double> readings = new HashMap<>();
        readings.put("AQI", simulate(55, 200)); // PPM equivalent
        return readings;
    }
}
