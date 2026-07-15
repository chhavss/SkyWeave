package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import config.DatabaseConfig;
import utils.JsonParser;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FlightHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();

        if ("OPTIONS".equalsIgnoreCase(method)) {
            HandlerUtils.handleOptions(exchange);
            return;
        }

        try {
            switch (method.toUpperCase()) {
                case "GET":
                    handleGet(exchange);
                    break;
                case "POST":
                    handlePost(exchange);
                    break;
                case "PUT":
                    handlePut(exchange);
                    break;
                case "DELETE":
                    handleDelete(exchange);
                    break;
                default:
                    HandlerUtils.sendResponse(exchange, 405, "{\"status\":\"error\",\"message\":\"Method Not Allowed\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            HandlerUtils.sendResponse(exchange, 500, "{\"status\":\"error\",\"message\":\"" + JsonParser.escape(e.getMessage()) + "\"}");
        }
    }

    private void handleGet(HttpExchange exchange) throws Exception {
        List<Map<String, String>> flights = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Flight ORDER BY flight_number")) {
            
            while (rs.next()) {
                Map<String, String> f = new HashMap<>();
                f.put("id", String.valueOf(rs.getInt("id")));
                f.put("flight_number", rs.getString("flight_number"));
                f.put("airline", rs.getString("airline"));
                f.put("source_airport", rs.getString("source_airport"));
                f.put("destination_airport", rs.getString("destination_airport"));
                f.put("distance", String.valueOf(rs.getInt("distance")));
                f.put("duration", String.valueOf(rs.getInt("duration")));
                f.put("cost", String.valueOf(rs.getInt("cost")));
                f.put("status", rs.getString("status"));
                flights.add(f);
            }
        }

        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < flights.size(); i++) {
            Map<String, String> f = flights.get(i);
            json.append("{")
                .append("\"id\":").append(f.get("id")).append(",")
                .append("\"flight_number\":\"").append(JsonParser.escape(f.get("flight_number"))).append("\",")
                .append("\"airline\":\"").append(JsonParser.escape(f.get("airline"))).append("\",")
                .append("\"source_airport\":\"").append(JsonParser.escape(f.get("source_airport"))).append("\",")
                .append("\"destination_airport\":\"").append(JsonParser.escape(f.get("destination_airport"))).append("\",")
                .append("\"distance\":").append(f.get("distance")).append(",")
                .append("\"duration\":").append(f.get("duration")).append(",")
                .append("\"cost\":").append(f.get("cost")).append(",")
                .append("\"status\":\"").append(JsonParser.escape(f.get("status"))).append("\"")
                .append("}");
            if (i < flights.size() - 1) {
                json.append(",");
            }
        }
        json.append("]");

        HandlerUtils.sendResponse(exchange, 200, json.toString());
    }

    private void handlePost(HttpExchange exchange) throws Exception {
        String body = HandlerUtils.readRequestBody(exchange);
        Map<String, String> data = JsonParser.parseObject(body);

        String fn = data.get("flight_number");
        String airline = data.get("airline");
        String source = data.get("source_airport");
        String dest = data.get("destination_airport");
        String distStr = data.get("distance");
        String durStr = data.get("duration");
        String costStr = data.get("cost");
        String status = data.get("status");

        if (fn == null || airline == null || source == null || dest == null || 
            distStr == null || durStr == null || costStr == null) {
            HandlerUtils.sendResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"Missing required fields\"}");
            return;
        }

        int dist = Integer.parseInt(distStr.trim());
        int dur = Integer.parseInt(durStr.trim());
        int cost = Integer.parseInt(costStr.trim());
        if (status == null) status = "Available";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "INSERT INTO Flight (flight_number, airline, source_airport, destination_airport, distance, duration, cost, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
            pstmt.setString(1, fn.toUpperCase().trim());
            pstmt.setString(2, airline.trim());
            pstmt.setString(3, source.toUpperCase().trim());
            pstmt.setString(4, dest.toUpperCase().trim());
            pstmt.setInt(5, dist);
            pstmt.setInt(6, dur);
            pstmt.setInt(7, cost);
            pstmt.setString(8, status.trim());
            pstmt.executeUpdate();
        }

        HandlerUtils.sendResponse(exchange, 201, "{\"status\":\"success\",\"message\":\"Flight added successfully\"}");
    }

    private void handlePut(HttpExchange exchange) throws Exception {
        String body = HandlerUtils.readRequestBody(exchange);
        Map<String, String> data = JsonParser.parseObject(body);

        String idStr = data.get("id");
        String fn = data.get("flight_number");
        String airline = data.get("airline");
        String source = data.get("source_airport");
        String dest = data.get("destination_airport");
        String distStr = data.get("distance");
        String durStr = data.get("duration");
        String costStr = data.get("cost");
        String status = data.get("status");

        if (idStr == null || fn == null || airline == null || source == null || dest == null || 
            distStr == null || durStr == null || costStr == null || status == null) {
            HandlerUtils.sendResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"Missing required fields\"}");
            return;
        }

        int id = Integer.parseInt(idStr);
        int dist = Integer.parseInt(distStr.trim());
        int dur = Integer.parseInt(durStr.trim());
        int cost = Integer.parseInt(costStr.trim());

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "UPDATE Flight SET flight_number = ?, airline = ?, source_airport = ?, destination_airport = ?, distance = ?, duration = ?, cost = ?, status = ? WHERE id = ?")) {
            pstmt.setString(1, fn.toUpperCase().trim());
            pstmt.setString(2, airline.trim());
            pstmt.setString(3, source.toUpperCase().trim());
            pstmt.setString(4, dest.toUpperCase().trim());
            pstmt.setInt(5, dist);
            pstmt.setInt(6, dur);
            pstmt.setInt(7, cost);
            pstmt.setString(8, status.trim());
            pstmt.setInt(9, id);
            int rows = pstmt.executeUpdate();

            if (rows > 0) {
                HandlerUtils.sendResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"Flight updated successfully\"}");
            } else {
                HandlerUtils.sendResponse(exchange, 404, "{\"status\":\"error\",\"message\":\"Flight not found\"}");
            }
        }
    }

    private void handleDelete(HttpExchange exchange) throws Exception {
        String query = exchange.getRequestURI().getQuery();
        String idStr = null;

        if (query != null) {
            String[] params = query.split("&");
            for (String param : params) {
                String[] pair = param.split("=");
                if (pair.length > 1 && "id".equals(pair[0])) {
                    idStr = URLDecoder.decode(pair[1], StandardCharsets.UTF_8.name());
                }
            }
        }

        if (idStr == null) {
            HandlerUtils.sendResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"Missing required 'id' parameter\"}");
            return;
        }

        int id = Integer.parseInt(idStr);

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("DELETE FROM Flight WHERE id = ?")) {
            pstmt.setInt(1, id);
            int rows = pstmt.executeUpdate();

            if (rows > 0) {
                HandlerUtils.sendResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"Flight deleted successfully\"}");
            } else {
                HandlerUtils.sendResponse(exchange, 404, "{\"status\":\"error\",\"message\":\"Flight not found\"}");
            }
        }
    }
}
