package com.fileservice.service;

import com.fileservice.config.AppConfig;
import com.fileservice.model.FileMetadata;
import com.fileservice.util.LoggerUtil;
import com.fileservice.util.IoUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileStorageService {

    private static final Logger logger = LoggerUtil.getLogger(FileStorageService.class);

    private final Path storageDir;
    private final Path metaDir;

    public FileStorageService() {
        this.storageDir = Paths.get(AppConfig.UPLOAD_DIR).toAbsolutePath().normalize();
        this.metaDir = Paths.get(AppConfig.META_DIR).toAbsolutePath().normalize();
        createDirectories();
    }

    public FileStorageService(Path storageDir, Path metaDir) {
        this.storageDir = storageDir.toAbsolutePath().normalize();
        this.metaDir = metaDir.toAbsolutePath().normalize();
        createDirectories();
    }

    private void createDirectories() {
        try {
            Files.createDirectories(storageDir);
            Files.createDirectories(metaDir);
            Files.createDirectories(Paths.get(AppConfig.LOGS_DIR));
            logger.info("Директории созданы: " + storageDir + ", " + metaDir);
        } catch (IOException e) {
            logger.severe("Не удалось создать директории!");
            throw new RuntimeException("Failed to create storage directories", e);
        }
    }

    public Path getStorageDir() {
        return storageDir;
    }

    public Path getMetaDir() {
        return metaDir;
    }

    private FileMetadata readMetadata(String fileId) throws IOException, NumberFormatException {
        Path metaPath = metaDir.resolve(fileId + ".meta");
        if (!Files.exists(metaPath)) {
            throw new FileNotFoundException("Metadata not found for ID: " + fileId);
        }

        String content = IoUtils.readFileContent(metaPath);
        if (content == null) {
            throw new IOException("Empty metadata content for ID: " + fileId);
        }

        String[] parts = content.split("\\|", 3);
        if (parts.length != 3) {
            throw new IOException("Invalid metadata format for ID: " + fileId);
        }

        String originalName = parts[0];
        long uploadMillis = Long.parseLong(parts[1]);
        long lastAccessMillis = Long.parseLong(parts[2]);

        return new FileMetadata(originalName, Instant.ofEpochMilli(uploadMillis), lastAccessMillis);
    }

    public void saveMetadata(String fileId, String originalFilename, Instant uploadTime) {
        Path metaPath = metaDir.resolve(fileId + ".meta");
        FileMetadata metadata = new FileMetadata(
                originalFilename,
                uploadTime,
                System.currentTimeMillis()
        );

        try {
            Files.write(metaPath, metadata.toStorageString().getBytes("UTF-8"),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Не удалось сохранить метаданные для " + fileId, e);
        }
    }

    public void updateLastAccessTime(String fileId) {
        Path metaPath = metaDir.resolve(fileId + ".meta");
        Path tempPath = metaDir.resolve(fileId + ".meta." + System.nanoTime() + ".tmp");

        try {
            FileMetadata oldMetadata = readMetadata(fileId);

            FileMetadata newMetadata = new FileMetadata(
                    oldMetadata.getOriginalFilename(),
                    oldMetadata.getUploadTime(),
                    System.currentTimeMillis()
            );
            // Атомарная запись: создаем временный файл, потом переименовываем
            Files.write(tempPath, newMetadata.toStorageString().getBytes("UTF-8"),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE);

            Files.move(tempPath, metaPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            logger.fine("Updated access time for file: " + fileId);

        } catch (FileNotFoundException | NoSuchFileException ignored) {
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to update access time for " + fileId, e);
            cleanupTempFile(tempPath);
        }
    }

    private void cleanupTempFile(Path tempPath) {
        try {
            Files.deleteIfExists(tempPath);
        } catch (Exception deleteEx) {
            logger.log(Level.FINE, "Failed to delete temp file: " + tempPath, deleteEx);
        }
    }

    public String getOriginalFilename(String fileId) {
        try {
            return readMetadata(fileId).getOriginalFilename();
        } catch (NumberFormatException | IOException e) {
            logger.fine("Не удалось прочитать имя файла для " + fileId + ": " + e.getMessage());
            return null;
        }
    }

    public long getLastAccessTimeMillis(String fileId) {
        try {
            return readMetadata(fileId).getLastAccessTimeMillis();
        } catch (NumberFormatException | IOException e) {
            logger.fine("Не удалось прочитать время доступа для " + fileId + ": " + e.getMessage());
            return 0L;
        }
    }

    public void deleteFile(String fileId) {
        try {
            Files.deleteIfExists(storageDir.resolve(fileId));
            Files.deleteIfExists(metaDir.resolve(fileId + ".meta"));
            logger.fine("Удалён файл: " + fileId);
        } catch (IOException e) {
            logger.warning("Ошибка удаления файла " + fileId + ": " + e.getMessage());
        }
    }

    public FileMetadata getMetadata(String fileId) {
        try {
            return readMetadata(fileId);
        } catch (Exception e) {
            return null;
        }
    }
}