package proxy.src.Helpers;


import proxy.src.Services.ProxyLogger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Properties;
import java.util.stream.Stream;

public class AppConfig {
    private static final Properties properties = new Properties();
    public static final HashSet<String> LIST_BLOCKED_DOMAINS_FROM_FILE;
    static {
        try {
            try (InputStream input = new FileInputStream("Resources/config.properties")) {
                properties.load(input);
            }
            LIST_BLOCKED_DOMAINS_FROM_FILE = AppConfig.readFile("Resources/light.txt");
            System.out.println("Configuration and blocklist loaded successfully.");
        } catch (IOException e) {
            System.err.println("Critical Error: Failed to initialize application settings.");
            throw new RuntimeException("Initialization failed", e);
        }
    }

    public static String get(String key) {
        return properties.getProperty(key);
    }

    public static String get(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public static int getInt(String key, int defaultValue) {
        String value = properties.getProperty(key);
        return (value != null) ? Integer.parseInt(value) : defaultValue;
    }

    public static HashSet<String> readFile(String filePath) throws IOException {
        HashSet<String> listOfString = new HashSet<>();
        Path path = Path.of(filePath);
        try (Stream<String> lines = Files.lines(path)) {
            lines.forEach(listOfString::add);
        } catch (IOException e) {
            ProxyLogger logger = new ProxyLogger();
            logger.log("ERROR", "NO IP", "ERROR WHILE LOADING FILE " + filePath);
        }
        return listOfString;
    }
}