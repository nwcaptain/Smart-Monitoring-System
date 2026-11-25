import java.util.Map;

/**
 * Abstract base class for all hardware sensors.
 * Demonstrates ABSTRACTION: readValues() hides how each physical sensor is
 * actually read (hardware protocol, calibration, etc.) behind one method.
 * Subclasses provide POLYMORPHIC implementations.
 */
public abstract class Sensor {

    protected String sensorId;   // e.g. "MQ-135"
    protected String sensorName; // e.g. "Air Quality Sensor"

    public Sensor(String sensorId, String sensorName) {
        this.sensorId = sensorId;
        this.sensorName = sensorName;
    }

    public String getSensorId() {
        return sensorId;
    }

    public String getSensorName() {
        return sensorName;
    }

    /**
     * Reads current value(s) from the sensor.
     * Returns a map of reading-name -> value because some sensors expose
     * more than one measurement (e.g. DHT11 -> temperature + humidity).
     */
    public abstract Map<String, Double> readValues();

    /**
     * Shared helper (inherited by every subclass) that simulates a hardware
     * reading. Replace with real ADC/I2C/serial reads on actual hardware.
     */
    protected double simulate(double min, double max) {
        return Math.round((min + Math.random() * (max - min)) * 100.0) / 100.0;
    }
}
