package com.fileservice.model;

import java.time.Instant;

public class FileMetadata {

    private final String originalFilename;
    private final Instant uploadTime;
    private final long lastAccessTimeMillis;

    public FileMetadata(String originalFilename, Instant uploadTime, long lastAccessTimeMillis) {
        this.originalFilename = originalFilename;
        this.uploadTime = uploadTime;
        this.lastAccessTimeMillis = lastAccessTimeMillis;
    }

    public String getOriginalFilename() { return originalFilename; }
    public Instant getUploadTime() { return uploadTime; }
    public long getLastAccessTimeMillis() { return lastAccessTimeMillis; }

    // Метод для сериализации в строку (для сохранения на диск)
    public String toStorageString() {
        return originalFilename.replace("|", "\\|") + "|" +
                uploadTime.toEpochMilli() + "|" +
                lastAccessTimeMillis;
    }
}
