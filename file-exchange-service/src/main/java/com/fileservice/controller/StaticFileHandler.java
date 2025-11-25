package com.fileservice.controller;

import com.fileservice.config.ContentTypeResolverConfig;
import com.fileservice.util.HttpHelper;
import com.fileservice.util.IoUtils;
import com.fileservice.util.LoggerUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.logging.Logger;

public class StaticFileHandler implements HttpHandler {

    private static final Logger logger = LoggerUtil.getLogger(StaticFileHandler.class);

    private static final byte[] INDEX_HTML = IoUtils.readResourceAsBytes("/public/index.html");

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        HttpHelper.addCorsHeaders(exchange);

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        String path = exchange.getRequestURI().getPath();
        if (path.equals("/") || path.isEmpty()) {
            sendIndexHtml(exchange);
            return;
        } else if (path.equals("/stats")) {
            path = "/stats.html";
        }

        String normalized = normalizeAndValidatePath(path);
        if (normalized == null) {
            HttpHelper.sendError(exchange, 400, "Invalid path");
            return;
        }

        String resourcePath = "/public" + normalized;
        byte[] data = IoUtils.readResourceAsBytes(resourcePath);

        if (data == null) {
            HttpHelper.sendError(exchange, 404, "Not found");
            return;
        }

        String fileName = normalized.substring(normalized.lastIndexOf('/') + 1);
        String mime = ContentTypeResolverConfig.resolve(fileName);

        exchange.getResponseHeaders().set("Content-Type", mime);
        exchange.getResponseHeaders().set("Cache-Control", "public, max-age=3600");
        exchange.sendResponseHeaders(200, data.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(data);
        }
    }

    private void sendIndexHtml(HttpExchange exchange) throws IOException {
        if (INDEX_HTML == null) {
            HttpHelper.sendError(exchange, 500, "index.html not available");
            return;
        }
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
        exchange.getResponseHeaders().set("Cache-Control", "no-cache");
        exchange.sendResponseHeaders(200, INDEX_HTML.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(INDEX_HTML);
        }
    }

    private String normalizeAndValidatePath(String path) {
        int q = path.indexOf('?');
        if (q > 0) path = path.substring(0, q);

        String decoded;
        try {
            decoded = URLDecoder.decode(path, "UTF-8");
        } catch (Exception e) {
            return null;
        }

        if (decoded.contains("..") || decoded.contains("\\") || !decoded.startsWith("/")) {
            logger.warning("Попытка traversal: " + path);
            return null;
        }
        return decoded;
    }
}