package com.fileservice.config;

import java.util.concurrent.TimeUnit;

public final class AppConfig {

    public static final String SERVER_HOST     = "0.0.0.0";
    public static final int    SERVER_PORT     = 8080;
    public static final int    THREAD_POOL_SIZE = 10;

    public static final String UPLOAD_DIR = "uploads";
    public static final String META_DIR   = "meta";
    public static final String LOGS_DIR   = "logs";

    public static final String BASE_URL = "http://localhost:8080";

    public static final long MAX_FILE_SIZE_MB = 200;
    public static final long MAX_FILE_SIZE = MAX_FILE_SIZE_MB * 1024 * 1024L;

    public static final long CLEANUP_MAX_AGE_DAYS   = 30;
    public static final long CLEANUP_MAX_AGE_MILLIS = TimeUnit.DAYS.toMillis(CLEANUP_MAX_AGE_DAYS);
    public static final long CLEANUP_INTERVAL_HOURS = 24;

    private AppConfig() {
        throw new UnsupportedOperationException("Utility class");
    }
}