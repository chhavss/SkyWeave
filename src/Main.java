import com.sun.net.httpserver.HttpServer;
import handlers.AirportHandler;
import handlers.FlightHandler;
import handlers.RouteHandler;
import handlers.AuthHandler;
import handlers.StaticFileHandler;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) {
        int port = 8080;
        try {
            // Initialize HttpServer on port 8080
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

            // Bind REST API routes
            server.createContext("/api/airports", new AirportHandler());
            server.createContext("/api/flights", new FlightHandler());
            server.createContext("/api/routes", new RouteHandler());
            server.createContext("/api/auth", new AuthHandler());

            // Bind fallback static file serving context (serves HTML/CSS/JS)
            server.createContext("/", new StaticFileHandler("web"));

            // Use default executor
            server.setExecutor(null); 
            server.start();

            System.out.println("=============================================================");
            System.out.println("  SKYWEAVE Route Planner Server started on port " + port);
            System.out.println("  Access the application: http://localhost:" + port);
            System.out.println("=============================================================");

        } catch (IOException e) {
            System.err.println("Failed to start HttpServer on port " + port + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
