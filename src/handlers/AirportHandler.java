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

public class AirportHandler implements HttpHandler {

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
        List<Map<String, String>> airports = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Airport ORDER BY city")) {
            
            while (rs.next()) {
                Map<String, String> ap = new HashMap<>();
                ap.put("id", String.valueOf(rs.getInt("id")));
                ap.put("airport_code", rs.getString("airport_code"));
                ap.put("airport_name", rs.getString("airport_name"));
                ap.put("city", rs.getString("city"));
                ap.put("country", rs.getString("country"));
                airports.add(ap);
            }
        }

        // Build JSON manually to avoid dependencies
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < airports.size(); i++) {
            Map<String, String> ap = airports.get(i);
            json.append("{")
                .append("\"id\":").append(ap.get("id")).append(",")
                .append("\"airport_code\":\"").append(JsonParser.escape(ap.get("airport_code"))).append("\",")
                .append("\"airport_name\":\"").append(JsonParser.escape(ap.get("airport_name"))).append("\",")
                .append("\"city\":\"").append(JsonParser.escape(ap.get("city"))).append("\",")
                .append("\"country\":\"").append(JsonParser.escape(ap.get("country"))).append("\"")
                .append("}");
            if (i < airports.size() - 1) {
                json.append(",");
            }
        }
        json.append("]");

        HandlerUtils.sendResponse(exchange, 200, json.toString());
    }

    private void handlePost(HttpExchange exchange) throws Exception {
        String body = HandlerUtils.readRequestBody(exchange);
        Map<String, String> data = JsonParser.parseObject(body);

        String code = data.get("airport_code");
        String name = data.get("airport_name");
        String city = data.get("city");
        String country = data.get("country");

        if (code == null || name == null || city == null || country == null) {
            HandlerUtils.sendResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"Missing required fields\"}");
            return;
        }

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "INSERT INTO Airport (airport_code, airport_name, city, country) VALUES (?, ?, ?, ?)")) {
            pstmt.setString(1, code.toUpperCase().trim());
            pstmt.setString(2, name.trim());
            pstmt.setString(3, city.trim());
            pstmt.setString(4, country.trim());
            pstmt.executeUpdate();
        }

        HandlerUtils.sendResponse(exchange, 201, "{\"status\":\"success\",\"message\":\"Airport added successfully\"}");
    }

    private void handlePut(HttpExchange exchange) throws Exception {
        String body = HandlerUtils.readRequestBody(exchange);
        Map<String, String> data = JsonParser.parseObject(body);

        String idStr = data.get("id");
        String code = data.get("airport_code");
        String name = data.get("airport_name");
        String city = data.get("city");
        String country = data.get("country");

        if (idStr == null || code == null || name == null || city == null || country == null) {
            HandlerUtils.sendResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"Missing required fields\"}");
            return;
        }

        int id = Integer.parseInt(idStr);

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "UPDATE Airport SET airport_code = ?, airport_name = ?, city = ?, country = ? WHERE id = ?")) {
            pstmt.setString(1, code.toUpperCase().trim());
            pstmt.setString(2, name.trim());
            pstmt.setString(3, city.trim());
            pstmt.setString(4, country.trim());
            pstmt.setInt(5, id);
            int rows = pstmt.executeUpdate();

            if (rows > 0) {
                HandlerUtils.sendResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"Airport updated successfully\"}");
            } else {
                HandlerUtils.sendResponse(exchange, 404, "{\"status\":\"error\",\"message\":\"Airport not found\"}");
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
             PreparedStatement pstmt = conn.prepareStatement("DELETE FROM Airport WHERE id = ?")) {
            pstmt.setInt(1, id);
            int rows = pstmt.executeUpdate();

            if (rows > 0) {
                HandlerUtils.sendResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"Airport deleted successfully\"}");
            } else {
                HandlerUtils.sendResponse(exchange, 404, "{\"status\":\"error\",\"message\":\"Airport not found\"}");
            }
        }
    }
}
