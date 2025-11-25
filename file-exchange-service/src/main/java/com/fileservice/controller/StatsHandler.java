package com.fileservice.controller;

import com.fileservice.model.FileMetadata;
import com.fileservice.model.UploadResponse;
import com.fileservice.service.FileStorageService;
import com.fileservice.service.LinkService;
import com.fileservice.util.HttpHelper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class StatsHandler implements HttpHandler {

    private final FileStorageService storageService;
    private final LinkService linkService;
    private final long startTime = System.currentTimeMillis();

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM HH:mm").withZone(ZoneId.systemDefault());

    public StatsHandler(FileStorageService storageService, LinkService linkService) {
        this.storageService = storageService;
        this.linkService = linkService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        HttpHelper.addCorsHeaders(exchange);

        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            String json = buildStatsJson();
            HttpHelper.sendJson(exchange, 200, json);
        } else {
            exchange.sendResponseHeaders(405, -1);
        }
    }

    private String buildStatsJson() {
        long totalFiles = 0;
        long totalBytes = 0;
        List<UploadResponse> recentFiles = new ArrayList<>();

        DirectoryStream<Path> stream = null;
        try {
            stream = Files.newDirectoryStream(storageService.getStorageDir());
            for (Path path : stream) {
                if (!Files.isRegularFile(path)) {
                    continue;
                }

                String fileId = path.getFileName().toString();
                long size = Files.size(path);
                totalFiles++;
                totalBytes += size;

                try {
                    FileMetadata meta = storageService.getMetadata(fileId);
                    if (meta != null) {
                        String downloadLink = linkService.generateDownloadLink(fileId);
                        UploadResponse dto = new UploadResponse(
                                true,
                                "ok",
                                downloadLink,
                                fileId,
                                size,
                                meta.getOriginalFilename(),
                                meta.getUploadTime()
                        );
                        recentFiles.add(dto);
                    }
                } catch (Exception ignored) {
                }
            }
        } catch (IOException e) {
            // если папка недоступна — статистика будет пустой
        } finally {
            if (stream != null) {
                try { stream.close(); } catch (IOException ignored) {}
            }
        }

        // Сортируем от новых к старым
        Collections.sort(recentFiles, new Comparator<UploadResponse>() {
            @Override
            public int compare(UploadResponse a, UploadResponse b) {
                Instant t1 = a.getUploadedAt();
                Instant t2 = b.getUploadedAt();
                if (t1 == null) return 1;
                if (t2 == null) return -1;
                return t2.compareTo(t1);
            }
        });

        // Берём только 5 последних
        int limit = Math.min(5, recentFiles.size());
        List<UploadResponse> top5 = recentFiles.subList(0, limit);

        double avgMb = totalFiles > 0 ? (double) totalBytes / (1024 * 1024 * totalFiles) : 0.0;
        long uptimeSec = (System.currentTimeMillis() - startTime) / 1000;
        String uptime = String.format("%dч %dм", uptimeSec / 3600, (uptimeSec % 3600) / 60);

        // Формируем JSON
        StringBuilder recentJson = new StringBuilder("[");
        for (int i = 0; i < top5.size(); i++) {
            if (i > 0) recentJson.append(",");
            UploadResponse f = top5.get(i);
            recentJson.append(String.format(
                    "{\"name\":\"%s\",\"size\":\"%s\",\"uploaded\":\"%s\"}",
                    escapeJson(f.getOriginalFilename()),
                    f.getFormattedSize(),
                    f.getUploadedAt() != null ?
                            DATE_FORMATTER.format(f.getUploadedAt()) : ""
            ));
        }
        recentJson.append("]");

        return String.format(
                "{\"totalFiles\":%d,\"totalSize\":\"%s\",\"avgSize\":\"%.2f MB\",\"uptime\":\"%s\",\"recentFiles\":%s}",
                totalFiles,
                formatBytes(totalBytes),
                avgMb,
                uptime,
                recentJson.toString()
        );
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / 1024.0 / 1024.0);
        return String.format("%.1f GB", bytes / 1024.0 / 1024.0 / 1024.0);
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}