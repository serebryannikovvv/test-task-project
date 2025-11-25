package com.fileservice.config;

import com.fileservice.controller.*;
import com.fileservice.service.FileCleanupScheduler;
import com.fileservice.service.FileStorageService;
import com.fileservice.service.LinkService;
import com.fileservice.util.HttpHelper;
import com.fileservice.util.LoggerUtil;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public final class ServerConfig {

    private static final Logger logger = LoggerUtil.getLogger(ServerConfig.class);

    private ServerConfig() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static HttpServer startServer(
            FileStorageService storageService,
            FileCleanupScheduler cleanupScheduler) throws IOException {

        HttpServer server = HttpServer.create(
                new InetSocketAddress(AppConfig.SERVER_HOST, AppConfig.SERVER_PORT), 0);

        LinkService linkService = new LinkService(AppConfig.BASE_URL);

        // Обработчики
        UploadHandler uploadHandler = new UploadHandler(storageService, linkService);
        DownloadHandler downloadHandler = new DownloadHandler(storageService);
        FileRouter apiRouter = new FileRouter(uploadHandler, downloadHandler);

        StaticFileHandler staticHandler = new StaticFileHandler();

        server.createContext("/upload", apiRouter);
        server.createContext("/file/", apiRouter);
        server.createContext("/stats", staticHandler);
        server.createContext("/api/stats", new StatsHandler(storageService, linkService));
        server.createContext("/health", healthHandler());
        server.createContext("/", staticHandler);

        server.setExecutor(Executors.newFixedThreadPool(AppConfig.THREAD_POOL_SIZE));

        //Запуск
        server.start();

        //Graceful shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Завершение работы сервера...");
            cleanupScheduler.shutdown();
            server.stop(0);
            logger.info("Сервер остановлен.");
        }, "Shutdown-Hook"));
        return server;
    }

    private static HttpHandler healthHandler() {
        return exchange -> {
            HttpHelper.addCorsHeaders(exchange);

            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            long uptime = (System.currentTimeMillis() -
                    ManagementFactory.getRuntimeMXBean().getStartTime()) / 1000;

            String json = String.format(
                    "{\"status\":\"OK\",\"uptime_seconds\":%d,\"service\":\"Temp File Share\"}",
                    uptime);

            HttpHelper.sendJson(exchange, 200, json);
        };
    }
}