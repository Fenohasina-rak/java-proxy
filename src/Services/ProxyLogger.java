package proxy.src.Services;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ProxyLogger {

    private static final String LOG_FILE = "proxy.log";
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final PrintWriter fileWriter;

    public ProxyLogger() throws IOException {
        this.fileWriter = new PrintWriter(new FileWriter(LOG_FILE, true), true);
        log("INFO", "SYSTEM", "Proxy logger started");
    }

    public synchronized void logRequest(String clientIp, String method, String host, String url) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String line = String.format("[%s] | %s | IP:%s | HOST: %s | URL: %s",
                timestamp, method, clientIp, host, url);
        fileWriter.println(line);
    }

    public synchronized void log(String level, String clientIp, String message) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String line = String.format("[%s] | level:%s | IP:%s | message:%s",
                timestamp, level, clientIp, message);
        fileWriter.println(line);
    }
}
