package com.fileservice.util;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public final class HttpHelper {

    private HttpHelper() {
    }
    public static void sendJson(HttpExchange ex, int code, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        ex.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    public static void sendError(HttpExchange ex, int code, String msg) throws IOException {
        String safeMsg = msg.replace("\"", "\\\"");
        String json = "{\"success\":false,\"message\":\"" + safeMsg + "\"}";
        sendJson(ex, code, json);
    }

    public static void addCorsHeaders(HttpExchange ex) {
        Headers h = ex.getResponseHeaders();
        h.set("Access-Control-Allow-Origin", "*");
        h.set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        h.set("Access-Control-Allow-Headers", "*");
        h.set("Access-Control-Expose-Headers", "Content-Disposition, Content-Range"); // Добавлен Content-Range
    }

    public static String getClientIp(HttpExchange ex) {
        String fwd = ex.getRequestHeaders().getFirst("X-Forwarded-For");
        if (fwd != null && !fwd.trim().isEmpty()) {
            return fwd.split(",")[0].trim();
        }
        return ex.getRemoteAddress().getAddress().getHostAddress();
    }
}