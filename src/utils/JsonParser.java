package utils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonParser {

    /**
     * Parses a simple flat JSON object string into a key-value Map.
     * Handles string values, integer values, and ignores nested structures or arrays.
     */
    public static Map<String, String> parseObject(String json) {
        Map<String, String> map = new HashMap<>();
        if (json == null || json.trim().isEmpty()) {
            return map;
        }

        // Clean braces
        String clean = json.trim();
        if (clean.startsWith("{")) {
            clean = clean.substring(1);
        }
        if (clean.endsWith("}")) {
            clean = clean.substring(0, clean.length() - 1);
        }

        // Match "key" : "value" or "key" : number
        Pattern pattern = Pattern.compile("\"([^\"]+)\"\\s*:\\s*(?:\"([^\"]*)\"|([^,{}]+))");
        Matcher matcher = pattern.matcher(clean);

        while (matcher.find()) {
            String key = matcher.group(1);
            String value = matcher.group(2) != null ? matcher.group(2) : matcher.group(3).trim();
            map.put(key, value);
        }

        return map;
    }

    /**
     * Escapes special JSON characters.
     */
    public static String escape(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\b", "\\b")
                    .replace("\f", "\\f")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
    }
}
