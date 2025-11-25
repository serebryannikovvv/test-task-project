package com.fileservice.service;

import com.fileservice.config.AppConfig;
import com.fileservice.util.LoggerUtil;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class FileCleanupScheduler {

    private static final Logger logger = LoggerUtil.getLogger(FileCleanupScheduler.class);

    private final FileStorageService storageService;
    private final Path metaDir;
    private final ScheduledExecutorService scheduler;

    public FileCleanupScheduler(FileStorageService storageService) {
        this.storageService = storageService;
        this.metaDir = storageService.getMetaDir();
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "File-Cleanup-Thread");
            t.setDaemon(true);
            return t;
        });

        startCleanupTask();
    }

    private void startCleanupTask() {
        scheduler.scheduleAtFixedRate(this::cleanupOldFiles,
                AppConfig.CLEANUP_INTERVAL_HOURS,
                AppConfig.CLEANUP_INTERVAL_HOURS,
                TimeUnit.HOURS);

        logger.info("Автоочистка запущена (каждые " + AppConfig.CLEANUP_INTERVAL_HOURS + " ч, хранение: " + AppConfig.CLEANUP_MAX_AGE_DAYS + " дней)");
    }

    private void cleanupOldFiles() {
        long cutoff = System.currentTimeMillis() - AppConfig.CLEANUP_MAX_AGE_MILLIS;
        logger.info("Запуск очистки файлов, старше " + AppConfig.CLEANUP_MAX_AGE_DAYS + " дней...");

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(metaDir, "*.meta")) {
            int deleted = 0;
            for (Path metaPath : stream) {
                String fileId = metaPath.getFileName().toString().replace(".meta", "");

                long lastAccess = storageService.getLastAccessTimeMillis(fileId);

                if (lastAccess == 0L || lastAccess < cutoff) {
                    storageService.deleteFile(fileId);
                    deleted++;
                }
            }
            logger.info("Очистка завершена. Удалено файлов: " + deleted);
        } catch (Exception e) {
            logger.warning("Ошибка при автоочистке: " + e.getMessage());
        }
    }

    /**
     * Остановка планировщика.
     */
    public void shutdown() {
        if (scheduler != null) {
            scheduler.shutdownNow();
            logger.info("Планировщик очистки остановлен.");
        }
    }
}