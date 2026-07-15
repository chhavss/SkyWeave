package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class StaticFileHandler implements HttpHandler {
    private final String webRoot;

    public StaticFileHandler(String webRoot) {
        this.webRoot = webRoot;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String requestPath = exchange.getRequestURI().getPath();
        
        // Default to index.html if pointing to root or page without extension
        if (requestPath.equals("/") || requestPath.isEmpty()) {
            requestPath = "/index.html";
        } else if (!requestPath.contains(".") && !requestPath.endsWith("/")) {
            // For routing URLs without extension, append .html if it exists
            File file = new File(webRoot, requestPath + ".html");
            if (file.exists()) {
                requestPath += ".html";
            }
        }

        Path path = Paths.get(webRoot, requestPath);
        File file = path.toFile();

        // Security check to prevent Directory Traversal attacks
        if (!file.getCanonicalPath().startsWith(new File(webRoot).getCanonicalPath())) {
            String response = "403 Forbidden";
            exchange.sendResponseHeaders(403, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
            return;
        }

        if (!file.exists() || file.isDirectory()) {
            // Serve index.html or 404 page
            File fallback = new File(webRoot, "index.html");
            if (fallback.exists()) {
                file = fallback;
                path = fallback.toPath();
            } else {
                String response = "404 Not Found";
                exchange.sendResponseHeaders(404, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
                return;
            }
        }

        String contentType = getContentType(file.getName());
        exchange.getResponseHeaders().set("Content-Type", contentType);
        
        byte[] bytes = Files.readAllBytes(path);
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private String getContentType(String fileName) {
        if (fileName.endsWith(".html") || fileName.endsWith(".htm")) return "text/html; charset=utf-8";
        if (fileName.endsWith(".css")) return "text/css; charset=utf-8";
        if (fileName.endsWith(".js")) return "application/javascript; charset=utf-8";
        if (fileName.endsWith(".png")) return "image/png";
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) return "image/jpeg";
        if (fileName.endsWith(".gif")) return "image/gif";
        if (fileName.endsWith(".svg")) return "image/svg+xml";
        if (fileName.endsWith(".ico")) return "image/x-icon";
        if (fileName.endsWith(".json")) return "application/json";
        return "application/octet-stream";
    }
}
