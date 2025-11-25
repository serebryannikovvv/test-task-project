package com.fileservice.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class FileUtils {

    public static String sanitizeFilename(String name) {
        return name.replaceAll("[^\\p{L}0-9.\\-_()\\[\\] ]", "_")
                .replaceAll("[.]{2,}", ".")
                .trim();
    }

    public static String urlEncode(String s) {
        try {
            return URLEncoder.encode(s, StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20");
        } catch (Exception e) {
            return s;
        }
    }
}
