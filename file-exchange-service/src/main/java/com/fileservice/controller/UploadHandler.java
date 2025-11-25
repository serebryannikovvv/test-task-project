package com.fileservice.controller;

import com.fileservice.config.AppConfig;
import com.fileservice.exception.SizeLimitExceededException;
import com.fileservice.io.LimitedInputStream;
import com.fileservice.model.UploadResponse;
import com.fileservice.service.FileStorageService;
import com.fileservice.service.LinkService;
import com.fileservice.util.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;
import java.util.logging.Logger;

public class UploadHandler implements HttpHandler {

    private static final Logger logger = LoggerUtil.getLogger(UploadHandler.class);

    private final FileStorageService storageService;
    private final LinkService linkService;

    public UploadHandler(FileStorageService storageService, LinkService linkService) {
        this.storageService = storageService;
        this.linkService = linkService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        HttpHelper.addCorsHeaders(exchange);

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpHelper.sendError(exchange, 405, "Method Not Allowed");
            return;
        }

        String clientIp = HttpHelper.getClientIp(exchange);
        logger.info("Upload request from " + clientIp);

        String originalName = extractAndSanitizeFilename(exchange);
        String fileId = UUID.randomUUID().toString();
        Path filePath = storageService.getStorageDir().resolve(fileId);

        try (LimitedInputStream limitedIn = new LimitedInputStream(exchange.getRequestBody(), AppConfig.MAX_FILE_SIZE);
             OutputStream fileOut = Files.newOutputStream(filePath)) {

            long uploadedBytes = IoUtils.copyStream(limitedIn, fileOut);

            if (uploadedBytes == 0) {
                Files.deleteIfExists(filePath);
                HttpHelper.sendError(exchange, 400, "Empty file");
                return;
            }

            storageService.saveMetadata(fileId, originalName, Instant.now());
            storageService.updateLastAccessTime(fileId);

            String downloadUrl = linkService.generateDownloadLink(fileId);
            UploadResponse response = new UploadResponse(true, "File uploaded successfully",
                    downloadUrl, fileId, uploadedBytes, originalName, Instant.now());

            HttpHelper.sendJson(exchange, 200, JsonUtils.toJson(response));

            logger.info(String.format("Uploaded %s (%s, %d bytes) from %s", fileId, originalName, uploadedBytes, clientIp));

        } catch (SizeLimitExceededException e) {
            Files.deleteIfExists(filePath);
            HttpHelper.sendError(exchange, 413, "File too large (max " + AppConfig.MAX_FILE_SIZE_MB + " MB)");
        } catch (Exception e) {
            Files.deleteIfExists(filePath);
            logger.severe("Upload failed from " + clientIp + ": " + e.toString());
            HttpHelper.sendError(exchange, 500, "Server error");
        }
    }

    private String extractAndSanitizeFilename(HttpExchange exchange) {
        String rawHeader = exchange.getRequestHeaders().getFirst("X-Filename");
        String originalName = "unknown-file";

        if (rawHeader != null && !rawHeader.trim().isEmpty()) {
            // Берем только имя, отбрасывая путь
            String candidate = new java.io.File(rawHeader).getName();

            // Дополнительная проверка безопасности
            if (candidate != null && !candidate.trim().isEmpty() && !candidate.contains("..") && !candidate.startsWith("/")) {
                originalName = FileUtils.sanitizeFilename(candidate);
            }
        }
        return originalName;
    }
}
