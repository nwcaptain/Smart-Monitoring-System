import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * Exposes the existing backend (Authentication, MonitoringSystem, Database,
 * AlertSystem, Sensor classes) over plain HTTP/JSON so code.html can call it
 * with fetch(), instead of using its internal random-jitter simulation.
 *
 * Endpoints:
 *   POST /api/login    {username, password}          -> {success, name}
 *   POST /api/register {name, username, password,
 *                        confirmPassword}             -> {success, message}
 *   GET  /api/readings                                -> latest sensor snapshot
 *   GET  /api/alerts                                  -> alert history
 *
 * No new backend classes were invented: this only wraps the existing ones.
 */
public class ApiServer {

    private static final Authentication auth = new Authentication();
    private static final List<Sensor> sensors = Arrays.asList(
            new MQ135Sensor(), new MQ7Sensor(), new MQ4Sensor(),
            new MQ136Sensor(), new DHT11Sensor(), new MAX30102Sensor());
    private static final Database database = new Database();
    private static final AlertSystem alertSystem = new AlertSystem();
    private static final MonitoringSystem monitoringSystem =
            new MonitoringSystem(sensors, database, alertSystem);

    private static volatile Map<String, Map<String, Double>> latestReadings = new LinkedHashMap<>();

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(
        System.getenv().getOrDefault("PORT", "8080")
        );
        latestReadings = monitoringSystem.scanAll(); // initial snapshot so /api/readings is never empty
        HttpServer server = HttpServer.create(
            new InetSocketAddress("0.0.0.0", port), 0
        );
        server.setExecutor(Executors.newFixedThreadPool(4));
        server.createContext("/api/login", ApiServer::handleLogin);
        server.createContext("/api/register", ApiServer::handleRegister);
        server.createContext("/api/readings", ApiServer::handleReadings);
        server.createContext("/api/alerts", ApiServer::handleAlerts);
        server.start();

        // Background hardware-scan loop, same cadence as the frontend's old 3s simulator
        Thread scanner = new Thread(() -> {
            while (true) {
                latestReadings = monitoringSystem.scanAll();
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ignored) {
                }
            }
        });
        scanner.setDaemon(true);
        scanner.start();
        System.out.println("API server running on port " + port);
        }

    private static void handleLogin(HttpExchange ex) throws IOException {
        if (preflight(ex)) return;
        if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
            send(ex, 405, "{\"error\":\"Method not allowed\"}");
            return;
        }
        Map<String, String> body = parseJson(readBody(ex));
        User user = auth.login(body.getOrDefault("username", ""), body.getOrDefault("password", ""));
        if (user != null) {
            send(ex, 200, "{\"success\":true,\"name\":\"" + escape(user.getName()) + "\"}");
        } else {
            send(ex, 401, "{\"success\":false,\"message\":\"Invalid username or password\"}");
        }
    }

    private static void handleRegister(HttpExchange ex) throws IOException {
        if (preflight(ex)) return;
        if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
            send(ex, 405, "{\"error\":\"Method not allowed\"}");
            return;
        }
        Map<String, String> body = parseJson(readBody(ex));
        boolean ok = auth.register(
                body.getOrDefault("name", ""),
                body.getOrDefault("username", ""),
                body.getOrDefault("password", ""),
                body.getOrDefault("confirmPassword", ""));
        if (ok) {
            send(ex, 200, "{\"success\":true}");
        } else {
            send(ex, 400, "{\"success\":false,\"message\":\"Registration failed (duplicate username or password mismatch)\"}");
        }
    }

    private static void handleReadings(HttpExchange ex) throws IOException {
        if (preflight(ex)) return;
        Map<String, Double> flat = flatten(latestReadings);
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Double> e : flat.entrySet()) {
            if (!first) json.append(",");
            json.append("\"").append(e.getKey()).append("\":").append(e.getValue());
            first = false;
        }
        json.append("}");
        send(ex, 200, json.toString());
    }

    private static Map<String, Double> flatten(Map<String, Map<String, Double>> readings) {
        Map<String, Double> flat = new LinkedHashMap<>();
        flat.put("co", val(readings, "MQ-7", "CO"));
        flat.put("ch4", val(readings, "MQ-4", "CH4"));
        flat.put("h2s", val(readings, "MQ-136", "H2S"));
        flat.put("aqi", val(readings, "MQ-135", "AQI"));
        flat.put("temp", val(readings, "DHT11", "Temperature"));
        flat.put("hum", val(readings, "DHT11", "Humidity"));
        flat.put("hr", val(readings, "MAX30102", "HeartRate"));
        flat.put("spo2", val(readings, "MAX30102", "SpO2"));
        return flat;
    }

    private static double val(Map<String, Map<String, Double>> readings, String sensorId, String key) {
        Map<String, Double> r = readings.get(sensorId);
        return (r != null && r.get(key) != null) ? r.get(key) : 0.0;
    }

    private static void handleAlerts(HttpExchange ex) throws IOException {
        if (preflight(ex)) return;
        List<Alert> alerts = database.getAlertLog();
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < alerts.size(); i++) {
            Alert a = alerts.get(i);
            if (i > 0) json.append(",");
            json.append("{")
                    .append("\"sensor\":\"").append(escape(a.getSensor())).append("\",")
                    .append("\"alert\":\"").append(escape(a.getMessage())).append("\",")
                    .append("\"severity\":\"").append(escape(capitalize(a.getSeverity()))).append("\",")
                    .append("\"value\":\"").append(a.getValue()).append("\",")
                    .append("\"date\":\"").append(a.getDate()).append("\",")
                    .append("\"time\":\"").append(a.getTime()).append("\",")
                    .append("\"action\":\"").append(escape(a.getRecommendedAction())).append("\"")
                    .append("}");
        }
        json.append("]");
        send(ex, 200, json.toString());
    }

    // ---- helpers ----

    /** Adds CORS headers and, for an OPTIONS preflight request, answers it directly. */
    private static boolean preflight(HttpExchange ex) throws IOException {
        ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        ex.getResponseHeaders().add("Access-Control-Allow-Methods", "GET,POST,OPTIONS");
        ex.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
        if ("OPTIONS".equalsIgnoreCase(ex.getRequestMethod())) {
            ex.sendResponseHeaders(204, -1);
            return true;
        }
        return false;
    }

    private static String readBody(HttpExchange ex) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        InputStream in = ex.getRequestBody();
        byte[] buf = new byte[1024];
        int n;
        while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
        return out.toString(StandardCharsets.UTF_8);
    }

    /** Minimal flat-JSON-object parser - enough for the small login/register payloads sent here. */
    private static Map<String, String> parseJson(String json) {
        Map<String, String> map = new HashMap<>();
        json = json.trim();
        if (json.startsWith("{")) json = json.substring(1);
        if (json.endsWith("}")) json = json.substring(0, json.length() - 1);
        if (json.isEmpty()) return map;
        for (String pair : json.split(",")) {
            String[] kv = pair.split(":", 2);
            if (kv.length == 2) {
                String key = kv[0].trim().replaceAll("^\"|\"$", "");
                String val = kv[1].trim().replaceAll("^\"|\"$", "");
                map.put(key, val);
            }
        }
        return map;
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.charAt(0) + s.substring(1).toLowerCase();
    }

    private static void send(HttpExchange ex, int status, String body) throws IOException {
        ex.getResponseHeaders().add("Content-Type", "application/json");
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }
}
