package com.fileservice.controller;

import com.fileservice.util.HttpHelper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

public class FileRouter implements HttpHandler {

    private final UploadHandler uploadHandler;
    private final DownloadHandler downloadHandler;

    public FileRouter(UploadHandler uploadHandler, DownloadHandler downloadHandler) {
        this.uploadHandler = uploadHandler;
        this.downloadHandler = downloadHandler;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpHelper.addCorsHeaders(exchange);
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        String path = exchange.getRequestURI().getPath();

        if (path.startsWith("/file/") && path.length() > "/file/".length()) {
            downloadHandler.handle(exchange);
        } else if ("/upload".equals(path)) {
            uploadHandler.handle(exchange);
        } else {
            HttpHelper.sendError(exchange, 404, "Not found");
        }
    }
}
