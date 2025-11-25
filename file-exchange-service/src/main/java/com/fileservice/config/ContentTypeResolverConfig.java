package com.fileservice.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class ContentTypeResolverConfig {
    private static final String DEFAULT_TYPE = "application/octet-stream";

    private static final Map<String, String> MIME_MAP = createMimeMap();

    private ContentTypeResolverConfig() {}

    private static Map<String, String> createMimeMap() {
        Map<String, String> map = new HashMap<>();

        // Текстовые типы
        map.put("txt", "text/plain; charset=utf-8");
        map.put("log", "text/plain; charset=utf-8");
        map.put("md", "text/plain; charset=utf-8");
        map.put("html", "text/html; charset=utf-8");
        map.put("css", "text/css; charset=utf-8");
        map.put("js", "application/javascript; charset=utf-8");
        map.put("json", "application/json; charset=utf-8");

        // Документы
        map.put("pdf", "application/pdf");
        map.put("zip", "application/zip");
        map.put("rar", "application/zip");
        map.put("7z", "application/zip");

        // Изображения
        map.put("png", "image/png");
        map.put("jpg", "image/jpeg");
        map.put("jpeg", "image/jpeg");
        map.put("gif", "image/gif");
        map.put("webp", "image/webp");
        map.put("svg", "image/svg+xml");

        // Медиа
        map.put("mp4", "video/mp4");
        map.put("webm", "video/mp4");
        map.put("mp3", "audio/mpeg");
        map.put("wav", "audio/wav");

        return Collections.unmodifiableMap(map);
    }

    /**
     * Разрешает MIME-тип по имени файла.
     */
    public static String resolve(String filename) {
        if (filename == null) return DEFAULT_TYPE;
        int lastDot = filename.lastIndexOf('.');
        if (lastDot == -1 || lastDot == filename.length() - 1) {
            return DEFAULT_TYPE;
        }
        String ext = filename.substring(lastDot + 1).toLowerCase();
        String mimeType = MIME_MAP.get(ext);
        return mimeType != null ? mimeType : DEFAULT_TYPE;
    }
}
