package proxy;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProxyServer {
    public static int APP_PORT = AppConfig.getInt("app.port", 8080);

    public static void main(String[] args) throws IOException {
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        ProxyLogger logger = new ProxyLogger();
        try (ServerSocket serverSocket = new ServerSocket(APP_PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                executor.submit(new ProxyHandler(clientSocket, logger));
            }
        }
    }
}
