package proxy;

import java.io.*;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

public class ProxyHandler implements Runnable {


    private static final int REMOTE_TIMEOUT_MS = AppConfig.getInt("remote.timeout", 10000);
    private static final int BUFFER_SIZE = AppConfig.getInt("buffer.size", 8000);
    private final Socket clientSocket;
    private final ProxyLogger logger;
    private final String clientIp;
    public List<String> LIST_BLOCKED_DOMAINS = Arrays.stream(AppConfig.get("blocked.websites.keyword").split(",")).toList();

    public ProxyHandler(Socket clientSocket, ProxyLogger logger) {
        this.clientSocket = clientSocket;
        this.logger = logger;
        this.clientIp = clientSocket.getInetAddress().getHostAddress();
    }

    @Override
    public void run() {
        try {
            clientSocket.setSoTimeout(REMOTE_TIMEOUT_MS);
            InputStream clientIn = clientSocket.getInputStream();
            OutputStream clientOut = clientSocket.getOutputStream();

            String requestLine = readLine(clientIn);
            if (requestLine == null || requestLine.isEmpty()) return;


            StringBuilder headersBuilder = new StringBuilder();
            headersBuilder.append(requestLine).append("\r\n");
            String hostHeader = null;
            String line;
            while (!(line = readLine(clientIn)).isEmpty()) {
                headersBuilder.append(line).append("\r\n");
                if (line.toLowerCase().startsWith("host:")) {
                    hostHeader = line.substring(5).trim();
                }
            }
            headersBuilder.append("\r\n");

            String[] parts = requestLine.split(" ");
            if (parts.length < 2) return;

            String method = parts[0];
            String requestTarget = parts[1];
            if(!LIST_BLOCKED_DOMAINS.stream().anyMatch(domain -> requestTarget.contains(domain))){

                if ("CONNECT".equalsIgnoreCase(method)) {
                    handleHttps(requestTarget, clientIn, clientOut);
                } else {
                    handleHttp(method, requestTarget, hostHeader, headersBuilder.toString(), clientIn, clientOut);
                }
            } else {
                logger.log("UNAUTHORIZED", clientSocket.getInetAddress().getHostAddress(),"BLOCKED HOST: " + requestTarget);
            }

        } catch (IOException e) {
            closeQuietly(clientSocket);
        } finally {
            closeQuietly(clientSocket);
        }
    }


    private void handleHttps(String requestTarget, InputStream clientIn, OutputStream clientOut) throws IOException {
        String host = requestTarget;
        int port = 443;
        if (requestTarget.contains(":")) {
            String[] hp = requestTarget.split(":");
            host = hp[0];
            port = Integer.parseInt(hp[1]);
        }

        logger.logRequest(clientIp, "CONNECT", host, "https://" + requestTarget);

        try (Socket remote = new Socket(host, port)) {
            remote.setSoTimeout(REMOTE_TIMEOUT_MS);
            clientOut.write("HTTP/1.1 200 Connection Established\r\n\r\n".getBytes());
            clientOut.flush();

            Thread toRemote = new Thread(() -> pipe(clientIn, remote));
            Thread toClient = new Thread(() -> {
                try {
                    pipe(remote.getInputStream(), clientSocket);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            toRemote.start();
            toClient.start();
            try { toRemote.join(); toClient.join(); } catch (InterruptedException ignored) {}
        } catch (IOException e) {
            clientOut.write("HTTP/1.1 502 Bad Gateway\r\n\r\n".getBytes());
            clientOut.flush();
            logger.log("ERROR", clientIp, "CONNECT failed: " + requestTarget + " — " + e.getMessage());
        }
    }

    private void pipe(InputStream in, Socket outSocket) {
        try {
            pipe(in, outSocket.getOutputStream());
        } catch (IOException ignored) {}
    }

    private void pipe(InputStream in, OutputStream out) {
        byte[] buf = new byte[BUFFER_SIZE];
        int n;
        try {
            while ((n = in.read(buf)) != -1) {
                out.write(buf, 0, n);
                out.flush();
            }
        } catch (IOException ignored) {}
    }


    private void handleHttp(String method, String requestTarget, String hostHeader,
                             String headers, InputStream clientIn, OutputStream clientOut) throws IOException {


        String host;
        int port = 80;
        String path = requestTarget;

        try {
            URI uri = new URI(requestTarget);
            host = uri.getHost();
            if (uri.getPort() != -1) port = uri.getPort();
            String rawPath = uri.getRawPath();
            String query = uri.getRawQuery();
            path = (rawPath == null || rawPath.isEmpty()) ? "/" : rawPath;
            if (query != null) path += "?" + query;
        } catch (URISyntaxException e) {
            if (hostHeader != null) {
                host = hostHeader.contains(":") ? hostHeader.split(":")[0] : hostHeader;
                if (hostHeader.contains(":")) {
                    try { port = Integer.parseInt(hostHeader.split(":")[1]); } catch (NumberFormatException ignored) {}
                }
            } else {
                logger.log("ERROR", clientIp, "Cannot parse URI: " + requestTarget);
                return;
            }
        }

        if (host == null) host = hostHeader;

        logger.logRequest(clientIp, method, host + ":" + port, requestTarget);


        String rewrittenHeaders = headers.replaceFirst(
                "^(\\S+)\\s+\\S+\\s+(HTTP/\\S+)",
                "$1 " + path.replace("$", "\\$") + " $2"
        );

        try (Socket remote = new Socket(host, port)) {
            remote.setSoTimeout(REMOTE_TIMEOUT_MS);
            OutputStream remoteOut = remote.getOutputStream();
            InputStream remoteIn = remote.getInputStream();
            remoteOut.write(rewrittenHeaders.getBytes());
            forwardBody(clientIn, remoteOut, headers);
            remoteOut.flush();
            byte[] buf = new byte[BUFFER_SIZE];
            int n;
            while ((n = remoteIn.read(buf)) != -1) {
                clientOut.write(buf, 0, n);
            }
            clientOut.flush();
        } catch (IOException e) {
            String errResp = "HTTP/1.1 502 Bad Gateway\r\nContent-Length: 0\r\n\r\n";
            clientOut.write(errResp.getBytes());
            clientOut.flush();
            logger.log("ERROR", clientIp, "HTTP forward failed: " + host + " — " + e.getMessage());
        }
    }

    private void forwardBody(InputStream clientIn, OutputStream remoteOut, String headers) throws IOException {
        String lower = headers.toLowerCase();
        int idx = lower.indexOf("content-length:");
        if (idx == -1) return;

        int end = lower.indexOf("\r\n", idx);
        String lenStr = lower.substring(idx + 15, end == -1 ? lower.length() : end).trim();
        try {
            int contentLength = Integer.parseInt(lenStr);
            byte[] body = clientIn.readNBytes(contentLength);
            remoteOut.write(body);
        } catch (NumberFormatException ignored) {}
    }

    private String readLine(InputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();
        int b;
        while ((b = in.read()) != -1) {
            if (b == '\r') {
                in.read();
                break;
            }
            if (b == '\n') break;
            sb.append((char) b);
        }
        return sb.toString();
    }

    private void closeQuietly(Closeable c) {
        try { if (c != null) c.close(); } catch (IOException ignored) {}
    }
}
