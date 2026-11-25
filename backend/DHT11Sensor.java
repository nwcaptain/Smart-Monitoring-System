import java.util.HashMap;
import java.util.Map;

/** Temperature + humidity sensor (INHERITANCE; returns two readings). */
public class DHT11Sensor extends Sensor {

    public DHT11Sensor() {
        super("DHT11", "Temperature & Humidity");
    }

    @Override
    public Map<String, Double> readValues() {
        Map<String, Double> readings = new HashMap<>();
        readings.put("Temperature", simulate(27.5, 31.5)); // Celsius
        readings.put("Humidity", simulate(65, 80));         // %
        return readings;
    }
}
