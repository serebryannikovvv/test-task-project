package com.fileservice.model;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class UploadResponse {
    private final boolean success;
    private final String message;
    private final String downloadLink;
    private final String fileId;
    private final long size;
    private final String originalFilename;
    private final Instant uploadedAt;

    public UploadResponse(boolean success, String message, String downloadLink, String fileId, long size) {
        this(success, message, downloadLink, fileId, size, null, null);
    }

    public UploadResponse(boolean success, String message, String downloadLink, String fileId, long size,
                          String originalFilename, Instant uploadedAt) {
        this.success = success;
        this.message = message;
        this.downloadLink = downloadLink;
        this.fileId = fileId;
        this.size = size;
        this.originalFilename = originalFilename != null ? originalFilename : "unknown";
        this.uploadedAt = uploadedAt;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getDownloadLink() {
        return downloadLink;
    }

    public String getFileId() {
        return fileId;
    }

    public long getSize() {
        return size;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public Instant getUploadedAt() {
        return uploadedAt;
    }

    public String getFormattedSize() {
        return formatBytes(size);
    }

    public String getFormattedDate() {
        return uploadedAt != null ?
                uploadedAt.atZone(ZoneId.systemDefault())
                        .format(DateTimeFormatter.ofPattern("dd MMM HH:mm")) : "";
    }

    private static String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / 1024.0 / 1024.0);
        return String.format("%.1f GB", bytes / 1024.0 / 1024.0 / 1024.0);
    }
}