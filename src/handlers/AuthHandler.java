package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import config.DatabaseConfig;
import utils.JsonParser;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

public class AuthHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();

        if ("OPTIONS".equalsIgnoreCase(method)) {
            HandlerUtils.handleOptions(exchange);
            return;
        }

        if (!"POST".equalsIgnoreCase(method)) {
            HandlerUtils.sendResponse(exchange, 405, "{\"status\":\"error\",\"message\":\"Method Not Allowed\"}");
            return;
        }

        try {
            String body = HandlerUtils.readRequestBody(exchange);
            Map<String, String> data = JsonParser.parseObject(body);

            String username = data.get("username");
            String password = data.get("password");

            if (username == null || password == null) {
                HandlerUtils.sendResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"Username and password are required\"}");
                return;
            }

            boolean authenticated = false;

            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("SELECT password FROM Admin WHERE username = ?")) {
                pstmt.setString(1, username.trim());
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        String hashed = rs.getString("password");
                        if (BCrypt.checkpw(password, hashed)) {
                            authenticated = true;
                        }
                    }
                }
            }

            if (authenticated) {
                HandlerUtils.sendResponse(exchange, 200, 
                        "{\"status\":\"success\",\"token\":\"skyweave-admin-session-token\",\"message\":\"Welcome to SKYWEAVE Admin Console\"}");
            } else {
                HandlerUtils.sendResponse(exchange, 401, "{\"status\":\"error\",\"message\":\"Invalid username or password\"}");
            }

        } catch (Exception e) {
            e.printStackTrace();
            HandlerUtils.sendResponse(exchange, 500, "{\"status\":\"error\",\"message\":\"" + JsonParser.escape(e.getMessage()) + "\"}");
        }
    }
}
