package com.fileservice.util;

import com.fileservice.model.UploadResponse;

public final class JsonUtils {

    public static String toJson(UploadResponse resp) {
        return String.format(
                "{\"success\":%b," +
                        "\"message\":\"%s\"," +
                        "\"downloadLink\":\"%s\"," +
                        "\"fileId\":\"%s\"," +
                        "\"size\":%d," +
                        "\"originalFilename\":\"%s\"," +
                        "\"uploadedAt\":\"%s\"}",
                resp.isSuccess(),
                escape(resp.getMessage()),
                escape(resp.getDownloadLink()),
                escape(resp.getFileId()),
                resp.getSize(),
                escape(resp.getOriginalFilename()),
                resp.getUploadedAt() != null ? resp.getUploadedAt().toString() : ""
        );
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private JsonUtils() {}
}
