package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import config.DatabaseConfig;
import models.Airport;
import models.Flight;
import algorithms.Graph;
import algorithms.Dijkstra;
import algorithms.BFS;
import algorithms.DFS;
import utils.JsonParser;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RouteHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();

        if ("OPTIONS".equalsIgnoreCase(method)) {
            HandlerUtils.handleOptions(exchange);
            return;
        }

        if (!"GET".equalsIgnoreCase(method)) {
            HandlerUtils.sendResponse(exchange, 405, "{\"status\":\"error\",\"message\":\"Method Not Allowed\"}");
            return;
        }

        try {
            // Parse query params
            String query = exchange.getRequestURI().getQuery();
            Map<String, String> queryMap = parseQueryParams(query);

            String source = queryMap.get("source");
            String destination = queryMap.get("destination");
            String optimization = queryMap.get("optimization"); // distance, cost, duration
            String algorithm = queryMap.get("algorithm"); // dijkstra, bfs, dfs

            if (source == null) {
                HandlerUtils.sendResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"Missing 'source' parameter\"}");
                return;
            }

            // Default to Dijkstra if no algorithm is specified (frontend does not expose this choice)
            if (algorithm == null || algorithm.isEmpty()) {
                algorithm = "dijkstra";
            }

            source = source.toUpperCase().trim();
            if (destination != null) {
                destination = destination.toUpperCase().trim();
            }
            if (optimization == null) {
                optimization = "distance";
            }
            optimization = optimization.toLowerCase().trim();
            algorithm = algorithm.toLowerCase().trim();

            // Load Graph from Database
            Graph graph = new Graph();
            try (Connection conn = DatabaseConfig.getConnection();
                 Statement stmt = conn.createStatement()) {
                
                // Load Airports
                try (ResultSet rs = stmt.executeQuery("SELECT * FROM Airport")) {
                    while (rs.next()) {
                        graph.addAirport(new Airport(
                                rs.getInt("id"),
                                rs.getString("airport_code"),
                                rs.getString("airport_name"),
                                rs.getString("city"),
                                rs.getString("country")
                        ));
                    }
                }

                // Load Flights
                try (ResultSet rs = stmt.executeQuery("SELECT * FROM Flight")) {
                    while (rs.next()) {
                        graph.addFlight(new Flight(
                                rs.getInt("id"),
                                rs.getString("flight_number"),
                                rs.getString("airline"),
                                rs.getString("source_airport"),
                                rs.getString("destination_airport"),
                                rs.getInt("distance"),
                                rs.getInt("duration"),
                                rs.getInt("cost"),
                                rs.getString("status")
                        ));
                    }
                }
            }

            // Run chosen algorithm
            StringBuilder response = new StringBuilder("{\"status\":\"success\",\"algorithm\":\"" + algorithm + "\",");

            if ("dijkstra".equals(algorithm)) {
                if (destination == null || destination.isEmpty()) {
                    HandlerUtils.sendResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"Dijkstra requires 'destination' airport\"}");
                    return;
                }
                Dijkstra.RouteResult route = Dijkstra.findShortestPath(graph, source, destination, optimization);
                response.append("\"found\":").append(route.found).append(",")
                        .append("\"total_distance\":").append(route.totalDistance).append(",")
                        .append("\"total_cost\":").append(route.totalCost).append(",")
                        .append("\"total_duration\":").append(route.totalDuration).append(",")
                        .append("\"stops\":").append(route.stops).append(",");

                // Append path
                response.append("\"path\":[");
                for (int i = 0; i < route.path.size(); i++) {
                    Flight f = route.path.get(i);
                    response.append("{")
                            .append("\"flight_number\":\"").append(JsonParser.escape(f.getFlightNumber())).append("\",")
                            .append("\"airline\":\"").append(JsonParser.escape(f.getAirline())).append("\",")
                            .append("\"source_airport\":\"").append(JsonParser.escape(f.getSource())).append("\",")
                            .append("\"destination_airport\":\"").append(JsonParser.escape(f.getDestination())).append("\",")
                            .append("\"distance\":").append(f.getDistance()).append(",")
                            .append("\"duration\":").append(f.getDuration()).append(",")
                            .append("\"cost\":").append(f.getCost()).append(",")
                            .append("\"status\":\"").append(JsonParser.escape(f.getStatus())).append("\"")
                            .append("}");
                    if (i < route.path.size() - 1) {
                        response.append(",");
                    }
                }
                response.append("],");

                // Append logs
                response.append("\"logs\":[");
                for (int i = 0; i < route.logs.size(); i++) {
                    response.append("\"").append(JsonParser.escape(route.logs.get(i))).append("\"");
                    if (i < route.logs.size() - 1) {
                        response.append(",");
                    }
                }
                response.append("]");

            } else if ("bfs".equals(algorithm)) {
                BFS.BFSResult bfs = BFS.traverse(graph, source);
                response.append("\"visit_order\":[");
                for (int i = 0; i < bfs.visitOrder.size(); i++) {
                    response.append("\"").append(JsonParser.escape(bfs.visitOrder.get(i))).append("\"");
                    if (i < bfs.visitOrder.size() - 1) {
                        response.append(",");
                    }
                }
                response.append("],");

                // Append logs
                response.append("\"logs\":[");
                for (int i = 0; i < bfs.logs.size(); i++) {
                    response.append("\"").append(JsonParser.escape(bfs.logs.get(i))).append("\"");
                    if (i < bfs.logs.size() - 1) {
                        response.append(",");
                    }
                }
                response.append("]");

            } else if ("dfs".equals(algorithm)) {
                DFS.DFSResult dfs = DFS.traverse(graph, source);
                response.append("\"visit_order\":[");
                for (int i = 0; i < dfs.visitOrder.size(); i++) {
                    response.append("\"").append(JsonParser.escape(dfs.visitOrder.get(i))).append("\"");
                    if (i < dfs.visitOrder.size() - 1) {
                        response.append(",");
                    }
                }
                response.append("],");

                // Append logs
                response.append("\"logs\":[");
                for (int i = 0; i < dfs.logs.size(); i++) {
                    response.append("\"").append(JsonParser.escape(dfs.logs.get(i))).append("\"");
                    if (i < dfs.logs.size() - 1) {
                        response.append(",");
                    }
                }
                response.append("]");
            } else {
                HandlerUtils.sendResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"Unsupported algorithm type\"}");
                return;
            }

            response.append("}");
            HandlerUtils.sendResponse(exchange, 200, response.toString());

        } catch (Exception e) {
            e.printStackTrace();
            HandlerUtils.sendResponse(exchange, 500, "{\"status\":\"error\",\"message\":\"" + JsonParser.escape(e.getMessage()) + "\"}");
        }
    }

    private Map<String, String> parseQueryParams(String query) throws IOException {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isEmpty()) {
            return map;
        }

        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=");
            if (kv.length > 1) {
                String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8.name());
                String value = URLDecoder.decode(kv[1], StandardCharsets.UTF_8.name());
                map.put(key, value);
            }
        }
        return map;
    }
}
