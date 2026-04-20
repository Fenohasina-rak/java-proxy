package proxy;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppConfig {
    private static final Properties properties = new Properties();

    static {
        try (InputStream input = new FileInputStream("config.properties")) {
            properties.load(input);
        } catch (IOException e) {
            System.err.println("Could not load configuration file!");
            e.printStackTrace();
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
}