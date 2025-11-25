package com.fileservice;

import com.fileservice.config.AppConfig;
import com.fileservice.config.ServerConfig;
import com.fileservice.service.FileCleanupScheduler;
import com.fileservice.service.FileStorageService;
import com.fileservice.util.LoggerUtil;
import com.sun.net.httpserver.HttpServer;

import java.io.InputStream;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Main {

    private static final Logger logger = LoggerUtil.getLogger(Main.class);


    private static final CountDownLatch shutdownLatch = new CountDownLatch(1);

    private static volatile HttpServer httpServer;
    private static volatile FileCleanupScheduler cleanupScheduler;

    public static void main(String[] args) {
        configureLogging();

        logger.info("╔══════════════════════════════════════╗");
        logger.info("║   Запуск временного файлообменника   ║");
        logger.info("╚══════════════════════════════════════╝");

        try {
            FileStorageService storageService = new FileStorageService();
            cleanupScheduler = new FileCleanupScheduler(storageService);

            // Запуск HTTP-сервера
            httpServer = ServerConfig.startServer(storageService,cleanupScheduler);

            logger.info("Сервис полностью запущен и готов к работе!");
            logger.info("• URL: " + AppConfig.BASE_URL);
            logger.info("• Загрузка: POST " + AppConfig.BASE_URL + "/upload");
            logger.info("• Скачивание: GET " + AppConfig.BASE_URL + "/file/<id>");
            logger.info("• Фронтенд: " + AppConfig.BASE_URL + "/");
            logger.info("• Автоудаление: через " + AppConfig.CLEANUP_MAX_AGE_DAYS + " дней без скачиваний");

            setupShutdownHook();

            // Блокируем основной поток до сигнала завершения
            shutdownLatch.await();

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Критическая ошибка при запуске", e);
            System.exit(1);
        }
    }

    private static void configureLogging() {
        try (InputStream is = Main.class.getResourceAsStream("/logging.properties")) {
            if (is != null) {
                LogManager.getLogManager().readConfiguration(is);
            } else {
                System.err.println("logging.properties не найден — используется стандартное логирование");
            }
        } catch (Exception e) {
            System.err.println("Ошибка настройки логирования: " + e.getMessage());
        }
    }

    private static void setupShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Получен сигнал завершения. Останавливаем сервис...");

            if (httpServer != null) {
                logger.info("Останавливаем HTTP-сервер...");
                httpServer.stop(3);
            }
            if (cleanupScheduler != null) {
                cleanupScheduler.shutdown();
            }
            logger.info("Сервис остановлен корректно. Пока!");
            shutdownLatch.countDown();
        }, "Shutdown-Hook"));
    }
}