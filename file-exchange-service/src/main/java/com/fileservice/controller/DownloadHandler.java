package com.fileservice.controller;

import com.fileservice.config.ContentTypeResolverConfig;
import com.fileservice.service.FileStorageService;
import com.fileservice.util.FileUtils;
import com.fileservice.util.HttpHelper;
import com.fileservice.util.IoUtils;
import com.fileservice.util.LoggerUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

public class DownloadHandler implements HttpHandler {

    private static final Logger logger = LoggerUtil.getLogger(DownloadHandler.class);

    private final FileStorageService storageService;

    public DownloadHandler(FileStorageService storageService) {
        this.storageService = storageService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        HttpHelper.addCorsHeaders(exchange);

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpHelper.sendError(exchange, 405, "Method Not Allowed");
            return;
        }

        String fileId = fileIdFromPath(exchange.getRequestURI().getPath());

        if (fileId.isEmpty() || fileId.contains("..") || fileId.contains("/")) {
            HttpHelper.sendError(exchange, 400, "Invalid file ID");
            return;
        }

        Path filePath = storageService.getStorageDir().resolve(fileId);
        if (!Files.isRegularFile(filePath)) {
            HttpHelper.sendError(exchange, 404, "File not found");
            return;
        }

        String originalName = storageService.getOriginalFilename(fileId);
        if (originalName == null || originalName.trim().isEmpty()) originalName = fileId;
        String safeName = FileUtils.sanitizeFilename(originalName);
        long fileSize = Files.size(filePath);

        exchange.getResponseHeaders().set("Content-Type", ContentTypeResolverConfig.resolve(safeName));
        exchange.getResponseHeaders().set("Content-Disposition",
                "attachment; filename=\"" + safeName + "\"; filename*=UTF-8''" + FileUtils.urlEncode(safeName));
        exchange.getResponseHeaders().set("Accept-Ranges", "bytes");
        exchange.getResponseHeaders().set("Cache-Control", "no-cache");

        // Обработка Range-запроса или полное скачивание
        String rangeHeader = exchange.getRequestHeaders().getFirst("Range");
        if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
            handleRange(exchange, filePath, fileId, fileSize, rangeHeader);
        } else {
            exchange.sendResponseHeaders(200, fileSize);
            storageService.updateLastAccessTime(fileId);
            streamFile(filePath, exchange.getResponseBody());
        }

        logger.info(String.format("Downloaded %s (%s, %d bytes) by %s", fileId, originalName, fileSize, HttpHelper.getClientIp(exchange)));
    }

    private String fileIdFromPath(String path) {
        if (!path.startsWith("/file/")) {
            return "";
        }
        String rawId = path.substring("/file/".length());
        String decoded;
        try {
            decoded = URLDecoder.decode(rawId, "UTF-8");
        } catch (Exception e) {
            return "";
        }
        // Проверяем на traversal
        if (decoded.contains("..") || decoded.contains("/") || decoded.contains("\\")) {
            return "";
        }
        Path resolved = storageService.getStorageDir().resolve(decoded).normalize();
        if (!resolved.startsWith(storageService.getStorageDir())) {
            return "";
        }
        return decoded;
    }

    private void handleRange(HttpExchange exchange, Path filePath, String fileId, long fileSize, String rangeHeader) throws IOException {
        long start = 0;
        long end = fileSize - 1;

        try {
            String range = rangeHeader.substring(6).trim();
            int dash = range.indexOf('-');

            if (dash != -1) {
                String from = range.substring(0, dash).trim();
                String to = range.substring(dash + 1).trim();

                if (!from.isEmpty()) start = Long.parseLong(from);
                if (!to.isEmpty()) end = Long.parseLong(to);
            }
        } catch (NumberFormatException e) {
            exchange.getResponseHeaders().set("Content-Range", "bytes */" + fileSize);
            exchange.sendResponseHeaders(416, -1);
            return;
        }

        if (start >= fileSize || start > end) {
            exchange.getResponseHeaders().set("Content-Range", "bytes */" + fileSize);
            exchange.sendResponseHeaders(416, -1);
            return;
        }

        if (end >= fileSize) end = fileSize - 1;

        long length = end - start + 1;
        exchange.getResponseHeaders().set("Content-Range", "bytes " + start + "-" + end + "/" + fileSize);
        exchange.sendResponseHeaders(206, length);

        storageService.updateLastAccessTime(fileId);

        try (RandomAccessFile raf = new RandomAccessFile(filePath.toFile(), "r");
             OutputStream out = exchange.getResponseBody()) {

            raf.seek(start);
            byte[] buf = new byte[16384];
            long remaining = length;
            while (remaining > 0) {
                int read = raf.read(buf, 0, (int) Math.min(buf.length, remaining));
                if (read == -1) break;
                out.write(buf, 0, read);
                remaining -= read;
            }
        }
    }

    private void streamFile(Path filePath, OutputStream out) throws IOException {
        try (InputStream in = Files.newInputStream(filePath)) {
            IoUtils.copyStream(in, out);
        }
    }
}
